from pathlib import Path
import hashlib,json,shutil,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];out=ROOT/'docs/test-results/2.9.0';out.mkdir(parents=True,exist_ok=True)
jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.9.0.jar';sources=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.9.0-sources.jar'
total=0
for path in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
 suite=ET.parse(path).getroot();assert int(suite.attrib.get('failures',0))==0 and int(suite.attrib.get('errors',0))==0;total+=int(suite.attrib['tests']);shutil.copy2(path,out/path.name)
assert total==130
reports=[];native=0
for folder,phases in [('combined-final-b',['initial','restart']),('pack-final-b',['initial','restart']),('reference-a',['initial']),('reference-pack',['initial'])]:
 for phase in phases:
  source=ROOT/'.toolchain/oc-tests'/folder/('oc-'+phase+'.txt');name=folder+'-'+phase+'.txt';data=source.read_text();assert data.splitlines()[-1]=='Failed: 0';native+=sum(s.startswith('PASS:') for s in data.splitlines());shutil.copy2(source,out/name);reports.append(name)
 shutil.copy2(ROOT/'.toolchain/oc-tests'/folder/'oc-catalog.txt',out/(folder+'-catalog.txt'))
clientDir=ROOT/'.toolchain/oc-tests'/__import__('sys').argv[1]
data=(clientDir/'oc-client.txt').read_text();assert data.splitlines()[-1]=='Failed: 0';client=sum(s.startswith('PASS:') for s in data.splitlines());shutil.copy2(clientDir/'oc-client.txt',out/'client.txt');reports.append('client.txt')
(out/'screenshots').mkdir(exist_ok=True)
for path in (clientDir/'screenshots').glob('oc-original-*.png'):shutil.copy2(path,out/'screenshots'/path.name)
shutil.copy2(clientDir/'openos-boot.txt',out/'openos-boot.txt')
record={'version':'2.9.0','openComputers':'1.8.10+667626d','sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'bytes':jar.stat().st_size,'sourcesSha256':hashlib.sha256(sources.read_bytes()).hexdigest(),'junit':total,'nativeIncludingStockReferences':native,'graphical':client,'totalChecks':total+native+client,'reports':reports,'clientDirectory':str(clientDir.relative_to(ROOT))}
(out/'release.json').write_text(json.dumps(record,indent=2)+'\n')
import audit_opencomputers_release;audit_opencomputers_release.main()
seal=ROOT/'.toolchain/opencomputers-release-2.9.0';seal.mkdir(exist_ok=False)
for path in [jar,sources,out/'release.json']:shutil.copy2(path,seal/path.name)
print('Sealed release: '+str(seal));print(json.dumps(record,indent=2))
