import CoreGraphics
import Foundation
import ImageIO
import MnnNative

/// Converts bounded images to RGB tiles and returns a complete lossless PNG.
enum MnnUpscalerExecution {
  static func run(
    handle: OpaquePointer?, modelPath: String, encoded: Data,
    onProgress: @escaping @Sendable (Int) -> Void
  ) throws -> Data {
    guard let source = CGImageSourceCreateWithData(encoded as CFData, nil),
      CGImageSourceGetStatus(source) == .statusComplete,
      let properties = CGImageSourceCopyPropertiesAtIndex(source, 0, nil) as? [CFString: Any],
      let width = properties[kCGImagePropertyPixelWidth] as? Int,
      let height = properties[kCGImagePropertyPixelHeight] as? Int,
      (1...1024).contains(width), (1...1024).contains(height),
      let image = CGImageSourceCreateImageAtIndex(source, 0, nil)
    else { throw MnnFailure.invalidImage }
    let colorSpace = CGColorSpaceCreateDeviceRGB()
    var rgba = [UInt8](repeating: 0, count: width * height * 4)
    let didDraw = rgba.withUnsafeMutableBytes { buffer -> Bool in
      guard
        let context = CGContext(
          data: buffer.baseAddress, width: width, height: height, bitsPerComponent: 8,
          bytesPerRow: width * 4, space: colorSpace,
          bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)
      else { return false }
      context.draw(image, in: CGRect(x: 0, y: 0, width: width, height: height))
      return true
    }
    guard didDraw else { throw MnnFailure.invalidImage }
    var input = [UInt8](repeating: 0, count: width * height * 3)
    for pixel in 0..<(width * height) {
      for channel in 0..<3 { input[pixel * 3 + channel] = rgba[pixel * 4 + channel] }
    }
    let outWidth = width * 4
    let outHeight = height * 4
    var output = [UInt8](repeating: 0, count: outWidth * outHeight * 3)
    let callback = UpscalerProgress(onProgress)
    let status = withExtendedLifetime(callback) {
      input.withUnsafeBufferPointer { inputBuffer in
        output.withUnsafeMutableBufferPointer { outputBuffer in
          mnn_attempt_upscale(
            handle, modelPath, inputBuffer.baseAddress, Int32(width), Int32(height),
            outputBuffer.baseAddress,
            { context, percent in
              guard let context else { return }
              Unmanaged<UpscalerProgress>.fromOpaque(context).takeUnretainedValue().send(
                Int(percent))
            }, Unmanaged.passUnretained(callback).toOpaque())
        }
      }
    }
    if status == 1 { throw MnnFailure.cancelled }
    guard status == 0 else { throw MnnFailure.nativeFailure }
    let pixelData = Data(output)
    guard let provider = CGDataProvider(data: pixelData as CFData),
      let result = CGImage(
        width: outWidth, height: outHeight, bitsPerComponent: 8, bitsPerPixel: 24,
        bytesPerRow: outWidth * 3, space: colorSpace, bitmapInfo: CGBitmapInfo(rawValue: 0),
        provider: provider, decode: nil, shouldInterpolate: false, intent: .defaultIntent)
    else { throw MnnFailure.invalidImage }
    let encodedOutput = NSMutableData()
    guard
      let destination = CGImageDestinationCreateWithData(
        encodedOutput, "public.png" as CFString, 1, nil)
    else { throw MnnFailure.invalidImage }
    CGImageDestinationAddImage(destination, result, nil)
    guard CGImageDestinationFinalize(destination) else { throw MnnFailure.invalidImage }
    return encodedOutput as Data
  }
}

private final class UpscalerProgress {
  let send: @Sendable (Int) -> Void
  init(_ send: @escaping @Sendable (Int) -> Void) { self.send = send }
}
