from pathlib import Path
import zipfile,hashlib,json,struct
ROOT=Path(__file__).resolve().parents[1]
mapping={}
for line in (ROOT/'.gradle-user/caches/minecraft/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/srgs/srg-mcp.srg').read_text().splitlines():
 a=line.split()
 if a and a[0] in ('MD:','FD:'):
  left=a[1].rsplit('/',1)[-1];right=a[3 if a[0]=='MD:' else 2].rsplit('/',1)[-1]
  if left.startswith(('func_','field_')):mapping[left.encode()]=right.encode()
def deobf(data):
 count=struct.unpack('>H',data[8:10])[0];out=bytearray(data[:10]);i=10;slot=1;sizes={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,18:4,17:4,19:2,20:2}
 while slot<count:
  tag=data[i];i+=1;out.append(tag)
  if tag==1:
   size=struct.unpack('>H',data[i:i+2])[0];i+=2;s=data[i:i+size];i+=size;s=mapping.get(s,s);out+=struct.pack('>H',len(s))+s
  else:out+=data[i:i+sizes[tag]];i+=sizes[tag]
  slot+=2 if tag in (5,6) else 1
 return bytes(out)+data[i:]

source=ROOT/'vendor/baubles/Baubles-1.7.10-1.0.1.10.jar'
lock=json.loads((ROOT/'vendor/baubles/upstream.json').read_text());assert hashlib.sha256(source.read_bytes()).hexdigest()==lock['sha256']
target=ROOT/'.toolchain/accessories-upstream/Baubles-dev.jar';target.parent.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(source) as old,zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as new:
 for name in old.namelist():new.writestr(name,deobf(old.read(name)) if name.endswith('.class') else old.read(name))
print('Prepared original Baubles development API.')
