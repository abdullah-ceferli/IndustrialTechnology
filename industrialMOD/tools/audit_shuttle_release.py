"""Audit 2.1.0 using only archived reports from actually executed test runs."""
from pathlib import Path
import hashlib,json,re,zipfile,struct,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def archive(p):
    with zipfile.ZipFile(p) as z:
        assert z.testzip() is None
        names=[n for n in z.namelist() if not n.endswith('/')];assert len(set(names))==len(names)
        return {n:z.read(n) for n in names}
def main():
    version=re.search(r"(?m)^version = '([^']+)'",(ROOT/'build.gradle').read_text()).group(1);assert version=='2.1.0'
    out=ROOT/'docs/test-results/2.1.0';jar=ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}.jar';data=archive(jar)
    classes={n:b for n,b in data.items() if n.endswith('.class')}
    assert all(b[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',b[6:8])[0]==52 for b in classes.values())
    assert not any(n.startswith(('net/minecraft/','cpw/','org/','com/hbm/','api/hbm/','codechicken/')) or '/test/' in n or '/spacecheck/' in n for n in classes)
    assert not any(n.endswith('.jar') for n in data)
    assert json.loads(data['mcmod.info'])[0]['version']==version
    old=archive(ROOT/'.toolchain/shuttle-baseline-2.0.1/IndustrialTechnology-1.7.10-2.0.1.jar')
    assets={n:b for n,b in old.items() if '/textures/' in n or n.endswith('automation_reference.json')}
    assert all(data.get(n)==b for n,b in assets.items()),'Existing artwork/reference changed'
    assert all(n in data for n in old if n.endswith('.class')),'An existing class disappeared'
    preserved_classes=0
    for n,b in old.items():
        if n.endswith('.class') and n.startswith('dev/industrialtechnology/') and '/space/' not in n and n!='dev/industrialtechnology/core/IndustrialTechnology.class':
            assert data[n]==b,('Non-space subsystem bytecode changed',n);preserved_classes+=1
    baseline=json.loads((ROOT/'.toolchain/shuttle-baseline-2.0.1/manifest.json').read_text());touched=[]
    for n,before in baseline.items():
        p=ROOT/n;assert p.is_file(),('Missing existing source',n)
        if sha(p)!=before:
            touched.append(n)
            if n.endswith('.java'):
                assert '/space/' in n or n=='src/main/java/dev/industrialtechnology/core/IndustrialTechnology.java',('Unexpected source edit',n)
    art=json.loads((ROOT/'docs/art/shuttle-art-manifest.json').read_text())
    for entry in art:
        path=entry['path'];local=ROOT/path;assert sha(local)==entry['sha256'];assert data[path.removeprefix('src/main/resources/')]==local.read_bytes()
    runtime=json.loads((out/'runtime-copies.json').read_text())
    for folder in runtime:assert sha(ROOT/folder/'mods'/jar.name)==sha(jar),folder
    checks=json.loads((out/'checks.json').read_text());total=0
    for name,count in checks.items():
        p=out/name
        if name.endswith('.xml'):
            suite=ET.parse(p).getroot();assert int(suite.attrib['tests'])==count;assert all(int(suite.attrib.get(k,0))==0 for k in ('errors','failures','skipped'))
        else:
            lines=p.read_text(encoding='utf-8-sig').splitlines();assert lines[-1]=='Failed: 0' and not any(n.startswith('FAIL:') for n in lines),name;assert sum(n.startswith('PASS:') for n in lines)==count
        total+=count
    for required in ('space-standalone-initial.txt','space-standalone-restart.txt','space-hbm5771-initial.txt','space-hbm5771-restart.txt','shuttle-flight-client-hbm5771.txt','shuttle-gallery-client-hbm5771.txt','upgrade-from-2.0.1.txt'):assert required in checks
    sources=archive(ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}-sources.jar')
    for n in ('SPACE_EXPLORATION_GUIDE.md','SPACE_TECHNICAL_NOTES.md','SPACE_HANDOFF.md','docs/SHUTTLE_LC39_RESOURCES.md','tools/create_shuttle_art.py'):assert sources[n]==(ROOT/n).read_bytes(),n
    report='\n'.join([f'PASS: {len(classes)} Java 8 classes; no bundled HBM/Forge/NEI/test classes or nested JARs',f'PASS: {len(assets)} existing artwork/reference files preserved byte for byte',f'PASS: {preserved_classes} preexisting non-space subsystem classes preserved byte for byte',f'PASS: {len(art)} original new pixel assets match source and packaged hashes','PASS: Existing source files and registry classes retained; changes confined to space and mod version',f'PASS: Final production JAR matches all {len(runtime)} native dedicated-server/upgrade copies',f'PASS: {total} recorded successful checks from performed reports','PASS: Source archive contains current operation guide, handoff, scale/geology notes and texture authoring source',f'SHA256: {sha(jar)}',f'Size: {jar.stat().st_size} bytes','Failed: 0'])+'\n'
    (out/'release-audit.txt').write_text(report,encoding='utf-8');(out/'release-sha256.txt').write_text(f'{sha(jar)}  {jar.name}\n',encoding='utf-8');print(report)
if __name__=='__main__':main()
