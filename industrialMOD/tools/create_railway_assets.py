from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def write(path,text):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text.strip()+'\n',encoding='utf-8')
from PIL import Image,ImageDraw
import math,shutil,json,random,struct,wave
ASSET=ROOT/'src/main/resources/assets/industrialtech';DEV=ROOT/'development-assets/railway';DEV.mkdir(parents=True,exist_ok=True)
# Official untouched textures in the mod atlas; enhanced tiers retain the official cell surface.
for i,n in enumerate(['energetic','pulsating','vibrant','vibrant','vibrant']):
 for part in ['top','side']:
  target=ASSET/f'textures/blocks/solar_{i}_{part}.png';target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/f'development-assets/solar/enderio-26.3/{n}_{part}.png',target)
# Authored material atlas: deliberate panel seams, grille slats, glass, paint and steel.
colors=[(45,50,52),(89,101,105),(160,168,169),(205,210,205),(135,30,32),(35,67,101),(16,32,42),(209,186,100),(108,69,40),(61,83,55),(21,23,25),(231,222,178),(83,43,35),(25,78,89),(193,196,187),(129,111,77)]
atlas=Image.new('RGB',(256,256));d=ImageDraw.Draw(atlas)
for k,c in enumerate(colors):
 x=k%4*64;y=k//4*64;d.rectangle((x,y,x+63,y+63),fill=c)
 if k in [0,1,2,3,4,5,9,12,13]:
  dark=tuple(max(0,v-18) for v in c);light=tuple(min(255,v+14) for v in c);d.line((x,y,x+63,y),fill=light);d.line((x,y,x,y+63),fill=light);d.line((x,y+62,x+63,y+62),fill=dark);d.line((x+62,y,x+62,y+63),fill=dark)
  for qx,qy in [(4,4),(59,4),(4,59),(59,59)]:d.rectangle((x+qx,y+qy,x+qx+1,y+qy+1),fill=light)
 if k==6:
  d.polygon([(x+5,y+5),(x+50,y+5),(x+5,y+45)],fill=(37,63,76));d.line((x+5,y+52,x+55,y+9),fill=(61,89,100),width=2)
 if k==10:
  for v in range(5,60,5):d.line((x+3,y+v,x+60,y+v),fill=(63,70,71));d.line((x+3,y+v+1,x+60,y+v+1),fill=(10,13,15))
atlaspath=ASSET/'textures/entity/rail_materials.png';atlaspath.parent.mkdir(parents=True,exist_ok=True);atlas.save(atlaspath);atlas.save(DEV/'rail_materials.png')
class Mesh:
 def __init__(self):self.v=[];self.faces=[]
 def face(self,points,mat):
  ids=[]
  for p in points:self.v.append(p);ids.append(len(self.v))
  self.faces.append((ids,mat))
 def box(self,x,y,z,w,h,l,m):
  x0=x-w/2;x1=x+w/2;y0=y;y1=y+h;z0=z-l/2;z1=z+l/2
  for f in [[(x0,y0,z0),(x0,y1,z0),(x1,y1,z0),(x1,y0,z0)],[(x1,y0,z1),(x1,y1,z1),(x0,y1,z1),(x0,y0,z1)],[(x0,y0,z1),(x0,y1,z1),(x0,y1,z0),(x0,y0,z0)],[(x1,y0,z0),(x1,y1,z0),(x1,y1,z1),(x1,y0,z1)],[(x0,y1,z0),(x0,y1,z1),(x1,y1,z1),(x1,y1,z0)],[(x0,y0,z1),(x0,y0,z0),(x1,y0,z0),(x1,y0,z1)]]:self.face(f,m)
 def cylinder(self,x,y,z,r,length,axis,m,sides=16):
  rings=[]
  for k in [-.5,.5]:
   ring=[]
   for i in range(sides):
    a=i*2*math.pi/sides;u=math.cos(a)*r;v=math.sin(a)*r
    ring.append((x+length*k,y+u,z+v) if axis=='x' else (x+u,y+length*k,z+v) if axis=='y' else (x+u,y+v,z+length*k))
   rings.append(ring)
  self.face(list(reversed(rings[0])),m);self.face(rings[1],m)
  for i in range(sides):self.face([rings[0][i],rings[0][(i+1)%sides],rings[1][(i+1)%sides],rings[1][i]],m)
 def bevelbody(self,width,bottom,height,z0,z1,paint,drop=.38):
  # Sloped front/rear windscreen shoulders and chamfered roof, not a rectangular shell.
  profiles=[]
  for z,w,top in [(z0,width*.73,height-drop),(z0+.8,width,height),(z1-.8,width,height),(z1,width*.73,height-drop)]:profiles.append([( -w/2,bottom,z),(-w/2,top-.35,z),(-w/2+.25,top,z),(w/2-.25,top,z),(w/2,top-.35,z),(w/2,bottom,z)])
  self.face(list(reversed(profiles[0])),paint);self.face(profiles[-1],paint)
  for a,b in zip(profiles,profiles[1:]):
   for i in range(6):self.face([a[i],a[(i+1)%6],b[(i+1)%6],b[i]],paint if i!=2 else 2)
 def save(self,name):
  target=ASSET/f'models/railway/{name}.obj';target.parent.mkdir(parents=True,exist_ok=True);out=['# Original Industrial Technology model; metres; forward +Z; material tile UVs.']
  out+=['v %.5f %.5f %.5f'%p for p in self.v]
  for k in range(16):
   x=k%4/4;y=k//4/4;out+=['vt %.6f %.6f'%(u,1-v) for u,v in [(x+.008,y+.008),(x+.242,y+.008),(x+.242,y+.242),(x+.008,y+.242)]]
  for ids,m in self.faces:out.append('f '+' '.join(f'{n}/{m*4+min(i,3)+1}' for i,n in enumerate(ids)))
  target.write_text('\n'.join(out)+'\n');shutil.copyfile(target,DEV/f'{name}.obj');print(name,len(self.faces),'faces')
