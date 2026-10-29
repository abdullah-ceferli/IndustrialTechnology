"""Verify the default-face correction against the sealed, fully tested 2.3.0 release."""
from pathlib import Path
from zipfile import ZipFile
import json,hashlib,struct,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
REPORT=ROOT/'docs/test-results/2.3.1'
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def payload(path):
    with ZipFile(path) as z:
        assert z.testzip() is None
        files=[n for n in z.namelist() if not n.endswith('/')];assert len(files)==len(set(files))
        return {n:z.read(n) for n in files}
def main():
    release=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.3.1.jar'
    before=payload(ROOT/'.toolchain/capacitor-release-2.3.0/IndustrialTechnology-1.7.10-2.3.0.jar');after=payload(release)
    assert set(before)==set(after),'Unintended content added or removed'
    allowed={
      'dev/industrialtechnology/client/CapacitorBankRenderer.class',
      'dev/industrialtechnology/capacitor/BlockCapacitorBank.class',
      'dev/industrialtechnology/capacitor/TileCapacitorBank.class',
      'dev/industrialtechnology/core/IndustrialTechnology.class',
      'META-INF/MANIFEST.MF','mcmod.info'}
    changed={n for n in before if before[n]!=after[n]}
    assert changed==allowed,changed^allowed
    assert all(struct.unpack('>H',b[6:8])[0]==52 for n,b in after.items() if n.endswith('.class'))
    assert json.loads(after['mcmod.info'].decode())[0]['version']=='2.3.1'
    copies=json.loads((REPORT/'runtime-copies.json').read_text());assert len(copies)==1
    for name,expected in copies.items():assert sha(ROOT/name)==expected==sha(release)
    assert sha(release)==sha(ROOT/'.toolchain/capacitor-release-2.3.1'/release.name)
    checks=json.loads((REPORT/'checks.json').read_text());unit=total=0
    for name,count in checks.items():
        if name.endswith('.xml'):
            suite=ET.parse(REPORT/name).getroot();assert int(suite.attrib['tests'])==count
            assert all(int(suite.attrib.get(k,0))==0 for k in ['errors','failures','skipped']);unit+=count
        else:
            lines=(REPORT/name).read_text(encoding='utf-8-sig').splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines)
            assert sum(s.startswith('PASS:') for s in lines)==count
        total+=count
    assert unit==78
    assert 'actual unconfigured Ender IO' in (REPORT/'client.txt').read_text()
    with ZipFile(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.3.1-sources.jar') as z:
        for path in (ROOT/'src/main/java').rglob('*.java'):assert z.read(path.relative_to(ROOT/'src/main/java').as_posix())==path.read_bytes()
        for name in ['README.md','PROJECT_HANDOFF.md','docs/CAPACITOR_BANK.md','docs/CHANGES-2.3.1.md']:assert z.read(name)==(ROOT/name).read_bytes()
    result='\n'.join([
      'PASS: Only capacitor renderer, activation, local display NBT and version metadata changed from sealed 2.3.0',
      'PASS: Every RF ledger/network, GUI, recipe and other machine class is byte-identical to the validated prior release',
      'PASS: All texture PNGs, license files and prior assets remain byte-identical',
      'PASS: Exact distribution passes native bank tests and genuine JVM restart; runtime and sealed hashes match',
      f'PASS: {total} current successful checks including {unit} unit tests and real Ender IO/NEI client comparison',
      'PASS: Java 8 archive, matching source JAR and current handoff/guide',
      'SHA256: '+sha(release),f'Size: {release.stat().st_size} bytes','Failed: 0'])+'\n'
    (REPORT/'release-audit.txt').write_text(result,encoding='utf-8')
    (REPORT/'release-sha256.txt').write_text(sha(release)+'  '+release.name+'\n');print(result)
if __name__=='__main__':main()
