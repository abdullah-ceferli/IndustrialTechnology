"""Original, hand-positioned wireless pixel artwork matching the existing mod palette."""
from pathlib import Path
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/industrialtech'
items=ASSETS/'textures/items'; blocks=ASSETS/'textures/blocks'; gui=ASSETS/'textures/gui'
for folder in (items,blocks,gui):folder.mkdir(parents=True,exist_ok=True)
def torch(bright,dark):
    im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
    d.rectangle((7,9,8,15),fill='#765a34')
    for y in range(9,16):
        d.point((7,y),fill=('#947447','#9b7b4b','#88683b')[y%3])
        d.point((8,y),fill=('#40321f','#493822','#3b2e1c')[y%3])
    d.rectangle((7,5,8,8),fill=dark);d.rectangle((6,6,9,7),fill=dark)
    d.rectangle((7,6,8,7),fill=bright);d.point((7,6),fill='#fffbc0');d.point((8,6),fill='#f5fff0')
    return im
for name,bright,dark in [('green_stick','#51f567','#168b32'),('yellow_stick','#ffef54','#c9a51c'),('blue_stick','#54baff','#226bca')]:
    torch(bright,dark).save(items/(name+'.png'))
    if name!='yellow_stick':torch(bright,dark).save(blocks/(name+'.png'))
im=Image.new('RGBA',(32,32),(0,0,0,0));d=ImageDraw.Draw(im)
d.polygon([(5,7),(8,4),(24,4),(27,7),(27,25),(24,28),(8,28),(5,25)],fill='#101820')
d.rectangle((7,6,25,26),fill='#34434e');d.line((8,6,24,6),fill='#8d9c9b');d.line((7,7,7,24),fill='#758b94')
d.rectangle((9,8,23,19),fill='#0d262a',outline='#71888a');d.rectangle((10,9,22,18),fill='#123b3e')
d.line((11,16,13,16,13,12,16,12,16,14,20,14),fill='#84e4c8');d.rectangle((19,10,21,11),fill='#64bbc8')
d.rectangle((10,22,13,24),fill='#99b4b5');d.rectangle((15,22,18,24),fill='#d9bd6c');d.rectangle((20,22,23,24),fill='#718eac')
d.rectangle((11,2,12,4),fill='#819798');d.point((11,1),fill='#b6d5d2');im.save(items/'wireless_terminal.png')
torch('#ffef54','#c9a51c').save(blocks/'redstone_relay.png')
torch('#b4a247','#6b5d29').save(blocks/'redstone_relay_off.png')
im=Image.new('RGB',(512,256),'#171c20');d=ImageDraw.Draw(im)
d.rectangle((1,1,314,234),fill='#3b4449',outline='#7e898a');d.rectangle((3,3,312,232),outline='#222b31')
d.rectangle((7,6,308,24),fill='#262f35');d.line((8,26,307,26),fill='#aa864a')
def panel(box):
    x,y,r,b=box;d.rectangle(box,fill='#262f35',outline='#6f7a7d');d.line((x+1,b-1,r-1,b-1),fill='#151e24');d.line((r-1,y+1,r-1,b-1),fill='#151e24')
panel((8,29,114,139));panel((119,29,307,139))
d.rectangle((76,150,240,225),fill='#171f24',outline='#77868a')
def slot(x,y):
    d.rectangle((x-1,y-1,x+16,y+16),fill='#1c252b');d.line((x-1,y-1,x+16,y-1),fill='#10191e');d.line((x-1,y-1,x-1,y+16),fill='#10191e');d.rectangle((x,y,x+15,y+15),fill='#46545b');d.line((x,y+15,x+15,y+15),fill='#879696');d.line((x+15,y,x+15,y+15),fill='#879696')
for row in range(3):
    for col in range(9):slot(78+col*18,151+row*18)
for col in range(9):slot(78+col*18,209)
for x,y in ((5,5),(308,5),(5,228),(308,228)):
    d.rectangle((x,y,x+2,y+2),fill='#1b2329');d.point((x,y),fill='#9da5a1')
im.save(gui/'wireless_terminal.png')
sheet=Image.new('RGB',(760,380),'#19232c');out=ImageDraw.Draw(sheet)
for i,name in enumerate(('wireless_terminal','green_stick','yellow_stick','blue_stick')):
    icon=Image.open(items/(name+'.png'));large=icon.resize((96,96),Image.Resampling.NEAREST);sheet.paste(large,(16+i*180,22),large);out.text((16+i*180,126),name,fill='#c9d2ce')
sheet.paste(im.crop((0,0,316,236)),(16,144));(ROOT/'docs').mkdir(exist_ok=True);sheet.save(ROOT/'docs/wireless-artwork.png')
book=Image.new('RGBA',(16,16),(0,0,0,0));b=ImageDraw.Draw(book)
b.polygon([(2,3),(10,1),(14,4),(14,12),(6,15),(2,12)],fill='#39271b')
b.polygon([(3,4),(10,2),(13,4),(6,7)],fill='#e6d6ac')
b.polygon([(6,8),(13,5),(13,11),(6,14)],fill='#cabc94')
b.line((7,9,12,7),fill='#f7e9c9');b.line((7,11,12,9),fill='#9f8e6c')
b.polygon([(2,4),(6,7),(6,14),(2,11)],fill='#755137')
b.line((3,5,5,7,5,12),fill='#9c714c');b.polygon([(2,3),(6,6),(14,3),(14,5),(6,8),(2,5)],fill='#8b5a37')
b.line((3,3,6,5,12,3),fill='#b78552');book.save(items/'automation_guide.png')
print('Created original terminal icon and 3 classic 16px colored torch icons, 4 placed-torch textures, and 1 terminal GUI panel.')
