"""Generate the lamp's two intentionally flat color tiles; no casing or decorative pixels."""
from pathlib import Path
from PIL import Image
ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / 'src/main/resources/assets/industrialtech/textures/blocks'
for state, color in [('off', (26, 128, 46)), ('on', (76, 255, 92))]:
    Image.new('RGB', (16, 16), color).save(TEXTURES / ('green_lamp_' + state + '.png'))
