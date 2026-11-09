from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
def payload(path):
 with zipfile.ZipFile(path) as z:
  assert z.testzip() is None
  return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
 out=ROOT/'docs/test-results/2.10.0';record=json.loads((out/'release.json').read_text());jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.10.0.jar';sources=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.10.0-sources.jar'
 assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['sha256'];assert hashlib.sha256(sources.read_bytes()).hexdigest()==record['sourcesSha256']
 now=payload(jar);old=payload(ROOT/'.toolchain/opencomputers-release-2.9.0/IndustrialTechnology-1.7.10-2.9.0.jar')
 allowed=('dev/industrialtechnology/core/IndustrialTechnology','dev/industrialtechnology/core/ModRegistry','dev/industrialtechnology/client/ClientProxy')
 for name,data in old.items():
  if name.endswith('.class') and not name.startswith(allowed):assert now.get(name)==data,'Old class changed: '+name
  if name.startswith('assets/') and name not in ['assets/industrialtech/lang/en_US.lang','assets/industrialtech/sounds.json']:assert now.get(name)==data,'Old asset changed: '+name
 assert now['assets/industrialtech/lang/en_US.lang'].decode().splitlines()[:len(old['assets/industrialtech/lang/en_US.lang'].decode().splitlines())]==old['assets/industrialtech/lang/en_US.lang'].decode().splitlines()
 previous=json.loads(old['assets/industrialtech/sounds.json']);current=json.loads(now['assets/industrialtech/sounds.json']);assert all(current[k]==v for k,v in previous.items())
 for name in now:
  if name.endswith('.class') and name not in old:assert name.startswith(('dev/industrialtechnology/accessories/','dev/industrialtechnology/client/AccessoryInput')),'Unexpected bundled class '+name
 assert not any(n.startswith('dev/industrialtechnology/test/') for n in now)
 assert b'func_110549_a' in now['dev/industrialtechnology/client/RestoredStorageNames.class'],'Not production mappings'
 mods={v['modid']:v for v in json.loads(now['mcmod.info'])};assert mods['industrialtech']['version']=='2.10.0' and mods['OpenComputers']['version']=='1.8.10'
 upstream=payload(ROOT/'vendor/opencomputers/OpenComputers-MC1.7.10-1.8.10+667626d-universal.jar')
 for name,data in upstream.items():
  if name not in ['META-INF/MANIFEST.MF','mcmod.info','pack.mcmeta']:assert now[name]==data,'OpenComputers changed '+name
 for folder in ['standalone-final-b','coexist-final-b']:assert now==payload(ROOT/'.toolchain/accessory-tests'/folder/'mods'/jar.name),'Runtime differs from executed native payload '+folder
 mapping=json.loads((ROOT/'development-assets/accessories/original-assets.json').read_text());assert json.loads(now['META-INF/accessories/original-assets.json'])==mapping
 for asset in mapping['assets']:assert hashlib.sha256(now[asset['target']]).hexdigest()==asset['sha256'],asset['target']
 src=payload(sources)
 for path in (ROOT/'src/main/java').rglob('*.java'):assert src[path.relative_to(ROOT/'src/main/java').as_posix()]==path.read_bytes()
 for name in ['ACCESSORIES_GUIDE.md','README.md','PROJECT_HANDOFF.md','ASSET_CREDITS.md','THIRD_PARTY.md','docs/CHANGES-2.10.0.md']:assert src[name]==(ROOT/name).read_bytes()
 for name in record['reports']:
  data=(out/name).read_text().splitlines();assert data[-1]=='Failed: 0' and not any(s.startswith('FAIL:') for s in data),name
 assert record['junit']==130
 print('PASS: standalone accessories, original sprites/audio, coal-only fuel, native/restart/GUI checks, old machines and complete OpenComputers preserved; matching sources')
 print('SHA256: '+record['sha256'])
if __name__=='__main__':main()
