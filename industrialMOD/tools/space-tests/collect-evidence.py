from pathlib import Path
import shutil,json,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2];version='2.0.0';dest=ROOT/'docs/test-results'/version;dest.mkdir(parents=True,exist_ok=True);counts={}
runs={
 'legacy-standalone':f'run-test-production-{version}-release-standalone',
 'legacy-hbm':f'run-test-production-{version}-release-hbm',
 'space-standalone':f'run-test-space-{version}-release-standalone',
 'space-hbm5771':f'run-test-space-{version}-release-hbm5771',
 'space-hbm5808':f'run-test-space-{version}-release-hbm5808',
 'space-conflict':f'run-test-space-{version}-release-conflict'}
for label,run in runs.items():
    for phase in ['initial','restart']:
        name=f'{label}-{phase}.txt';src=ROOT/run/(f'integration-{phase}.txt' if label.startswith('legacy') else f'space-{phase}.txt');shutil.copy2(src,dest/name)
        lines=(dest/name).read_text(encoding='utf-8-sig').splitlines();assert lines[-1]=='Failed: 0',src;counts[name]=sum(l.startswith('PASS:') for l in lines)
for label,run,name in [
 ('space-client-standalone','run-test-space-client-5','space-client-report.txt'),
 ('space-client-hbm5771','run-test-space-client-2.0.0-release','space-client-report.txt'),
 ('space-sky','run-test-space-sky-hbm5771-verified2','space-sky-report.txt'),
 ('reactor-client','run-test-reactor-client-2.0.0-final','reactor-client-integration.txt')]:
    target=label+'.txt';shutil.copy2(ROOT/run/name,dest/target);lines=(dest/target).read_text(encoding='utf-8-sig').splitlines();assert lines[-1]=='Failed: 0',target;counts[target]=sum(l.startswith('PASS:') for l in lines)
    shots=ROOT/run/'screenshots'
    if shots.is_dir():
        out=dest/'screenshots'/label;out.mkdir(parents=True,exist_ok=True)
        for p in shots.glob('*.png'):shutil.copy2(p,out/p.name)
for p in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
    suite=ET.parse(p).getroot();assert all(int(suite.attrib.get(k,0))==0 for k in ['failures','errors','skipped']);shutil.copy2(p,dest/p.name);counts[p.name]=int(suite.attrib['tests'])
(dest/'checks.json').write_text(json.dumps(counts,indent=2)+'\n',encoding='utf-8');(dest/'runtime-copies.json').write_text(json.dumps(list(runs.values()),indent=2)+'\n',encoding='utf-8')
total=sum(counts.values());rows='\n'.join(f'| {n} | {c} |' for n,c in counts.items());summary=f'''# Verification — Industrial Technology {version}

All listed reports finish with `Failed: 0`. Counts are recorded test executions, including repeated compatibility runs, not distinct features. Earlier failed diagnostic runs remain in their disposable directories and are excluded.

| Recorded report | Passed checks |
|---|---:|
{rows}
| Total recorded checks, including duplicate compatibility runs | {total} |

Native production runs use reobfuscated release copies and real Forge 10.13.4.1614 dedicated servers, followed by actual saves/restarts. Legacy headless regression runs explicitly disable the new idle policy so their original no-player quarry/ticket expectations remain comparable. Separate space tests exercise suspension/resume and a remaining player. Space conflict tests deliberately pre-register a different provider at the configured lunar ID.

The space client traverses Earth → Moon → Earth → Mars → Earth in survival mode after fixture preparation. It uses real SimpleImpl packets, animated vehicle phases, server fuel consumption, custom dimensions, vanilla inventory/XP, gravity, pressurized-room helmet removal and a wall breach. Crafting and research tests use server fixtures; no claim is made that an entire material progression was hand-played from an empty survival inventory. The HBM client also loads NEI. Focused sky screenshots compare near-side Earth and its absence on the far side at low render distance.

HBM native compatibility checks run against original X5771 and X5808 jars. The legacy reactor client exercises the existing ZIRNOX/RBMK interfaces. Actual tests and screenshots are archived under `docs/test-results/{version}/`. The final release auditor verifies Java 8 bytecode, forbidden bundled classes, original texture/reference preservation, exact native runtime jar hashes and source-archive documentation.

Not exhaustively exercised: two independently connected human clients, every third-party claim/energy/fluid mod, full-size maximum-volume bases under sustained server load, NASA scientific-file preprocessing, and hours-long survival balancing. These are limits of performed testing, not claims of failure. The mod does not provide universal HBM pipe or portable gas-item integration; see the space guide's supported tank route.

No existing user save was edited, deleted or used as a fixture. All new test worlds live under fresh project `run-test-*` folders. Earlier 1.6.3 evidence is preserved in `docs/TESTING-1.6.3.md` and its original result directory.
''';(ROOT/'docs/TESTING.md').write_text(summary,encoding='utf-8')
notes=ROOT/'SPACE_TECHNICAL_NOTES.md';s=notes.read_text(encoding='utf-8');s=s.replace('Status: implementation and integration verification in progress, 2026-10-21. The final verification section is updated after the test runs finish. Do not interpret an intermediate successful compile as a complete playtest.','Version 2.0.0, implementation verified 2026-10-21. Performed checks and practical limits are recorded below.')
s=s.replace('Pending final update after all scheduled checks and repairs finish. Current reports are under the fresh `run-test-space-*` directories. Earlier failed runs are retained for diagnosis and are not counted as successful checks. Existing user saves have not been used as test fixtures.',f'{total} successful recorded test executions (including repeated compatibility runs). See `docs/TESTING.md` and `docs/test-results/2.0.0/checks.json` for individual counts. The final release audit compares the actual archive with all native runtime copies. The test client completed both planetary round trips with inventory/XP preserved, observed higher lunar/Martian jumps, and exercised helmet removal and leak detection in a real generated Moon world. Native server cases verify recipes, exact HDD ticks, separate propellant manufacturing, finite suit/room/chamber air, physical door interlocks, lamp light propagation/reload, optional height-reference bounds, ticket suspension/resume, HBM native tank extraction and ID-conflict isolation. The old quarry, automation and reactor regressions are included. All test worlds are disposable project folders; no existing user world was used. Two separate human clients and every third-party integration are not exhaustively tested.')
notes.write_text(s,encoding='utf-8');print(f'Collected {total} successful recorded checks')
