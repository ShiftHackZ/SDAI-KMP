import CryptoKit
import Foundation
import ZIPFoundation

/// Strict regular-file IO used by the common Kotlin manifest policy.
public struct MnnFiles: Sendable {
  public init() {}

  public func isRegularFile(_ path: String) -> Bool {
    let url = URL(fileURLWithPath: path)
    guard url.resolvingSymlinksInPath().standardizedFileURL.path == url.standardizedFileURL.path,
      let values = try? url.resourceValues(forKeys: [.isRegularFileKey, .isSymbolicLinkKey])
    else { return false }
    return values.isRegularFile == true && values.isSymbolicLink != true
  }

  public func size(_ path: String) throws -> Int64 {
    guard isRegularFile(path),
      let value = try FileManager.default.attributesOfItem(atPath: path)[.size] as? NSNumber
    else { throw MnnFailure.invalidBundle }
    return value.int64Value
  }

  public func manifest(_ path: String) throws -> String {
    guard try (1...65_536).contains(size(path)) else { throw MnnFailure.invalidBundle }
    return try String(contentsOfFile: path, encoding: .utf8)
  }

  public func sha256(_ path: String) throws -> String {
    guard isRegularFile(path) else { throw MnnFailure.invalidBundle }
    let input = try FileHandle(forReadingFrom: URL(fileURLWithPath: path))
    defer { try? input.close() }
    var digest = SHA256()
    while let data = try input.read(upToCount: 65_536), !data.isEmpty {
      digest.update(data: data)
    }
    return digest.finalize().map { String(format: "%02x", $0) }.joined()
  }

  public func entries(_ path: String) throws -> [String] {
    try FileManager.default.contentsOfDirectory(atPath: path)
  }

  /// Extracts a flat inventory into an empty staging root; activation is the caller's responsibility.
  public func extract(archivePath: String, stagingPath: String) throws {
    let fileManager = FileManager.default
    let root = URL(fileURLWithPath: stagingPath, isDirectory: true)
      .resolvingSymlinksInPath().standardizedFileURL
    let archiveURL = URL(fileURLWithPath: archivePath)
    let canonicalArchive = archiveURL.deletingLastPathComponent().resolvingSymlinksInPath()
      .appendingPathComponent(archiveURL.lastPathComponent).standardizedFileURL
    guard
      try fileManager.contentsOfDirectory(atPath: root.path).isEmpty,
      isRegularFile(canonicalArchive.path)
    else { throw MnnFailure.invalidBundle }
    let archive = try Archive(url: canonicalArchive, accessMode: .read)
    let entries = Array(archive)
    guard entries.count == MnnModelLayout.names.count,
      Set(entries.map(\.path)) == MnnModelLayout.names,
      entries.allSatisfy({
        $0.type == .file && $0.uncompressedSize > 0
          && $0.uncompressedSize <= 1_200_000_000
          && ($0.path != "manifest.json" || $0.uncompressedSize <= 65_536)
      }),
      entries.reduce(UInt64(0), { $0 + $1.uncompressedSize }) <= 1_200_000_000
    else { throw MnnFailure.invalidBundle }
    for entry in entries {
      let destination = root.appendingPathComponent(entry.path)
      guard destination.deletingLastPathComponent() == root else { throw MnnFailure.invalidBundle }
      _ = try archive.extract(entry, to: destination, skipCRC32: false)
      guard isRegularFile(destination.path),
        try size(destination.path) == Int64(entry.uncompressedSize)
      else { throw MnnFailure.invalidBundle }
    }
  }
}
