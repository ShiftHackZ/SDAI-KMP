import Foundation

/// A single executor owner. Interrupt is nonblocking; execute returns only after its resources drain.
public protocol MnnExecutionAttempt: AnyObject, Sendable {
  /// Executes fixed inputs in the supplied backup-excluded workspace on a serial background queue.
  func execute(
    request: MnnRequest, workspace: URL,
    onProgress: @escaping @Sendable (Int) -> Void
  ) throws -> Data
  /// Requests cooperative cancellation without freeing resources concurrently.
  func interrupt()
}
