from pathlib import Path
import hashlib,json,shutil,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];out=ROOT/'docs/test-results/2.8.0';out.mkdir(parents=True,exist_ok=True)
jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.8.0.jar';sources=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.8.0-sources.jar'
total=0
for path in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
    suite=ET.parse(path).getroot();assert int(suite.attrib.get('failures',0))==0 and int(suite.attrib.get('errors',0))==0;total+=int(suite.attrib['tests']);shutil.copy2(path,out/path.name)
assert total==130
for folder,target,count,report in [('run-test-batteries-2.8.0-release-hbm','native-hbm.txt',14,'battery-report.txt'),('run-test-batteries-2.8.0-release-standalone','native-standalone.txt',1,'battery-report.txt'),('run-test-batteries-client-2.8.0-d','client.txt',4,'battery-client-report.txt')]:
    data=(ROOT/folder/report).read_text();lines=data.splitlines();assert lines[-1]=='Failed: 0' and sum(s.startswith('PASS:') for s in lines)==count;shutil.copy2(ROOT/folder/report,out/target)
(out/'screenshots').mkdir(exist_ok=True)
for path in (ROOT/'run-test-batteries-client-2.8.0-d/screenshots').glob('battery-*.png'):shutil.copy2(path,out/'screenshots'/path.name)
assert len(list((out/'screenshots').glob('*.png')))==6
record={'version':'2.8.0','sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'bytes':jar.stat().st_size,'sourcesSha256':hashlib.sha256(sources.read_bytes()).hexdigest(),'junit':total,'nativeHbm':14,'nativeStandalone':1,'client':4,'totalChecks':149,'clientDirectory':'run-test-batteries-client-2.8.0-d'}
(out/'release.json').write_text(json.dumps(record,indent=2)+'\n')
import audit_battery_monitor_release
audit_battery_monitor_release.main()
seal=ROOT/'.toolchain/battery-monitor-release-2.8.0';seal.mkdir(exist_ok=False)
for path in [jar,sources,out/'release.json']:shutil.copy2(path,seal/path.name)
print('Sealed release: '+str(seal))
