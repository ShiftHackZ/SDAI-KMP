import ActivityKit

/// Shares localized, measured background-work state between the host and its widget extension.
struct WorkActivityAttributes: ActivityAttributes {
  struct ContentState: Codable, Hashable {
    var title: String
    var subtitle: String
    var percentage: Int?
    var isRunning: Bool
  }

  let ownerId: String
}
