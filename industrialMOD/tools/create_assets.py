"""Original, hand-positioned pixel artwork. No source textures or generated-image placeholders.
Run with Python + Pillow. Every asset uses hard pixel edges and a limited material palette.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import math

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/industrialtech'
DARK = '#171c20'
STEEL = '#525c62'
LIGHT = '#8d9799'
PALETTES = [('#72746c', '#b48249', '#ddb471'), ('#525f69', '#7898ac', '#afc8ce'), ('#343d45', '#aa8645', '#e5bc64'), ('#4a585f', '#718e8a', '#b5cfc0')]

def corners(d, top=False):
    positions=((0,0),(24,0),(0,24),(24,24)) if top else ((0,0),(24,0))
    for x,y in positions:
        d.rectangle((x,y,x+7,y+7),fill='#155a61')
        d.rectangle((x+1,y+1,x+6,y+6),fill='#2eb5b7')
        d.rectangle((x+1,y+1,x+5,y+2),fill='#80e8d9')
        d.rectangle((x+2,y+3,x+5,y+5),fill='#218e99')
        d.point((x+2,y+1),fill='#d0fff0')

def plate(tier):
    im=Image.new('RGB',(32,32),'#242b30'); d=ImageDraw.Draw(im)
    base=('#626a6d','#53616b','#3d464e')[min(tier,2)]
    d.rectangle((1,1,30,30),fill=base)
    d.line((1,1,30,1),fill='#a8aaa0');d.line((1,1,1,30),fill='#939c9b')
    d.line((2,30,30,30),fill='#222a30');d.line((30,2,30,30),fill='#222a30')
    for x,y in ((2,2),(28,2),(2,28),(28,28)):
        d.rectangle((x,y,x+1,y+1),fill='#1c252d');d.point((x,y),fill='#b7b7a8')
    return im

def loader_face(kind,active):
    im=Image.new('RGB',(32,32),'#101018');d=ImageDraw.Draw(im)
    d.rectangle((2,2,29,29),fill='#18151e')
    for box in ((5,12,10,16),(16,18,22,24),(6,24,11,28),(24,9,27,13)):
        d.rectangle(box,fill='#22212a')
    if kind=='top':
        d.rectangle((0,0,31,31),fill='#99721b')
        d.rectangle((2,2,29,29),fill='#d8b92d')
        d.rectangle((4,4,27,27),fill='#f4da45')
        d.rectangle((7,7,24,24),fill='#ffe965')
        d.line((7,6,24,6),fill='#fff7a2');d.line((5,7,5,23),fill='#fbed76')
        d.rectangle((8,25,24,27),fill='#d4b435')
        corners(d,True)
    elif kind=='bottom':
        d.rectangle((2,2,29,29),outline='#b28b27',width=2)
        d.rectangle((8,8,23,23),fill='#0c1017',outline='#3a3e42')
        corners(d,True)
    else:
        d.rectangle((1,0,30,5),fill='#d5af29');d.line((8,1,23,1),fill='#fce55e')
        d.rectangle((2,5,3,30),fill='#c69c29');d.rectangle((28,5,29,30),fill='#937022')
        d.rectangle((9,5,22,7),fill='#c6a332');d.rectangle((12,8,19,9),fill='#a58624')
        d.line((4,30,27,30),fill='#715d2d')
        corners(d)
        # Small original area emblem is inset into the dark utility casing.
        for y in range(3):
            for x in range(3):
                color='#a99b53' if active and x==y else '#343846'
                d.rectangle((12+x*3,17+y*3,13+x*3,18+y*3),fill=color)
    return im

def face(tier,kind,active=False):
    if tier==3:return loader_face(kind,active)
    im=plate(tier);d=ImageDraw.Draw(im)
    frame=('#cf8b32','#b88b46','#d8b65e')[tier]
    light=('#e8ba6b','#cbb377','#f4d98c')[tier]
    if kind=='front':
        d.rectangle((1,1,30,30),outline=frame,width=2)
        d.rectangle((4,4,27,27),fill=('#b0b5b3','#a2afb4','#88969d')[tier])
        d.line((4,4,27,4),fill='#e0ded1');d.line((4,4,4,27),fill='#d1d5ce')
        d.rectangle((6,6,25,20),fill='#293a3b',outline='#4b5252')
        d.rectangle((8,8,23,18),fill='#152c26' if active else '#17221f')
        # Stepped process diagram inside a visible monitor, with a restrained indicator line.
        d.line((10,10,14,10),fill='#b6c8ae');d.line((14,10,14,12),fill='#b6c8ae')
        d.line((15,12,19,12),fill='#d5dfcf');d.line((19,12,19,16),fill='#b6c8ae')
        d.line((16,16,19,16),fill='#b6c8ae')
        if tier>0:d.line((10,16,13,16),fill='#779caa');d.point((11,14),fill='#b4d1d2')
        if tier==2:d.line((21,9,21,16),fill=light);d.point((20,16),fill=light)
        d.rectangle((6,23,9,26),fill='#424d54');d.point((7,23),fill='#a7bbb4')
        for x in (13,18,23):d.rectangle((x,23,x+2,25),fill='#46515a');d.point((x,23),fill='#c2c9c0')
        d.point((4,10),fill='#68a664' if active else '#708169');d.point((4,14),fill='#bc7850')
        if tier>0:
            for x,y in ((1,1),(27,1),(1,27),(27,27)):
                d.rectangle((x,y,x+3,y+3),fill='#394a59');d.point((x+1,y+1),fill=light)
    elif kind=='top':
        d.rectangle((3,3,28,28),outline=frame,width=2)
        d.rectangle((7,7,24,24),fill='#303a3e',outline='#879497')
        d.rectangle((9,9,22,12),fill='#193328')
        for x in (10,13,16,19):d.rectangle((x,10,x+1,11),fill='#74967a')
        for y in (16,19,22):d.line((9,y,22,y),fill='#1c262d');d.line((10,y+1,22,y+1),fill='#65727a')
    elif kind=='side':
        d.rectangle((3,3,28,28),fill='#303a43',outline=frame,width=2)
        d.rectangle((7,7,24,24),fill='#252e34',outline='#71828a')
        for y in (10,14,18,22):d.line((9,y,22,y),fill='#0e1820');d.line((9,y+1,22,y+1),fill='#596971')
        if tier>0:
            for x in (5,26):d.line((x,7,x,24),fill='#79919e')
    else:
        d.rectangle((6,6,25,25),fill='#26343d',outline='#88979a')
        for x in range(9,24,3):d.line((x,8,x,23),fill='#101a22');d.line((x+1,9,x+1,23),fill='#485d67')
    return im

blocks = ASSETS / 'textures/blocks'; blocks.mkdir(parents=True, exist_ok=True)
names = ['quarry_mk1','quarry_mk2','quarry_mk3','chunk_loader']
for tier, name in enumerate(names):
    for kind in ('front','side','top','bottom'):
        face(tier,kind).save(blocks / f'{name}_{kind}.png')
    face(tier,'front',True).save(blocks / f'{name}_front_active.png')

items = ASSETS / 'textures/items'; items.mkdir(parents=True, exist_ok=True)
im = Image.new('RGBA',(32,32)); d = ImageDraw.Draw(im)
d.rectangle((5,5,26,26), fill='#1c3534', outline='#8f976f')
d.rectangle((7,7,24,24), fill='#30574c')
for y in (8,12,19,23):
    d.line((8,y,23,y), fill='#ac8c4c'); d.point((8,y), fill='#e3c17a')
for x in (10,14,18,22):
    d.rectangle((x,3,x+1,4), fill='#c8a05b'); d.rectangle((x,27,x+1,28), fill='#c8a05b')
d.rectangle((11,10,20,21), fill='#162127', outline='#819395')
d.rectangle((13,12,18,19), fill='#48595b'); d.line((13,12,18,12), fill='#aebbb7')
im.save(items/'circuit.png')
im = Image.new('RGBA',(32,32)); d = ImageDraw.Draw(im)
for angle in range(0,360,45):
    a=math.radians(angle); x=int(16+11*math.cos(a)); y=int(16+11*math.sin(a))
    d.rectangle((x-3,y-3,x+3,y+3),fill='#5d6a70',outline='#a6b2b0')
d.ellipse((6,6,25,25),fill='#6e7d84',outline='#bec9c1')
d.ellipse((10,10,21,21),fill='#40515c',outline='#96a5a5')
d.rectangle((13,13,18,18),fill='#17242b',outline='#b8c1b8')
im.save(items/'steel_gear.png')
im = Image.new('RGBA',(32,32)); d=ImageDraw.Draw(im)
d.polygon([(3,25),(7,29),(16,20),(12,16)],fill='#323f48',outline='#a7b5b4')
d.polygon([(10,15),(17,22),(22,17),(15,10)],fill='#8c754e',outline='#d7b875')
d.polygon([(16,10),(22,16),(28,4),(27,3)],fill='#71aaa9',outline='#d0e7db')
for x,y in ((20,9),(22,7),(24,5)): d.line((x-1,y,x+2,y+2),fill='#294d59')
im.save(items/'drill.png')

gui = ASSETS/'textures/gui'; gui.mkdir(parents=True,exist_ok=True)
def panel(d,box):
    x,y,x2,y2=box; d.rectangle(box,fill='#272e33',outline='#11171b')
    d.line((x+1,y+1,x2-1,y+1),fill='#4c565b'); d.line((x+1,y+1,x+1,y2-1),fill='#424b50')
def slot(d,x,y):
    d.rectangle((x-1,y-1,x+16,y+16),fill='#172026')
    d.rectangle((x,y,x+15,y+15),fill='#444e53')
    d.line((x,y,x+15,y),fill='#0c1319'); d.line((x,y,x,y+15),fill='#0c1319')
    d.line((x+1,y+16,x+16,y+16),fill='#7b8587'); d.line((x+16,y+1,x+16,y+16),fill='#7b8587')
for storage in (False,True):
    im=Image.new('RGB',(512,256),DARK); d=ImageDraw.Draw(im)
    d.rectangle((1,1,314,234),fill='#3b4449',outline='#7e898a')
    d.rectangle((3,3,312,232),outline='#222b31')
    d.rectangle((7,5,309,25),fill='#262f35')
    d.line((8,26,307,26),fill='#aa864a')
    for x,y in ((5,5),(308,5),(5,228),(308,228)):
        d.rectangle((x,y,x+2,y+2),fill='#1b2329'); d.point((x,y),fill='#9da5a1')
    if storage:
        panel(d,(8,29,306,139))
        for i in range(54): slot(d,78+i%9*18,33+i//9*18)
    else:
        panel(d,(8,29,104,139));panel(d,(108,29,211,141));panel(d,(215,29,307,139))
    panel(d,(74,147,244,228))
    for row in range(3):
        for col in range(9): slot(d,78+col*18,151+row*18)
    for col in range(9): slot(d,78+col*18,209)
    im.save(gui / ('storage.png' if storage else 'controls.png'))

# Filter panel extends the established control/storage artwork with eighteen ghost icons.
im=Image.open(gui/'storage.png');d=ImageDraw.Draw(im)
panel(d,(8,29,306,139))
for i in range(18): slot(d,78+i%9*18,73+i//9*18)
im.save(gui/'void_filter.png')

# Dedicated compact loader panel, with the same stable metal palette and no inventory section.
im=Image.new('RGB',(256,256),DARK);d=ImageDraw.Draw(im)
d.rectangle((1,1,204,202),fill='#3b4449',outline='#7e898a');d.rectangle((3,3,202,200),outline='#222b31')
d.rectangle((7,6,198,24),fill='#262f35');d.line((8,26,197,26),fill='#aa864a')
panel(d,(8,29,197,144))
for x,y in ((5,5),(198,5),(5,197),(198,197)):
    d.rectangle((x,y,x+2,y+2),fill='#1b2329');d.point((x,y),fill='#9da5a1')
im.save(gui/'chunk_loader.png')

# Enlarged contact sheet: actual source pixels, nearest-neighbor only.
sheet=Image.new('RGB',(800,700),'#172026'); d=ImageDraw.Draw(sheet)
for tier,name in enumerate(names):
    d.text((12,12+tier*103),name,fill='#cfbd93')
    for n,kind in enumerate(('front','front_active','side','top','bottom')):
        image=Image.open(blocks/f'{name}_{kind}.png').resize((80,80),Image.Resampling.NEAREST)
        sheet.paste(image,(155+n*100,8+tier*103))
d.text((12,429),'Machine control / storage panels (native pixels)',fill='#cfbd93')
sheet.paste(Image.open(gui/'controls.png').crop((0,0,316,236)),(12,449))
sheet.paste(Image.open(gui/'storage.png').crop((0,0,316,236)),(344,449))
(ROOT/'docs').mkdir(exist_ok=True)
sheet.save(ROOT/'docs/artwork-contact-sheet.png')
print('Created 20 reference-inspired block textures, 3 item icons, 4 GUI panels, and contact sheet.')
