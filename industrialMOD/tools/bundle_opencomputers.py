"""Bundle every upstream runtime entry after ForgeGradle reobf, with strict collision checks."""
from pathlib import Path
import argparse,hashlib,json,zipfile,os
ROOT=Path(__file__).resolve().parents[1]
def contents(path):
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None,'Invalid ZIP: '+str(path)
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--base',required=True);args=parser.parse_args()
    base=Path(args.base).resolve();assert base.parent==(ROOT/'build/libs').resolve() and base.name.startswith('IndustrialTechnology-1.7.10-')
    lock=json.loads((ROOT/'vendor/opencomputers/upstream.json').read_text());pin=next(f for f in lock['files'] if f['name'].endswith('-universal.jar'));upstream=ROOT/'vendor/opencomputers'/pin['name']
    assert hashlib.sha256(upstream.read_bytes()).hexdigest()==pin['sha256'],'Upstream archive changed'
    own=contents(base);oc=contents(upstream)
    assert b'func_110549_a' in own['dev/industrialtechnology/client/RestoredStorageNames.class'],'Bundle only reobfuscated production code'
    merged=dict(own)
    for name,data in oc.items():
        if name in ['META-INF/MANIFEST.MF','mcmod.info','pack.mcmeta']:continue
        if name in merged and merged[name]!=data:raise RuntimeError('Conflicting runtime path: '+name)
        merged[name]=data
    metadata=json.loads(own['mcmod.info']);assert not any(m['modid']=='OpenComputers' for m in metadata)
    metadata[0]['description']+=' Includes complete original OpenComputers 1.8.10 content.'
    merged['mcmod.info']=(json.dumps(metadata+json.loads(oc['mcmod.info']),indent=2)+'\n').encode()
    merged['META-INF/opencomputers/pack.mcmeta']=oc['pack.mcmeta']
    merged['pack.mcmeta']=(json.dumps({'pack':{'pack_format':1,'description':'Industrial Technology and complete OpenComputers 1.8.10'}},indent=2)+'\n').encode()
    manifest=own['META-INF/MANIFEST.MF'].decode().replace('\r','')
    assert 'FMLCorePlugin: dev.industrialtechnology.energy.core.CombinedCorePlugin' in manifest
    assert 'FMLAT: oc_at.cfg' in manifest
    merged['META-INF/industrialtech-opencomputers.json']=(json.dumps(lock,indent=2)+'\n').encode()
    assert all(merged[n]==b for n,b in oc.items() if n not in ['META-INF/MANIFEST.MF','mcmod.info','pack.mcmeta'])
    temp=base.with_suffix('.bundling.tmp')
    try:
        with zipfile.ZipFile(temp,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as z:
            directories=set()
            for name in merged:
                for parent in Path(name).parents:
                    if str(parent)!='.':directories.add(parent.as_posix()+'/')
            for name in sorted(directories):z.writestr(name,b'')
            for name,data in sorted(merged.items()):z.writestr(name,data)
        assert contents(temp)==merged
        os.replace(temp,base)
    finally:
        if temp.exists():temp.unlink()
    print('Bundled complete OpenComputers '+lock['version']+': '+str(len(oc)-2)+' original runtime entries preserved; '+str(base.stat().st_size)+' bytes')
if __name__=='__main__':main()
