import Foundation
import ImageIO
import MnnNative

/// Owns the opaque C runtime until the native call returns to its executing queue.
public final class MnnNativeExecutionAttempt: MnnExecutionAttempt, @unchecked Sendable {
  private let handle: OpaquePointer?
  public init() { handle = mnn_attempt_create() }
  deinit { mnn_attempt_destroy(handle) }
  public func interrupt() { mnn_attempt_interrupt(handle) }

  public func execute(
    request: MnnRequest, workspace: URL,
    onProgress: @escaping @Sendable (Int) -> Void
  ) throws -> Data {
    #if targetEnvironment(simulator) || os(macOS)
      throw MnnFailure.unavailable
    #else
      guard (1...50).contains(request.steps), request.seed >= 0,
        !request.prompt.utf8.contains(0), !request.modelPath.utf8.contains(0),
        request.modelPath.hasPrefix("/"),
        handle != nil
      else { throw MnnFailure.invalidRequest }
      if request.backend == .metal && mnn_metal_available() == 0 {
        throw MnnFailure.metalUnavailable
      }
      let output = workspace.appendingPathComponent("result.png")
      let callback = MnnProgressCallback(onProgress)
      let status = withExtendedLifetime(callback) {
        mnn_attempt_generate(
          handle, request.modelPath, request.prompt,
          output.path, workspace.path, Int32(request.steps), request.seed,
          request.backend == .metal ? 1 : 0,
          { context, percentage in
            guard let context else { return }
            Unmanaged<MnnProgressCallback>.fromOpaque(context).takeUnretainedValue().onProgress(
              Int(percentage))
          }, Unmanaged.passUnretained(callback).toOpaque())
      }
      if status == 1 { throw MnnFailure.cancelled }
      guard status == 0 else { throw MnnFailure.nativeFailure }
      guard try MnnFiles().size(output.path) <= 16_777_216 else { throw MnnFailure.invalidImage }
      let data = try Data(contentsOf: output)
      guard let image = CGImageSourceCreateWithData(data as CFData, nil),
        CGImageSourceGetStatus(image) == .statusComplete,
        CGImageSourceGetType(image) as String? == "public.png",
        let properties = CGImageSourceCopyPropertiesAtIndex(image, 0, nil) as? [CFString: Any],
        properties[kCGImagePropertyPixelWidth] as? Int == 512,
        properties[kCGImagePropertyPixelHeight] as? Int == 512,
        CGImageSourceCreateImageAtIndex(image, 0, nil) != nil
      else { throw MnnFailure.invalidImage }
      return data
    #endif
  }
}

private final class MnnProgressCallback {
  let onProgress: @Sendable (Int) -> Void
  init(_ callback: @escaping @Sendable (Int) -> Void) { onProgress = callback }
}
