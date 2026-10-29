"""Read-only audit of the 2.6.1 rendering-only release and executed evidence."""
from pathlib import Path
import zipfile,hashlib,json,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def payload(p):
    with zipfile.ZipFile(p) as z:
        assert z.testzip() is None
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    folder=ROOT/'docs/test-results/2.6.1';record=json.loads((folder/'release.json').read_text())
    jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.6.1.jar';sha=digest(jar)
    assert sha==record['sha256']
    now=payload(jar);old=payload(ROOT/'.toolchain/consoles-release-2.6.0/IndustrialTechnology-1.7.10-2.6.0.jar')
    allowed={'dev/industrialtechnology/core/IndustrialTechnology.class','dev/industrialtechnology/client/ConsoleBlockRenderer.class'}
    for n,b in old.items():
        if n.endswith('.class') and n not in allowed:assert now.get(n)==b,'Unrelated runtime change: '+n
        if n.startswith('assets/'):assert now.get(n)==b,'Asset changed: '+n
    assert {n for n in now if n.startswith('assets/')}=={n for n in old if n.startswith('assets/')}
    for n in now:
        if n.endswith('.class') and n not in old:assert n.startswith('dev/industrialtechnology/client/ConsoleScreenMesh'),n
    assert json.loads(now['mcmod.info'])[0]['version']=='2.6.1'
    assert b'func_145845_h' in now['dev/industrialtechnology/console/TileConsole.class'],'Development JAR'
    assert digest(ROOT/record['nativeDirectory']/'mods'/jar.name)==sha
    assert digest(ROOT/'.toolchain/console-screen-release-2.6.1'/jar.name)==sha
    sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.6.1-sources.jar')
    for p in (ROOT/'src/main/java').rglob('*.java'):
        assert sources[p.relative_to(ROOT/'src/main/java').as_posix()]==p.read_bytes(),'Stale source: '+str(p)
    for n in ['PROJECT_HANDOFF.md','README.md','CONSOLE_GUIDE.md','docs/CHANGES-2.6.1.md']:
        assert sources[n]==(ROOT/n).read_bytes(),'Stale guide: '+n
    total=0
    for name,count in record['checks'].items():
        if name=='JUnit':
            nodes=[ET.parse(p).getroot() for p in folder.glob('TEST-*.xml')]
            assert sum(int(n.get('tests')) for n in nodes)==count
            assert all(int(n.get('failures'))==int(n.get('errors'))==int(n.get('skipped'))==0 for n in nodes)
        else:
            lines=(folder/name).read_text().splitlines()
            assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines)
            assert sum(s.startswith('PASS:') for s in lines)==count
        total+=count
    assert total==record['totalChecks']
    print('PASS: %s recorded checks; exact production bytes; matching sources; all old machine code and assets unchanged'%total)
    print('SHA256: '+sha)
if __name__=='__main__':main()
