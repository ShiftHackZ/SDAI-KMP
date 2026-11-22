import ActivityKit
import SwiftUI
import WidgetKit

@main
struct WorkActivityWidget: Widget {
  var body: some WidgetConfiguration {
    ActivityConfiguration(for: WorkActivityAttributes.self) { context in
      WorkActivityContent(state: context.state)
        .padding()
    } dynamicIsland: { context in
      DynamicIsland {
        DynamicIslandExpandedRegion(.bottom) {
          WorkActivityContent(state: context.state)
        }
      } compactLeading: {
        Image(
          systemName: context.state.isRunning
            ? "sparkles" : context.state.percentage == 100 ? "checkmark" : "xmark")
      } compactTrailing: {
        WorkActivityProgress(state: context.state)
      } minimal: {
        WorkActivityProgress(state: context.state)
      }
    }
  }
}
