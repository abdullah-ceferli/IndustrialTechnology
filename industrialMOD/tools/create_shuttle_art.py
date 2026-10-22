from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def write(path,text):
    p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text.strip()+"\n",encoding="utf-8")
from PIL import Image, ImageDraw, ImageFont
import random,json,hashlib
A=ROOT/'src/main/resources/assets/industrialtech/textures'
made=[]
def save(im,path):
    f=A/path;f.parent.mkdir(parents=True,exist_ok=True);im.save(f);made.append(f)
def rock(name,mars,veins):
    host='mars_rock' if mars else 'moon_rock'
    im=Image.open(A/'blocks'/f'{host}.png').convert('RGBA');d=ImageDraw.Draw(im)
    palettes=[('#4d4943','#797568','#a4a18e'),('#292c30','#51545a','#818b94'),('#c5c0b0','#e2dac7','#f0e6ce'),('#6d3530','#b16345','#da9d69'),('#504c51','#807783','#a0a2ab'),('#c9b3a0','#e2d7c7','#eee7d6')]
    colors=palettes[veins];r=random.Random(270+veins)
    for j in range(8):
        x=r.randrange(2,14);y=r.randrange(2,14);d.rectangle((x-1,y,x+1,y+1),fill=colors[0]);d.point((x,y),fill=colors[2]);d.point((x+1,y+1),fill=colors[1])
    save(im,Path('blocks')/f'{name}.png')
names=['lunar_iron_rock','lunar_ilmenite','lunar_feldspar','martian_hematite','martian_titanium_rock','martian_silica']
for k,n in enumerate(names):rock(n,k>=3,k)
for i in range(7):
    im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
    if i==0:
        im.paste('#86897f',(0,0,16,16));d.line((0,0,15,0),fill='#a3a69d');d.line((0,15,15,15),fill='#626860');d.line((15,0,15,15),fill='#626860')
        for x,y in [(3,4),(12,5),(7,11),(10,14),(1,9)]:d.point((x,y),fill='#757c74')
    elif i==1:
        im.paste('#88918b',(0,0,16,16));d.rectangle((0,0,15,15),outline='#495454');d.line((1,1,14,1),fill='#c0c5b9');d.line((1,2,1,14),fill='#a5afa5');d.line((2,12,13,12),fill='#576362');d.rectangle((2,3,13,10),fill='#75817b')
        for x,y in [(2,2),(13,2),(2,13),(13,13)]:d.point((x,y),fill='#d0d2bf')
    elif i==2:
        im.paste('#4e5c5c',(0,0,16,16));d.rectangle((0,0,15,15),outline='#9eaaa3')
        for x in range(2,15,4):d.line((x,1,x,14),fill='#85968f');d.line((1,x,14,x),fill='#687e77')
    elif i==3:
        im.paste('#4c4c46',(0,0,16,16));d.line((0,15,15,0),fill='#6b6659',width=2);d.line((0,7,7,0),fill='#353b37',width=2);d.line((8,15,15,8),fill='#353b37',width=2)
    elif i==4:
        im.paste('#9ba59d',(0,0,16,16));d.rectangle((0,0,15,15),outline='#465451');d.line((2,0,2,15),fill='#d0d3c0',width=2);d.line((12,0,12,15),fill='#56635f',width=2)
    elif i==5:
        im.paste('#b7a86c',(0,0,16,16));d.line((1,0,1,15),fill='#ddd09b',width=3);d.line((13,0,13,15),fill='#665f3d',width=2)
    else:
        d.rectangle((2,0,4,15),fill='#6e817c');d.rectangle((11,0,13,15),fill='#6e817c')
        for y in [2,6,10,14]:d.rectangle((3,y,12,y+1),fill='#c0c9b7');d.line((4,y+1,11,y+1),fill='#64776f')
    save(im,Path('blocks')/f'facility_part_{i}.png')
