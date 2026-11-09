from pathlib import Path
import hashlib,json,shutil,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];out=ROOT/'docs/test-results/2.10.0';out.mkdir(parents=True,exist_ok=True)
jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.10.0.jar';sources=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.10.0-sources.jar';total=0
for path in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
 s=ET.parse(path).getroot();assert int(s.attrib.get('failures',0))==0 and int(s.attrib.get('errors',0))==0;total+=int(s.attrib['tests']);shutil.copy2(path,out/path.name)
assert total==130
reports=[];native=0;client=0
for folder in ['standalone-final-b','coexist-final-b']:
 for phase in ['initial','restart']:
  source=ROOT/'.toolchain/accessory-tests'/folder/('accessories-'+phase+'.txt');data=source.read_text();assert data.splitlines()[-1]=='Failed: 0';native+=sum(s.startswith('PASS:') for s in data.splitlines());name=folder+'-'+phase+'.txt';shutil.copy2(source,out/name);reports.append(name)
for folder in ['client-a','client-standalone']:
 source=ROOT/'.toolchain/accessory-tests'/folder/'accessories-client.txt';data=source.read_text();assert data.splitlines()[-1]=='Failed: 0';client+=sum(s.startswith('PASS:') for s in data.splitlines());name=folder+'.txt';shutil.copy2(source,out/name);reports.append(name)
 target=out/'screenshots'/folder;target.mkdir(parents=True,exist_ok=True)
 for path in (source.parent/'screenshots').glob('accessories-*.png'):shutil.copy2(path,target/path.name)
record={'version':'2.10.0','sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'bytes':jar.stat().st_size,'sourcesSha256':hashlib.sha256(sources.read_bytes()).hexdigest(),'junit':total,'native':native,'graphical':client,'totalChecks':total+native+client,'reports':reports,'originalModsRemoved':False,'savedWorldsAccessed':False}
(out/'release.json').write_text(json.dumps(record,indent=2)+'\n')
import audit_accessories_release;audit_accessories_release.main()
seal=ROOT/'.toolchain/accessories-release-2.10.0';seal.mkdir(exist_ok=False)
for path in [jar,sources,out/'release.json']:shutil.copy2(path,seal/path.name)
print(json.dumps(record,indent=2));print('Sealed release: '+str(seal))
