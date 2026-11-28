/** Device chrome surrounds an aspect-preserving capture; the bezel never covers its edges. */
export function deviceFrame({
  id,
  sourceHref,
  sourceSize,
  x,
  y,
  width,
  kind,
  platform,
  profile,
  shouldRenderCutout = false,
}) {
  const insets = bezelInsets(kind, profile);
  const bezel = width * insets.y;
  const sideBezel = width * insets.x;
  const screenWidth = width - sideBezel * 2;
  const screenHeight = (screenWidth * sourceSize.height) / sourceSize.width;
  const height = screenHeight + bezel * 2;
  const radius =
    width * (kind === "phone" ? 0.09 : kind === "foldable" ? 0.07 : 0.035);
  const isIos = platform === "ios";
  const isDuoOuter = profile === "duo-outer";
  const isDuoInner = profile === "duo-inner";
  const isPixelTablet = platform === "android" && profile === "tablet-10";
  const leftRadius = isDuoOuter ? width * 0.012 : radius;
  const rightRadius = isDuoOuter ? width * 0.16 : radius;
  const outline = (inset) =>
    framePath(
      x + inset,
      y + inset,
      width - inset * 2,
      height - inset * 2,
      Math.max(0, leftRadius - inset),
      Math.max(0, rightRadius - inset),
    );
  const screenRadius =
    profile === "tablet-7"
      ? 0
      : Math.max(0, radius - Math.min(bezel, sideBezel));
  const displayOutline = framePath(
    x + sideBezel,
    y + bezel,
    screenWidth,
    screenHeight,
    isDuoOuter ? 0 : screenRadius,
    isDuoOuter ? rightRadius - sideBezel : screenRadius,
  );
  const side = width * (isDuoInner ? 0.006 : 0.012);
  const buttons =
    isDuoOuter || isDuoInner
      ? duoButtons({ x, y, width, height, side, isClosed: isDuoOuter })
      : kind === "tablet"
        ? isIos
          ? `<rect x="${x + width * 0.78}" y="${y - side}" width="${width * 0.09}" height="${side}" rx="${side / 2}" fill="#a8a9af"/>
         <rect x="${x + width}" y="${y + height * 0.12}" width="${side}" height="${height * 0.09}" rx="${side / 2}" fill="#a8a9af"/>`
          : isPixelTablet
            ? `<rect x="${x + width * 0.12}" y="${y - side}" width="${width * 0.055}" height="${side}" rx="${side / 2}" fill="#979a9c"/>
             <rect x="${x + width * 0.21}" y="${y - side}" width="${width * 0.09}" height="${side}" rx="${side / 2}" fill="#979a9c"/>`
            : `<rect x="${x + width}" y="${y + height * 0.12}" width="${side}" height="${height * 0.045}" rx="${side / 2}" fill="#7f838d"/>
             <rect x="${x + width}" y="${y + height * 0.19}" width="${side}" height="${height * 0.08}" rx="${side / 2}" fill="#7f838d"/>`
        : isIos
          ? `<rect x="${x - side}" y="${y + height * 0.18}" width="${side}" height="${height * 0.06}" rx="${side / 2}" fill="#97948e"/>
         <rect x="${x - side}" y="${y + height * 0.29}" width="${side}" height="${height * 0.11}" rx="${side / 2}" fill="#97948e"/>
         <rect x="${x + width}" y="${y + height * 0.265}" width="${side}" height="${height * 0.13}" rx="${side / 2}" fill="#d8d0c4"/>`
          : `<rect x="${x + width}" y="${y + height * 0.19}" width="${side}" height="${height * 0.095}" rx="${side / 2}" fill="#7f838d"/>
         <rect x="${x + width}" y="${y + height * 0.33}" width="${side}" height="${height * 0.13}" rx="${side / 2}" fill="#7f838d"/>`;

  const cameraCutout =
    shouldRenderCutout && profile === "iphone"
      ? `<rect x="${x + sideBezel + screenWidth * 0.39242}" y="${y + bezel + screenWidth * 0.03182}" width="${screenWidth * 0.21515}" height="${screenWidth * 0.08333}" rx="${screenWidth * 0.041665}" fill="#000"/>`
      : shouldRenderCutout && isDuoOuter
        ? `<circle cx="${x + sideBezel + screenWidth * 0.897}" cy="${y + bezel + screenWidth * 0.1023}" r="${screenWidth * 0.04}" fill="#000"/>`
        : platform === "android" && kind === "phone"
          ? `<circle cx="${x + width / 2}" cy="${y + bezel + screenWidth * 0.04}" r="${screenWidth * 0.018}" fill="#050508"/>`
          : "";
  const camera =
    kind === "tablet"
      ? isIos
        ? `<circle cx="${x + width - sideBezel / 2}" cy="${y + height / 2}" r="${width * 0.0035}" fill="#050508"/>`
        : `<circle cx="${x + width / 2}" cy="${y + bezel / 2}" r="${width * 0.0035}" fill="#050508"/>`
      : "";
  return `<defs>
    <linearGradient id="edge-${id}" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="${isIos ? "#eee7dc" : "#9b9da6"}"/>
      <stop offset="0.42" stop-color="${isIos ? "#a8a096" : "#353740"}"/>
      <stop offset="1" stop-color="${isIos ? "#f8f4eb" : "#727781"}"/>
    </linearGradient>
  </defs>
  <g filter="url(#shadow-${id})">
    ${buttons}
    <path d="${outline(0)}" fill="url(#edge-${id})"/>
    <path d="${outline(width * 0.005)}" fill="${isPixelTablet ? "#e9e7de" : "#101116"}"/>
    <clipPath id="display-${id}"><path d="${displayOutline}"/></clipPath>
    <image href="${sourceHref}" x="${x + sideBezel}" y="${y + bezel}" width="${screenWidth}" height="${screenHeight}" preserveAspectRatio="xMidYMid meet" clip-path="url(#display-${id})"/>
    ${cameraCutout}
    ${camera}
    <path d="${outline(width * 0.004)}" fill="none" stroke="#ffffff" stroke-opacity="0.3" stroke-width="${width * 0.002}"/>
  </g>`;
}