def underframe(m,length):
 m.box(0,.72,0,2.6,.3,length,0)
 for x in [-1.24,1.24]:m.box(x,.94,0,.12,.25,length,1)
 for z in [-length/2-.3,length/2+.3]:m.box(0,.65,z,.35,.35,.7,1);m.box(0,.7,z+(.3 if z>0 else -.3),.62,.2,.2,0)
def cabdetails(m,z,width=2.7,paint=3,sloped=False):
 for x in [-.57,.57]:
  if sloped:m.face([(x-.47,2.74,z+.015),(x+.47,2.74,z+.015),(x+.47,3.64,z-.65),(x-.47,3.64,z-.65)],6)
  else:m.box(x,2.6,z,1.02,.75,.035,6)
  m.box(x,2.55,z+.025,1.12,.045,.05,0)
 for x in [-1,1]:m.box(x,1.55,z+.1,.28,.2,.12,11);m.box(x,2.05,z+.08,.3,.08,.12,11)
 for x in [-width/2,width/2]:
  m.box(x,2.5,z-1,.03,.85,1.1,6);m.box(x,1.45,z-2.15,.04,1.85,.7,2);m.box(x,2.2,z-2.15,.055,.55,.5,6)
  for h in [1.2,1.45,1.7]:m.box(x, h,z-2.2,.32,.07,.8,0)
def electric(kind,length,paint):
 m=Mesh();underframe(m,length-1);m.bevelbody(2.9,1.1,3.9,-length/2+.25,length/2-.25,paint,1.15);cabdetails(m,length/2-.2,sloped=True)
 for x in [-1.46,1.46]:
  for z in range(-int(length/2)+3,int(length/2)-3,2):m.box(x,1.65,z,.035,1.3,1.45,10);m.box(x,1.25,z,.05,.15,1.65,4 if kind==2 else 3)
  m.box(x,3.55,0,.03,.15,length-4,4 if kind==2 else 5)
 for z in [-3,2]:m.box(0,3.9,z,1.8,.25,2,0);m.cylinder(0,4.15,z,.58,.08,'y',10)
 # Folded pantograph: both triangles, insulators, contact strip. Station-only power, no fictitious overhead energy.
 for z in [-length/2+4]:
  for x in [-.6,.6]:
   m.cylinder(x,4,z,.13,.25,'y',7);m.face([(x-.04,4.2,z-1),(x+.04,4.2,z-1),(x+.04,4.8,z+.4),(x-.04,4.8,z+.4)],1);m.face([(x-.04,4.8,z+.4),(x+.04,4.8,z+.4),(x+.04,4.4,z+1.1),(x-.04,4.4,z+1.1)],1)
  m.box(0,4.78,z+.4,1.8,.06,.15,0)
 if kind in [1,2]:
  for x in [-1.48,1.48]:
   m.box(x,1.18,0,.045,.38,length-1.7,4 if kind==2 else 5)
   m.box(x,3.48,0,.045,.2,length-3,4 if kind==2 else 5)
  if kind==2:m.box(0,1.3,length/2-.21,1.9,.92,.05,4)
 m.save(['industrial','acs64','az8a_section'][kind])
