import SwiftUI

/// Keeps compact and minimal presentations determinate only while a measurement exists.
struct WorkActivityProgress: View {
  let state: WorkActivityAttributes.ContentState

  var body: some View {
    if !state.isRunning {
      Image(systemName: state.percentage == 100 ? "checkmark" : "xmark")
    } else if let percentage = state.percentage {
      Text("\(percentage)%").font(.caption2).monospacedDigit()
    } else {
      ProgressView().controlSize(.mini)
    }
  }
}
