"""Disposable production-JAR test: two offline Forge clients plus a local server."""
from pathlib import Path
from zipfile import ZipFile
import json
import hashlib
import os
import shutil
import socket
import subprocess
import sys
import time
import uuid

project = Path(__file__).resolve().parents[1]
workspace = project.parent
run = project / 'build' / sys.argv[1]
friends_mode = len(sys.argv)>2 and sys.argv[2]=='friends'
assert run.parent == project / 'build' and not run.exists()
run.mkdir()
game = Path(r'D:\programs\Maincraft\Minecraft\game')
java = Path(r'D:\programs\Maincraft\Minecraft\jre\jre-legacy\windows-x64\jre-legacy\bin\java.exe')
runtime = (workspace / '.toolchain/runtime-classpath.txt').read_text().strip()
old_test = workspace / 'build/youtube-nei-ae2-compare-20261025'
server_cp = (old_test / 'server-classpath.txt').read_text(encoding='utf-8-sig').strip()
client_cp = (old_test / 'client-classpath.txt').read_text(encoding='utf-8-sig').strip()
with socket.socket() as sock:
    sock.bind(('127.0.0.1', 0))
    port = sock.getsockname()[1]

def skin_png(color):
    # PNG generation is a deterministic test fixture, not user artwork.
    import struct, zlib
    def chunk(kind, data):
        return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)
    rows = b''.join(b'\0' + bytes(color) * 64 for _ in range(32))
    return b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', 64, 32, 8, 6, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(rows)) + chunk(b'IEND', b'')

for name in ('server', 'client-a', 'client-b'):
    directory = run / name
    (directory / 'mods').mkdir(parents=True)
    for filename in ('OfflineSkins-1.7.10-1.2.0.jar', 'offline-skins-test-harness-1.2.0.jar'):
        if friends_mode and name=='server' and filename.startswith('OfflineSkins-'):
            continue
        shutil.copy2(project / 'build/libs' / filename, directory / 'mods')
    if name != 'server':
        shutil.copy2(game / 'mods/OptiFine_1.7.10_HD_U_E7_MOD.jar', directory / 'mods')
        skins = directory / 'config/offlineskins/skins'
        skins.mkdir(parents=True)
        (skins / 'skin.png').write_bytes(skin_png((204, 51, 51, 255) if name.endswith('a') else (51, 102, 204, 255)))
        (skins / 'alternate.png').write_bytes(skin_png((51, 204, 102, 255)))
        (directory / 'options.txt').write_text('renderDistance:2\nmaxFps:30\npauseOnLostFocus:false\nfullscreen:false\nmusic:0\nsound:0\n')
        (directory / 'optionsof.txt').write_text('ofRenderDistanceChunks:2\nofChunkUpdates:1\n')
        natives = directory / 'natives'
        natives.mkdir()
        for entry in runtime.split(';'):
            if 'natives-windows' in entry:
                with ZipFile(entry) as archive:
                    for member in archive.namelist():
                        if member.lower().endswith('.dll'):
                            (natives / Path(member).name).write_bytes(archive.read(member))

server_dir = run / 'server'
(server_dir / 'eula.txt').write_text('eula=true\n')
# Test-only operator for /kill, used to verify death/respawn synchronization.
test_uuid = str(uuid.UUID(bytes=hashlib.md5(b'OfflinePlayer:SkinTestA').digest(), version=3))
(server_dir / 'ops.json').write_text(json.dumps([{'uuid': test_uuid, 'name': 'SkinTestA', 'level': 4}]))
(server_dir / 'server.properties').write_text(f'online-mode=false\nserver-ip=127.0.0.1\nserver-port={port}\nlevel-name=skin-test\nlevel-type=FLAT\ngenerate-structures=false\nspawn-animals=false\nspawn-monsters=false\nview-distance=3\nmax-players=4\n')
processes, logs = [], []
server = None
relay = None
service_address = ''

def launch(command, directory):
    log = (directory / 'console.log').open('w', encoding='utf-8')
    logs.append(log)
    process = subprocess.Popen(command, cwd=directory, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT,
                               creationflags=subprocess.CREATE_NO_WINDOW)
    processes.append(process)
    return process

