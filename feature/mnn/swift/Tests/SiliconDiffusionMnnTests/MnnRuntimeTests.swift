import Foundation
import Testing

@testable import SiliconDiffusionMnn

@Test("given MNN regular file validation, symbolic link is supplied, expected rejection")
func rejectsSymbolicLink() throws {
  // Given
  let root = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  try FileManager.default.createDirectory(at: root, withIntermediateDirectories: true)
  defer { try? FileManager.default.removeItem(at: root) }
  let source = root.appendingPathComponent("source")
  try Data("payload".utf8).write(to: source)
  let link = root.appendingPathComponent("link")
  try FileManager.default.createSymbolicLink(at: link, withDestinationURL: source)
  // When
  let isRegular = MnnFiles().isRegularFile(link.path)
  // Then
  #expect(!isRegular)
}

@Test("given MNN payload digest, complete file is read, expected exact SHA256")
func hashesWholeFile() throws {
  // Given
  let file = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  try Data("abc".utf8).write(to: file)
  defer { try? FileManager.default.removeItem(at: file) }
  // When
  let digest = try MnnFiles().sha256(file.path)
  // Then
  #expect(digest == "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
}

@Test("given MNN manifest reading, size exceeds limit, expected rejection")
func rejectsLargeManifest() throws {
  // Given
  let file = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  try Data(repeating: 0, count: 65_537).write(to: file)
  defer { try? FileManager.default.removeItem(at: file) }
  // When
  let error = Result { try MnnFiles().manifest(file.path) }
  // Then
  #expect(throws: MnnFailure.invalidBundle) { try error.get() }
}

@Test("given MNN archive extraction, incomplete ZIP is supplied, expected no published install")
func rejectsPartialArchive() throws {
  // Given
  let root = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  try FileManager.default.createDirectory(at: root, withIntermediateDirectories: true)
  defer { try? FileManager.default.removeItem(at: root) }
  let zip = root.appendingPathComponent("model.zip")
  try Data([0x50, 0x4b, 3, 4]).write(to: zip)
  let staging = root.appendingPathComponent("staging")
  try FileManager.default.createDirectory(at: staging, withIntermediateDirectories: false)
  // When
  let result = Result { try MnnFiles().extract(archivePath: zip.path, stagingPath: staging.path) }
  // Then
  #expect(throws: (any Error).self) { try result.get() }
  #expect(try FileManager.default.contentsOfDirectory(atPath: staging.path).isEmpty)
}

@Test(
  "given MNN simulator runtime, generation is submitted, expected explicit unavailable completion")
func rejectsHostInference() async {
  // Given
  let runtime = SiliconDiffusionMnnGenerator(makeAttempt: { MnnNativeExecutionAttempt() })
  let request = MnnRequest(modelPath: "/missing", prompt: "cat", steps: 20, seed: 1, backend: .cpu)
  // When
  let result: Result<Data, MnnFailure> = await withCheckedContinuation { continuation in
    runtime.generate(
      ownerId: UUID().uuidString, request: request,
      onProgress: { _ in }, completion: { continuation.resume(returning: $0) })
  }
  // Then
  #expect(throws: MnnFailure.unavailable) { try result.get() }
}
