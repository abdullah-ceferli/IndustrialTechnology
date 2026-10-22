"""Verify the actual v2 release against native runtime copies and retained baseline assets."""
from pathlib import Path
import hashlib,json,re,struct,zipfile,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def contents(path):
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None,path
        files=[n for n in z.namelist() if not n.endswith('/')]
        assert len(files)==len(set(files)),path
        return {n:z.read(n) for n in files}
def main():
    version=re.search(r"(?m)^version = '([^']+)'",(ROOT/'build.gradle').read_text()).group(1)
    jar=ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}.jar';release=contents(jar);folder=ROOT/f'docs/test-results/{version}'
    classes={n:b for n,b in release.items() if n.endswith('.class')}
    assert classes and all(b[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',b[6:8])[0]==52 for b in classes.values())
    assert not any(n.startswith(('net/minecraft/','cpw/','org/','com/hbm/','codechicken/')) or '/test/' in n or '/spacecheck/' in n for n in classes)
    assert not any(n.endswith('.jar') for n in release)
    assert json.loads(release['mcmod.info'].decode())[0]['version']==version
    baseline=contents(ROOT/'.toolchain/space-baseline-1.6.3/IndustrialTechnology-1.7.10-1.6.3.jar')
    old_assets={n:b for n,b in baseline.items() if '/textures/' in n or n.endswith('automation_reference.json')}
    assert all(release.get(n)==b for n,b in old_assets.items()),'Existing texture/reference regression'
    for name in ['LICENSE','THIRD_PARTY.md','licenses/CoFH-RF-LGPL-3.0.txt']:assert 'META-INF/'+name in release
    runtime=json.loads((folder/'runtime-copies.json').read_text());assert len(runtime)>=5
    digest=hashlib.sha256(jar.read_bytes()).hexdigest()
    for run in runtime:
        other=ROOT/run/'mods'/jar.name
        assert contents(other)==release,run
        assert hashlib.sha256(other.read_bytes()).hexdigest()==digest,(run,'jar bytes differ')
    checks=json.loads((folder/'checks.json').read_text());total=units=0
    for name,count in checks.items():
        p=folder/name
        if name.endswith('.xml'):
            suite=ET.parse(p).getroot();assert int(suite.attrib['tests'])==count
            assert all(int(suite.attrib.get(k,0))==0 for k in ['failures','errors','skipped']);units+=count
        else:
            lines=p.read_text(encoding='utf-8-sig').splitlines();assert lines[-1]=='Failed: 0',name
            assert not any(l.startswith('FAIL:') for l in lines),name
            assert sum(l.startswith('PASS:') for l in lines)==count,(name,count)
        total+=count
    assert units>=66
    for name,count in [('legacy-standalone-initial.txt',128),('legacy-standalone-restart.txt',11),('legacy-hbm-initial.txt',165),('legacy-hbm-restart.txt',12),('reactor-client.txt',27)]:assert checks[name]==count,(name,checks.get(name))
    assert checks['space-hbm5771-initial.txt']>=21 and checks['space-sky.txt']>=4
    assert not any('hbm' in p.name.lower() for p in (ROOT/f'run-test-production-{version}-release-standalone/mods').glob('*.jar'))
    for name in ['SPACE_EXPLORATION_GUIDE.md','SPACE_TECHNICAL_NOTES.md']:assert (ROOT/name).is_file()
    source=contents(ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}-sources.jar')
    for name in ['SPACE_EXPLORATION_GUIDE.md','SPACE_TECHNICAL_NOTES.md']:assert source[name]==(ROOT/name).read_bytes()
    text='\n'.join([f'PASS: {len(classes)} Java 8 classes; no bundled test/Minecraft/Forge/HBM/NEI classes or nested mod JARs',f'PASS: All {len(old_assets)} baseline textures and shared automation reference preserved byte-for-byte',f'PASS: Final archive bytes match all {len(runtime)} native dedicated-server runtime copies',f'PASS: {total} recorded successful checks, including repeated compatibility runs; {units} unit tests', 'PASS: Current space guide and technical notes are included in the source archive',f'SHA256: {digest}',f'Size: {jar.stat().st_size} bytes','Failed: 0'])+'\n'
    (folder/'release-audit.txt').write_text(text,encoding='utf-8');(folder/'release-sha256.txt').write_text(f'{digest}  {jar.name}\n',encoding='utf-8');print(text)
if __name__=='__main__':main()
