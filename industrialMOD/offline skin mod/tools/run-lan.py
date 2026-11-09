"""Two production clients using a fresh integrated LAN world and the current mod pack."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import re
import shutil
import socket
import struct
import subprocess
import sys
import time
import zlib

project = Path(__file__).resolve().parents[1]
workspace = project.parent
run = project / 'build' / sys.argv[1]
assert run.parent == project / 'build' and not run.exists()
run.mkdir()
version = re.search(r"^version = '([^']+)'", (project/'build.gradle').read_text(), re.M).group(1)
game = Path(r'D:\programs\Maincraft\Minecraft\game')
java = game.parent / 'jre/jre-legacy/windows-x64/jre-legacy/bin/java.exe'
runtime = (workspace/'.toolchain/runtime-classpath.txt').read_text().strip()
classpath = (workspace/'build/youtube-nei-ae2-compare-20261025/client-classpath.txt').read_text(encoding='utf-8-sig').strip()
with socket.socket() as listener:
    listener.bind(('127.0.0.1',0))
    configured_port=listener.getsockname()[1]

def png(color):
    def chunk(kind, data):
        return struct.pack('>I', len(data))+kind+data+struct.pack('>I', zlib.crc32(kind+data)&0xffffffff)
    rows = b''.join(b'\0'+bytes(color)*64 for _ in range(32))
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',64,32,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(rows))+chunk(b'IEND',b'')

for role in ('a','b'):
    directory = run / ('client-'+role)
    (directory/'mods').mkdir(parents=True)
    for jar in (game/'mods').glob('*.jar'):
        if not jar.name.lower().startswith('offlineskins'):
            shutil.copy2(jar,directory/'mods')
    for filename in (f'OfflineSkins-1.7.10-{version}.jar',f'offline-skins-test-harness-{version}.jar'):
        shutil.copy2(project/'build/libs'/filename,directory/'mods')
    config = directory/'config/offlineskins'
    skins = config/'skins'
    skins.mkdir(parents=True)
    (skins/'skin.png').write_bytes(png((204,51,51,255) if role=='a' else (51,102,204,255)))
    (skins/'alternate.png').write_bytes(png((51,204,102,255) if role=='a' else (238,204,51,255)))
    # Both clients retain friends-only mode, with no separate service running.
    (config/'client.cfg').write_text('skin {\n S:selected=skin.png\n}\nsharing {\n B:friendsOnly=true\n S:service=\n}\n')
    (directory/'config/easylan.cfg').write_text(f'online-mode=false\nHttp-Api=false\nPort={configured_port}\n')
    (directory/'options.txt').write_text('renderDistance:2\nmaxFps:30\npauseOnLostFocus:false\nfullscreen:false\nmusic:0\nsound:0\n')
    (directory/'optionsof.txt').write_text('ofRenderDistanceChunks:2\nofChunkUpdates:1\n')
    natives = directory/'natives'
    natives.mkdir()
    for entry in runtime.split(';'):
        if 'natives-windows' in entry:
            with ZipFile(entry) as archive:
                for name in archive.namelist():
                    if name.lower().endswith('.dll'):
                        (natives/Path(name).name).write_bytes(archive.read(name))

processes, logs = [], []

def launch(role, port=None):
    directory = run/('client-'+role.lower())
    command = [str(java),'-Xms256M','-Xmx2G',f'-Djava.library.path={directory/"natives"}',
               f'-Dskin.test.root={run}','-Dskin.test.lan=true',f'-Dskin.test.role={role}',
               '-cp',classpath,'net.minecraft.launchwrapper.Launch','--username','SkinTest'+role,
               '--version','1.7.10','--gameDir',str(directory),'--assetsDir',str(game/'assets'),
               '--assetIndex','1.7.10','--accessToken','0','--userProperties','{}',
               '--tweakClass','cpw.mods.fml.common.launcher.FMLTweaker','--width','854','--height','480']
    if port is not None:
        command += ['--server','127.0.0.1','--port',str(port)]
    log = (directory/'console.log').open('w',encoding='utf-8')
    logs.append(log)
    process = subprocess.Popen(command,cwd=directory,stdout=log,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    processes.append(process)
    return process

try:
    host = launch('A')
    deadline = time.monotonic()+300
    while not (run/'host-lan-auto.txt').exists():
        if host.poll() is not None or list(run.glob('*FAILED*')) or time.monotonic()>deadline:
            raise RuntimeError('LAN host did not become ready; inspect client-a/console.log')
        time.sleep(1)
    port = int((run/'lan-port.txt').read_text().strip())
    # EasyLAN's custom port may replace the argument to shareToLAN. Read the
    # actual server listener from the integrated server's startup log if needed.
    if port == 0:
        raise RuntimeError('The test LAN host did not report a usable port')
    print('Integrated LAN host ready on localhost:'+str(port),flush=True)
    guest = launch('B',port)
    seen = set()
    while any(p.poll() is None for p in processes):
        for marker in sorted(run.glob('*.txt')):
            if marker.name not in seen:
                seen.add(marker.name)
                print(marker.read_text(),flush=True)
        if list(run.glob('*FAILED*')):
            raise RuntimeError('A LAN assertion failed; inspect failure marker and client logs')
        if time.monotonic()>deadline:
            raise RuntimeError('LAN integration timed out')
        time.sleep(1)
    assert all(p.returncode==0 for p in processes)
    assert all((run/(name+'.txt')).is_file() for name in ('host-complete','guest-complete','host-private-again'))
    jar = project/'build/libs'/f'OfflineSkins-1.7.10-{version}.jar'
    for role in ('a','b'):
        assert jar.read_bytes()==(run/('client-'+role)/'mods'/jar.name).read_bytes()
    (run/'RESULT.json').write_text(json.dumps({'passed':True,'version':version,'java':'8u51',
        'full_mod_pack':True,'integrated_lan':True,'friends_only_on_both_clients':True,
        'separate_service':False,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'checks':sorted(seen)},indent=2))
    print('FULL MOD PACK TWO-PLAYER LAN SKIN TEST PASSED',flush=True)
finally:
    for process in processes:
        if process.poll() is None:
            process.terminate()
            process.wait(timeout=20)
    for log in logs:
        log.close()
