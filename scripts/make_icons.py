from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
src = Image.open(root / "docs/images/icon-source.jpg").convert("RGBA")
w, h = src.size
side = min(w, h)
left = (w - side) // 2
top = (h - side) // 2
src = src.crop((left, top, left + side, top + side))

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}
for dens, px in sizes.items():
    d = root / f"app/src/main/res/mipmap-{dens}"
    d.mkdir(parents=True, exist_ok=True)
    src.resize((px, px), Image.Resampling.LANCZOS).save(d / "ic_launcher.png", "PNG")

meta = root / "metadata/en-US/images"
meta.mkdir(parents=True, exist_ok=True)
src.resize((512, 512), Image.Resampling.LANCZOS).save(meta / "icon.png", "PNG")
src.resize((1024, 500), Image.Resampling.LANCZOS).save(root / "docs/images/feature.png", "PNG")
print("icons written")
