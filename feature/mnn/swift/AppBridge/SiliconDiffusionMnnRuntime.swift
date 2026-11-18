@preconcurrency import AiSdPresentation
import Foundation
import SiliconDiffusionMnn

/// App composition adapter; all runtime and storage work remains in the feature package.
final class SiliconDiffusionMnnRuntimeAdapter: NSObject, MnnAppleBridge,
  DomainMnnBundleFileSystem, DomainMnnArchiveExtractor, @unchecked Sendable
{
  private let generator = SiliconDiffusionMnnGenerator(makeAttempt: { MnnNativeExecutionAttempt() })
  private let files = MnnFiles()

  func generate(
    ownerId: String, request: DomainNativeGenerationRequest,
    onProgress: @escaping @Sendable (KotlinInt) -> Void,
    completion: @escaping @Sendable (MnnAppleResult) -> Void
  ) {
    let payload = request.payload
    guard let seed = Int32(payload.seed),
      let backend = MnnBackend(rawValue: payload.mnnBackend.key),
      payload.width == 512, payload.height == 512, payload.cfgScale == 7.5,
      payload.batchCount == 1, payload.negativePrompt.isEmpty,
      payload.sampler == "PNDM", payload.mnnProfile == "sd15-512-general-int8-v1"
    else {
      completion(
        MnnAppleResult(
          imageBase64: nil,
          errorMessage: MnnFailure.invalidRequest.rawValue, isCancelled: false))
      return
    }
    generator.generate(
      ownerId: ownerId,
      request: MnnRequest(
        modelPath: request.modelPath, prompt: payload.prompt,
        steps: Int(payload.samplingSteps), seed: seed, backend: backend),
      onProgress: { onProgress(KotlinInt(int: Int32($0))) },
      completion: { result in
        switch result {
        case .success(let data):
          completion(
            MnnAppleResult(
              imageBase64: data.base64EncodedString(),
              errorMessage: nil, isCancelled: false))
        case .failure(let error):
          completion(
            MnnAppleResult(
              imageBase64: nil,
              errorMessage: error.rawValue, isCancelled: error == .cancelled))
        }
      })
  }

  func interrupt(ownerId: String) { generator.interrupt(ownerId: ownerId) }
  func readManifest(path: String) throws -> String { try files.manifest(path) }
  func canonicalRoot(path: String) throws -> String {
    URL(fileURLWithPath: path).resolvingSymlinksInPath().standardizedFileURL.path
  }
  func sha256(path: String) throws -> String { try files.sha256(path) }
  func isRegularFile(path: String) -> Bool { files.isRegularFile(path) }
  func metadata(path: String) throws -> DomainMnnFileMetadata {
    DomainMnnFileMetadata(sizeBytes: try files.size(path))
  }
  func entries(path: String) throws -> [String] { try files.entries(path) }
  func extract(archivePath: String, stagingPath: String) throws {
    try files.extract(archivePath: archivePath, stagingPath: stagingPath)
  }
}

enum SiliconDiffusionMnnRuntimeComposition {
  static func install(koin: Koin_coreKoin) {
    let adapter = SiliconDiffusionMnnRuntimeAdapter()
    MnnHostModuleKt.installMnnHost(
      koin: koin, runtime: adapter,
      fileSystem: adapter, extractor: adapter)
  }
}
