import Foundation

/// Immutable diffusion or super-resolution inputs captured before serial queue admission.
public struct MnnRequest: Sendable {
  public let modelPath: String
  public let prompt: String
  public let steps: Int
  public let seed: Int32
  public let backend: MnnBackend
  /// Encoded input for dedicated super-resolution; nil preserves the diffusion request contract.
  public let upscaleImage: Data?

  public init(
    modelPath: String, prompt: String, steps: Int, seed: Int32, backend: MnnBackend,
    upscaleImage: Data? = nil
  ) {
    self.modelPath = modelPath
    self.prompt = prompt
    self.steps = steps
    self.seed = seed
    self.backend = backend
    self.upscaleImage = upscaleImage
  }
}

/// iOS supports CPU and Metal; selecting Metal never silently selects CPU.
public enum MnnBackend: String, Sendable {
  case cpu, metal
}