/** Foldable controls remain outside the display in both postures. */
function duoButtons({ x, y, width, height, side, isClosed }) {
  const hinge = isClosed
    ? `<rect x="${x - width * 0.025}" y="${y + side}" width="${width * 0.02}" height="${height - side * 2}" rx="${side / 2}" fill="#777775"/>`
    : "";
  const buttonWidth = width * (isClosed ? 0.07 : 0.035);
  return `${hinge}
    <rect x="${x + width * (isClosed ? 0.46 : 0.73)}" y="${y - side}" width="${buttonWidth}" height="${side}" rx="${side / 2}" fill="#a8a9af"/>
    <rect x="${x + width * (isClosed ? 0.65 : 0.83)}" y="${y - side}" width="${buttonWidth}" height="${side}" rx="${side / 2}" fill="#a8a9af"/>
    <rect x="${x + width}" y="${y + height * 0.28}" width="${side}" height="${height * 0.13}" rx="${side / 2}" fill="#a8a9af"/>`;
}

/** Closed foldables have a flat spine and a larger radius on the opposite edge. */
function framePath(x, y, width, height, left, right) {
  return `M ${x + left} ${y} H ${x + width - right} A ${right} ${right} 0 0 1 ${x + width} ${y + right} V ${y + height - right} A ${right} ${right} 0 0 1 ${x + width - right} ${y + height} H ${x + left} A ${left} ${left} 0 0 1 ${x} ${y + height - left} V ${y + left} A ${left} ${left} 0 0 1 ${x + left} ${y} Z`;
}

function bezelInsets(kind, profile) {
  if (profile === "tablet-7") return { x: 0.045, y: 0.045 };
  if (profile === "tablet-10") return { x: 0.043, y: 0.043 };
  const inset = kind === "tablet" ? 0.025 : 0.018;
  return { x: inset, y: inset };
}

export function framedSize(sourceSize, width, kind, profile) {
  const inset = bezelInsets(kind, profile);
  return {
    width,
    height:
      width *
      (((1 - inset.x * 2) * sourceSize.height) / sourceSize.width +
        inset.y * 2),
  };
}
