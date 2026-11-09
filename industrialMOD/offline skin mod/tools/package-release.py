"""Package a verified release. Optional arguments name successful LAN/friends test directories."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib
import json
import re
import shutil
import sys
import xml.etree.ElementTree as ET

project = Path(__file__).resolve().parents[1]
version = re.search(r"^version = '([^']+)'", (project/'build.gradle').read_text(), re.M).group(1)
jar = project/'build/libs'/f'OfflineSkins-1.7.10-{version}.jar'
service = project/'build/libs'/f'OfflineSkins-Friends-Service-{version}.jar'
lan = project/'build'/(sys.argv[1] if len(sys.argv)>1 else f'lan-{version}-final')
friends = project/'build'/(sys.argv[2] if len(sys.argv)>2 else f'friends-{version}-final')
for name in ('host-complete','guest-complete','host-private-again','host-sees-guest-rejoined','guest-sees-host-respawn'):
    assert (lan/(name+'.txt')).is_file(), 'Missing LAN test: '+name
assert (friends/'a-complete.txt').is_file() and (friends/'b-complete.txt').is_file()
assert not list(lan.glob('*FAILED*')) and not list(friends.glob('*FAILED*'))
assert not list((friends/'server/mods').glob('OfflineSkins-*.jar'))
assert json.loads((lan/'RESULT.json').read_text())['passed']
assert json.loads((friends/'RESULT.json').read_text())['passed']

reports = [ET.parse(path).getroot() for path in (project/'build/test-results/test').glob('TEST-*.xml')]
tests = sum(int(report.get('tests')) for report in reports)
assert tests >= 22 and all(report.get('failures')=='0' and report.get('errors')=='0' for report in reports)

def code_content(path):
    with ZipFile(path) as archive:
        return {name:archive.read(name) for name in archive.namelist() if name.endswith('.class') or name.startswith('assets/')}

for directory in (lan/'client-a',lan/'client-b',friends/'client-a',friends/'client-b'):
    assert code_content(jar)==code_content(directory/'mods'/jar.name), 'Production code differs from tested JAR: '+str(directory)
with ZipFile(jar) as archive:
    assert not any('/test/' in name or '/relay/' in name for name in archive.namelist())
    assert all(not name.startswith(('net/minecraft/','cpw/','org/lwjgl/','com/mojang/')) for name in archive.namelist())
    assert json.loads(archive.read('mcmod.info'))[0]['version']==version
with ZipFile(service) as archive:
    assert not any('/client/' in name or name.startswith(('net/minecraft/','cpw/')) for name in archive.namelist())

sha = hashlib.sha256(jar.read_bytes()).hexdigest()
service_sha = hashlib.sha256(service.read_bytes()).hexdigest()
shutil.copy2(jar,project/jar.name)
shutil.copy2(service,project/'friends-service'/service.name)
report = f'''# Verification - Offline Skins {version}

Verified on 2026-11-02 for Minecraft 1.7.10 / Forge 10.13.4.1614.

- **{tests} automated tests passed**: PNG validation/normalization, safe skin filenames,
  bounded packet lengths, UUID identity, clear operations, the backward-compatible
  four-byte LAN greeting, local/private/IPv6 LAN addresses, rejection of Internet
  and unresolved LAN announcements, and private single-player/friends-mode policy.
- **Two real clients passed in an integrated Open to LAN world with the current
  full mod pack**, including EasyLAN, HBM, OpenComputers and OptiFine. Both clients
  had the saved friends-only preference enabled and no separate skin service.
  The host's skin stayed local in private single-player, then automatically shared
  on opening LAN. Host and guest saw each other's actual skin texture pixels.
  Both sides' live changes, clear/restore, guest reconnect, host death/respawn,
  and returning to a new private world passed. The external friends service stayed
  paused during LAN and the saved sharing preference was retained.
- **Accepted-friends regression passed with two real clients on a Forge server
  with no skin mod**, using the standalone HTTPS service. Before-acceptance
  privacy, live changes, removal/revocation, re-acceptance and reconnect passed.
- Runtime tests used the user's **Java 8u51**. Every world and client configuration
  was created under this project's ignored build directory. No main save was used.
- The LAN skin chooser was captured for visual review. Test clients closed and
  the integrated/dedicated test servers saved and stopped normally.
- Release JAR contains no test harness, Minecraft classes or bundled dependencies.
  The production class/resource bytes match all four tested clients.

Release SHA256: {sha}

Service SHA256: {service_sha}

LAN evidence: {lan.relative_to(project).as_posix()}/.
Friends evidence: {friends.relative_to(project).as_posix()}/.
Unit results: build/test-results/test/.

The test skins are generated solid-color fixtures, not user artwork. Skin files,
accounts, secrets, world saves, test logs and screenshots are excluded from the
source ZIP. Classic Steve / 4-pixel arm limitations remain documented in README.

PNG uploads remain limited/coalesced to one accepted update per player per second.
LAN uses the existing game connection and sends PNGs only on join/change; the LAN
state transition causes one greeting, not a per-tick upload or chunk rebuild.

Android/Zalith was not exercised on a physical phone in this test environment.
Both devices need the same release JAR and compatible Forge/gameplay mods.
Minecraft authentication and EasyLAN settings are not changed by the skin mod.
'''
(project/'VERIFICATION.md').write_text(report,encoding='utf-8')
(project/'SHA256SUMS.txt').write_text(f'{sha}  {jar.name}\n{service_sha}  friends-service/{service.name}\n',encoding='utf-8')

distribution = project/'build/distributions'
distribution.mkdir(exist_ok=True)
source_zip = distribution/f'OfflineSkins-1.7.10-{version}-source.zip'
with ZipFile(source_zip,'w',ZIP_DEFLATED) as archive:
    for name in ('README.md','LAN-SETUP.txt','VERIFICATION.md','LICENSE','.gitignore','build.gradle','settings.gradle',
                 'gradle.properties','gradlew','gradlew.bat','build.ps1','install.ps1','SHA256SUMS.txt'):
        archive.write(project/name,'offline skin mod/'+name)
    for folder in ('src','gradle','tools'):
        for file in sorted((project/folder).rglob('*')):
            if file.is_file() and '__pycache__' not in file.parts:
                archive.write(file,'offline skin mod/'+file.relative_to(project).as_posix())
    for file in (project/'friends-service').iterdir():
        if file.is_file() and file.suffix in ('.md','.ps1','.cmd'):
            archive.write(file,'offline skin mod/friends-service/'+file.name)

lan_zip = distribution/f'OfflineSkins-1.7.10-{version}-LAN.zip'
bundle = distribution/f'OfflineSkins-1.7.10-{version}-with-friends-service.zip'
for destination,include_service in ((lan_zip,False),(bundle,True)):
    with ZipFile(destination,'w',ZIP_DEFLATED) as archive:
        archive.write(jar,jar.name)
        for name in ('README.md','LAN-SETUP.txt','VERIFICATION.md','SHA256SUMS.txt','LICENSE'):
            archive.write(project/name,name)
        if include_service:
            archive.write(service,'friends-service/'+service.name)
            for file in (project/'friends-service').iterdir():
                if file.is_file() and file.suffix in ('.md','.ps1','.cmd'):
                    archive.write(file,'friends-service/'+file.name)
print(json.dumps({'jar':str(jar),'sha256':sha,'lan_zip':str(lan_zip),'bundle':str(bundle),'source_zip':str(source_zip),'tests':tests},indent=2))
