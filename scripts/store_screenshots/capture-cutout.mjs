import fs from "node:fs";
import { execFileSync } from "node:child_process";
import { inflateSync } from "node:zlib";

const cache = new Map();

/** Native iOS routes can include the hardware mask while Compose routes omit it. */
export function shouldRenderCutout(source, profile) {
  if (!["iphone", "duo-outer"].includes(profile)) return false;
  const key = `${profile}:${source.file}:${fs.statSync(source.file).mtimeMs}`;
  if (cache.has(key)) return cache.get(key);
  const x = source.size.width * (profile === "iphone" ? 0.5 : 0.897);
  const y = source.size.width * (profile === "iphone" ? 0.075 : 0.1023);
  const href = `data:image/png;base64,${fs.readFileSync(source.file).toString("base64")}`;
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="1" height="1" viewBox="${x} ${y} 1 1"><image href="${href}" width="${source.size.width}" height="${source.size.height}"/></svg>`;
  const png = execFileSync("rsvg-convert", [], { input: svg });
  const chunks = [];
  for (let offset = 8; offset < png.length; ) {
    const length = png.readUInt32BE(offset);
    if (png.toString("ascii", offset + 4, offset + 8) === "IDAT") {
      chunks.push(png.subarray(offset + 8, offset + 8 + length));
    }
    offset += length + 12;
  }
  if (png[24] !== 8 || ![2, 6].includes(png[25])) {
    throw new Error("Cutout sampling requires an 8-bit RGB pixel");
  }
  // Every PNG row filter has zero neighbours for a single isolated pixel.
  const pixel = inflateSync(Buffer.concat(chunks));
  const shouldRender = [pixel[1], pixel[2], pixel[3]].some(
    (value) => value > 16,
  );
  cache.set(key, shouldRender);
  return shouldRender;
}
