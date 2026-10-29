# Archive only passing executed 2.6.0 test results and final production identity.
from pathlib import Path
import shutil,hashlib,json,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def main():
    folder=ROOT/'docs/test-results/2.6.0';folder.mkdir(parents=True,exist_ok=True)
    native=['run-test-consoles-final2-2.6.0-hbm','run-test-consoles-final2-2.6.0-standalone','run-test-consoles-final2-2.6.0-steel']
    reports={
      'console-hbm-initial.txt':native[0]+'/console-initial.txt',
      'console-hbm-restart.txt':native[0]+'/console-restart.txt',
      'console-standalone-initial.txt':native[1]+'/console-initial.txt',
      'console-standalone-restart.txt':native[1]+'/console-restart.txt',
      'steel-initial.txt':native[2]+'/steel-initial.txt',
      'steel-restart.txt':native[2]+'/steel-restart.txt',
      'console-client.txt':'run-test-console-client-2.6.0-b/console-client-report.txt'}
    checks={}
    for name,source in reports.items():
        text=(ROOT/source).read_text();lines=text.splitlines()
        assert lines[-1]=='Failed: 0' and not any(x.startswith('FAIL:') for x in lines),source
        checks[name]=sum(x.startswith('PASS:') for x in lines);shutil.copy2(ROOT/source,folder/name)
    nodes=[]
    for path in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
        nodes.append(ET.parse(path).getroot());shutil.copy2(path,folder/path.name)
    assert nodes and all(int(n.get('failures'))==int(n.get('errors'))==int(n.get('skipped'))==0 for n in nodes)
    checks['JUnit']=sum(int(n.get('tests')) for n in nodes)
    jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.6.0.jar'
    sha=hashlib.sha256(jar.read_bytes()).hexdigest()
    for directory in native:assert hashlib.sha256((ROOT/directory/'mods'/jar.name).read_bytes()).hexdigest()==sha
    record=dict(version='2.6.0',sha256=sha,bytes=jar.stat().st_size,nativeDirectories=native,
      clientDirectory='run-test-console-client-2.6.0-b',checks=checks,totalChecks=sum(checks.values()))
    (folder/'release.json').write_text(json.dumps(record,indent=2)+'\n')
    screenshots=folder/'screenshots';screenshots.mkdir(exist_ok=True)
    for path in (ROOT/'run-test-console-client-2.6.0-b/screenshots').glob('*.png'):shutil.copy2(path,screenshots/path.name)
    archive=ROOT/'.toolchain/consoles-release-2.6.0';archive.mkdir(exist_ok=True)
    shutil.copy2(jar,archive/jar.name)
    print(json.dumps(record,indent=2))
    guide="""# 2.6.0 verification — tank history, refinery control and HE/RF telemetry

Production JAR: `IndustrialTechnology-1.7.10-2.6.0.jar` ({size:,} bytes).
SHA256: `{sha}`.

**{total} passing recorded checks**. Exact counts, reports, native run directories and visually inspected screenshots are archived in `docs/test-results/2.6.0/release.json` and adjacent files. JUnit includes the existing arithmetic/security suites and 12 new history/hysteresis cases.

The exact final production bytes run in disposable native Forge 1.7.10 servers with HBM X5771, AE2 rv3-beta-6 and Thermal Expansion/Dynamics/Foundation, and separately without optional mods. Both console worlds undergo genuine JVM restarts. Real Green/Blue/Yellow sticks link through the terminal; a real HBM Big-Ass Tank reports 2M mB, a Yellow stick on a real HBM battery reports unchanged HE, and a 100-bank RF network reports exact 2.5-billion RF totals without clamping, consuming or converting energy.

Native tests cover release/refill limits, holding in the middle band, manual override, unavailable-data OFF, fluid-identity changes, pause/break behavior, duplicate Yellow ownership, existing Blue-program precedence, packet owner/distance/window guards, foreign sensor rejection, bounded samples, zero versus unavailable gaps, NBT and genuine world restart, pickup settings and previous solar/stacking behavior. The old steel server suite runs on the same final JAR with real HBM HE wiring, Thermal servos/itemducts and AE2 InventoryAdaptor, including its upgrade/locked-batch/GUI/crafting/restart checks. This is not a claim of end-to-end AE2 autocrafting validation.

The graphical development client runs HBM and NEI in another disposable world. Its eight recorded checks exercise actual client/server screens and Enable packets, ON at 2M mB, holding at 1.9M, OFF at 1.8M, stored fluid graphs, 50M RF network telemetry, Yellow-on-battery HE in both monitor and terminal, native block displays, and restored OpenGL state. Screenshots were inspected for layout and block appearance. Native tests separately establish production remapping/loading. Recorded graph history is fixture data for visual QA; it is not a multi-hour performance benchmark.

`tools/audit_console_release.py` verifies tested production identity, Java 8/runtime mappings, matching source JAR/guide, excluded optional-mod code, unchanged older machine classes and assets against 2.5.1, and no new raster textures. Only the two new localization entries extend the old asset payload. New displays use native code geometry and existing game atlas entries. No AI textures, world scans, chunk tickets or continuously rendered block screens are added. The global polling limit is established by code inspection; no FPS improvement or large-factory benchmark is claimed.

Only fresh project `run-test-*` worlds were created. User saves and the installed Minecraft/mod files were not modified. Earlier releases' verification remains in their archived folders.
""".format(size=jar.stat().st_size,sha=sha,total=sum(checks.values()))
    (ROOT/'docs/TESTING.md').write_text(guide,encoding='utf-8')
    p=ROOT/'PROJECT_HANDOFF.md';text=p.read_text(encoding='utf-8');needle='## Current expansion: tank and factory consoles (2.6.0)'
    line='Final recorded verification: {total} passing checks; production SHA256 `{sha}`. Evidence: docs/test-results/2.6.0.\n\n'.format(total=sum(checks.values()),sha=sha)
    assert 'Final recorded verification:' not in text
    p.write_text(text.replace(needle,needle+'\n\n'+line,1),encoding='utf-8')
if __name__=='__main__':main()
