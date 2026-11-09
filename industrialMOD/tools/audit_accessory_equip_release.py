"""Audit the narrowly scoped 2.10.1 wearable fix against the sealed 2.10.0 payload."""
from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
def payload(path):
 with zipfile.ZipFile(path) as z:
  assert z.testzip() is None
  return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
 out=ROOT/'docs/test-results/2.10.1';record=json.loads((out/'release.json').read_text())
 jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.10.1.jar';sources=jar.with_name(jar.stem+'-sources.jar')
 assert sha(jar)==record['sha256'] and sha(sources)==record['sourcesSha256']
 oldpath=ROOT/'.toolchain/accessories-release-2.10.0/IndustrialTechnology-1.7.10-2.10.0.jar'
 assert sha(oldpath)=='58c14ff4b4fc51186a4f0fb55055afdfecebf34ed175fc6acbfb3760708dd0fe'
 old=payload(oldpath);now=payload(jar);assert set(now)==set(old)
 changed={n for n in old if old[n]!=now[n]}
 expected={'dev/industrialtechnology/accessories/ItemWearable.class','dev/industrialtechnology/core/IndustrialTechnology.class','META-INF/MANIFEST.MF','mcmod.info'}
 assert changed==expected,changed
 assert not any(n.startswith('dev/industrialtechnology/test/') for n in now)
 assert b'func_110549_a' in now['dev/industrialtechnology/client/RestoredStorageNames.class'],'Not production mappings'
 mods={v['modid']:v for v in json.loads(now['mcmod.info'])};assert mods['industrialtech']['version']=='2.10.1' and mods['OpenComputers']['version']=='1.8.10'
 for folder in record['nativeFolders']:
  assert payload(ROOT/'.toolchain/accessory-tests'/folder/'mods'/jar.name)==now,'Different tested runtime'
  for phase in ['initial','restart']:
   log=(ROOT/'.toolchain/accessory-tests'/(folder+'-'+phase+'.log')).read_text(errors='replace')
   assert 'An error occured trying to load a proxy' not in log,'Proxy injection still failing'
   assert 'ClassNotFoundException: dev.industrialtechnology.core.IndustrialTechnology$' not in log
 for name in record['reports']:
  lines=(out/name).read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(l.startswith('FAIL:') for l in lines)
 client=(out/'client-equip-2.10.1-projecte-clean.txt').read_text();assert 'INFO: original mods present=true,false' in client
 log=(ROOT/'.toolchain/accessory-tests/client-equip-2.10.1-projecte-clean.log').read_text(errors='replace');assert 'Exception in thread \"Server thread\"' not in log
 assert 'Unreported exception thrown!' not in log and 'Encountered an unexpected exception' not in log
 baseline=(out/'old-2.10.0-regression.txt').read_text().splitlines()
 assert baseline[-1]=='Failed: 2' and sum(l.startswith('FAIL:') and 'NullPointerException' in l for l in baseline)==2
 source=payload(sources)
 for p in (ROOT/'src/main/java').rglob('*.java'):assert source[p.relative_to(ROOT/'src/main/java').as_posix()]==p.read_bytes()
 for name in ['ACCESSORIES_GUIDE.md','README.md','PROJECT_HANDOFF.md','docs/CHANGES-2.10.1.md']:assert source[name]==(ROOT/name).read_bytes()
 assert record['junit']==130
 print('PASS: reproduced old crash; fixed vanilla item-use and real Survival LAN packets; no proxy warning; every other runtime entry including original OpenComputers/art/recipes unchanged; matching sources')
 print('SHA256: '+record['sha256'])
if __name__=='__main__':main()