for n,c,hi in [('titanium_concentrate','#6d737c','#acb5ba'),('aluminium_concentrate','#9d9c8d','#dddbbf'),('space_aluminium','#9eaeb0','#e1eddf')]:
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
    if n.endswith('concentrate'):
        d.polygon([(2,11),(4,6),(7,4),(10,5),(14,10),(12,14),(4,14)],fill='#343d3e');d.polygon([(3,10),(5,7),(8,5),(12,9),(12,12),(5,13)],fill=c)
        for x,y in [(6,8),(9,6),(11,10),(5,11),(8,12)]:d.point((x,y),fill=hi)
    else:d.polygon([(1,9),(5,5),(13,5),(15,9),(12,12),(3,12)],fill='#46585c');d.polygon([(2,8),(5,5),(13,5),(12,9),(3,9)],fill=hi);d.rectangle((3,10,11,11),fill=c)
    save(im,Path('items')/f'{n}.png')
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.rectangle((1,9,14,14),fill='#4e5b58');d.polygon([(1,9),(4,6),(14,6),(14,11),(3,11)],fill='#99a49a');d.line((3,12,12,12),fill='#c9bd7c');d.rectangle((9,1,11,8),fill='#7a8b82');d.line((9,1,11,8),fill='#d4d8c2');save(im,Path('items/launch_facility_section.png'))
im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
d.polygon([(13,25),(13,5),(16,0),(19,5),(19,25)],fill='#c17c45');d.rectangle((14,8,18,24),fill='#c98b52')
for x in [8,22]:d.polygon([(x-2,26),(x-2,8),(x,4),(x+2,8),(x+2,26)],fill='#d9ded3');d.line((x-2,24,x+2,24),fill='#515b5b')
d.polygon([(14,24),(13,10),(16,5),(19,10),(18,24),(28,28),(18,27),(16,29),(14,27),(4,28)],fill='#333c3e')
d.polygon([(14,23),(14,11),(16,7),(18,11),(18,23),(25,26),(18,25),(16,27),(14,25),(7,26)],fill='#e6e4cf');d.rectangle((15,11,17,12),fill='#465f67');d.line((16,18,16,29),fill='#717d79');save(im,Path('items/space_shuttle.png'))
atlas=Image.new('RGBA',(128,128),'#515953');d=ImageDraw.Draw(atlas)
colors=['#dedfcf','#282f32','#c0854d','#656e69','#666e70','#98a89d','#385561','#aa9a60']
for m,c in enumerate(colors):
    x=m*16;d.rectangle((x,0,x+15,15),fill=c)
    if m in [0,1]:
        seam='#c0c4b8' if m==0 else '#384144'
        for yy in range(0,16,4):d.line((x,yy,x+15,yy),fill=seam)
        for yy in range(0,16,4):
            for xx in range(0,16,4):d.line((x+xx+(2 if yy%8 else 0),yy,x+xx+(2 if yy%8 else 0),yy+3),fill=seam)
    elif m==2:
        for yy in [3,11]:d.line((x,yy,x+15,yy),fill='#c98e57')
        for xx,yy in [(3,6),(9,14),(13,2)]:d.point((x+xx,yy),fill='#aa7245')
    elif m==4:
        for xx in range(1,16,3):d.line((x+xx,0,x+xx,15),fill='#a0aaa4')
    elif m==5:d.rectangle((x,0,x+15,15),outline='#53665e');d.line((x+1,1,x+14,1),fill='#d9dec9')
    elif m==6:d.line((x+1,1,x+14,1),fill='#839d99');d.line((x+2,2,x+6,6),fill='#547b80')
save(atlas,Path('models/shuttle_atlas.png'))
lang=ROOT/'src/main/resources/assets/industrialtech/lang/en_US.lang'
titles=['Lunar Iron-Bearing Rock','Lunar Ilmenite (Iron / Titanium)','Lunar Feldspar (Aluminium)','Martian Hematite (Iron)','Martian Titanium-Bearing Rock','Martian Silica']
lines=[f'tile.industrialtech.{n}.name={t}' for n,t in zip(names,titles)]
lines+=['item.industrialtech.titanium_concentrate.name=Titanium Concentrate','item.industrialtech.aluminium_concentrate.name=Aluminium Concentrate','item.industrialtech.space_aluminium.name=Aluminium Ingot','item.industrialtech.space_shuttle.name=Space Shuttle Stack','tile.industrialtech.shuttle_launch_facility.name=LC-39 Shuttle Launch Facility Kit','tile.industrialtech.launch_facility_part.name=LC-39 Structure Part','item.industrialtech.launch_facility_section.name=Launch Facility Section']
keys={line.split('=',1)[0] for line in lines}
existing=[line for line in lang.read_text(encoding='utf-8').splitlines() if line.split('=',1)[0] not in keys]
lang.write_text('\n'.join(existing+lines)+'\n',encoding='utf-8')
board=Image.new('RGB',(640,260),'#263135');dr=ImageDraw.Draw(board)
for k,f in enumerate(made):
    x=8+(k%10)*63;y=10+(k//10)*85;img=Image.open(f);img.thumbnail((48,48));img=img.resize((48,48),Image.Resampling.NEAREST);board.paste(img,(x,y),img);dr.text((x,y+51),f.stem[:10],fill='#d9dfd0')
dest=ROOT/'docs/art/shuttle-resources-board.png';board.save(dest)
write('docs/art/shuttle-art-manifest.json',json.dumps([{'path':str(f.relative_to(ROOT)).replace('\\','/'),'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in made],indent=2))
