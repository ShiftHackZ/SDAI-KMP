import Foundation

/// Keeps the C handle alive until the serial executor has returned, including cancelled requests.
final class MnnAttemptOwner: @unchecked Sendable {
  let executor: any MnnExecutionAttempt
  let onProgress: @Sendable (Int) -> Void
  private var percentage = 0
  private let lock = NSLock()
  private var isCancelled = false

  init(executor: any MnnExecutionAttempt, onProgress: @escaping @Sendable (Int) -> Void) {
    self.executor = executor
    self.onProgress = onProgress
  }
  func interrupt() {
    lock.lock()
    isCancelled = true
    lock.unlock()
    executor.interrupt()
  }
  func checkCancellation() throws {
    lock.lock()
    defer { lock.unlock() }
    if isCancelled { throw MnnFailure.cancelled }
  }
  func progress(_ value: Int) {
    lock.lock()
    if isCancelled {
      lock.unlock()
      return
    }
    let bounded = min(100, max(percentage, value))
    guard bounded > percentage else {
      lock.unlock()
      return
    }
    percentage = bounded
    lock.unlock()
    onProgress(bounded)
  }
}
