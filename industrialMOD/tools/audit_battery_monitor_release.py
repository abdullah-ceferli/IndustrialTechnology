"""Audit tested production battery payload, original assets and matching sources."""
from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
def payload(path):
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
    record=json.loads((ROOT/'docs/test-results/2.8.0/release.json').read_text());jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.8.0.jar'
    assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['sha256']
    now=payload(jar);old=payload(ROOT/'.toolchain/legacy-names-release-2.7.2/IndustrialTechnology-1.7.10-2.7.2.jar')
    allowed=('dev/industrialtechnology/core/IndustrialTechnology.class','dev/industrialtechnology/client/GuiWirelessTerminal.class','dev/industrialtechnology/wireless/WirelessEndpoint.class','dev/industrialtechnology/wireless/WirelessCommand','dev/industrialtechnology/wireless/TerminalContainer')
    for name,data in old.items():
        if name.endswith('.class') and not name.startswith(allowed):assert now.get(name)==data,'Unrelated class changed: '+name
        if name.startswith('assets/'):assert now.get(name)==data,'Asset changed: '+name
    assert {n for n in old if n.startswith('assets/')}=={n for n in now if n.startswith('assets/')}
    for name in now:
        if name.endswith('.class') and name not in old:assert name.startswith(('dev/industrialtechnology/energy/','dev/industrialtechnology/wireless/BatteryMonitor')),'Unexpected runtime class: '+name
    assert not any(n.startswith(('com/hbm/','dev/industrialtechnology/test/')) for n in now)
    assert json.loads(now['mcmod.info'])[0]['version']=='2.8.0'
    assert b'FMLCorePlugin: dev.industrialtechnology.energy.core.BatteryMeterPlugin' in now['META-INF/MANIFEST.MF']
    assert b'FMLCorePluginContainsFMLMod: true' in now['META-INF/MANIFEST.MF']
    assert b'func_110549_a' in now['dev/industrialtechnology/client/RestoredStorageNames.class'],'Development JAR'
    assert now==payload(ROOT/'run-test-batteries-2.8.0-release-hbm/mods/IndustrialTechnology-1.7.10-2.8.0.jar'),'Final runtime differs from the tested production payload'
    sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.8.0-sources.jar')
    for path in (ROOT/'src/main/java').rglob('*.java'):assert sources[path.relative_to(ROOT/'src/main/java').as_posix()]==path.read_bytes()
    for name in ['README.md','PROJECT_HANDOFF.md','BATTERY_MONITOR_GUIDE.md','docs/CHANGES-2.8.0.md']:assert sources[name]==(ROOT/name).read_bytes()
    for name,count in [('native-hbm.txt',14),('native-standalone.txt',1),('client.txt',4)]:
        lines=(ROOT/'docs/test-results/2.8.0'/name).read_text().splitlines();assert lines[-1]=='Failed: 0' and sum(s.startswith('PASS:') for s in lines)==count
    assert record['junit']==130
    print('PASS: tested production battery monitoring payload; 149 checks; original unrelated gameplay/assets preserved; matching sources')
    print('SHA256: '+record['sha256'])
if __name__=='__main__':main()
