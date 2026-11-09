"""Preserve all OC development classes/assets; let the combined plugin load its core once."""
from pathlib import Path
import json,hashlib,zipfile
ROOT=Path(__file__).resolve().parents[1];lock=json.loads((ROOT/'vendor/opencomputers/upstream.json').read_text());pin=next(f for f in lock['files'] if f['name'].endswith('-dev.jar'));source=ROOT/'vendor/opencomputers'/pin['name']
assert hashlib.sha256(source.read_bytes()).hexdigest()==pin['sha256']
target=ROOT/'.toolchain/opencomputers-upstream/OpenComputers-combined-development.jar'
with zipfile.ZipFile(source) as old,zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as new:
    for name in old.namelist():
        new.writestr(name,b'Manifest-Version: 1.0\r\nFMLAT: oc_at.cfg\r\n\r\n' if name=='META-INF/MANIFEST.MF' else old.read(name))
print(target)
