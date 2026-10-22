"""Archive performed v2.0.1 checks; keep earlier diagnostics separate."""
from pathlib import Path
import json,shutil,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[2]
if not (ROOT/'build.gradle').exists():ROOT=Path(r'D:\IndustrialTechnology\industrialMOD')
version='2.0.1';out=ROOT/'docs/test-results'/version;out.mkdir(parents=True,exist_ok=True);counts={}
runs={
 'visual-client-hbm5771':('run-test-space-visual-2.0.1-accepted','space-visual-report.txt'),
 'flight-client-hbm5771':('run-test-space-flight-2.0.1-hbm','space-client-report.txt'),
 'rocket-model-final':('run-test-space-model-2.0.1-final','space-visual-report.txt'),
 'space-standalone-initial':('run-test-space-2.0.1-accepted-standalone','space-initial.txt'),
 'space-standalone-restart':('run-test-space-2.0.1-accepted-standalone','space-restart.txt'),
 'space-hbm5771-initial':('run-test-space-2.0.1-accepted-hbm5771','space-initial.txt'),
 'space-hbm5771-restart':('run-test-space-2.0.1-accepted-hbm5771','space-restart.txt'),
 'upgrade-from-2.0.0':('run-test-space-2.0.1-accepted-upgrade','upgrade-report.txt')}
for label,(run,name) in runs.items():
    src=ROOT/run/name;lines=src.read_text(encoding='utf-8-sig').splitlines();assert lines[-1]=='Failed: 0' and not any(l.startswith('FAIL:') for l in lines),src
    dest=out/(label+'.txt');shutil.copy2(src,dest);counts[dest.name]=sum(l.startswith('PASS:') for l in lines)
    if 'client' in label or label=='rocket-model-final':
        folder=out/'screenshots'/label;folder.mkdir(parents=True,exist_ok=True)
        for p in (ROOT/run/'screenshots').glob('*.png'):
            if 'warmup' not in p.name:shutil.copy2(p,folder/p.name)
for p in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
    suite=ET.parse(p).getroot();assert all(int(suite.attrib.get(k,0))==0 for k in ('errors','failures','skipped'))
    shutil.copy2(p,out/p.name);counts[p.name]=int(suite.attrib['tests'])
(out/'checks.json').write_text(json.dumps(counts,indent=2)+'\n')
runtime=['run-test-space-2.0.1-accepted-'+s for s in ('standalone','hbm5771','upgrade')]
(out/'runtime-copies.json').write_text(json.dumps(runtime,indent=2)+'\n')
shutil.copy2(ROOT/runtime[-1]/'unchanged-source-save-sha256.json',out/'upgrade-source-save-sha256.json')
total=sum(counts.values());unit=sum(c for n,c in counts.items() if n.endswith('.xml'))
previous=ROOT/'docs/TESTING-2.0.0.md'
if not previous.exists():shutil.copy2(ROOT/'docs/TESTING.md',previous)
rows='\n'.join(f'| {n} | {c} |' for n,c in counts.items())
(ROOT/'docs/TESTING.md').write_text(f'''# Verification — Industrial Technology 2.0.1

The visual release compiled successfully with Java 8 / Forge 10.13.4.1614. The {unit} JUnit tests and all native reports below passed. Counts are recorded executions, including repeated checks across compatibility and close-up runs, not distinct features. Intermediate diagnostic runs are excluded and retained in their original temporary project directories.

| Archived report | Passed checks |
|---|---:|
{rows}
| Total recorded successful executions | {total} |

## What was checked

- Actual HBM X5771 + NEI client: all 44 machine-face PNGs and six armor sheets load, metadata/placement matches panel direction, tile/inventory survives facing changes, all 11 server containers open, all space item/block icons render, suit tiers display from front/back and custom meshes render. Captures were visually inspected. The final focused rocket run verifies the corrected outward landing-strut geometry with an engine/feet close-up.
- For each of the 11 GUIs, the real NEI item panel is populated with stable existing quarry/loader and space-machine icons. Framebuffer captures with/without button hover compare the sampled item pixels exactly. GUI color remains white; GL errors are checked. A full resource reload then renders the cached textured rocket again.
- Real Earth → Moon → Earth → Mars → Earth survival round trip after preparing fixtures: server fuel use, actual travel phases, retained inventory/XP, terrain landing, finite canister refueling, gravity, helmet removal in a pressurized base and a wall breach. This flight completed before the final minor armor-fit/landing-strut appearance corrections; unchanged common gameplay bytecode is checked against the baseline, and the final models were separately rechecked.
- Exact final production JAR on standalone and original HBM X5771 dedicated servers, with save/restart: crafting, physical assembly, HDD/telescope powered research, fuel manufacture, real air/filter/suit/canister conservation, terrain, pressure/leaks, physical two-door interlocks, launchpad validation, lamp lighting, recovery and flight identity. HBM tests use a genuinely placed storage tank and exposed fluid port, not a fabricated adapter.
- Upgrade: a copy of the earlier disposable 2.0.0 standalone server save loads under 2.0.1. Existing stored launch assembly, researched HDD, recovered fluids, suit output, planetary save folder and lamp light persist. Every source-save file hash is verified unchanged. No human player's game world is used as a fixture.
- Release audit: all legacy artwork/reference bytes retained; changes from the 2.0.0 source baseline confined to the visual whitelist; core gameplay bytecode unchanged; all 109 PNGs match the recorded hashes and packaged resources; final production JAR bytes match each dedicated-server runtime copy; no tests or third-party Minecraft/HBM/NEI classes bundled.

## Practical limits

The full flight and model/GUI checks run in a development client, while the final packaged JAR is verified in the native production dedicated servers. This is not an exhaustive test of every GPU, shader/OptiFine configuration, arbitrary resource pack, multiplayer combination or long-running survival game. The block/item art is deliberately 16px and the rocket remains a low-poly legacy mesh. Existing Earth sky art was retained for a possible separate pass. Old 2.0.0 gameplay/regression evidence remains in `docs/TESTING-2.0.0.md` and `docs/test-results/2.0.0/`; its older counts are not presented as newly rerun tests.

All current artifacts and screenshots are under `docs/test-results/2.0.1/`. Existing installed Minecraft worlds and mod files were untouched. Install by replacing the older IndustrialTechnology JAR with `IndustrialTechnology-1.7.10-2.0.1.jar`, keeping the same configuration and saves.
''',encoding='utf-8')
print('Archived checks:',total,'Unit tests:',unit,'Reports:',len(counts))
