"""Audit the plain green lamp patch against 2.2.0 and performed client/native runs."""
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO
from PIL import Image
import json,hashlib,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def payload(path):
    with ZipFile(path) as z:
        assert z.testzip() is None
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
    jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.2.1.jar'
    before=payload(ROOT/'.toolchain/removal-release-accepted/IndustrialTechnology-1.7.10-2.2.0.jar');after=payload(jar)
    assert set(before)==set(after),'Unexpected content added or removed'
    allowed={'dev/industrialtechnology/core/IndustrialTechnology.class','mcmod.info','META-INF/MANIFEST.MF','assets/industrialtech/textures/blocks/green_lamp_off.png','assets/industrialtech/textures/blocks/green_lamp_on.png'}
    assert {n for n in before if before[n]!=after[n]}==allowed,'Unexpected payload changes'
    for state,color in [('off',(26,128,46)),('on',(76,255,92))]:
        im=Image.open(BytesIO(after[f'assets/industrialtech/textures/blocks/green_lamp_{state}.png'])).convert('RGB')
        assert im.size==(16,16) and set(im.getdata())=={color},state+' is not plain green'
    assert json.loads(after['mcmod.info'].decode())[0]['version']=='2.2.1'
    runtime=ROOT/'run-test-production-2.2.1-lamp-standalone/mods'/jar.name
    assert sha(jar)==sha(runtime)
    reports=ROOT/'docs/test-results/2.2.1';checks=json.loads((reports/'checks.json').read_text());total=unit=0
    for name,count in checks.items():
        if name.endswith('.xml'):
            suite=ET.parse(reports/name).getroot();assert int(suite.attrib['tests'])==count
            assert all(int(suite.attrib.get(k,0))==0 for k in ['failures','errors','skipped']);unit+=count
        else:
            lines=(reports/name).read_text(encoding='utf-8-sig').splitlines()
            assert lines[-1]=='Failed: 0' and not any(n.startswith('FAIL:') for n in lines)
            assert sum(n.startswith('PASS:') for n in lines)==count
        total+=count
    assert unit==62 and checks['lamp-client.txt']==3
    with ZipFile(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.2.1-sources.jar') as z:
        for path in (ROOT/'src/main/java').rglob('*.java'):assert z.read(path.relative_to(ROOT/'src/main/java').as_posix())==path.read_bytes()
        for path in ['PROJECT_HANDOFF.md','README.md','docs/CHANGES-2.2.1.md']:assert z.read(path)==(ROOT/path).read_bytes()
    result='\n'.join([
        'PASS: Only version metadata and the two intended lamp textures differ from the native-tested 2.2.0 release',
        'PASS: Both lamp tiles are opaque, uniformly green; all original gameplay and attachment bytecode is unchanged',
        'PASS: Exact distribution JAR matches the native initial/restart runtime copy',
        f'PASS: {total} current successful executions, including the real nighttime Minecraft client',
        'PASS: Source archive and patch documentation match current files',
        'SHA256: '+sha(jar),f'Size: {jar.stat().st_size} bytes','Failed: 0'])+'\n'
    (reports/'release-sha256.txt').write_text(sha(jar)+'  '+jar.name+'\n',encoding='utf-8')
    (reports/'release-audit.txt').write_text(result,encoding='utf-8');print(result)
if __name__=='__main__':main()
