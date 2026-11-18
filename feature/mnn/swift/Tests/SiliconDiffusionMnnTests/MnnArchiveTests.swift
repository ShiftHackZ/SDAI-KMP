import Foundation
import Testing
import ZIPFoundation

@testable import SiliconDiffusionMnn

private func extractionFailure(names: [String], linkedName: String? = nil) throws -> Bool {
  let root = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
  try FileManager.default.createDirectory(at: root, withIntermediateDirectories: true)
  defer { try? FileManager.default.removeItem(at: root) }
  let staging = root.appendingPathComponent("staging")
  try FileManager.default.createDirectory(at: staging, withIntermediateDirectories: false)
  let zip = root.appendingPathComponent("bundle.zip")
  let archive = try Archive(url: zip, accessMode: .create)
  for name in names {
    let bytes = Data("data".utf8)
    try archive.addEntry(
      with: name, type: name == linkedName ? .symlink : .file,
      uncompressedSize: Int64(bytes.count),
      provider: { position, size in
        bytes.subdata(in: Int(position)..<(Int(position) + size))
      })
  }
  do {
    try MnnFiles().extract(archivePath: zip.path, stagingPath: staging.path)
    return false
  } catch {
    return try FileManager.default.contentsOfDirectory(atPath: staging.path).isEmpty
  }
}

@Test(
  "given MNN archive extraction, root inventory contains traversal, expected rejection before extraction"
)
func rejectsTraversal() throws {
  // Given
  let names = MnnModelLayout.names.map { $0 == "NOTICE.md" ? "../NOTICE.md" : $0 }
  // When
  let hasRejected = try extractionFailure(names: names)
  // Then
  #expect(hasRejected)
}

@Test(
  "given MNN archive extraction, inventory contains duplicate name, expected rejection before extraction"
)
func rejectsDuplicateNames() throws {
  // Given
  let names = MnnModelLayout.names.map { $0 == "NOTICE.md" ? "LICENSE" : $0 }
  // When
  let hasRejected = try extractionFailure(names: names)
  // Then
  #expect(hasRejected)
}

@Test(
  "given MNN archive extraction, payload is a symbolic link, expected rejection before extraction")
func rejectsArchiveSymlink() throws {
  // Given
  let names = Array(MnnModelLayout.names)
  // When
  let hasRejected = try extractionFailure(names: names, linkedName: "unet.mnn")
  // Then
  #expect(hasRejected)
}

@Test(
  "given MNN archive extraction, payload inventory is incomplete, expected rejection before extraction"
)
func rejectsMissingPayload() throws {
  // Given
  let names = Array(MnnModelLayout.names.filter { $0 != "unet.mnn" })
  // When
  let hasRejected = try extractionFailure(names: names)
  // Then
  #expect(hasRejected)
}

@Test(
  "given MNN trusted container root, ancestor is a system alias, expected successful extraction into canonical owned staging"
)
func acceptsTrustedContainerAlias() throws {
  // Given
  let manager = FileManager.default
  let root = manager.temporaryDirectory.appendingPathComponent(UUID().uuidString)
    .resolvingSymlinksInPath()
  try manager.createDirectory(at: root, withIntermediateDirectories: true)
  defer { try? manager.removeItem(at: root) }
  let container = root.appendingPathComponent("container")
  let alias = root.appendingPathComponent("container-alias")
  try manager.createDirectory(at: container, withIntermediateDirectories: false)
  try manager.createSymbolicLink(at: alias, withDestinationURL: container)
  let staging = container.appendingPathComponent("staging")
  try manager.createDirectory(at: staging, withIntermediateDirectories: false)
  let zip = container.appendingPathComponent("bundle.zip")
  let archive = try Archive(url: zip, accessMode: .create)
  for name in MnnModelLayout.names {
    let bytes = Data("payload".utf8)
    try archive.addEntry(
      with: name, type: .file, uncompressedSize: Int64(bytes.count),
      provider: { position, size in bytes.subdata(in: Int(position)..<(Int(position) + size)) })
  }

  // When
  try MnnFiles().extract(
    archivePath: alias.appendingPathComponent("bundle.zip").path,
    stagingPath: alias.appendingPathComponent("staging").path)

  // Then
  #expect(Set(try manager.contentsOfDirectory(atPath: staging.path)) == MnnModelLayout.names)
  let payload = staging.appendingPathComponent("unet.mnn")
  let link = staging.appendingPathComponent("linked-payload")
  try manager.createSymbolicLink(at: link, withDestinationURL: payload)
  #expect(!MnnFiles().isRegularFile(link.path))
}
