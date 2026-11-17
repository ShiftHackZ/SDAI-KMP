import Foundation

/// Serial in-process native execution. Files and handles remain owned until native drain.
public final class SiliconDiffusionMnnGenerator: @unchecked Sendable {
  private let queue = DispatchQueue(label: "cc.moroz.sdai.mnn", qos: .userInitiated)
  private let lock = NSLock()
  private var owners: [String: MnnAttemptOwner] = [:]
  private var cancelledOwners: Set<String> = []
  private let attemptRoot: URL
  private let initializationFailure: MnnFailure?
  private let makeAttempt: @Sendable () -> any MnnExecutionAttempt

  public init(
    attemptRoot: URL? = nil, makeAttempt: @escaping @Sendable () -> any MnnExecutionAttempt
  ) {
    self.makeAttempt = makeAttempt
    self.attemptRoot =
      (attemptRoot
      ?? FileManager.default.urls(
        for: .applicationSupportDirectory,
        in: .userDomainMask)[0].appendingPathComponent("mnn-attempts", isDirectory: true))
      .resolvingSymlinksInPath().standardizedFileURL
    // The process owns the only serial executor; old roots cannot be live after restart.
    do {
      if FileManager.default.fileExists(atPath: self.attemptRoot.path) {
        try FileManager.default.removeItem(at: self.attemptRoot)
      }
      initializationFailure = nil
    } catch {
      initializationFailure = .nativeFailure
    }
  }

  public func interrupt(ownerId: String) {
    lock.lock()
    defer { lock.unlock() }
    if let owner = owners[ownerId] {
      owner.interrupt()
    } else {
      if cancelledOwners.count >= 128 { cancelledOwners.removeAll() }
      cancelledOwners.insert(ownerId)
    }
  }

  public func generate(
    ownerId: String, request: MnnRequest,
    onProgress: @escaping @Sendable (Int) -> Void,
    completion: @escaping @Sendable (Result<Data, MnnFailure>) -> Void
  ) {
    let owner = MnnAttemptOwner(executor: makeAttempt(), onProgress: onProgress)
    lock.lock()
    guard owners[ownerId] == nil else {
      lock.unlock()
      completion(.failure(.invalidRequest))
      return
    }
    owners[ownerId] = owner
    if cancelledOwners.remove(ownerId) != nil { owner.interrupt() }
    lock.unlock()
    queue.async { [self, owner] in
      let workspace = attemptRoot.appendingPathComponent(UUID().uuidString, isDirectory: true)
      var result: Result<Data, MnnFailure>
      do {
        result = .success(try execute(owner: owner, request: request, workspace: workspace))
      } catch let failure as MnnFailure {
        result = .failure(failure)
      } catch {
        result = .failure(.nativeFailure)
      }
      // Never clear an executing owner's workspace in interrupt or from another queue.
      do {
        if FileManager.default.fileExists(atPath: workspace.path) {
          try FileManager.default.removeItem(at: workspace)
        }
      } catch {
        if case .success = result { result = .failure(.nativeFailure) }
      }
      lock.lock()
      owners.removeValue(forKey: ownerId)
      lock.unlock()
      completion(result)
    }
  }

  private func execute(owner: MnnAttemptOwner, request: MnnRequest, workspace: URL) throws -> Data {
    if let initializationFailure { throw initializationFailure }
    try owner.checkCancellation()
    try FileManager.default.createDirectory(at: workspace, withIntermediateDirectories: true)
    var resources = URLResourceValues()
    resources.isExcludedFromBackup = true
    var excludedRoot = attemptRoot
    try excludedRoot.setResourceValues(resources)
    let data = try owner.executor.execute(
      request: request, workspace: workspace, onProgress: owner.progress)
    try owner.checkCancellation()
    return data
  }
}
