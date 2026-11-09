from pathlib import Path
import hashlib,json,shutil,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];out=ROOT/'docs/test-results/2.10.1';out.mkdir(parents=True,exist_ok=True)
jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.10.1.jar';sources=jar.with_name(jar.stem+'-sources.jar');total=0
for path in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
 s=ET.parse(path).getroot();assert int(s.attrib.get('failures',0))==0 and int(s.attrib.get('errors',0))==0;total+=int(s.attrib['tests']);shutil.copy2(path,out/path.name)
assert total==130
folders=['equip-2.10.1-standalone-final','equip-2.10.1-projecte-hbm-final'];reports=[];native=0;graphical=0
for folder in folders:
 for phase in ['initial','restart']:
  p=ROOT/'.toolchain/accessory-tests'/folder/('accessories-'+phase+'.txt');lines=p.read_text().splitlines();assert lines[-1]=='Failed: 0';native+=sum(l.startswith('PASS:') for l in lines);name=folder+'-'+phase+'.txt';shutil.copy2(p,out/name);reports.append(name)
folder='client-equip-2.10.1-projecte-clean';p=ROOT/'.toolchain/accessory-tests'/folder/'accessories-client.txt';lines=p.read_text().splitlines();assert lines[-1]=='Failed: 0';graphical=sum(l.startswith('PASS:') for l in lines);name=folder+'.txt';shutil.copy2(p,out/name);reports.append(name)
target=out/'screenshots'/folder;target.mkdir(parents=True,exist_ok=True)
for p in (p.parent/'screenshots').glob('accessories-*.png'):shutil.copy2(p,target/p.name)
shutil.copy2(ROOT/'.toolchain/accessory-tests/equip-old-2.10.0/accessories-initial.txt',out/'old-2.10.0-regression.txt')
log=(ROOT/'.toolchain/accessory-tests/equip-old-2.10.0-initial.log').read_text(errors='replace').splitlines();extract=[]
for i,line in enumerate(log):
 if 'An error occured trying to load a proxy' in line or ('Survival right-click' in line and '/ERROR]' in line):extract+=log[i:i+9]+['']
(out/'old-2.10.0-crash-excerpts.txt').write_text('\n'.join(extract),encoding='utf-8')
record={'version':'2.10.1','sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'bytes':jar.stat().st_size,'sourcesSha256':hashlib.sha256(sources.read_bytes()).hexdigest(),'junit':total,'native':native,'graphical':graphical,'totalChecks':total+native+graphical,'reports':reports,'nativeFolders':folders,'baselineExpectedFailures':2,'savedWorldsAccessed':False}
(out/'release.json').write_text(json.dumps(record,indent=2)+'\n')
import audit_accessory_equip_release;audit_accessory_equip_release.main()
seal=ROOT/'.toolchain/accessories-release-2.10.1';seal.mkdir(exist_ok=False)
for p in [jar,sources,out/'release.json']:shutil.copy2(p,seal/p.name)
print(json.dumps(record,indent=2));print('Sealed release: '+str(seal))
