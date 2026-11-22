import ActivityKit
import Foundation
import os

/// Serializes measured updates and terminal states for one generation or download operation.
/// The host calls all entry points on main, alongside its execution grant bookkeeping.
final class WorkLiveActivity {
  static func removeAbandonedActivities() {
    let abandoned = Activity<WorkActivityAttributes>.activities
    Task {
      for activity in abandoned { await activity.end(nil, dismissalPolicy: .immediate) }
    }
  }

  private let ownerId: String
  private var state: WorkActivityAttributes.ContentState
  private var activity: Activity<WorkActivityAttributes>?
  private var pendingUpdate: Task<Void, Never>?
  private var isEnded = false
  private let logger = Logger(
    subsystem: "com.shifthackz.aisdv1.app", category: "WorkActivity")

  init(ownerId: String, title: String, subtitle: String) {
    self.ownerId = ownerId
    state = .init(title: title, subtitle: subtitle, percentage: nil, isRunning: true)
    start()
  }

  private func start() {
    guard !isEnded, activity == nil, ActivityAuthorizationInfo().areActivitiesEnabled else {
      return
    }
    do {
      activity = try Activity.request(
        attributes: WorkActivityAttributes(ownerId: ownerId),
        content: ActivityContent(state: state, staleDate: nil), pushType: nil)
      logger.notice("Work Live Activity started in foreground")
    } catch {
      logger.error(
        "Work Live Activity request failed: \(String(describing: error), privacy: .public)")
    }
  }

  func update(title: String, subtitle: String, percentage: Int?) {
    guard !isEnded else { return }
    state = .init(
      title: title, subtitle: subtitle,
      percentage: percentage.map { min(99, max(0, $0)) }, isRunning: true)
    guard let activity else { return }
    let content = ActivityContent(state: state, staleDate: nil)
    let previous = pendingUpdate
    pendingUpdate = Task {
      await previous?.value
      await activity.update(content)
    }
  }

  func finish(title: String, subtitle: String, isSuccess: Bool) {
    isEnded = true
    state = .init(
      title: title, subtitle: subtitle, percentage: isSuccess ? 100 : nil, isRunning: false)
    guard let activity else { return }
    self.activity = nil
    let content = ActivityContent(state: state, staleDate: nil)
    let previous = pendingUpdate
    pendingUpdate = Task {
      await previous?.value
      await activity.end(content, dismissalPolicy: .after(Date().addingTimeInterval(10)))
    }
  }
}
