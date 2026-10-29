"""Audit the executed 2.6.0 release; read-only and independent of the game installation."""
from pathlib import Path
import hashlib,json,zipfile,struct,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def payload(path):
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None
        names=[n for n in z.namelist() if not n.endswith('/')]
        assert len(names)==len(set(names))
        return {n:z.read(n) for n in names}
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    folder=ROOT/'docs/test-results/2.6.0'
    record=json.loads((folder/'release.json').read_text())
    jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.6.0.jar'
    sha=digest(jar);assert sha==record['sha256']
    now=payload(jar);old=payload(ROOT/'.toolchain/steel-upgrades-release-2.5.1/IndustrialTechnology-1.7.10-2.5.1.jar')
    allowed={'dev/industrialtechnology/client/ClientProxy.class','dev/industrialtechnology/client/ClientProxy$1.class',
      'dev/industrialtechnology/client/GuiWirelessTerminal.class','dev/industrialtechnology/core/CommonProxy.class',
      'dev/industrialtechnology/core/IndustrialTechnology.class','dev/industrialtechnology/core/ModRegistry.class',
      'dev/industrialtechnology/wireless/HbmMonitoring.class','dev/industrialtechnology/wireless/ItemWirelessTerminal.class',
      'dev/industrialtechnology/wireless/TerminalContainer.class'}
    for n,b in old.items():
        if n.endswith('.class') and n not in allowed:assert now.get(n)==b,'Unrelated class changed: '+n
        if n.startswith('assets/') and n!='assets/industrialtech/lang/en_US.lang':assert now.get(n)==b,'Unrelated asset changed: '+n
    assert {n for n in now if n.startswith('assets/')}=={n for n in old if n.startswith('assets/')},'New texture assets'
    assert now['assets/industrialtech/lang/en_US.lang'].startswith(old['assets/industrialtech/lang/en_US.lang'])
    assert json.loads(now['mcmod.info'])[0]['version']=='2.6.0'
    assert b'func_145845_h' in now['dev/industrialtechnology/console/TileConsole.class'],'Development mappings'
    for n,b in now.items():
        if n.endswith('.class'):
            assert b[:4]==bytes.fromhex('cafebabe') and struct.unpack('>H',b[6:8])[0]==52
            assert not n.startswith(('api/hbm/','com/hbm/','appeng/','codechicken/','net/minecraft/','cpw/'))
            assert '/test/' not in n and '/space/' not in n
        assert not n.startswith('assets/hbm/') and not n.endswith('.jar')
    sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.6.0-sources.jar')
    for path in (ROOT/'src/main/java').rglob('*.java'):
        n=path.relative_to(ROOT/'src/main/java').as_posix();assert sources[n]==path.read_bytes(),'Stale source: '+n
    for n in ['CONSOLE_GUIDE.md','PROJECT_HANDOFF.md','README.md','ASSET_CREDITS.md','docs/CHANGES-2.6.0.md']:
        assert sources[n]==(ROOT/n).read_bytes(),'Stale guide: '+n
    assert digest(ROOT/'.toolchain/consoles-release-2.6.0'/jar.name)==sha
    for directory in record['nativeDirectories']:assert digest(ROOT/directory/'mods'/jar.name)==sha,'Untested production bytes'
    total=0
    for name,count in record['checks'].items():
        if name=='JUnit':
            nodes=[ET.parse(f).getroot() for f in folder.glob('TEST-*.xml')]
            assert all(int(n.get('failures'))==0 and int(n.get('errors'))==0 and int(n.get('skipped'))==0 for n in nodes)
            assert sum(int(n.get('tests')) for n in nodes)==count
        else:
            lines=(folder/name).read_text().splitlines()
            assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines)
            assert sum(s.startswith('PASS:') for s in lines)==count
        total+=count
    assert total==record['totalChecks']
    print('PASS: %s checks; production bytes tested; matching sources; unrelated machines/textures unchanged; no new texture files or bundled optional mods'%total)
    print('SHA256: '+sha)
if __name__=='__main__':main()
