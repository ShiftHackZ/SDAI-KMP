import Foundation

/// Immutable fixed-profile generation inputs captured before queue admission.
public struct MnnRequest: Sendable {
  public let modelPath: String
  public let prompt: String
  public let steps: Int
  public let seed: Int32
  public let backend: MnnBackend

  public init(modelPath: String, prompt: String, steps: Int, seed: Int32, backend: MnnBackend) {
    self.modelPath = modelPath
    self.prompt = prompt
    self.steps = steps
    self.seed = seed
    self.backend = backend
  }
}

/// iOS supports CPU and Metal; selecting Metal never silently selects CPU.
public enum MnnBackend: String, Sendable {
  case cpu, metal
}
