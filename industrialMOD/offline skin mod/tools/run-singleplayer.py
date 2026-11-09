"""Fresh single-player world with the user's current mod pack. No real saves copied."""
from pathlib import Path
from zipfile import ZipFile
import shutil
import subprocess
import sys

project = Path(__file__).resolve().parents[1]
workspace = project.parent
run = project / 'build' / sys.argv[1]
assert run.parent == project / 'build' and not run.exists()
run.mkdir()
game = Path(r'D:\programs\Maincraft\Minecraft\game')
java = Path(r'D:\programs\Maincraft\Minecraft\jre\jre-legacy\windows-x64\jre-legacy\bin\java.exe')
runtime = (workspace / '.toolchain/runtime-classpath.txt').read_text().strip()
cp = (workspace / 'build/youtube-nei-ae2-compare-20261025/client-classpath.txt').read_text(encoding='utf-8-sig').strip()
(run/'mods').mkdir()
for jar in (game/'mods').glob('*.jar'):
    if not jar.name.lower().startswith('offlineskins'):
        shutil.copy2(jar,run/'mods')
for filename in ('OfflineSkins-1.7.10-1.2.0.jar', 'offline-skins-test-harness-1.2.0.jar'):
    shutil.copy2(project / 'build/libs' / filename, run / 'mods')
skins = run / 'config/offlineskins/skins'
skins.mkdir(parents=True)
fixtures = project / 'build/integration-4/client-a/config/offlineskins/skins'
for filename in ('skin.png', 'alternate.png'):
    shutil.copy2(fixtures / filename, skins)
(run / 'options.txt').write_text('renderDistance:2\nmaxFps:30\npauseOnLostFocus:false\nfullscreen:false\nmusic:0\nsound:0\n')
(run / 'optionsof.txt').write_text('ofRenderDistanceChunks:2\nofChunkUpdates:1\n')
natives = run / 'natives'
natives.mkdir()
for entry in runtime.split(';'):
    if 'natives-windows' in entry:
        with ZipFile(entry) as archive:
            for name in archive.namelist():
                if name.lower().endswith('.dll'):
                    (natives / Path(name).name).write_bytes(archive.read(name))
command = [str(java), '-Xms256M', '-Xmx3G', f'-Djava.library.path={natives}', f'-Dskin.test.root={run}',
           '-Dskin.test.role=S', '-Dskin.test.single=true', '-cp', cp, 'net.minecraft.launchwrapper.Launch',
           '--username', 'SkinTestSolo', '--version', '1.7.10', '--gameDir', str(run), '--assetsDir', str(game / 'assets'),
           '--assetIndex', '1.7.10', '--accessToken', '0', '--userProperties', '{}',
           '--tweakClass', 'cpw.mods.fml.common.launcher.FMLTweaker', '--width', '854', '--height', '480']
with (run / 'console.log').open('w', encoding='utf-8') as log:
    process = subprocess.Popen(command, cwd=run, stdout=log, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    try:
        code = process.wait(timeout=240)
    finally:
        if process.poll() is None:
            process.terminate()
            process.wait(timeout=15)
assert code == 0 and (run / 'singleplayer-complete.txt').exists() and not (run / 'singleplayer-FAILED.txt').exists()
print('FULL MOD PACK SINGLE-PLAYER SKIN TEST PASSED', flush=True)
