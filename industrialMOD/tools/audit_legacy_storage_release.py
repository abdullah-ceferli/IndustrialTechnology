"""Audit exact original-recipe restoration, production identity and executed test evidence."""
from pathlib import Path
import zipfile,json,hashlib,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def payload(p):
    with zipfile.ZipFile(p) as z:
        assert z.testzip() is None
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    folder=ROOT/'docs/test-results/2.7.1';r=json.loads((folder/'release.json').read_text());jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.7.1.jar';sha=digest(jar);assert sha==r['sha256']
    now=payload(jar);old=payload(ROOT/'.toolchain/bags-release-2.7.0/IndustrialTechnology-1.7.10-2.7.0.jar')
    allowed={'dev/industrialtechnology/core/IndustrialTechnology.class','dev/industrialtechnology/core/ModRegistry.class','dev/industrialtechnology/core/ModConfig.class'}
    for n,b in old.items():
        if n.endswith('.class') and n not in allowed:assert now.get(n)==b,'Unrelated class changed: '+n
        if n.startswith('assets/'):assert now.get(n)==b,'Unrelated asset changed: '+n
    art=json.loads((ROOT/'config-updates/hbm-legacy-storage-20261030/original-cell-assets.json').read_text())
    names={p.removeprefix('src/main/resources/') for p in art}
    assert {n for n in now if n.startswith('assets/') and n not in old}==names
    for p,info in art.items():assert hashlib.sha256(now[p.removeprefix('src/main/resources/')]).hexdigest()==info['sha256']
    assert json.loads(now['mcmod.info'])[0]['version']=='2.7.1'
    assert not any(n.startswith(('com/hbm/','api/hbm/','codechicken/','net/minecraft/')) or '/test/' in n or n.endswith('.jar') for n in now)
    for n in now:
        if n.endswith('.class') and n not in old:assert n in ['dev/industrialtechnology/integration/LegacyHbmRecipes.class','dev/industrialtechnology/integration/LegacyHbmSparkBridge.class']
    sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.7.1-sources.jar')
    for p in (ROOT/'src/main/java').rglob('*.java'):assert sources[p.relative_to(ROOT/'src/main/java').as_posix()]==p.read_bytes(),'Stale source: '+str(p)
    for n in ['LEGACY_HBM_STORAGE.md','PROJECT_HANDOFF.md','README.md','ASSET_CREDITS.md','docs/CHANGES-2.7.1.md']:assert sources[n]==(ROOT/n).read_bytes()
    for d in r['nativeDirectories']:assert digest(ROOT/d/'mods'/jar.name)==sha
    assert digest(ROOT/'.toolchain/legacy-storage-release-2.7.1'/jar.name)==sha
    assert digest(ROOT/r['nativeDirectories'][0]/'config/hbmRecipes/hbmAssemblyMachine.json')==digest(ROOT/'config-updates/hbm-legacy-storage-20261030/hbmAssemblyMachine.json')
    total=0
    for name,count in r['checks'].items():
        if name=='JUnit':
            nodes=[ET.parse(p).getroot() for p in folder.glob('TEST-*.xml')];assert sum(int(n.get('tests')) for n in nodes)==count;assert all(int(n.get('failures'))==int(n.get('errors'))==int(n.get('skipped'))==0 for n in nodes)
        else:
            lines=(folder/name).read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines);assert sum(s.startswith('PASS:') for s in lines)==count
        total+=count
    assert total==r['totalChecks']
    print('PASS: %s checks; original ingredient grids/assembler recipes; tested production bytes; matching source package; unrelated machines/assets unchanged'%total)
    print('SHA256: '+sha)
if __name__=='__main__':main()
