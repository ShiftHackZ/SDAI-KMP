@preconcurrency import AiSdPresentation
import BackgroundTasks
import UIKit
import os

/// Bridges captured generation and download lifetimes to system grants. State is confined to main.
final class IosWorkExecutionAdapter: NSObject, IosWorkExecution {
  private var sessions: [String: IosWorkSession] = [:]
  private let logger = Logger(
    subsystem: "com.shifthackz.aisdv1.app", category: "BackgroundWork")

  override init() {
    super.init()
    WorkLiveActivity.removeAbandonedActivities()
  }

  func begin(
    ownerId: String, title: String, subtitle: String, requiresGpu: Bool,
    onExpiration: @escaping () -> Void, onReady: @escaping () -> Void,
    onUnavailable: @escaping () -> Void
  ) {
    DispatchQueue.main.async {
      guard self.sessions[ownerId] == nil else { return }
      let session = IosWorkSession(
        onExpiration: onExpiration, onReady: onReady, onUnavailable: onUnavailable)
      self.sessions[ownerId] = session
      guard UIApplication.shared.applicationState == .active else {
        onExpiration()
        return
      }
      if requiresGpu {
        guard #available(iOS 26.0, *), BGTaskScheduler.supportedResources.contains(.gpu) else {
          self.logger.notice("Background GPU unavailable; rejecting work before native inference")
          onUnavailable()
          return
        }
      }
      if #available(iOS 26.0, *) {
        self.submit(
          session, ownerId: ownerId, title: title, subtitle: subtitle, requiresGpu: requiresGpu)
      } else {
        self.logger.notice("Continued processing unavailable on this OS; using finite execution")
        self.beginFinite(session, ownerId: ownerId, title: title, subtitle: subtitle)
      }
    }
  }

  @available(iOS 26.0, *)
  private func submit(
    _ session: IosWorkSession, ownerId: String, title: String, subtitle: String,
    requiresGpu: Bool
  ) {
    let identifier = "\(Bundle.main.bundleIdentifier!).work.\(ownerId)"
    let canUseBackgroundGpu = requiresGpu && BGTaskScheduler.supportedResources.contains(.gpu)
    session.identifier = identifier
    let scheduler = BGTaskScheduler.shared
    let isRegistered = scheduler.register(forTaskWithIdentifier: identifier, using: .main) {
      [weak self, weak session] task in
      guard let self, let session, self.sessions[ownerId] === session,
        let continuedTask = task as? BGContinuedProcessingTask
      else {
        task.setTaskCompleted(success: false)
        return
      }
      session.task = continuedTask
      self.logger.notice("Continued processing admitted; background GPU: \(canUseBackgroundGpu)")
      continuedTask.progress.totalUnitCount = -1
      continuedTask.expirationHandler = {
        DispatchQueue.main.async {
          guard self.sessions[ownerId] === session else { return }
          session.onExpiration()
        }
      }
      session.onReady()
    }
    guard isRegistered else {
      logger.error("Continued processing handler registration rejected")
      useFallback(
        session, ownerId: ownerId, title: title, subtitle: subtitle, requiresGpu: requiresGpu)
      return
    }
    let request = BGContinuedProcessingTaskRequest(
      identifier: identifier, title: title, subtitle: subtitle)
    request.strategy = .fail
    if canUseBackgroundGpu { request.requiredResources = .gpu }
    do {
      try scheduler.submit(request)
    } catch {
      logger.error(
        "Continued processing admission failed: \(String(describing: error), privacy: .public)")
      useFallback(
        session, ownerId: ownerId, title: title, subtitle: subtitle, requiresGpu: requiresGpu)
    }
  }

  private func useFallback(
    _ session: IosWorkSession, ownerId: String, title: String, subtitle: String,
    requiresGpu: Bool
  ) {
    if requiresGpu {
      session.onUnavailable()
    } else {
      beginFinite(session, ownerId: ownerId, title: title, subtitle: subtitle)
    }
  }

  private func beginFinite(
    _ session: IosWorkSession, ownerId: String, title: String, subtitle: String
  ) {
    session.liveActivity = WorkLiveActivity(
      ownerId: ownerId, title: title, subtitle: subtitle)
    session.finiteTask = UIApplication.shared.beginBackgroundTask(withName: title) {
      session.onExpiration()
    }
    session.onReady()
  }

  func update(ownerId: String, title: String, subtitle: String, percentage: KotlinInt?) {
    DispatchQueue.main.async {
      self.sessions[ownerId]?.liveActivity?.update(
        title: title, subtitle: subtitle, percentage: percentage?.intValue)
      guard #available(iOS 26.0, *),
        let task = self.sessions[ownerId]?.task as? BGContinuedProcessingTask
      else { return }
      task.updateTitle(title, subtitle: subtitle)
      task.progress.totalUnitCount = percentage == nil ? -1 : 100
      // A measured 100% does not mean activation or result persistence has finished.
      task.progress.completedUnitCount = Int64(min(99, max(0, percentage?.intValue ?? 0)))
    }
  }

  func end(ownerId: String, isSuccess: Bool, title: String, subtitle: String) {
    DispatchQueue.main.async {
      guard let session = self.sessions.removeValue(forKey: ownerId) else { return }
      session.liveActivity?.finish(title: title, subtitle: subtitle, isSuccess: isSuccess)
      if let task = session.task {
        if #available(iOS 26.0, *), let continuedTask = task as? BGContinuedProcessingTask {
          continuedTask.updateTitle(title, subtitle: subtitle)
          if isSuccess {
            continuedTask.progress.totalUnitCount = 100
            continuedTask.progress.completedUnitCount = 100
          }
        }
        task.expirationHandler = nil
        task.setTaskCompleted(success: isSuccess)
      } else if let identifier = session.identifier {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: identifier)
      }
      if session.finiteTask != .invalid {
        UIApplication.shared.endBackgroundTask(session.finiteTask)
      }
    }
  }
}
