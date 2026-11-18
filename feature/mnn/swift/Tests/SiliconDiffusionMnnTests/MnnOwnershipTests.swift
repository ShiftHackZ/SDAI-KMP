import Foundation
import Testing

@testable import SiliconDiffusionMnn

private final class ControlledAttempt: MnnExecutionAttempt, @unchecked Sendable {
  private let lock = NSLock()
  private var isStarted = false
  private var isInterrupted = false
  private var workspace: URL?
  let canReturn = DispatchSemaphore(value: 0)
  var hasStarted: Bool { lock.withLock { isStarted } }
  var hasInterrupted: Bool { lock.withLock { isInterrupted } }
  var workspaceExists: Bool {
    lock.withLock { workspace.map { FileManager.default.fileExists(atPath: $0.path) } ?? false }
  }
  func interrupt() { lock.withLock { isInterrupted = true } }
  func execute(
    request: MnnRequest, workspace: URL,
    onProgress: @escaping @Sendable (Int) -> Void
  ) throws -> Data {
    lock.withLock {
      isStarted = true
      self.workspace = workspace
    }
    onProgress(40)
    guard canReturn.wait(timeout: .now() + 5) == .success else { throw MnnFailure.nativeFailure }
    onProgress(20)
    onProgress(120)
    return Data("owned output".utf8)
  }
}

private final class AttemptSequence: @unchecked Sendable {
  private let lock = NSLock()
  private var attempts: [ControlledAttempt]
  init(_ attempts: [ControlledAttempt]) { self.attempts = attempts }
  func next() -> any MnnExecutionAttempt { lock.withLock { attempts.removeFirst() } }
}

private final class ProgressRecord: @unchecked Sendable {
  private let lock = NSLock()
  private var values: [Int] = []
  func append(_ value: Int) { lock.withLock { values.append(value) } }
  var snapshot: [Int] { lock.withLock { values } }
}

private func awaitStart(_ attempt: ControlledAttempt) async -> Bool {
  for _ in 0..<200 {
    if attempt.hasStarted { return true }
    try? await Task.sleep(for: .milliseconds(10))
  }
  return false
}

private let request = MnnRequest(
  modelPath: "/owned/model", prompt: "cat", steps: 20, seed: 1, backend: .cpu)

@Test(
  "given MNN owner cancellation, native returns late output, expected cancelled result and retained workspace until drain"
)
func fencesLateOutput() async {
  // Given
  let native = ControlledAttempt()
  let progress = ProgressRecord()
  let root = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  let runtime = SiliconDiffusionMnnGenerator(attemptRoot: root, makeAttempt: { native })
  let result = Task {
    await withCheckedContinuation { continuation in
      runtime.generate(
        ownerId: "first", request: request, onProgress: progress.append,
        completion: { continuation.resume(returning: $0) })
    }
  }
  #expect(await awaitStart(native))

  // When
  runtime.interrupt(ownerId: "first")
  let hasWorkspaceBeforeReturn = native.workspaceExists
  native.canReturn.signal()
  let output = await result.value

  // Then
  #expect(native.hasInterrupted && hasWorkspaceBeforeReturn)
  #expect(throws: MnnFailure.cancelled) { try output.get() }
  #expect(progress.snapshot == [40])
  #expect(!native.workspaceExists)
  try? FileManager.default.removeItem(at: root)
}

@Test(
  "given MNN serial admission, successor is queued during cancelled native work, expected successor waits for drain"
)
func serializesReplacement() async {
  // Given
  let first = ControlledAttempt()
  let second = ControlledAttempt()
  let attempts = AttemptSequence([first, second])
  let root = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  let runtime = SiliconDiffusionMnnGenerator(attemptRoot: root, makeAttempt: attempts.next)
  let firstResult = Task {
    await withCheckedContinuation { continuation in
      runtime.generate(
        ownerId: "first", request: request, onProgress: { _ in },
        completion: { continuation.resume(returning: $0) })
    }
  }
  #expect(await awaitStart(first))

  // When
  runtime.interrupt(ownerId: "first")
  let secondResult = Task {
    await withCheckedContinuation { continuation in
      runtime.generate(
        ownerId: "second", request: request, onProgress: { _ in },
        completion: { continuation.resume(returning: $0) })
    }
  }
  try? await Task.sleep(for: .milliseconds(30))
  let hasStartedBeforeDrain = second.hasStarted
  first.canReturn.signal()
  _ = await firstResult.value
  #expect(await awaitStart(second))
  runtime.interrupt(ownerId: "first")
  second.canReturn.signal()
  let output = await secondResult.value

  // Then
  #expect(!hasStartedBeforeDrain)
  #expect(!second.hasInterrupted)
  #expect((try? output.get()) == Data("owned output".utf8))
  #expect(!first.workspaceExists && !second.workspaceExists)
  try? FileManager.default.removeItem(at: root)
}

@Test(
  "given MNN progress reporting, native emits descending and oversized percentages, expected monotonic bounded updates"
)
func boundsProgress() async {
  // Given
  let native = ControlledAttempt()
  let progress = ProgressRecord()
  let root = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  let runtime = SiliconDiffusionMnnGenerator(attemptRoot: root, makeAttempt: { native })
  native.canReturn.signal()

  // When
  let result = await withCheckedContinuation { continuation in
    runtime.generate(
      ownerId: "owner", request: request, onProgress: progress.append,
      completion: { continuation.resume(returning: $0) })
  }

  // Then
  #expect((try? result.get()) != nil)
  #expect(progress.snapshot == [40, 100])
  try? FileManager.default.removeItem(at: root)
}

@Test(
  "given MNN owner identity, duplicate request arrives during native execution, expected one rejection and original completes once"
)
func rejectsDuplicateOwner() async {
  // Given
  let native = ControlledAttempt()
  let root = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  let runtime = SiliconDiffusionMnnGenerator(attemptRoot: root, makeAttempt: { native })
  let completions = ProgressRecord()
  let original = Task {
    await withCheckedContinuation { continuation in
      runtime.generate(
        ownerId: "same", request: request, onProgress: { _ in },
        completion: {
          completions.append(1)
          continuation.resume(returning: $0)
        })
    }
  }
  #expect(await awaitStart(native))

  // When
  let duplicate = await withCheckedContinuation { continuation in
    runtime.generate(
      ownerId: "same", request: request, onProgress: { _ in },
      completion: {
        completions.append(2)
        continuation.resume(returning: $0)
      })
  }
  let hasWorkspaceDuringRejection = native.workspaceExists
  native.canReturn.signal()
  let output = await original.value

  // Then
  #expect(throws: MnnFailure.invalidRequest) { try duplicate.get() }
  #expect(hasWorkspaceDuringRejection)
  #expect((try? output.get()) == Data("owned output".utf8))
  #expect(completions.snapshot == [2, 1])
  #expect(!native.workspaceExists)
  try? FileManager.default.removeItem(at: root)
}
