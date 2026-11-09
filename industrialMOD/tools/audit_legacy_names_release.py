"""Verify the display-name patch changes no gameplay code, assets or recipe data."""
from pathlib import Path
import zipfile,json,hashlib
ROOT=Path(__file__).resolve().parents[1]
def payload(p):
    with zipfile.ZipFile(p) as z:
        assert z.testzip() is None
        return {n:z.read(n) for n in z.namelist() if not n.endswith('/')}
def main():
    r=json.loads((ROOT/'docs/test-results/2.7.2/release.json').read_text());jar=ROOT/'build/libs/IndustrialTechnology-1.7.10-2.7.2.jar'
    assert hashlib.sha256(jar.read_bytes()).hexdigest()==r['sha256']
    now=payload(jar);old=payload(ROOT/'.toolchain/legacy-storage-release-2.7.1/IndustrialTechnology-1.7.10-2.7.1.jar')
    allowed={'dev/industrialtechnology/core/IndustrialTechnology.class','dev/industrialtechnology/client/ClientProxy.class','dev/industrialtechnology/client/ClientProxy$1.class'}
    for n,b in old.items():
        if n.endswith('.class') and n not in allowed:assert now.get(n)==b,'Gameplay changed: '+n
        if n.startswith('assets/'):assert now.get(n)==b,'Old asset changed: '+n
    assert {n for n in now if n.startswith('assets/')}=={n for n in old if n.startswith('assets/')}
    for n in now:
        if n.endswith('.class') and n not in old:assert n=='dev/industrialtechnology/client/RestoredStorageNames.class'
    assert json.loads(now['mcmod.info'])[0]['version']=='2.7.2'
    assert b'func_110549_a' in now['dev/industrialtechnology/client/RestoredStorageNames.class'],'Development JAR'
    sources=payload(ROOT/'build/libs/IndustrialTechnology-1.7.10-2.7.2-sources.jar')
    for p in (ROOT/'src/main/java').rglob('*.java'):assert sources[p.relative_to(ROOT/'src/main/java').as_posix()]==p.read_bytes()
    for n in ['README.md','PROJECT_HANDOFF.md','LEGACY_HBM_STORAGE.md','docs/CHANGES-2.7.2.md']:assert sources[n]==(ROOT/n).read_bytes()
    lines=(ROOT/'docs/test-results/2.7.2/nei-names.txt').read_text().splitlines()
    assert lines[-1]=='Failed: 0' and sum(s.startswith('PASS:') for s in lines)==9
    assert all('(LEGACY)' not in s for s in lines)
    print('PASS: actual NEI item names cleaned; gameplay classes, recipes and assets unchanged; final production JAR and matching sources')
    print('SHA256: '+r['sha256'])
if __name__=='__main__':main()