try:
    if friends_mode:
        import secrets, re
        relay_dir=run/'friends-service';relay_dir.mkdir()
        (relay_dir/'service.password').write_text(secrets.token_hex(32),encoding='utf-8')
        keytool=workspace/'.toolchain/jdk8/jdk8u504-b01/bin/keytool.exe'
        subprocess.run([str(keytool),'-genkeypair','-alias','offlineskins','-keyalg','RSA','-keysize','2048','-sigalg','SHA256withRSA','-validity','30','-storetype','JKS',
                        '-keystore',str(relay_dir/'service.jks'),'-storepass:file',str(relay_dir/'service.password'),'-keypass:file',str(relay_dir/'service.password'),
                        '-dname','CN=Offline Skins Test','-noprompt'],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,check=True)
        relay=launch([str(java),'-Xms16M','-Xmx64M','-jar',str(project/'build/libs/OfflineSkins-Friends-Service-1.2.0.jar'),
                      '--port','0','--data',str(relay_dir/'data'),'--keystore',str(relay_dir/'service.jks'),'--password-file',str(relay_dir/'service.password')],relay_dir)
        for _ in range(30):
            log=(relay_dir/'console.log').read_text(errors='replace')
            match=re.search(r'This PC: (https://\S+)',log)
            if match:
                service_address=match.group(1)
                break
            assert relay.poll() is None, 'Service did not start'
            time.sleep(1)
        assert service_address, 'Missing secure service address'
        assert not list((server_dir/'mods').glob('OfflineSkins-*.jar')), 'Skin mod must be absent from test Minecraft server'
    server = launch([str(java), '-Xms128M', '-Xmx768M', '-cp', server_cp,
                     'cpw.mods.fml.relauncher.ServerLaunchWrapper', 'nogui'], server_dir)
    deadline = time.monotonic() + 240
    while 'Done (' not in (server_dir / 'console.log').read_text(errors='replace'):
        if server.poll() is not None or time.monotonic() > deadline:
            raise RuntimeError('Server did not start')
        time.sleep(1)
    print(f'Isolated skin-test server ready on localhost:{port}', flush=True)
    clients = []
    for role in ('A', 'B'):
        directory = run / ('client-' + role.lower())
        command = [str(java), '-Xms128M', '-Xmx1G', f'-Djava.library.path={directory / "natives"}',
                   f'-Dskin.test.root={run}', f'-Dskin.test.role={role}', f'-Dskin.test.port={port}',
                   '-cp', client_cp, 'net.minecraft.launchwrapper.Launch',
                   '--username', 'SkinTest' + role, '--version', '1.7.10', '--gameDir', str(directory),
                   '--assetsDir', str(game / 'assets'), '--assetIndex', '1.7.10', '--accessToken', '0',
                   '--userProperties', '{}', '--tweakClass', 'cpw.mods.fml.common.launcher.FMLTweaker',
                   '--width', '640', '--height', '480', '--server', '127.0.0.1', '--port', str(port)]
        if friends_mode:
            command[1:1]=['-Dskin.test.friends=true',f'-Dskin.test.service={service_address}']
        clients.append(launch(command, directory))
        if role == 'A':
            # Forge 1614's shared handshake channel can race simultaneous logins.
            # Serialize test logins, as a normal user joining an existing server would.
            while 'SkinTestA joined the game' not in (server_dir / 'console.log').read_text(errors='replace'):
                if clients[-1].poll() is not None or time.monotonic() > deadline:
                    raise RuntimeError('First test client did not join')
                time.sleep(1)
            time.sleep(1)
    seen = set()
    while any(client.poll() is None for client in clients):
        for marker in sorted(run.glob('*.txt')):
            if marker.name not in seen:
                seen.add(marker.name)
                print(marker.read_text(), flush=True)
        if time.monotonic() > deadline:
            raise RuntimeError('Integration test timed out')
        if 'critical exception handling a packet on channel FML' in (server_dir / 'console.log').read_text(errors='replace'):
            raise RuntimeError('Forge handshake failed')
        time.sleep(1)
    assert (run / 'a-complete.txt').exists() and (run / 'b-complete.txt').exists(), 'Clients did not finish every check'
    assert not list(run.glob('*FAILED*'))
    assert all(client.returncode == 0 for client in clients)
    (run / 'RESULT.json').write_text(json.dumps({'passed': True, 'java': '8u51', 'optifine': 'E7', 'friends_only':friends_mode,'skin_mod_on_server':not friends_mode,'checks': sorted(seen)}, indent=2))
    print('TWO-PLAYER SKIN SHARING PASSED', flush=True)
finally:
    if server is not None and server.poll() is None:
        server.stdin.write(b'stop\n')
        server.stdin.flush()
        server.wait(timeout=45)
    if relay is not None and relay.poll() is None:
        relay.stdin.write(b'stop\n');relay.stdin.flush();relay.wait(timeout=15)
    for process in processes:
        if process.poll() is None:
            process.terminate()
            process.wait(timeout=15)
    for log in logs:
        log.close()
