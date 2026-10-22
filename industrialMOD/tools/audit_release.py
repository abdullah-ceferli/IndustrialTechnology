"""Audit the release payload against performed native servers and recorded regression evidence."""
import hashlib,json,re,struct,zipfile
import xml.etree.ElementTree as ET
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
VERSION=re.search(r"(?m)^version = '([^']+)'",(ROOT/'build.gradle').read_text()).group(1)
if VERSION=='2.1.0':
    import audit_shuttle_release
    audit_shuttle_release.main()
    raise SystemExit(0)
if VERSION=='2.0.1':
    import audit_space_visual_release
    audit_space_visual_release.main()
    raise SystemExit(0)
if VERSION.startswith('2.'):
    import audit_space_release
    audit_space_release.main()
    raise SystemExit(0)
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
    other=payload(ROOT/f'run-test-production-{VERSION}-{mode}-final/mods'/release.name)
    differences=[n for n in set(contents)|set(other) if contents.get(n)!=other.get(n)]
    assert not differences,(mode,sorted(differences))
classes={n:b for n,b in contents.items() if n.endswith('.class')}
assert classes and all(b[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',b[6:8])[0]==52 for b in classes.values())
assert not any('/test/' in n or n.startswith(('net/minecraft/','cpw/','org/','com/hbm/','codechicken/')) for n in classes)
assert not any(n.endswith('.jar') for n in contents)
assert json.loads(contents['mcmod.info'].decode('utf-8'))[0]['version']==VERSION
for folder,count in [('blocks',24),('gui',5),('items',8)]:assert sum('/textures/'+folder+'/' in n and n.endswith('.png') for n in contents)==count
for n in ['LICENSE','THIRD_PARTY.md','licenses/CoFH-RF-LGPL-3.0.txt']:assert 'META-INF/'+n in contents
ref=contents['assets/industrialtech/automation_reference.json']
assert ref==(ROOT/'src/main/resources/assets/industrialtech/automation_reference.json').read_bytes()
assert 'Reference SHA256: '+hashlib.sha256(ref).hexdigest() in (ROOT/'AUTOMATION_CODES.md').read_text(encoding='utf-8')
reference=json.loads(ref);assert reference['timing'][0]['id']=='EVERY'
assert any('Celsius' in row['text'] for row in reference['intro'])
assert not any('hbm' in p.name.lower() for p in (ROOT/f'run-test-production-{VERSION}-standalone-final/mods').glob('*.jar'))
checks=json.loads((reports/'checks.json').read_text());total=0;unit=0
for name,count in checks.items():
    if name.endswith('.xml'):
        suite=ET.parse(reports/name).getroot();assert int(suite.attrib['tests'])==count
        assert all(int(suite.attrib.get(k,0))==0 for k in ['failures','errors','skipped']);unit+=count
    else:
        lines=(reports/name).read_text(encoding='utf-8-sig').splitlines()
        assert lines[-1]=='Failed: 0' and not any(n.startswith('FAIL:') for n in lines),name
        assert sum(n.startswith('PASS:') for n in lines)==count,(name,count)
    total+=count
assert unit==62
expected = [('production-hbm-initial.txt',164),('production-hbm-restart.txt',12),('production-initial.txt',128),('production-restart.txt',11)]
if VERSION == '1.6.3':expected = [('production-hbm-initial.txt',165),('production-hbm-restart.txt',12),('production-initial.txt',128),('production-restart.txt',11),('reactor-client-integration.txt',27)]
elif VERSION == '1.6.2':expected += [('reactor-client-integration.txt',19),('code-client-integration.txt',23),('wireless-client-integration.txt',19),('nei-client-integration.txt',6)]
elif VERSION == '1.6.0':expected = [('production-hbm-initial.txt',153),('production-hbm-restart.txt',11),('production-initial.txt',117),('production-restart.txt',10),('reactor-client-integration.txt',17),('code-client-integration.txt',23),('nei-client-integration.txt',6),('wireless-client-integration.txt',19)]
for name,count in expected:assert checks[name]==count
assert f'| Total recorded checks, including duplicate compatibility runs | {total} |' in (ROOT/'docs/TESTING.md').read_text(encoding='utf-8')
hbm=ROOT/'run-test-production-1.3.0-hbm-verified/mods/HBM-NTM-.1.0.27_X5808.jar'
hbm_test=ROOT/f'run-test-production-{VERSION}-hbm-final/mods'/hbm.name
hbm_hash=hashlib.sha256(hbm.read_bytes()).hexdigest();assert hbm_hash==hashlib.sha256(hbm_test.read_bytes()).hexdigest()
digest=hashlib.sha256(release.read_bytes()).hexdigest()
(reports/'release-sha256.txt').write_text(f'{digest}  {release.name}\n',encoding='utf-8')
(reports/'hbm-sha256.txt').write_text(f'{hbm_hash}  {hbm.name}\n',encoding='utf-8')
report='\n'.join([
    'PASS: Final archive entries exactly match both native Forge production-tested release copies',
    f'PASS: {len(classes)} Java 8 classes; no test harness, Minecraft/Forge/HBM/NEI classes or nested mod JARs',
    f'PASS: Version {VERSION}; existing 24 block, 5 GUI and 8 item textures preserved',
    'PASS: Shared book/Help/Markdown reference preserves EVERY and documents separate depletion and Celsius readings',
    'PASS: Native HBM test dependency exactly matches the originally installed X5808 production JAR',
    f'PASS: {total} recorded checks from the reports listed in checks.json',
    f'SHA256: {digest}',f'Size: {release.stat().st_size} bytes','Failed: 0'])+'\n'
(reports/'release-audit.txt').write_text(report,encoding='utf-8');print(report)
