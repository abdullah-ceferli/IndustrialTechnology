"""Release audit for the 2.0.1 space artwork patch; no fabricated past test counts."""
from pathlib import Path
import hashlib,json,re,zipfile,struct,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def contents(p):
    with zipfile.ZipFile(p) as z:
        assert z.testzip() is None,p
        names=[n for n in z.namelist() if not n.endswith('/')];assert len(names)==len(set(names)),p
        return {n:z.read(n) for n in names}
def main():
    version=re.search(r"(?m)^version = '([^']+)'",(ROOT/'build.gradle').read_text()).group(1)
    assert version=='2.0.1'
    out=ROOT/f'docs/test-results/{version}';out.mkdir(parents=True,exist_ok=True)
    jar=ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}.jar';release=contents(jar)
    classes={n:b for n,b in release.items() if n.endswith('.class')}
    assert classes and all(b[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',b[6:8])[0]==52 for b in classes.values())
    assert not any(n.startswith(('net/minecraft/','cpw/','org/','com/hbm/','codechicken/')) or '/test/' in n or '/spacecheck/' in n for n in classes)
    assert not any(n.endswith('.jar') for n in release)
    assert json.loads(release['mcmod.info'])[0]['version']==version
    old=contents(ROOT/'.toolchain/space-baseline-1.6.3/IndustrialTechnology-1.7.10-1.6.3.jar')
    old_art={n:b for n,b in old.items() if '/textures/' in n or n.endswith('automation_reference.json')}
    assert all(release.get(n)==b for n,b in old_art.items()),'Legacy artwork or automation reference changed'
    art=json.loads((ROOT/'docs/art/space-art-manifest.json').read_text());assert len(art)==109
    for n,info in art.items():
        path='assets/industrialtech/textures/'+n;b=release[path]
        assert hashlib.sha256(b).hexdigest()==info['sha256']==digest(ROOT/'src/main/resources'/path),n
        assert b[:8]==b'\x89PNG\r\n\x1a\n' and list(struct.unpack('>II',b[16:24]))==info['size'],n
    baseline=ROOT/'.toolchain/space-visual-baseline-2.0.0'
    before=json.loads((baseline/'manifest.json').read_text())
    allowed={
     'src/main/java/dev/industrialtechnology/core/IndustrialTechnology.java',
     'src/main/java/dev/industrialtechnology/space/BlockSpaceMachine.java',
     'src/main/java/dev/industrialtechnology/space/SpaceRegistry.java',
     'src/main/java/dev/industrialtechnology/space/BlockAirlockDoor.java',
     'src/main/java/dev/industrialtechnology/space/ItemSpaceSuit.java',
     'src/main/java/dev/industrialtechnology/client/space/SpaceModels.java',
     'src/main/java/dev/industrialtechnology/client/space/SpaceMachineRenderer.java',
     'src/main/java/dev/industrialtechnology/client/space/RocketRenderer.java',
     'src/main/java/dev/industrialtechnology/client/space/SpaceSuitModel.java',
     'src/main/java/dev/industrialtechnology/client/space/GuiSpace.java'}
    touched=[]
    for n,sha in before.items():
        assert (ROOT/n).is_file(),('Deleted baseline file',n)
        if digest(ROOT/n)!=sha:
            touched.append(n)
            assert n in allowed or n.startswith('src/main/resources/assets/industrialtech/textures/') and n.removeprefix('src/main/resources/assets/industrialtech/textures/') in art,('Unexpected source change',n)
    added=[p.relative_to(ROOT).as_posix() for p in (ROOT/'src').rglob('*') if p.is_file() and p.relative_to(ROOT).as_posix() not in before]
    assert all(n=='src/main/java/dev/industrialtechnology/space/BlockSpaceStructure.java' or n.startswith('src/main/resources/assets/industrialtech/textures/') and n.removeprefix('src/main/resources/assets/industrialtech/textures/') in art for n in added),added
    # Confirm the preexisting non-space systems are identical source, not just
    # that the author chose not to mention their changes.
    keep=sum(n.endswith('.java') and n not in touched for n in before)
    runtime=json.loads((out/'runtime-copies.json').read_text());assert len(runtime)>=3
    for name in runtime:assert digest(ROOT/name/'mods'/jar.name)==digest(jar),name
    checks=json.loads((out/'checks.json').read_text());total=0
    for name,count in checks.items():
        p=out/name
        if name.endswith('.xml'):
            suite=ET.parse(p).getroot();assert int(suite.attrib['tests'])==count
            assert all(int(suite.attrib.get(k,0))==0 for k in ('errors','failures','skipped'))
        else:
            lines=p.read_text(encoding='utf-8-sig').splitlines();assert lines[-1]=='Failed: 0' and not any(l.startswith('FAIL:') for l in lines),name
            assert sum(l.startswith('PASS:') for l in lines)==count,name
        total+=count
    for n in ('visual-client-hbm5771.txt','flight-client-hbm5771.txt','space-standalone-initial.txt','space-hbm5771-initial.txt','upgrade-from-2.0.0.txt'):assert n in checks,n
    source=contents(ROOT/f'build/libs/IndustrialTechnology-1.7.10-{version}-sources.jar')
    for n in ('SPACE_EXPLORATION_GUIDE.md','SPACE_TECHNICAL_NOTES.md','SPACE_HANDOFF.md','docs/SPACE_VISUAL_REWORK.md'):assert source[n]==(ROOT/n).read_bytes(),n
    # Production code equality against the 2.0.0 baseline outside render changes.
    old2=contents(baseline/'IndustrialTechnology-1.7.10-2.0.0.jar')
    preserved=['space/TileSpaceMachine','space/RocketTravel','space/EntitySpaceRocket','space/SpaceConfig','space/SpaceDimensions','space/ResearchData','space/Atmosphere','space/Airlock','space/HbmSpaceBridge','quarry/TileQuarry','chunkloading/TileChunkLoader']
    for n in preserved:
        path='dev/industrialtechnology/'+n+'.class';assert release[path]==old2[path],('Gameplay bytecode changed',n)
    (out/'changed-files.json').write_text(json.dumps({'changed':sorted(touched),'added':sorted(added)},indent=2)+'\n')
    lines=[f'PASS: {len(classes)} Java 8 classes; no bundled tests or third-party game/mod classes',f'PASS: {len(old_art)} legacy textures/reference resources preserved byte-for-byte',f'PASS: All 109 authored space textures match the PNG inventory and release bytes',f'PASS: {keep} baseline Java source files retained unchanged; all source changes match the visual whitelist',f'PASS: Core travel, machine, pressure, research, airlock, HBM and legacy machine bytecode matches 2.0.0',f'PASS: Production JAR bytes match all {len(runtime)} dedicated-server test copies',f'PASS: {total} successful recorded checks (including repeated compatibility runs)',f'PASS: Current guides, handoff and visual rework documentation in source archive',f'SHA256: {digest(jar)}',f'Size: {jar.stat().st_size} bytes','Failed: 0']
    result='\n'.join(lines)+'\n';(out/'release-audit.txt').write_text(result);(out/'release-sha256.txt').write_text(digest(jar)+'  '+jar.name+'\n');print(result)
if __name__=='__main__':main()
