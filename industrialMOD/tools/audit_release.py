"""Audit final 1.5.0 artifacts against native Forge releases and recorded test evidence."""
import hashlib,json,struct,zipfile
import xml.etree.ElementTree as ET
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
VERSION='1.5.0'
release=ROOT/f'build/libs/IndustrialTechnology-1.7.10-{VERSION}.jar'
reports=ROOT/f'docs/test-results/{VERSION}'
def payload(path):
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None,path
        names=[n for n in z.namelist() if not n.endswith('/')]
        assert len(names)==len(set(names)),path
        return {n:z.read(n) for n in names}
contents=payload(release)
for mode in ['hbm','standalone']:
    tested=ROOT/f'run-test-production-{VERSION}-{mode}-final2/mods'/release.name
    other=payload(tested)
    differences=[n for n in set(contents)|set(other) if contents.get(n)!=other.get(n)]
    assert not differences,(mode,sorted(differences))
classes={n:b for n,b in contents.items() if n.endswith('.class')}
assert classes and all(b[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',b[6:8])[0]==52 for b in classes.values())
assert not any('/test/' in n or n.startswith(('net/minecraft/','cpw/','org/','com/hbm/')) for n in classes)
assert not any(n.endswith('.jar') for n in contents)
assert not any(b'javax/script/' in b for b in classes.values())
assert json.loads(contents['mcmod.info'].decode('utf-8'))[0]['version']==VERSION
for folder,count in [('blocks',24),('gui',5),('items',8)]:assert sum('/textures/'+folder+'/' in n and n.endswith('.png') for n in contents)==count
png=contents['assets/industrialtech/textures/items/automation_guide.png']
assert png[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',png[16:24])==(16,16)
for n in ['LICENSE','THIRD_PARTY.md','licenses/CoFH-RF-LGPL-3.0.txt']:assert 'META-INF/'+n in contents
ref=contents['assets/industrialtech/automation_reference.json']
assert ref==(ROOT/'src/main/resources/assets/industrialtech/automation_reference.json').read_bytes()
reference_hash=hashlib.sha256(ref).hexdigest()
assert 'Reference SHA256: '+reference_hash in (ROOT/'AUTOMATION_CODES.md').read_text(encoding='utf-8')
assert not any('hbm' in p.name.lower() for p in (ROOT/f'run-test-production-{VERSION}-standalone-final2/mods').glob('*.jar'))
total=0
for name,count in [('unit-tests.xml',5),('boundary-tests.xml',5),('mining-rate-tests.xml',3),('wireless-packet-tests.xml',4),('script-parser-tests.xml',9),('editor-tests.xml',6),('script-packet-tests.xml',5),('reference-tests.xml',2)]:
    suite=ET.parse(reports/name).getroot();assert int(suite.attrib['tests'])==count
    assert all(int(suite.attrib.get(k,0))==0 for k in ['failures','errors','skipped'])
    total+=count
for name,count in [('production-hbm-initial.txt',122),('production-hbm-restart.txt',9),('production-initial.txt',105),('production-restart.txt',9),('client-integration.txt',56),('wireless-client-integration.txt',19),('code-client-integration.txt',23)]:
    lines=(reports/name).read_text(encoding='utf-8-sig').splitlines()
    assert lines[-1]=='Failed: 0' and not any(n.startswith('FAIL:') for n in lines),name
    assert sum(n.startswith('PASS:') for n in lines)==count,(name,count)
    total+=count
assert total==382
digest=hashlib.sha256(release.read_bytes()).hexdigest()
(reports/'release-sha256.txt').write_text(f'{digest}  {release.name}\n',encoding='utf-8')
report='\n'.join([
    'PASS: All final archive entries match both native Forge production-tested release JARs byte for byte',
    f'PASS: {len(classes)} Java 8 classes; no test harness or bundled Minecraft/Forge/HBM implementation',
    'PASS: Version 1.5.0; 24 block, 5 GUI, 8 item textures; original 16px leather guide icon',
    'PASS: Shared book/Help reference and complete Markdown source hash agree',
    'PASS: Project and CoFH notices included; no nested mod JARs or external script engine',
    'PASS: 382 successful recorded unit/native-server/restart/real-client checks',
    f'SHA256: {digest}',f'Size: {release.stat().st_size} bytes','Failed: 0'])+'\n'
(reports/'release-audit.txt').write_text(report,encoding='utf-8');print(report)
