from pathlib import Path
import zipfile,json,hashlib,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def payload(p):
    with zipfile.ZipFile(p) as z:
        assert z.testzip() is None
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
    folder=ROOT/'docs/test-results/2.5.1';record=json.loads((folder/'release.json').read_text());jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.5.1.jar';digest=hashlib.sha256(jar.read_bytes()).hexdigest();assert digest==record['sha256'];now=payload(jar);old=payload(ROOT/'.toolchain/steel-release-2.5.0/IndustrialTechnology-1.7.10-2.5.0.jar')
    for n,b in old.items():
        allowed=n.startswith('dev/industrialtechnology/steel/') or n in ['dev/industrialtechnology/core/IndustrialTechnology.class','dev/industrialtechnology/client/GuiSteelFactory.class']
        if n.endswith('.class') and not allowed:assert now.get(n)==b,'Unrelated code changed: '+n
        if n.startswith('assets/'):assert now.get(n)==b,'Asset changed: '+n
    assert json.loads(now['mcmod.info'])[0]['version']=='2.5.1'
    assert b'func_145845_h' in now['dev/industrialtechnology/steel/TileSteelFactory.class'],'Development JAR'
    assert not any(n.startswith(('api/hbm/','com/hbm/','appeng/','codechicken/nei/','assets/hbm/')) for n in now),'Bundled optional dependency'
    sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.5.1-sources.jar')
    for path in (ROOT/'src/main/java').rglob('*.java'):
        n=path.relative_to(ROOT/'src/main/java').as_posix();assert sources[n]==path.read_bytes(),'Stale sources: '+n
    for n in ['STEEL_FACTORY_GUIDE.md','PROJECT_HANDOFF.md','docs/CHANGES-2.5.1.md']:assert sources[n]==(ROOT/n).read_bytes(),'Stale guide: '+n
    for field in ['nativeDirectory','standaloneDirectory']:assert hashlib.sha256((ROOT/record[field]/'mods'/jar.name).read_bytes()).hexdigest()==digest,'Different tested JAR'
    total=0
    for name,count in record['checks'].items():
        if name=='JUnit':
            nodes=[ET.parse(f).getroot() for f in folder.glob('TEST-*.xml')];assert all(int(n.get('failures'))==0 and int(n.get('errors'))==0 for n in nodes);assert sum(int(n.get('tests')) for n in nodes)==count
        else:
            lines=(folder/name).read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines);assert sum(s.startswith('PASS:') for s in lines)==count
        total+=count
    assert total==record['totalChecks']
    print(f'PASS: {total} checks; final production bytes and matching sources; unrelated code/assets unchanged; optional dependencies excluded')
    print('SHA256: '+digest)
if __name__=='__main__':main()
