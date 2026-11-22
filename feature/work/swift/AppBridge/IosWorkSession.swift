import BackgroundTasks
import UIKit

/// Retains one operation's admission and expiration callbacks until its cleanup completes.
final class IosWorkSession {
  let onExpiration: () -> Void
  let onReady: () -> Void
  let onUnavailable: () -> Void
  var identifier: String?
  var task: BGTask?
  var finiteTask: UIBackgroundTaskIdentifier = .invalid
  var liveActivity: WorkLiveActivity?

  init(
    onExpiration: @escaping () -> Void, onReady: @escaping () -> Void,
    onUnavailable: @escaping () -> Void
  ) {
    self.onExpiration = onExpiration
    self.onReady = onReady
    self.onUnavailable = onUnavailable
  }
}
