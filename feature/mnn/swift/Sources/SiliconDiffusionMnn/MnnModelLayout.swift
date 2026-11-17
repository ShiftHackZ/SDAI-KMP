import Foundation

/// Root filenames accepted by extraction; metadata and payload hashes are verified by the shared policy.
public enum MnnModelLayout {
  public static let names: Set<String> = [
    "manifest.json", "text_encoder.mnn", "text_encoder.mnn.weight",
    "unet.mnn", "unet.mnn.weight", "vae_decoder.mnn", "vae_decoder.mnn.weight",
    "tokenizer.mtok", "LICENSE", "NOTICE.md",
  ]
}
