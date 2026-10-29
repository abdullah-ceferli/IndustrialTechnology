from pathlib import Path
import zipfile,json,hashlib,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def payload(p):
    with zipfile.ZipFile(p) as z:
        assert z.testzip() is None
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
    version='2.5.0';jar=ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}.jar';record=json.loads((ROOT/f'docs/test-results/{version}/release.json').read_text());digest=hashlib.sha256(jar.read_bytes()).hexdigest();assert digest==record['sha256']
    old=payload(ROOT/'.toolchain/capacitor-stack-release-2.4.2/IndustrialTechnology-1.7.10-2.4.2.jar');now=payload(jar)
    allowed=['core/IndustrialTechnology','core/ModRegistry','core/CommonProxy','client/ClientProxy','client/GuiCapacitorBank','client/CapacitorBankRenderer','capacitor/SideMode','capacitor/BlockCapacitorBank','capacitor/BankManager']
    def permitted(n):
        return any(n=='dev/industrialtechnology/'+s+'.class' or n.startswith('dev/industrialtechnology/'+s+'$') for s in allowed)
    for n,b in old.items():
        if n.endswith('.class') and not permitted(n):assert now.get(n)==b,'Unrelated code changed: '+n
        if n.startswith('assets/'):
            if n=='assets/industrialtech/lang/en_US.lang':assert now[n].startswith(b),'Old translations changed'
            else:assert now.get(n)==b,'Unrelated asset changed: '+n
    assert json.loads(now['mcmod.info'])[0]['version']==version
    assert b'func_145845_h' in now['dev/industrialtechnology/steel/TileSteelFactory.class'],'Development JAR'
    assert not any(n.startswith(('api/hbm/','com/hbm/','appeng/','cofh/thermaldynamics/')) for n in now),'Bundled optional mod classes'
    assert not any(n.startswith('assets/hbm/') for n in now),'Copied HBM assets'
    sources=payload(ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}-sources.jar')
    for path in (ROOT/'src/main/java/dev/industrialtechnology/steel').glob('*.java'):
        name=path.relative_to(ROOT/'src/main/java').as_posix();assert sources[name]==path.read_bytes(),'Stale sources: '+name
    for n in ['STEEL_FACTORY_GUIDE.md','PROJECT_HANDOFF.md','docs/CHANGES-2.5.0.md']:assert sources[n]==(ROOT/n).read_bytes(),'Stale guide: '+n
    for field in ['nativeDirectory','standaloneDirectory','bankDirectory']:
        assert hashlib.sha256((ROOT/record[field]/'mods'/jar.name).read_bytes()).hexdigest()==digest,'Different tested JAR'
    total=0
    for name,count in record['checks'].items():
        if name=='JUnit':
            unit=0
            for f in (ROOT/'docs/test-results/2.5.0').glob('TEST-*.xml'):
                node=ET.parse(f).getroot();assert int(node.get('failures'))==0 and int(node.get('errors'))==0;unit+=int(node.get('tests'))
            assert unit==count
        else:
            lines=(ROOT/'docs/test-results/2.5.0'/name).read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines);assert sum(s.startswith('PASS:') for s in lines)==count
        total+=count
    assert total==record['totalChecks']
    print(f'PASS: {total} recorded checks; tested production bytes, matching sources and guides; unrelated runtime/assets preserved; no bundled HBM dependencies')
    print('SHA256: '+digest)
if __name__=='__main__':main()