electric(0,18,9);electric(1,20.32,3);electric(2,15.6,3)
m=Mesh();underframe(m,23);m.cylinder(0,2.3,2.4,1.15,10,'z',0,24);m.cylinder(0,2.3,7.5,1.04,.16,'z',1,24);m.cylinder(0,2.3,7.61,.72,.04,'z',0);m.cylinder(0,3.9,6,.34,1.3,'y',0);m.cylinder(0,4.55,6,.45,.12,'y',1);m.cylinder(0,3.1,1.5,.5,.6,'y',1);m.cylinder(0,3.1,3.4,.32,.65,'y',7)
m.bevelbody(2.9,1.05,4.1,-5.8,-2.5,0);cabdetails(m,-2.5)
for x in [-1.28,1.28]:
 m.cylinder(x,2.45,2,.07,9,'z',1);m.box(x,1.25,1,.18,.12,10.5,1);m.cylinder(x,1,5.7,.32,1.6,'z',1)
 for z in [-1,1,3,5]:m.box(x,2.42,z,.2,.1,.1,7)
m.box(0,1.12,-9.1,2.8,1.65,5,0);m.box(0,2.77,-9.1,2.4,.1,4.6,10)
for z in [7,8]:m.box(0,1.05,z,2.6,.13,.2,0)
m.box(0,2.5,7.7,.3,.3,.15,11);m.save('steam')
front=Mesh();front.v=m.v;front.faces=[(ids,mat) for ids,mat in m.faces if sum(m.v[i-1][2] for i in ids)/len(ids)>-1];front.save('steam_front')
m=Mesh();underframe(m,13.6);m.box(0,1.07,0,2.45,2.6,12.2,13)
for x in [-1.25,1.25]:
 for z in [i*.45-5.85 for i in range(27)]:m.box(x,1.2,z,.07,2.28,.1,13)
 for y in [1.08,3.52]:m.box(x,y,0,.1,.13,12.3,1)
for z in [-6.12,6.12]:
 for x in [-.63,.63]:m.box(x,1.17,z,1.19,2.38,.04,13);m.cylinder(x,2.3,z+(.05 if z>0 else -.05),.035,2.1,'y',2)
 for x in [-1.17,1.17]:
  for y in [1.08,3.45]:m.box(x,y,z,.18,.18,.18,2)
m.save('freight')
m=Mesh();underframe(m,19.6);m.bevelbody(2.8,1.1,3.9,-9.8,9.8,3)
for x in [-1.41,1.41]:
 for z in range(-8,9,2):m.box(x,2.25,z,.03,.95,1.4,6)
 m.box(x,1.55,0,.04,.22,19,5)
for z in range(-8,9,2):
 for x in [-.8,.8]:m.box(x,1.25,z,.6,.6,.7,5)
m.save('passenger')
inside=Mesh();inside.box(0,1.05,0,2.7,.12,19.6,0);inside.box(0,3.9,0,2.8,.12,19.6,3)
for x in [-1.35,1.35]:
 inside.box(x,1.17,0,.08,1,19.6,3)
 for z in range(-9,10,2):inside.box(x,2.17,z,.1,1.73,.12,1)
for z in range(-8,9,2):
 for x in [-.8,.8]:inside.box(x,1.25,z,.6,.55,.7,5);inside.box(x,1.8,z-.32,.6,.7,.1,5)
inside.save('passenger_inside')
m=Mesh();m.box(0,-.15,0,2,.32,3.3,0)
for x in [-.9,.9]:m.box(x,-.35,0,.22,.35,3,1)
for z in [-1.05,1.05]:m.cylinder(0,-.05,z,.1,1.7,'x',1)
m.save('bogie')
m=Mesh();m.cylinder(0,0,0,.49,.16,'x',0,20);m.cylinder(0,0,0,.51,.08,'x',2,20);m.cylinder(.09,0,0,.15,.08,'x',1)
for i in range(10):
 a=i*math.pi/5;dy=math.cos(a)*.27;dz=math.sin(a)*.27;m.box(.13,dy-.025,dz,.035,.05,.15,1)
m.save('wheel')
m=Mesh();m.box(0,-.05,0,.12,.1,4.5,1);m.save('rod')
m=Mesh();m.box(0,1.05,0,2.7,.12,3.3,0);m.box(0,4.25,0,2.8,.12,3.4,1)
for x in [-1.28,1.28]:
 m.box(x,1.16,0,.1,1.1,3.2,0)
 for z in [-1.45,1.45]:m.box(x,2.2,z,.12,2.05,.13,1)
