"""Check the 2.2.0 removal payload against executed native tests and the pre-space baseline."""
import hashlib,json,re,struct,zipfile
import xml.etree.ElementTree as ET
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
VERSION=re.search(r"(?m)^version = '([^']+)'",(ROOT/'build.gradle').read_text()).group(1)
if VERSION=='2.7.0':
    import audit_bag_release
    audit_bag_release.main()
    raise SystemExit(0)
if VERSION=='2.6.1':
    import audit_console_screen_release
    audit_console_screen_release.main()
    raise SystemExit(0)
if VERSION=='2.6.0':
    import audit_console_release
    audit_console_release.main()
    raise SystemExit(0)
if VERSION=='2.5.1':
    import audit_steel_upgrade_release
    audit_steel_upgrade_release.main()
    raise SystemExit(0)
if VERSION=='2.5.0':
    import audit_steel_release
    audit_steel_release.main()
    raise SystemExit(0)
if VERSION=='2.4.2':
    import audit_capacitor_stack_release
    audit_capacitor_stack_release.main()
    raise SystemExit(0)
if VERSION=='2.4.1':
    import audit_no_solar_ui_release
    audit_no_solar_ui_release.main()
    raise SystemExit(0)
if VERSION=='2.4.0':
    import audit_solar_rail_release
    audit_solar_rail_release.main()
    raise SystemExit(0)
if VERSION=='2.3.1':
    import audit_capacitor_style_release
    audit_capacitor_style_release.main()
    raise SystemExit(0)
if VERSION=='2.3.0':
    import audit_capacitor_release
    audit_capacitor_release.main()
    raise SystemExit(0)
if VERSION=='2.2.1':
    import audit_lamp_release
    audit_lamp_release.main()
    raise SystemExit(0)
assert VERSION=='2.2.0', 'This audit describes the performed 2.2.0 removal verification.'
release=ROOT/f'build/libs/IndustrialTechnology-1.7.10-{VERSION}.jar'
reports=ROOT/f'docs/test-results/{VERSION}'
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def payload(path):
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None,path
        names=[n for n in z.namelist() if not n.endswith('/')]
        assert len(names)==len(set(names)),path
        return {n:z.read(n) for n in names}
contents=payload(release)
baseline=payload(ROOT/'.toolchain/space-baseline-1.6.3/IndustrialTechnology-1.7.10-1.6.3.jar')
classes={n:b for n,b in contents.items() if n.endswith('.class')}
assert classes and all(b[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',b[6:8])[0]==52 for b in classes.values())
assert not any('/space/' in n or '/test/' in n or n.startswith(('net/minecraft/','cpw/','org/','com/hbm/','codechicken/')) for n in classes)
assert not any(n.endswith('.jar') for n in contents)
modified={'dev/industrialtechnology/core/IndustrialTechnology.class','dev/industrialtechnology/core/ModRegistry.class'}
for n,b in baseline.items():
    if n.endswith('.class') and n not in modified:assert contents.get(n)==b, 'Original subsystem changed: '+n
    if n.startswith('assets/') and not n.endswith('lang/en_US.lang'):assert contents.get(n)==b,'Original artwork/reference changed: '+n
assert not any('SpaceRegistry' in b.decode('latin1') or 'DimensionActivity' in b.decode('latin1') for b in classes.values())
assert json.loads(contents['mcmod.info'].decode())[0]['version']==VERSION
for folder,count in [('blocks',26),('gui',5),('items',8)]:assert sum('/textures/'+folder+'/' in n and n.endswith('.png') for n in contents)==count
for n in ['LICENSE','THIRD_PARTY.md','licenses/CoFH-RF-LGPL-3.0.txt']:assert 'META-INF/'+n in contents
with zipfile.ZipFile(ROOT/'.toolchain/pre-space-removal-2.1.0/source-and-docs.zip') as z:
    for texture in ['green_lamp_on.png','green_lamp_off.png']:
        n='assets/industrialtech/textures/blocks/'+texture
        assert contents[n]==z.read('src/main/resources/'+n),'Retained lamp artwork changed'
ref=contents['assets/industrialtech/automation_reference.json']
assert 'Reference SHA256: '+hashlib.sha256(ref).hexdigest() in (ROOT/'AUTOMATION_CODES.md').read_text(encoding='utf-8')
assert len(json.loads((ROOT/'docs/retired-content-2.2.0.json').read_text()))==66
runtime=json.loads((reports/'runtime-copies.json').read_text())
for path,expected in runtime.items():assert digest(ROOT/path)==expected==digest(release),path
assert len(runtime)==3
assert not any('hbm' in p.name.lower() for p in (ROOT/'run-test-production-2.2.0-final-standalone/mods').glob('*.jar'))
hbm=ROOT/'run-test-production-2.2.0-final-hbm5771/mods/HBM-NTM-.1.0.27_X5771.jar'
assert digest(hbm)==digest(Path(r'D:\programs\Maincraft\Minecraft\game\mods\HBM-NTM-.1.0.27_X5771.jar'))
checks=json.loads((reports/'checks.json').read_text());total=unit=0
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
assert checks['lamp-client.txt']==3 and checks['upgrade-from-2.1.0.txt']==4
assert f'| Total recorded successful executions | {total} |' in (ROOT/'docs/TESTING.md').read_text(encoding='utf-8')
with zipfile.ZipFile(ROOT/f'build/libs/IndustrialTechnology-1.7.10-{VERSION}-sources.jar') as z:
    for path in (ROOT/'src/main/java').rglob('*.java'):
        assert z.read(path.relative_to(ROOT/'src/main/java').as_posix())==path.read_bytes(),path
    assert z.read('PROJECT_HANDOFF.md')==(ROOT/'PROJECT_HANDOFF.md').read_bytes()
    assert z.read('docs/CHANGES-2.2.0.md')==(ROOT/'docs/CHANGES-2.2.0.md').read_bytes()
report='\n'.join([
    'PASS: Final archive exactly matches standalone, HBM X5771 and copied-save upgrade native runtime JARs',
    f'PASS: {len(classes)} Java 8 classes; no space implementation, test harness, third-party mod classes or nested JARs',
    'PASS: Original pre-space subsystem bytecode retained except the two intentional core registration/event classes',
    'PASS: Original textures/reference retained byte-for-byte; both original green lamp textures retained',
    'PASS: Version 2.2.0 and current source/docs; exact 66 retired registry names accepted',
    'PASS: Native HBM dependency matches the installed X5771 JAR; no HBM dependency bundled',
    f'PASS: {total} recorded successful executions; 62 unit checks, native initial/restart, migration and real client checks',
    'SHA256: '+digest(release),f'Size: {release.stat().st_size} bytes','Failed: 0'])+'\n'
(reports/'release-sha256.txt').write_text(digest(release)+'  '+release.name+'\n',encoding='utf-8')
(reports/'release-audit.txt').write_text(report,encoding='utf-8');print(report)
