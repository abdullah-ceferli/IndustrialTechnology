"""Original, coordinate-authored pixel assets for the space content only.

No imported mod artwork, stochastic noise, diffusion, resampling blur or PBR.
Run from the checkout. See docs/SPACE_VISUAL_REWORK.md for palette/UV notes.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json, hashlib

ROOT = Path(__file__).resolve().parents[1]
if not (ROOT/'build.gradle').exists():
    ROOT=Path(r'D:\IndustrialTechnology\industrialMOD')
ART=ROOT/'src/main/resources/assets/industrialtech/textures'
WRITTEN=[]
INK='#252c30'; DEEP='#384247'; STEEL='#69767a'; MID='#909c9c'; LIGHT='#c5ccc6'; HI='#e3e5d8'
BEIGE='#b9b5a2'; BHI='#dad6bc'; BDARK='#777968'; BLUE='#62949b'; AMBER='#c2a164'; GREEN='#9ab68b'

def canvas(color=(0,0,0,0),size=(16,16)):
    im=Image.new('RGBA',size,color);return im,ImageDraw.Draw(im)
def save(im,folder,name):
    path=ART/folder/(name+'.png');path.parent.mkdir(parents=True,exist_ok=True);im.save(path,optimize=True);WRITTEN.append(path)
def rect(d,box,fill,outline=None): d.rectangle(box,fill=fill,outline=outline)
def frame(d,box,fill=STEEL):
    x,y,X,Y=box;rect(d,box,INK);rect(d,(x+1,y+1,X-1,Y-1),fill);d.line((x+1,Y-1,x+1,y+1,X-1,y+1),fill=MID);d.line((x+1,Y-1,X-1,Y-1,X-1,y+2),fill=DEEP)
def screws(d):
    for x,y in ((1,1),(14,1),(1,14),(14,14)):d.point((x,y),fill=LIGHT)
def casing(color=STEEL):
    im,d=canvas();frame(d,(0,0,15,15),color);screws(d);return im,d
def vent(d,x,y,w=8,n=3):
    for k in range(n):d.line((x,y+k*2,x+w-1,y+k*2),fill=INK);d.line((x,y+k*2+1,x+w-1,y+k*2+1),fill=MID)
def gauge(d,x,y):
    rect(d,(x,y,x+4,y+4),INK);rect(d,(x+1,y+1,x+3,y+3),BHI);d.line((x+2,y+3,x+3,y+1),fill='#a55e46')
def screen(d,box):
    rect(d,box,INK);x,y,X,Y=box;rect(d,(x+1,y+1,X-1,Y-1),'#283d35');d.line((x+2,y+2,X-2,y+2),fill=GREEN)
def socket(d,x,y,color=BLUE):
    rect(d,(x,y,x+4,y+4),INK);rect(d,(x+1,y+1,x+3,y+3),color);d.point((x+2,y+2),fill=DEEP)

MACHINES=['research_computer','telescope','rocket_assembly','fuel_refinery','oxygen_plant','air_compressor','suit_refill_station','atmosphere_pressurizer','launch_controller','fuel_port','airlock_controller']
for kind,name in enumerate(MACHINES):
    im,d=casing(BEIGE if kind==0 else STEEL)
    if kind==0:
        rect(d,(3,3,12,5),BDARK);d.line((4,3,11,3),fill=INK);rect(d,(3,7,10,9),BDARK);d.line((4,7,9,7),fill=INK);rect(d,(3,12,5,13),DEEP);d.point((11,12),fill=GREEN)
    elif kind==1:
        rect(d,(3,3,11,8),DEEP);d.line((4,7,6,5,8,6,10,4),fill=BLUE);gauge(d,3,10);rect(d,(10,10,12,12),AMBER)
    elif kind==2:
        frame(d,(2,3,13,10),DEEP);d.line((4,8,7,5,11,5),fill=LIGHT,width=2);rect(d,(9,7,11,8),AMBER);rect(d,(3,12,12,13),INK)
    elif kind==3:
        gauge(d,3,3);rect(d,(10,3,12,10),INK);rect(d,(11,4,11,8),'#b49966');socket(d,3,9,AMBER);d.line((9,12,12,12),fill=LIGHT)
    elif kind==4:
        rect(d,(3,3,6,11),INK);rect(d,(4,4,5,10),BLUE);rect(d,(9,3,12,11),INK);rect(d,(10,5,11,10),'#b4c7c8');d.line((4,12,11,12),fill=LIGHT)
    elif kind==5:
        frame(d,(3,3,12,12),DEEP);d.ellipse((4,4,11,11),fill=INK);d.line((6,5,9,10),fill=MID,width=2);d.line((5,9,10,6),fill=MID,width=2);rect(d,(7,7,8,8),LIGHT)
    elif kind==6:
        rect(d,(3,3,7,10),INK);rect(d,(4,4,6,9),LIGHT);rect(d,(4,4,6,5),BLUE);gauge(d,9,3);d.line((10,9,10,12,6,12),fill=INK);socket(d,3,10,BLUE)
    elif kind==7:
        gauge(d,3,3);rect(d,(10,3,12,5),GREEN);vent(d,3,9,10,2);d.point((12,7),fill=AMBER)
    elif kind==8:
        screen(d,(3,3,12,9));d.line((7,7,7,5,8,4,9,5,9,7),fill=LIGHT);rect(d,(3,11,5,12),'#9e634f');rect(d,(7,11,9,12),AMBER);rect(d,(11,11,12,12),GREEN)
    elif kind==9:
        socket(d,3,3,AMBER);socket(d,9,3,BLUE);socket(d,6,9,LIGHT);d.line((4,13,11,13),fill=INK)
    else:
        gauge(d,3,3);gauge(d,9,3);rect(d,(3,10,6,12),GREEN);rect(d,(9,10,12,12),AMBER);d.line((7,9,7,13),fill=INK)
    save(im,'blocks',name)  # Existing resource path remains the front.
    im,d=casing(BEIGE if kind==0 else STEEL)
    if kind in (3,4,5,6,7):
        frame(d,(3,3,12,12),DEEP)
        if kind in (3,4,6):
            d.line((5,4,5,10,11,10),fill=MID,width=2);socket(d,8,7,AMBER if kind==3 else BLUE)
        else:vent(d,4,4,8,4)
    elif kind in (2,8):
        frame(d,(3,3,12,12),DEEP);rect(d,(5,5,10,10),STEEL);d.line((4,12,11,12),fill=AMBER)
    elif kind==9:
        socket(d,6,5,DEEP);d.line((3,11,12,11),fill=MID)
    else:vent(d,4,4,8,4)
    save(im,'blocks',name+'_side')
    im,d=casing(BEIGE if kind==0 else STEEL)
    if kind in (0,1):vent(d,4,4,8,4)
    elif kind==8:
        frame(d,(3,3,12,12),DEEP);d.line((7,4,7,11),fill=LIGHT);d.line((4,7,11,7),fill=LIGHT)
    elif kind==2:
        rect(d,(2,2,13,13),DEEP);d.line((3,4,12,4),fill=LIGHT);d.line((3,11,12,11),fill=LIGHT);d.line((7,3,7,12),fill=AMBER)
    elif kind==9:socket(d,5,5,AMBER)
    else:
        frame(d,(3,3,12,12),MID);rect(d,(6,6,9,8),DEEP);d.line((4,11,11,11),fill=STEEL)
    save(im,'blocks',name+'_top')
    im,d=casing(DEEP);vent(d,3,4,10,4);save(im,'blocks',name+'_bottom')

# Structural textures: aligned ribs, fasteners and hatches, rather than screens.
for name in ('airtight_hull','rocket_hull'):
    im,d=casing(LIGHT if name=='rocket_hull' else MID);rect(d,(3,3,12,12),MID if name=='rocket_hull' else STEEL);d.line((4,4,11,4),fill=LIGHT);d.line((4,11,11,11),fill=DEEP);save(im,'blocks',name)
    im,d=casing(MID);frame(d,(3,3,12,12),STEEL);rect(d,(6,6,9,8),DEEP);save(im,'blocks',name+'_top')
im,d=casing(DEEP)
for y in (3,6,9,12):d.line((2,y,13,y),fill=INK);d.line((2,y+1,13,y+1),fill=STEEL)
for x in (0,15):
    for y in range(2,14):d.point((x,y),fill=AMBER if (y//3)%2 else INK)
save(im,'blocks','rocket_launchpad')
im,d=casing(DEEP);rect(d,(5,2,10,13),INK);d.line((6,3,6,12),fill=STEEL);d.line((9,3,9,12),fill=STEEL);save(im,'blocks','rocket_launchpad_side')
im,d=canvas();rect(d,(0,0,15,15),INK);rect(d,(1,1,14,14),MID);rect(d,(2,2,13,13),(0,0,0,0));d.line((3,8,8,3),fill=(191,218,217,150));d.line((4,11,5,10),fill=(191,218,217,100));d.line((10,12,12,10),fill=(191,218,217,100));save(im,'blocks','airtight_glass')
for upper in (False,True):
    im,d=casing(MID);d.line((7,1,7,14),fill=DEEP);d.line((8,1,8,14),fill=LIGHT)
    if upper:
        frame(d,(3,3,12,10),DEEP);rect(d,(4,4,11,9),'#536e74');d.line((4,4,10,4),fill='#9fb4b1');d.line((5,8,8,5),fill='#7d9494')
    else:
        rect(d,(10,3,12,7),INK);rect(d,(11,4,11,6),AMBER);vent(d,3,10,9,2)
    save(im,'blocks','airlock_door_top' if upper else 'airlock_door')
im,d=casing(DEEP);d.line((5,2,5,13),fill=MID);d.line((10,2,10,13),fill=STEEL);save(im,'blocks','airlock_door_edge')
for on in (False,True):
    im,d=casing(DEEP);frame(d,(2,2,13,13),'#477249' if on else '#354d3c')
    rect(d,(4,4,11,11),'#a9cb7d' if on else '#587255');d.line((4,4,11,4),fill='#d0dea4' if on else '#758a66')
    d.line((3,7,12,7),fill='#627a52' if on else '#3a5140');d.line((7,3,7,12),fill='#627a52' if on else '#3a5140');save(im,'blocks','green_lamp_on' if on else 'green_lamp_off')

# Geological structure uses explicit interlocking clasts and bedding; no RNG.
clasts=[([(0,1),(5,0),(6,3),(3,5),(-1,4)],2), ([(7,-1),(13,0),(12,4),(8,5),(6,3)],1), ([(14,1),(18,3),(17,7),(13,7),(12,4)],3), ([(2,6),(6,5),(9,8),(7,11),(3,10),(0,8)],1), ([(10,6),(13,7),(14,12),(10,13),(8,10)],2), ([(0,11),(4,11),(6,14),(3,17),(-1,15)],3), ([(7,12),(10,14),(12,18),(6,17),(5,15)],1), ([(14,12),(18,11),(18,16),(13,17),(12,15)],2)]
palettes={
 'moon_rock':['#737577','#8c8d8c','#7d8081','#62686a','#a0a09a'],
 'lunar_basalt':['#44494b','#5c6263','#4e5658','#373f42','#737878'],
 'lunar_breccia':['#737574','#9a9b94','#82877f','#595f60','#b1b0a4'],
 'mars_rock':['#81513e','#9d6b4b','#8d5c44','#684338','#b58359']}
for name,pal in palettes.items():
    im,d=canvas(pal[0])
    for dx in (-16,0,16):
        for dy in (-16,0,16):
            for polygon,c in clasts:
                p=[(x+dx,y+dy) for x,y in polygon];d.polygon(p,fill=pal[c]);d.line(p[:2],fill=pal[4])
    # A few intentional inclusions break up broad stones, not the silhouette.
    for x,y in ((3,3),(9,2),(5,8),(12,10),(2,14),(9,15)):d.line((x,y,x+1,y),fill=pal[0])
    if name=='lunar_breccia':
        for p in [[(2,2),(5,1),(5,3),(3,4)],[(9,8),(12,9),(11,11),(9,10)],[(4,12),(6,13),(5,15),(3,14)]]:d.polygon(p,fill=pal[4]);d.point(p[-1],fill=pal[3])
    save(im,'blocks',name)
grains=[(1,2),(3,2),(9,1),(12,3),(14,5),(6,5),(8,7),(2,7),(4,10),(12,10),(15,12),(9,13),(1,14),(6,15)]
for name,base,light,dark in [('lunar_regolith','#92918a','#a5a397','#7d7f7b'),('lunar_dust','#aaa89d','#bcb8a9','#96968d'),('mars_regolith','#ad7856','#be8d65','#946446')]:
    im,d=canvas(base)
    for x,y in grains:d.line((x,y,(x+1)%16,y),fill=light)
    for x,y in ((5,1),(11,6),(2,11),(7,11),(14,15)):d.line((x,y,x+1,y),fill=dark)
    if name!='lunar_dust':
        for x,y in ((3,4),(11,12),(6,8)):rect(d,(x,y,x+1,y+1),dark);d.point((x,y),fill=light)
    save(im,'blocks',name)
im,d=canvas('#93674f')
for y,h,c in ((0,3,'#a87959'),(3,2,'#795443'),(5,4,'#9f7355'),(9,2,'#bd9068'),(11,3,'#8d614c'),(14,2,'#a87959')):
    rect(d,(0,y,15,y+h-1),c)
for line in [[(0,3),(4,3),(7,2),(12,2),(15,3)],[(0,10),(5,10),(8,9),(15,9)],[(4,5),(4,7),(6,8)],[(12,12),(11,13),(11,15)]]:d.line(line,fill='#70503f')
save(im,'blocks','mars_layered_rock')
im,d=canvas('#8dacae')
for p in [[(0,0),(7,1),(5,7),(0,8)],[(9,2),(15,0),(15,10),(10,8)],[(3,9),(9,8),(12,15),(2,15)]]:d.polygon(p,fill='#a8c1be')
d.line((0,9,6,6,8,0),fill='#d1ddd0');d.line((6,6,11,10,15,10),fill='#d1ddd0');d.line((11,10,10,15),fill='#71989d');save(im,'blocks','subsurface_ice')

# Fluid atlas tiles: quiet broken wave crests, no stochastic noise.
for name,base,crest,deep in [('crude_oil','#302b25','#4c4130','#242321'),('kerosene','#987b43','#baa16a','#7c643a'),('oxygen','#769d9f','#b4c7c3','#62858b'),('liquid_oxygen','#769eaf','#b3cdd2','#5d879e'),('breathable_air','#9ab4af','#c0ccc0','#7e9e9c')]:
    im,d=canvas(base)
    for line in [[(0,4),(3,4),(5,3),(9,3)],[(11,4),(15,4)],[(0,11),(5,11),(8,10),(12,10)],[(14,11),(15,11)]]:d.line(line,fill=crest)
    for line in [[(2,7),(6,7)],[(10,14),(14,14)]]:d.line(line,fill=deep)
    save(im,'blocks/fluids',name)

# Item silhouettes are authored independently, rather than recolored templates.
for name in ['rocket_engine','rocket_fuel_tank','oxidizer_tank','guidance_computer','navigation_electronics','rocket_hull','rocket_fins','rocket_nozzle','crew_capsule','heat_shield','landing_legs','research_hdd','assembled_rocket','space_canister','space_steel','space_titanium','thermal_alloy','air_filter']:
    im,d=canvas()
    if name=='research_hdd':
        frame(d,(2,1,13,14),LIGHT);rect(d,(3,2,12,11),MID);d.ellipse((4,3,11,10),fill=DEEP);d.ellipse((5,3,10,9),fill=LIGHT);d.ellipse((7,5,8,6),fill=INK);d.line((11,9,8,7),fill=INK,width=2);rect(d,(4,12,10,13),INK);d.line((5,12,9,12),fill=AMBER)
    elif name=='guidance_computer':
        frame(d,(2,3,13,12),BEIGE);screen(d,(3,4,8,9));d.line((4,7,6,7,6,5),fill=GREEN);vent(d,10,5,2,2);rect(d,(4,11,9,11),INK);d.point((11,10),fill=AMBER)
    elif name=='navigation_electronics':
        rect(d,(2,2,13,13),'#44675c',INK);rect(d,(5,5,10,10),INK);rect(d,(6,6,9,9),DEEP)
        for a in (4,7,10):d.line((a,3,a,4),fill=AMBER);d.line((a,11,a,13),fill=AMBER)
        d.line((3,5,4,5,4,9,3,9),fill=LIGHT);rect(d,(11,4,12,6),BEIGE);rect(d,(11,9,12,11),BEIGE)
    elif name in ('rocket_fuel_tank','oxidizer_tank','space_canister'):
        x1,x2=(4,11) if name=='space_canister' else (3,12)
        d.polygon([(x1+2,2),(x2-2,2),(x2,4),(x2,12),(x2-2,14),(x1+2,14),(x1,12),(x1,4)],fill=MID,outline=INK);d.line((x1+1,4,x1+1,11),fill=HI);d.line((x2-1,4,x2-1,11),fill=STEEL);rect(d,(6,0,9,2),DEEP);rect(d,(x1+1,6,x2-1,8),BLUE if name=='oxidizer_tank' else AMBER if name=='rocket_fuel_tank' else LIGHT)
        if name=='oxidizer_tank':d.line((6,10,9,10),fill=LIGHT);d.line((7,9,7,12),fill=LIGHT)
        if name=='space_canister':rect(d,(5,10,10,12),DEEP);d.point((7,11),fill=BLUE)
    elif name=='rocket_engine':
        rect(d,(5,1,10,4),MID,INK);rect(d,(3,3,5,7),STEEL,INK);rect(d,(10,3,12,7),AMBER,INK);d.line((4,7,4,9,7,9),fill=LIGHT);d.polygon([(6,5),(9,5),(9,9),(12,14),(3,14),(6,9)],fill=STEEL,outline=INK);d.line((6,9,4,13,10,13),fill=MID);rect(d,(4,14,11,14),DEEP)
    elif name=='rocket_nozzle':
        rect(d,(6,2,9,5),MID,INK);d.polygon([(6,5),(9,5),(10,9),(13,13),(12,14),(3,14),(2,13),(5,9)],fill=STEEL,outline=INK);d.line((6,7,5,10,3,13),fill=LIGHT);d.line((3,13,12,13),fill=INK)
    elif name=='rocket_hull':
        d.polygon([(4,2),(10,1),(13,4),(13,12),(7,14),(3,11),(3,4)],fill=LIGHT,outline=INK);d.line((7,4,7,13),fill=MID);d.line((4,3,7,4,12,3),fill=HI);d.line((4,9,7,11,12,9),fill=STEEL);d.point((10,5),fill=STEEL)
    elif name=='rocket_fins':
        d.polygon([(9,1),(11,1),(11,12),(14,14),(3,14),(3,12),(7,8)],fill=MID,outline=INK);d.line((9,3,9,11,5,13),fill=HI);d.line((10,10,11,12),fill=DEEP)
    elif name=='landing_legs':
        rect(d,(6,1,9,4),STEEL,INK);d.line((6,4,4,11,2,12),fill=INK,width=3);d.line((9,4,11,11,13,12),fill=INK,width=3);d.line((6,4,4,10),fill=LIGHT);d.line((9,4,11,10),fill=MID);rect(d,(1,12,5,14),DEEP);rect(d,(10,12,14,14),DEEP);d.line((2,12,4,12),fill=MID);d.line((11,12,13,12),fill=MID)
    elif name=='crew_capsule':
        d.polygon([(7,1),(9,1),(13,10),(13,13),(3,13),(3,10)],fill=LIGHT,outline=INK);rect(d,(6,6,10,10),DEEP);rect(d,(7,7,9,9),BLUE);d.line((7,7,9,7),fill='#b2cccc');d.line((4,12,12,12),fill=STEEL)
    elif name=='heat_shield':
        d.polygon([(4,3),(11,3),(14,7),(13,11),(10,13),(4,12),(1,9),(1,6)],fill='#746b5e',outline=INK)
        for y in (5,8,11):d.line((3,y,12,y),fill='#494840')
        for x,y in ((5,4),(9,6),(6,9)):d.line((x,y,x,y+2),fill='#494840')
        d.line((3,5,5,4,10,4),fill='#a4957c')
    elif name=='assembled_rocket':
        d.polygon([(8,0),(10,4),(10,11),(13,14),(10,13),(10,15),(6,15),(6,13),(3,14),(6,11),(6,4)],fill=LIGHT,outline=INK);d.line((7,4,7,11),fill=HI);rect(d,(7,5,8,6),BLUE);d.line((6,9,9,9),fill=STEEL);rect(d,(7,13,8,14),DEEP)
    elif name=='air_filter':
        frame(d,(2,2,13,13),LIGHT)
        for x in (4,6,8,10):d.line((x,4,x,11),fill=DEEP);d.line((x+1,4,x+1,11),fill=BEIGE)
    else:
        p={'space_steel':(STEEL,MID,LIGHT),'space_titanium':(MID,LIGHT,HI),'thermal_alloy':('#756957','#9b8a6d','#c1ae86')}[name]
        d.polygon([(2,7),(5,3),(12,3),(14,7),(13,11),(3,12),(1,10)],fill=p[0],outline=INK);d.polygon([(2,7),(5,4),(12,4),(13,7),(5,8)],fill=p[1]);d.line((3,7,6,5,11,5),fill=p[2]);d.line((4,9,12,8),fill=p[2])
    save(im,'items',name)

# Vanilla 64x32 armor islands + dedicated 64x32 model detail area below them.
# Transparent unused pixels prevent the old full-sheet paint from covering faces.
for tier in range(1,4):
    cloth=['#c6c3ae','#b9c2bc','#d4d5c7'][tier-1];shade=['#9d9e91','#8f9f9b','#a6b0aa'][tier-1];accent=[AMBER,BLUE,'#b5a270'][tier-1]
    for layer in (1,2):
        im,d=canvas(size=(64,64))
        # Head cube UVs: top/bottom then left/front/right/back strips.
        for box in ((8,0,23,7),(0,8,31,15)):
            rect(d,box,cloth)
        rect(d,(8,8,15,15),shade);rect(d,(9,9,14,13),INK);rect(d,(10,10,14,12),'#405c65');d.line((10,10,13,10),fill='#9ab3b6');rect(d,(8,14,15,15),cloth)
        d.line((0,13,7,13),fill=shade);d.line((16,13,31,13),fill=shade);rect(d,(1,10,4,12),DEEP);rect(d,(25,10,28,12),DEEP)
        # Torso, arms and both mirrored leg islands use the vanilla armor layout.
        for x,y,w,h in ((16,20,24,12),(20,16,16,4),(40,20,16,12),(44,16,8,4),(0,20,16,12),(4,16,8,4)):
            rect(d,(x,y,x+w-1,y+h-1),cloth);d.line((x,y+h-1,x+w-1,y+h-1),fill=shade)
        rect(d,(20,20,27,29),shade);rect(d,(21,21,26,25),DEEP);rect(d,(22,22,25,23),LIGHT);d.point((22,24),fill=accent);d.point((25,24),fill=GREEN);d.line((20,28,27,28),fill=accent)
        for x in (0,4,8,12,40,44,48,52):
            d.line((x,26,x+3,26),fill=shade);d.line((x,28,x+3,28),fill=accent)
        # Leggings layer has cloth knees; boots layer has dark tread and toe caps.
        if layer==2:
            rect(d,(4,23,7,26),shade);rect(d,(4,24,7,25),LIGHT);rect(d,(20,20,27,22),shade)
        else:
            for x in (0,4,8,12):rect(d,(x,30,x+3,31),DEEP)
        if tier>=2:
            rect(d,(44,21,47,23),LIGHT);rect(d,(4,23,7,25),LIGHT);d.line((20,26,27,26),fill=LIGHT)
        if tier==3:
            d.line((8,8,15,8),fill=accent);d.line((20,20,27,20),fill=accent);rect(d,(44,29,47,30),DEEP)
        # Custom pack UV: box8x10x(3/4/5), top2rows. No reuse of arm UVs.
        rect(d,(0,32,39,47),shade);rect(d,(5,37,12,45),cloth);vent(d,6,38,6,3);rect(d,(24,34,39,45),DEEP);rect(d,(25,35,38,44),accent)
        depth=2+tier
        bx=depth*2+8;by=32+depth
        rect(d,(bx,by,bx+7,by+9),cloth);d.line((bx,by,bx+7,by),fill=LIGHT)
        for yy in (by+2,by+4,by+6):d.line((bx+1,yy,bx+6,yy),fill=DEEP)
        d.line((bx,by+8,bx+7,by+8),fill=accent)
        # Extra helmet rim, valves and soles use lower-right material islands.
        rect(d,(40,32,63,47),cloth);rect(d,(40,48,63,63),DEEP);rect(d,(0,48,23,63),accent)
        for x in (42,46,50,54,58):d.line((x,50,x,61),fill=STEEL)
        save(im,'armor',f'suit_{tier}_{layer}')
    for slot in range(4):
        im,d=canvas()
        if slot==0:
            d.polygon([(5,1),(10,1),(13,4),(13,11),(10,14),(5,14),(2,11),(2,4)],fill=cloth,outline=INK);rect(d,(4,4,11,10),shade);rect(d,(5,5,10,9),INK);rect(d,(6,6,10,8),'#405c65');d.line((6,6,9,6),fill='#a1b6b4');d.line((5,12,10,12),fill=shade)
            if tier>=2:rect(d,(2,7,3,10),STEEL);rect(d,(12,7,13,10),STEEL)
            if tier==3:d.line((5,2,10,2),fill=accent)
        elif slot==1:
            d.polygon([(3,2),(6,3),(9,3),(12,2),(15,6),(12,9),(11,14),(4,14),(3,9),(0,6)],fill=cloth,outline=INK);rect(d,(5,5,10,10),shade);rect(d,(6,6,9,8),DEEP);d.line((6,6,9,6),fill=LIGHT);d.point((7,8),fill=accent);d.line((4,12,11,12),fill=accent)
            if tier>=2:d.line((1,5,3,5),fill=LIGHT);d.line((12,5,14,5),fill=LIGHT)
            if tier==3:rect(d,(4,10,5,11),DEEP);rect(d,(10,10,11,11),DEEP)
        elif slot==2:
            d.polygon([(3,1),(12,1),(12,14),(9,14),(8,7),(7,7),(6,14),(3,14)],fill=cloth,outline=INK);rect(d,(4,2,11,3),shade);d.line((7,4,8,4),fill=DEEP);rect(d,(4,8,5,10),shade);rect(d,(10,8,11,10),shade)
            if tier>=2:rect(d,(4,8,5,9),LIGHT);rect(d,(10,8,11,9),LIGHT)
            if tier==3:d.line((4,12,5,12),fill=accent);d.line((10,12,11,12),fill=accent)
        else:
            for x in (2,9):
                rect(d,(x,3,x+3,11),cloth,INK);rect(d,(x-1,10,x+3,14),cloth,INK);rect(d,(x-1,13,x+3,14),DEEP);d.line((x,5,x+2,5),fill=accent);d.line((x,8,x+2,8),fill=shade)
                if tier>=2:rect(d,(x,10,x+2,11),LIGHT)
                if tier==3:d.point((x+1,6),fill=DEEP)
        save(im,'items',f'spacesuit_{tier}_{slot}')

# 128x128 faceted rocket wrap. Strips keep a steady texel density by section.
im,d=canvas(LIGHT,(128,128))
for seg in range(16):
    x=seg*8
    rect(d,(x,0,x+7,95),['#c5ccc6','#d5d9d0','#c5ccc6','#b4beb9'][seg%4]);d.line((x,0,x,95),fill='#8f9d9b');d.line((x+1,0,x+1,95),fill='#e4e5d8')
    for y in (31,59,87):d.line((x,y,x+7,y),fill=STEEL);d.point((x+3,y-1),fill=STEEL)
    rect(d,(x,96,x+7,111),DEEP);d.line((x+2,98,x+2,109),fill=STEEL)
    rect(d,(x,112,x+7,127),INK);d.line((x+1,114,x+1,126),fill=STEEL);d.line((x+5,114,x+5,126),fill=DEEP)
for x in (24,88):
    frame(d,(x+1,17,x+14,28),STEEL);rect(d,(x+3,19,x+12,25),'#405963');d.line((x+4,20,x+11,20),fill='#9eb5b7')
for x in (8,72):
    rect(d,(x+2,44,x+5,51),STEEL);rect(d,(x+3,45,x+4,50),LIGHT)
save(im,'models','expedition_rocket')

# Small model material atlas. Each 16px cell is purpose drawn; quads select it.
im,d=canvas(size=(128,64))
for col,c in enumerate((BEIGE,LIGHT,DEEP,STEEL,BLUE,INK,BDARK,AMBER)):
    x=col*16;rect(d,(x,0,x+15,15),c);d.line((x,0,x+15,0),fill=HI if col<2 else MID);d.line((x+15,1,x+15,15),fill=DEEP)
rect(d,(0,16,15,31),BEIGE);frame(d,(0,16,15,31),BEIGE);screen(d,(2,18,13,28));d.line((4,23,8,23,8,21,11,21),fill=GREEN);d.line((4,25,10,25),fill=GREEN)
rect(d,(16,16,31,31),BEIGE)
for y in (18,22,26):
    for x in range(18,30,3):rect(d,(x,y,x+1,y+2),BDARK);d.point((x,y),fill=BHI)
rect(d,(32,16,47,31),STEEL);vent(d,34,18,12,6)
rect(d,(48,16,63,31),DEEP);d.ellipse((49,17,62,30),fill=INK);d.ellipse((51,19,60,28),fill='#42636c');d.ellipse((53,21,58,26),fill='#709298');d.line((54,21,57,21),fill='#b9ccc5')
rect(d,(64,16,79,31),LIGHT);d.line((66,19,77,19),fill=STEEL);d.line((66,28,77,28),fill=STEEL)
rect(d,(80,16,95,31),DEEP);gauge(d,82,18);rect(d,(89,19,92,20),GREEN);vent(d,82,25,10,2)
rect(d,(96,16,111,31),MID);d.line((97,24,110,24),fill=DEEP);d.line((99,20,108,20),fill=LIGHT)
save(im,'models','space_equipment')

# A compact baked GUI shell; slots stay aligned with the existing container.
im,d=canvas(size=(512,256));rect(d,(0,0,319,237),INK);rect(d,(1,1,318,236),MID);rect(d,(2,2,317,235),'#555e5e');rect(d,(3,3,316,234),BEIGE)
rect(d,(5,5,314,18),DEEP);rect(d,(5,20,314,104),INK);rect(d,(6,21,313,103),'#d0cbb5');rect(d,(5,129,314,150),BDARK)
for i in range(12):
    x=7+i*25;rect(d,(x,131,x+17,148),DEEP);d.line((x+1,147,x+1,132,x+16,132),fill=INK);d.line((x+1,148,x+17,148,x+17,132),fill=HI)
for row in range(4):
    for col in range(9):
        x=7+col*18;y=(153+row*18 if row<3 else 213);rect(d,(x,y,x+17,y+17),MID);d.line((x,y+16,x,y,x+16,y),fill=BDARK);d.line((x+1,y+17,x+17,y+17,x+17,y+1),fill=HI)
for x,y in ((3,3),(314,3),(3,231),(314,231)):d.point((x,y),fill=LIGHT)
save(im,'gui','space_console')

# This manifest is an inventory, not a claim about runtime rendering.
out=ROOT/'docs/art';out.mkdir(parents=True,exist_ok=True)
manifest={p.relative_to(ART).as_posix():{'size':list(Image.open(p).size),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in WRITTEN}
(out/'space-art-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
print('Wrote',len(WRITTEN),'original space textures')
