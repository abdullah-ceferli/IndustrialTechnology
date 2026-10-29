"""Verify the actual 2.4.0 distribution, old subsystem preservation and recorded native evidence."""
from pathlib import Path
import json,hashlib,zipfile,struct,re,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def payload(p):
 with zipfile.ZipFile(p) as z:
  assert z.testzip() is None,p
  names=[n for n in z.namelist() if not n.endswith('/')]
  assert len(names)==len(set(names)),p
  return {n:z.read(n) for n in names}
def main():
 version='2.4.0';release=ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}.jar';content=payload(release);sources=payload(ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}-sources.jar')
 evidence=ROOT/f'docs/test-results/{version}';record=json.loads((evidence/'release.json').read_text());assert record['sha256']==digest(release),'Main JAR differs from executed native distribution'
 classes={n:b for n,b in content.items() if n.endswith('.class')};assert classes
 assert all(b[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',b[6:8])[0]==52 for b in classes.values()),'Java 8 class target'
 assert not any('/test/' in n or '/space/' in n or n.startswith(('net/minecraft/','org/','cpw/','com/hbm/','codechicken/')) for n in classes),'Bundled foreign/test/retired implementation'
 assert not any(n.endswith(('.jar','.jpg','.wav','.exe','.blend')) for n in content),'Development artifacts bundled'
 assert json.loads(content['mcmod.info'])[0]['version']==version
 assert 'func_70071_h_' in classes['dev/industrialtechnology/railway/EntityRollingStock.class'].decode('latin1'),'Distribution is the development jar, not production-mapped'
 old=payload(ROOT/'.toolchain/pre-solar-railway-2.3.1/IndustrialTechnology-1.7.10-2.3.1.jar')
 changed={'dev/industrialtechnology/core/'+n+'.class' for n in ['IndustrialTechnology','ModRegistry','ModConfig','CommonProxy']}
 changed|={'dev/industrialtechnology/client/ClientProxy.class','dev/industrialtechnology/chunkloading/ChunkTickets$Callback.class'}
 # The two nested files only inherit new source line numbers. Verify identical executable disassembly.
 import subprocess
 javap=ROOT/'.toolchain/jdk8/jdk8u504-b01/bin/javap.exe'
 for nested in ['dev/industrialtechnology/client/ClientProxy$1.class','dev/industrialtechnology/chunkloading/ChunkTickets$WorldEvents.class']:
  name=nested[:-6].replace('/','.')
  before=subprocess.check_output([str(javap),'-classpath',str(ROOT/'.toolchain/pre-solar-railway-2.3.1/IndustrialTechnology-1.7.10-2.3.1.jar'),'-c','-p',name])
  after=subprocess.check_output([str(javap),'-classpath',str(release),'-c','-p',name])
  assert before==after,'Nested executable code changed: '+nested
  changed.add(nested)
 stable=0
 for n,b in old.items():
  if n.endswith('.class') and n not in changed:assert content.get(n)==b,'Old subsystem bytecode changed: '+n;stable+=1
  if n.startswith('assets/') and not n.endswith(('lang/en_US.lang','sounds.json')):assert content.get(n)==b,'Old artwork/reference changed: '+n
 for i,name in enumerate(['energetic','pulsating','vibrant','vibrant','vibrant']):
  for part in ['top','side']:
   assert content[f'assets/industrialtech/textures/blocks/solar_{i}_{part}.png']==(ROOT/f'development-assets/solar/enderio-26.3/{name}_{part}.png').read_bytes(),'Upstream texture changed'
 for mesh in ['steam','industrial','acs64','az8a_section','freight','passenger','bogie','wheel','rod','cab','steam_front','passenger_inside']:
  n=f'assets/industrialtech/models/railway/{mesh}.obj';assert len(content[n])>100 and b'\nf ' in content[n],mesh
 for name in ['train_horn','steam_whistle','rail_roll','steam_exhaust','electric_motor','brake_air','coupler']:assert content['assets/industrialtech/sounds/'+name+'.ogg'].startswith(b'OggS'),name
 for n in ['ASSET_CREDITS.md','licenses/EnderIO-Modern-Unlicense.txt','licenses/EnderIO-Apache-2.0.txt','licenses/CoFH-RF-LGPL-3.0.txt','industrialtech-expansion-assets.json']:assert 'META-INF/'+n in content,n
 assets=json.loads(content['META-INF/industrialtech-expansion-assets.json'])
 for path,sha in assets['files'].items():assert hashlib.sha256(content[path]).hexdigest()==sha,path
 for n in ['SOLAR_AND_RAILWAY_GUIDE.md','ASSET_CREDITS.md','PROJECT_HANDOFF.md']:assert n in sources,n
 assert not any(n.startswith(('dev/industrialtechnology/test/','src/test')) for n in sources)
 tests=0
 for x in (evidence/'junit').glob('TEST-*.xml'):
  suite=ET.parse(x).getroot();assert int(suite.attrib.get('failures',0))==int(suite.attrib.get('errors',0))==0;tests+=int(suite.attrib['tests'])
 assert tests>=101,tests
 for run in record['runs']:
  p=ROOT/run['directory'];jar=p/'mods'/release.name;assert digest(jar)==record['sha256'],'Native run used different jar: '+str(p)
  for phase in ['initial','restart']:
   report=evidence/(run['label']+'-'+phase+'.txt');lines=report.read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(line.startswith('FAIL:') for line in lines),report
 client=(evidence/'client.txt').read_text().splitlines();assert client[-1]=='Failed: 0' and not any(x.startswith('FAIL:') for x in client)
 assert any('NEI' in x for x in client) and any('Passenger coach' in x for x in client)
 print('PASS: production Java 8 JAR, matched sources, licensed assets, '+str(stable)+' unchanged existing classes, '+str(tests)+' unit checks and all recorded native/client runs')
 print('SHA256: '+record['sha256'])
if __name__=='__main__':main()
