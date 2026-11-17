# MNN SD 1.5 model packaging

The Android MNN provider accepts the `sd15-512-general-int8-v1` profile. End users
select a complete downloaded bundle or use the existing custom model folder
picker outside the Play build. They do not convert weights.

The maintainer utility reuses the immutable taobao-mnn general SD 1.5 graphs and
weights. It compares their original vocabulary and merges with the source CLIP
tokenizer, exports `tokenizer.mtok` using the pinned MNN exporter, checks the actual
graph input declarations, and packages exact lengths, SHA256s, provenance and the
model license in a root-layout ZIP. No graph conversion occurs.

Use Python 3.14 with an isolated environment and the dependency lock:

```sh
python3.14 -m venv /tmp/mnn-packaging
/tmp/mnn-packaging/bin/pip install -r scripts/mnn/requirements.lock
curl -fL https://github.com/alibaba/MNN/archive/7e3c6fb5225d3ccf22807ee641332669df34650d.tar.gz -o /tmp/mnn-source.tar.gz
shasum -a 256 /tmp/mnn-source.tar.gz
tar -xzf /tmp/mnn-source.tar.gz -C /tmp
/tmp/mnn-packaging/bin/python scripts/package_mnn_sd15.py \
  --work /tmp/mnn-sd15 \
  --mnn-source /tmp/MNN-7e3c6fb5225d3ccf22807ee641332669df34650d
```

The source archive SHA256 must be
`01dcdce39072b70188079a1dca0600e782c8dd0fcf2ad77f610bab264166a0cf`.
The script verifies the pinned tokenizer exporter. The prepared profile ZIP is
about 1.15 GB. Allow several additional GB for downloads, staging and activation.
Keep generated files outside Git.

The pinned prebuilt declarations are dynamic: text `input_ids [1,-1]`, UNet
`sample [1,-1,-1,-1]`, `timestep [1]`, `encoder_hidden_states [1,-1,768]`, and VAE
`latent_sample [1,-1,-1,-1]`. The adapter encodes the unconditional and positive text together at batch two,
then runs two sequential batch-one UNet forwards using the same latent and
timestep. Explicitly copied branch predictions retain CFG 7.5 without sharing
mutable output buffers. The fixed 512 profile, initial noise seed and PNDM
scheduler remain unchanged. Modules load by stage; detached embeddings and
latents allow the previous stage and its unused memory pools to be released
before loading the next one. This reduces activation pressure but does not
guarantee a particular memory limit or device compatibility. CPU UNet weights and
workspace use private file-backed mappings initialized after text encoding. The
allocator reserves disk space before writes, uses 64 MiB minimum weight chunks,
and fails allocation instead of falling back to anonymous RAM. Mappings are never
trusted or reused across attempts. A cross-process lock protects cleanup of
abandoned UUID cache directories after process death; cleanup never follows
symlinks. OpenCL retains its existing memory policy. Bounded native lifecycle logs
distinguish stage loading from forward execution without including prompts.
The measured timestep declaration requires one initialized
integer. Do not apply a two-element timestep patch to this bundle.

Packaging success establishes artifact integrity. Validate real CPU/OpenCL image
generation through SDAI before distributing a bundle. Publish the exact reviewed
ZIP on the chosen artifact host, verify its bytes, then add its immutable working
URL to `docs/mnn.json` using id `mnn_sd15_general_int8_v1`, name, size and sources.
The catalog pins the published SD 1.5 General bundle from
[the MNN archive repository](https://huggingface.co/moroz-ellmsha/sdai-mnn-model-archives),
which also provides the manifest, archive SHA256, license and attribution.

Model weights/tokenizer retain CreativeML OpenRAIL-M restrictions and attribution.
MNN runtime code is separately Apache-2.0. Eight-bit weights do not imply INT8
activations or universal GPU acceleration. NPU, fused graphs, SDXL and arbitrary
checkpoints are outside this profile. Reconvert only after a demonstrated graph
incompatibility requires a replacement, using the same pinned upstream revision.