m.box(0,1.9,1.05,2.45,.35,.65,1);m.box(0,2.25,1.1,1.8,.08,.45,0)
for x in [-.5,0,.5]:m.cylinder(x,2.32,1.1,.15,.025,'y',7,12)
m.box(.85,2.25,.85,.08,.38,.08,0);m.box(.85,2.6,.85,.18,.1,.16,4)
m.box(0,1.2,-.7,.8,.55,.8,5);m.box(0,1.7,-1.05,.8,.9,.15,5);m.save('cab')
# Crisp authored inventory icons and station faces; separate from train UV materials.
for name in [*(f'stock_{i}' for i in range(6)),*(f'track_{i}' for i in range(6)),'rail_coupler','rail_bogie','rail_chassis','traction_motor','steam_boiler']:
 im=Image.new('RGBA',(32,32));q=ImageDraw.Draw(im)
 if name.startswith('stock'):
  i=int(name[-1]);q.polygon([(3,12),(8,8),(27,8),(30,12),(30,23),(3,23)],fill=colors[[0,9,3,4,13,3][i]],outline=colors[1]);q.rectangle((5,12,11,16),fill=colors[6]);q.rectangle((3,22,30,24),fill=colors[0]);q.ellipse((6,23,12,29),fill=colors[0],outline=colors[2]);q.ellipse((22,23,28,29),fill=colors[0],outline=colors[2]);q.line((13,11,27,11),fill=colors[2])
 elif name.startswith('track'):
  for y in range(4,30,5):q.rectangle((4,y,28,y+2),fill=colors[8])
  for x in [9,21]:q.rectangle((x,2,x+2,30),fill=colors[2])
 else:q.rectangle((5,8,26,24),fill=colors[1],outline=colors[2]);q.rectangle((8,11,23,21),fill=colors[0]);q.ellipse((12,10,22,23),outline=colors[7],width=2)
 target=ASSET/f'textures/items/{name}.png';target.parent.mkdir(parents=True,exist_ok=True);im.save(target)
for name in ['rail_anchor','rail_station_0','rail_station_1','rail_station_2']:
 im=Image.new('RGB',(32,32),colors[0]);q=ImageDraw.Draw(im);q.rectangle((1,1,30,30),outline=colors[2]);q.rectangle((3,3,28,28),outline=colors[1]);q.rectangle((7,6,24,23),fill=colors[6]);q.line((9,16,14,16,14,10,19,10,19,20,23,20),fill=colors[7],width=2)
 im.save(ASSET/f'textures/blocks/{name}.png')
# Reproducible original sound synthesis, band-limited components and envelopes, converted to Vorbis.
sounddir=DEV/'audio';sounddir.mkdir(exist_ok=True);rng=random.Random(774)
for name in ['train_horn','steam_whistle','rail_roll','steam_exhaust','electric_motor','brake_air','coupler']:
 sr=22050;duration=1.4 if name in ['train_horn','steam_whistle'] else .8;raw=[];noise=0
 for i in range(int(sr*duration)):
  t=i/sr;env=min(1,t/.04)*min(1,(duration-t)/.18);noise=.72*noise+.28*(rng.random()*2-1)
  if name=='train_horn':v=sum(math.sin(2*math.pi*f*t) for f in [220,277.18,329.63,440])*.12
  elif name=='steam_whistle':v=(math.sin(2*math.pi*660*t)+.4*math.sin(2*math.pi*990*t))*.26+noise*.1
  elif name=='electric_motor':v=(math.sin(2*math.pi*120*t)+.3*math.sin(2*math.pi*240*t))*.12
  elif name=='brake_air':v=noise*.8*math.exp(-t*4)
  elif name=='coupler':v=(noise*.7+math.sin(2*math.pi*95*t)*.4)*math.exp(-t*15)
  elif name=='steam_exhaust':v=noise*.55*(.35+.65*max(0,math.sin(t*math.pi*8)))
  else:v=noise*.15+noise*.4*max(0,1-(t%.2)*100)
  raw.append(struct.pack('<h',int(max(-1,min(1,v*env))*26000)))
 with wave.open(str(sounddir/f'{name}.wav'),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(sr);w.writeframes(b''.join(raw))
write('development-assets/railway/README.md','''Original mesh sources, metre units, forward +Z, 1.435 m wheel contact gauge. OBJ source is directly used by the native 1.7.10 renderer. Blender is not installed in this environment; these are authored component meshes, not AI-generated images or borrowed train models. Cylindrical boiler, chamfered cabs, modeled bogies/wheels/pipes/container ribs and separate animation parts. Use any OBJ-capable model editor to edit. Source generator is tools/create_railway_assets.py. Audio WAV masters are original synthesized approximations, not field recordings.''')

