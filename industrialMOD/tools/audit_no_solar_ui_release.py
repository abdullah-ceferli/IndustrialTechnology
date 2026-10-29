from pathlib import Path
import json,zipfile,hashlib
ROOT=Path(__file__).resolve().parents[1]
def payload(p):
 with zipfile.ZipFile(p) as z:
  assert z.testzip() is None
  return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
 jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.4.1.jar';current=payload(jar);old=payload(ROOT/'.toolchain/railway-release-2.4.0/IndustrialTechnology-1.7.10-2.4.0.jar');record=json.loads((ROOT/'docs/test-results/2.4.1/release.json').read_text())
 assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['sha256']
 allowed={'dev/industrialtechnology/'+x+'.class' for x in ['client/ClientProxy','client/GuiExpansion','core/CommonProxy','core/IndustrialTechnology','expansion/ExpansionContainer','solar/BlockSolar']}
 for name,data in old.items():
  if name.endswith('.class') and name not in allowed:assert current.get(name)==data,'Unrelated code changed: '+name
  if name.startswith('assets/'):assert current.get(name)==data,'Artwork/recipes changed: '+name
 assert json.loads(current['mcmod.info'])[0]['version']=='2.4.1'
 assert b'func_70071_h_' in current['dev/industrialtechnology/railway/EntityRollingStock.class'],'Dev jar'
 sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.4.1-sources.jar')
 for name in ['dev/industrialtechnology/solar/BlockSolar.java','dev/industrialtechnology/client/GuiExpansion.java','dev/industrialtechnology/core/CommonProxy.java']:
  assert sources[name]==(ROOT/'src/main/java'/name).read_bytes(),'Stale source jar'
 assert b'openGui' not in sources['dev/industrialtechnology/solar/BlockSolar.java']
 assert b'expansion.solar' not in sources['dev/industrialtechnology/client/GuiExpansion.java']
 native=ROOT/record['nativeDirectory']/'mods'/jar.name;assert hashlib.sha256(native.read_bytes()).hexdigest()==record['sha256']
 for name in ['native-initial.txt','native-restart.txt','client.txt']:
  lines=(ROOT/'docs/test-results/2.4.1'/name).read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines)
 print('PASS: solar UI removed; generation, arrays, machinery, assets and saves preserved; production JAR and sources match completed checks')
 print('SHA256: '+record['sha256'])
if __name__=='__main__':main()
