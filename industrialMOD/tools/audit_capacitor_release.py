"""Audit the exact native-tested capacitor distribution and original texture provenance."""
from pathlib import Path
from zipfile import ZipFile
from PIL import Image
from io import BytesIO
import hashlib,json,struct,subprocess,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
REPORTS=ROOT/'docs/test-results/2.3.0'
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def payload(path):
    with ZipFile(path) as z:
        assert z.testzip() is None
        names=[n for n in z.namelist() if not n.endswith('/')];assert len(names)==len(set(names))
        return {n:z.read(n) for n in names}
def main():
    jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.3.0.jar';after=payload(jar)
    assert sha(jar)==sha(ROOT/'.toolchain/capacitor-release-2.3.0'/jar.name)
    before=payload(ROOT/'.toolchain/pre-capacitor-2.2.1/IndustrialTechnology-1.7.10-2.2.1.jar')
    permitted={'dev/industrialtechnology/core/'+s+'.class' for s in ['IndustrialTechnology','ModRegistry','ModConfig','CommonProxy']}
    permitted.add('dev/industrialtechnology/client/ClientProxy.class')
    # Inserting the new proxy method shifts only the original anonymous runnable's debug lines.
    permitted.add('dev/industrialtechnology/client/ClientProxy$1.class')
    javap=ROOT/'.toolchain/jdk8/jdk8u504-b01/bin/javap.exe'
    def runnable(path):return subprocess.check_output([str(javap),'-p','-c','-s','-constants','-classpath',str(path),'dev.industrialtechnology.client.ClientProxy$1'],creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
    assert runnable(jar)==runnable(ROOT/'.toolchain/pre-capacitor-2.2.1/IndustrialTechnology-1.7.10-2.2.1.jar'),'Original snapshot runnable instructions changed' 
    for n,b in before.items():
        if n.endswith('.class') and n not in permitted:assert after.get(n)==b,'Existing machine/API bytecode changed: '+n
        if n.startswith('assets/') and n!='assets/industrialtech/lang/en_US.lang':assert after.get(n)==b,'Existing artwork/reference changed: '+n
    classes={n:b for n,b in after.items() if n.endswith('.class')}
    assert all(b[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',b[6:8])[0]==52 for b in classes.values())
    assert not any('/space/' in n or '/test/' in n or n.startswith(('net/minecraft/','cpw/','org/','com/hbm/','codechicken/','crazypants/')) for n in classes)
    assert not any(n.endswith('.jar') for n in after)
    assert json.loads(after['mcmod.info'].decode())[0]['version']=='2.3.0'
    names={'base':'capacitorBank','border':'capacitorBankVibrantBorder','gauge':'capacitorBankOverlays','fill':'capacitorBankFillBar','input':'capacitorBankInput','output':'capacitorBankOutput','disabled':'capacitorBankLocked'}
    ender=payload(Path(r'D:\programs\Maincraft\Minecraft\game\mods\EnderIO-1.7.10-2.3.0.430_beta.jar'))
    texture_prefix='assets/industrialtech/textures/blocks/capacitor/'
    assert {n for n in after if n.startswith(texture_prefix)}=={texture_prefix+n+'.png' for n in names}
    for dest,original in names.items():
        data=after[texture_prefix+dest+'.png'];assert data==ender['assets/enderio/textures/blocks/'+original+'.png']
        assert Image.open(BytesIO(data)).size==(32,32)
    for name in ['META-INF/licenses/EnderIO-Apache-2.0.txt','META-INF/EnderIO-NOTICE.txt','META-INF/capacitor-texture-attribution.txt','META-INF/THIRD_PARTY.md']:assert name in after
    runtime=json.loads((REPORTS/'runtime-copies.json').read_text())
    assert len(runtime)==4
    for path,expected in runtime.items():assert sha(ROOT/path)==expected==sha(jar),path
    checks=json.loads((REPORTS/'checks.json').read_text());total=unit=0
    for name,count in checks.items():
        if name.endswith('.xml'):
            s=ET.parse(REPORTS/name).getroot();assert int(s.attrib['tests'])==count
            assert all(int(s.attrib.get(k,0))==0 for k in ['failures','errors','skipped']);unit+=count
        else:
            lines=(REPORTS/name).read_text(encoding='utf-8-sig').splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines),name
            assert sum(s.startswith('PASS:') for s in lines)==count
        total+=count
    assert unit==78 and checks['capacitor-client.txt']==12
    assert '1000000 RF' in (REPORTS/'capacitor-pipes-hbm-initial.txt').read_text()
    assert f'| Total recorded successful executions | {total} |' in (ROOT/'docs/TESTING.md').read_text()
    with ZipFile(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.3.0-sources.jar') as z:
        for path in (ROOT/'src/main/java').rglob('*.java'):assert z.read(path.relative_to(ROOT/'src/main/java').as_posix())==path.read_bytes()
        for name in ['PROJECT_HANDOFF.md','README.md','docs/CHANGES-2.3.0.md','docs/CAPACITOR_BANK.md']:assert z.read(name)==(ROOT/name).read_bytes()
    lines=[
      'PASS: Distribution SHA256 matches all four exact native initial/restart runtime copies',
      'PASS: All pre-existing machine, quarry, wireless, reactor, lighting and RF API bytecode is unchanged outside five intentional core/client integration source files; the original snapshot runnable has identical instructions/signatures',
      'PASS: Existing artwork retained; seven capacitor PNGs exactly match the installed Ender IO source assets, with license/notice/credit',
      'PASS: Java 8 class files, valid archive, stable version; no space code, test fixtures, third-party mod classes or nested JARs',
      f'PASS: {total} successful recorded executions including {unit} unit tests, real cables, genuine chunk transitions/restarts, client pixels and actual NEI recipe',
      'PASS: Source archive and current handoff/guide match current source files',
      'SHA256: '+sha(jar),f'Size: {jar.stat().st_size} bytes','Failed: 0']
    (REPORTS/'release-audit.txt').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    (REPORTS/'release-sha256.txt').write_text(sha(jar)+'  '+jar.name+'\n',encoding='utf-8')
    print('\n'.join(lines))
if __name__=='__main__':main()
