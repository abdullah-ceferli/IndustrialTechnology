"""Regenerate the independent capacitor GUI atlas. Original Ender IO block PNGs are retained unchanged."""
from pathlib import Path
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/industrialtech/textures'
OUT=ASSETS/'blocks/capacitor';OUT.mkdir(parents=True,exist_ok=True)
# Block PNGs are unchanged Ender IO assets; provenance is in META-INF/capacitor-texture-attribution.txt.
# The normal vanilla 256px GUI atlas, fixed 1px beveled edges.
gui=Image.new('RGB',(256,256),'#141e24');d=ImageDraw.Draw(gui)
d.rectangle((1,1,254,234),fill='#26383f',outline='#6b8185');d.rectangle((3,3,252,232),outline='#17252b')
d.rectangle((6,6,249,22),fill='#1b2a31');d.line((8,23,247,23),fill='#a38651')
d.rectangle((7,48,248,167),fill='#21323a',outline='#3f5961')
d.rectangle((7,169,248,209),fill='#1e2d34',outline='#435b63')
for x,y in ((4,4),(250,4),(4,230),(250,230)):d.point((x,y),fill='#91a2a0')
(ASSETS/'gui').mkdir(exist_ok=True);gui.save(ASSETS/'gui/capacitor.png')
print('Wrote original capacitor GUI atlas; Ender IO block PNGs retained unchanged')
