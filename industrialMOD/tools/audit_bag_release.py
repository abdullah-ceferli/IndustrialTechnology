"""Read-only audit of the executed 2.7.0 bag release and original ProjectE asset identity."""
from pathlib import Path
import zipfile,json,hashlib,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def payload(p):
    with zipfile.ZipFile(p) as z:
        assert z.testzip() is None
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    folder=ROOT/'docs/test-results/2.7.0';record=json.loads((folder/'release.json').read_text())
    jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.7.0.jar';sha=digest(jar);assert sha==record['sha256']
    now=payload(jar);old=payload(ROOT/'.toolchain/console-screen-release-2.6.1/IndustrialTechnology-1.7.10-2.6.1.jar')
    allowed={'dev/industrialtechnology/core/IndustrialTechnology.class','dev/industrialtechnology/core/ModRegistry.class',
      'dev/industrialtechnology/core/CommonProxy.class','dev/industrialtechnology/client/ClientProxy.class','dev/industrialtechnology/client/ClientProxy$1.class'}
    for n,b in old.items():
        if n.endswith('.class') and n not in allowed:assert now.get(n)==b,'Unrelated runtime class changed: '+n
        if n.startswith('assets/') and not n.endswith('lang/en_US.lang'):assert now.get(n)==b,'Old asset changed: '+n
    assert now['assets/industrialtech/lang/en_US.lang'].startswith(old['assets/industrialtech/lang/en_US.lang'])
    provenance=json.loads((ROOT/'development-assets/bags/projecte-assets.json').read_text())
    expected=set(provenance['assets'])
    assert {n for n in now if n.startswith('assets/') and n not in old}==expected
    assert len(expected)==17
    for n,asset in provenance['assets'].items():assert hashlib.sha256(now[n]).hexdigest()==asset['sha256'],'Original texture changed: '+n
    for n in now:
        if n.endswith('.class') and n not in old:assert n.startswith('dev/industrialtechnology/bag/') or n=='dev/industrialtechnology/client/GuiStorageBag.class',n
        assert not n.startswith(('moze_intel/','assets/projecte/','api/hbm/','com/hbm/','codechicken/'))
        assert not n.endswith('.jar') and '/test/' not in n
    assert 'META-INF/notices/ProjectE-MC17-README.md' in now
    assert json.loads(now['mcmod.info'])[0]['version']=='2.7.0'
    assert b'func_70302_i_' in now['dev/industrialtechnology/bag/BagInventory.class'],'Development JAR'
    sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.7.0-sources.jar')
    for p in (ROOT/'src/main/java').rglob('*.java'):assert sources[p.relative_to(ROOT/'src/main/java').as_posix()]==p.read_bytes(),'Stale source: '+str(p)
    for n in ['BAG_GUIDE.md','PROJECT_HANDOFF.md','README.md','ASSET_CREDITS.md','docs/CHANGES-2.7.0.md']:assert sources[n]==(ROOT/n).read_bytes(),'Stale guide: '+n
    assert digest(ROOT/'.toolchain/bags-release-2.7.0'/jar.name)==sha
    for directory in record['nativeDirectories']:assert digest(ROOT/directory/'mods'/jar.name)==sha
    total=0
    for name,count in record['checks'].items():
        if name=='JUnit':
            nodes=[ET.parse(p).getroot() for p in folder.glob('TEST-*.xml')]
            assert sum(int(n.get('tests')) for n in nodes)==count
            assert all(int(n.get('failures'))==int(n.get('errors'))==int(n.get('skipped'))==0 for n in nodes)
        else:
            lines=(folder/name).read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines)
            assert sum(s.startswith('PASS:') for s in lines)==count
        total+=count
    assert total==record['totalChecks']
    print('PASS: %s checks; tested production identity; matching sources; all 17 ProjectE textures unchanged; older machinery unchanged; no ProjectE code dependency'%total)
    print('SHA256: '+sha)
if __name__=='__main__':main()
