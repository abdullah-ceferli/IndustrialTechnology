from pathlib import Path
import zipfile,json,hashlib
ROOT=Path(__file__).resolve().parents[1]
def payload(p):
 with zipfile.ZipFile(p) as z:
  assert z.testzip() is None
  return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
 jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.4.2.jar';old=payload(ROOT/'.toolchain/solar-no-ui-release-2.4.1/IndustrialTechnology-1.7.10-2.4.1.jar');now=payload(jar);record=json.loads((ROOT/'docs/test-results/2.4.2/release.json').read_text());assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['sha256']
 allowed={'dev/industrialtechnology/'+s+'.class' for s in ['capacitor/ItemBlockCapacitorBank','capacitor/TileCapacitorBank','core/IndustrialTechnology']}
 for n,b in old.items():
  if n.endswith('.class') and n not in allowed:assert now.get(n)==b,'Unrelated code changed: '+n
  if n.startswith('assets/'):assert now.get(n)==b,'Asset changed: '+n
 assert json.loads(now['mcmod.info'])[0]['version']=='2.4.2'
 assert b'func_70071_h_' in now['dev/industrialtechnology/railway/EntityRollingStock.class'],'Development JAR'
 sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.4.2-sources.jar')
 for n in ['dev/industrialtechnology/capacitor/ItemBlockCapacitorBank.java','dev/industrialtechnology/capacitor/TileCapacitorBank.java']:
  assert sources[n]==(ROOT/'src/main/java'/n).read_bytes(),'Stale source'
 copied=ROOT/record['nativeDirectory']/'mods'/jar.name;assert hashlib.sha256(copied.read_bytes()).hexdigest()==record['sha256']
 for phase in ['initial','restart']:
  lines=(ROOT/'docs/test-results/2.4.2'/('native-'+phase+'.txt')).read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines)
 print('PASS: empty stacks, charged RF protection, matching production/source archives; all unrelated runtime classes and assets unchanged')
 print('SHA256: '+record['sha256'])
if __name__=='__main__':main()
