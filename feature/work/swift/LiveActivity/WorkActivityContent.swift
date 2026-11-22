import SwiftUI

/// Displays the localized stage and measured completion on the Lock Screen and expanded island.
struct WorkActivityContent: View {
  let state: WorkActivityAttributes.ContentState

  var body: some View {
    VStack(alignment: .leading, spacing: 8) {
      HStack {
        Text(state.title).font(.headline)
        Spacer()
        if let percentage = state.percentage { Text("\(percentage)%").monospacedDigit() }
      }
      Text(state.subtitle).font(.caption).lineLimit(2)
      if state.isRunning {
        if let percentage = state.percentage {
          ProgressView(value: Double(percentage), total: 100)
        } else {
          ProgressView()
        }
      }
    }
  }
}
