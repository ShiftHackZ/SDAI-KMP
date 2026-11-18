import Foundation

/// Recoverable failures; process termination and jetsam cannot cross this boundary.
public enum MnnFailure: String, Error, Sendable {
  case invalidBundle = "MNN bundle is invalid or incomplete."
  case invalidRequest = "Unsupported MNN generation parameters."
  case unavailable = "MNN generation requires a physical iPhone or iPad."
  case metalUnavailable = "MNN Metal is unavailable on this device."
  case cancelled = "MNN generation cancelled."
  case nativeFailure = "MNN native generation failed."
  case invalidImage = "MNN returned an invalid image."
}
