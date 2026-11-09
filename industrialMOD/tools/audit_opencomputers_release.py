"""Verify complete original OC content and preserved unrelated Industrial Technology payload."""
from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
def payload(path):
 with zipfile.ZipFile(path) as z:
  assert z.testzip() is None
  return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
 out=ROOT/'docs/test-results/2.9.0';record=json.loads((out/'release.json').read_text())
 jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.9.0.jar';sources=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.9.0-sources.jar'
 assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['sha256']
 assert hashlib.sha256(sources.read_bytes()).hexdigest()==record['sourcesSha256']
 now=payload(jar);original=payload(ROOT/'vendor/opencomputers/OpenComputers-MC1.7.10-1.8.10+667626d-universal.jar')
 metadata={'META-INF/MANIFEST.MF','mcmod.info','pack.mcmeta'}
 for name,data in original.items():
  if name not in metadata:assert now.get(name)==data,'Upstream file changed/missing: '+name
 with zipfile.ZipFile(jar) as z:
  for name in ['assets/opencomputers/loot/openos/','assets/opencomputers/loot/openos/lib/']:assert name in z.namelist()
 old=payload(ROOT/'.toolchain/battery-monitor-release-2.8.0/IndustrialTechnology-1.7.10-2.8.0.jar')
 for name,data in old.items():
  if name.endswith('.class') and name!='dev/industrialtechnology/core/IndustrialTechnology.class':assert now.get(name)==data,'Existing gameplay changed: '+name
  if name.startswith('assets/'):assert now.get(name)==data,'Existing artwork changed: '+name
 for name in now:
  if name.startswith('dev/industrialtechnology/') and name.endswith('.class') and name not in old:assert name.startswith(('dev/industrialtechnology/integration/FactoryComputerDriver','dev/industrialtechnology/energy/core/CombinedCorePlugin')),'Unexpected new class '+name
 assert not any(n.startswith(('com/hbm/','dev/industrialtechnology/test/')) for n in now)
 mods={m['modid']:m for m in json.loads(now['mcmod.info'])};assert mods['industrialtech']['version']=='2.9.0' and mods['OpenComputers']['version']=='1.8.10'
 assert b'FMLCorePlugin: dev.industrialtechnology.energy.core.CombinedCorePlugin' in now['META-INF/MANIFEST.MF']
 assert b'FMLAT: oc_at.cfg' in now['META-INF/MANIFEST.MF']
 assert b'func_110549_a' in now['dev/industrialtechnology/client/RestoredStorageNames.class'],'Not production mappings'
 for folder in ['combined-final-b','pack-final-b']:
  assert now==payload(ROOT/'.toolchain/oc-tests'/folder/'mods'/jar.name),'Native tested payload differs: '+folder
  assert (ROOT/'.toolchain/oc-tests'/folder/'oc-catalog.txt').read_bytes()==(ROOT/'.toolchain/oc-tests'/('reference-a' if folder=='combined-final-b' else 'reference-pack')/'oc-catalog.txt').read_bytes(),'Catalog differs from original OC'
 src=payload(sources)
 for path in (ROOT/'src/main/java').rglob('*.java'):assert src[path.relative_to(ROOT/'src/main/java').as_posix()]==path.read_bytes()
 for path in ['OPENCOMPUTERS_GUIDE.md','PROJECT_HANDOFF.md','README.md','THIRD_PARTY.md','ASSET_CREDITS.md','docs/CHANGES-2.9.0.md']:assert src[path]==(ROOT/path).read_bytes()
 for name,data in payload(ROOT/'vendor/opencomputers/OpenComputers-MC1.7.10-1.8.10+667626d-sources.jar').items():assert src['opencomputers/src/'+name]==data
 for name,data in original.items():
  if name.startswith('assets/') or name in ['application.conf','pack.mcmeta']:assert src['opencomputers/resources/'+name]==data
 for name in record['reports']:
  lines=(out/name).read_text().splitlines();assert lines[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in lines),name
 assert record['junit']==130
 print('PASS: complete original OpenComputers runtime/source/assets; matching stock catalogs; native restart/pack and graphical checks; prior gameplay/assets unchanged')
 print('SHA256: '+record['sha256'])
if __name__=='__main__':main()
