"""Original classic 64x32 Minecraft artwork and a mesh-rendered preview.

Drawn from scratch for the user's white-shirt/red-tie reference. No pixels are
copied from the attached screenshot. UVs follow vanilla 1.7.10 ModelBiped/ModelBox.
"""
from pathlib import Path
from math import sin, cos, radians
from PIL import Image, ImageDraw, ImageFont
import random

root = Path(__file__).resolve().parents[1]
out = root / 'build' / 'skins' / 'normal-guy'
out.mkdir(parents=True, exist_ok=True)
skin = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
d = ImageDraw.Draw(skin)

hair = (81, 54, 34, 255)
hair_dark = (63, 42, 29, 255)
hair_light = (102, 70, 44, 255)
face = (211, 173, 125, 255)
face_light = (222, 185, 138, 255)
face_shadow = (193, 150, 103, 255)
shirt = (224, 226, 226, 255)
shirt_light = (242, 242, 237, 255)
shirt_shadow = (184, 191, 195, 255)
vest = (32, 35, 39, 255)
vest_light = (43, 47, 52, 255)
pants = (29, 32, 37, 255)
shoe = (16, 18, 21, 255)
red = (175, 33, 38, 255)
red_light = (206, 47, 48, 255)

def rect(x, y, w, h, color):
    d.rectangle((x, y, x + w - 1, y + h - 1), fill=color)

def hair_panel(x, y, w, h, seed):
    rng = random.Random(seed)
    for j in range(h):
        for i in range(w):
            n = rng.randrange(10)
            color = hair_light if n == 0 else hair_dark if n == 1 else hair
            skin.putpixel((x+i, y+j), color)

# Head base, with a clean, soft brown fringe and no beard or accessories.
hair_panel(8, 0, 8, 8, 10)
rect(16, 0, 8, 8, face_shadow)
for x in (0, 8, 16, 24):
    rect(x, 8, 8, 8, face)
    hair_panel(x, 8, 8, 3 if x == 8 else 4, 20+x)
hair_panel(24, 8, 8, 7, 99)
rect(24, 15, 8, 1, hair_dark)
# Ears and sideburns.
rect(0, 11, 2, 4, hair_dark)
rect(22, 11, 2, 4, hair_dark)
rect(3, 12, 2, 2, face_shadow)
rect(19, 12, 2, 2, face_shadow)
# Front face: eight pixels across, brown fringe, blue-gray eyes, neutral mouth.
rect(8, 11, 8, 5, face)
rect(8, 10, 8, 1, face_light)
for x in (8, 9, 10, 13, 14, 15):
    skin.putpixel((x, 10), hair)
skin.putpixel((8, 11), hair_dark)
skin.putpixel((15, 11), hair_dark)
rect(9, 12, 2, 1, (234, 232, 218, 255))
rect(13, 12, 2, 1, (234, 232, 218, 255))
skin.putpixel((10, 12), (85, 112, 122, 255))
skin.putpixel((13, 12), (85, 112, 122, 255))
rect(11, 13, 2, 1, (204, 163, 113, 255))
rect(11, 14, 2, 1, (170, 126, 86, 255))
rect(9, 15, 6, 1, (202, 164, 117, 255))

# Thin hair layer, keeping all hat-area pixels below the fringe transparent.
hair_panel(40, 0, 8, 8, 40)
hair_panel(32, 8, 8, 3, 43)
hair_panel(48, 8, 8, 3, 45)
hair_panel(56, 8, 8, 7, 47)
hair_panel(40, 8, 8, 1, 49)
for x in (40, 41, 42, 44, 45, 46, 47):
    skin.putpixel((x, 9), hair if x % 2 else hair_light)
for x in (40, 41, 46, 47):
    skin.putpixel((x, 10), hair)

# White short sleeves and a charcoal waistcoat over a white shirt.
rect(16, 20, 4, 12, vest)
rect(28, 20, 4, 12, vest)
rect(32, 20, 8, 12, vest)
rect(33, 21, 6, 9, vest_light)
rect(36, 22, 1, 8, (37, 40, 45, 255))
rect(20, 16, 8, 4, vest)
rect(22, 16, 4, 4, shirt)
rect(28, 16, 8, 4, vest)
rect(20, 20, 8, 12, shirt)
rect(20, 20, 2, 12, vest)
rect(26, 20, 2, 12, vest)
rect(20, 21, 1, 9, vest_light)
rect(27, 21, 1, 9, (24, 27, 31, 255))
rect(23, 20, 2, 1, face_shadow)
skin.putpixel((22, 21), shirt_light)
skin.putpixel((25, 21), shirt_light)
rect(23, 21, 2, 2, red)
rect(23, 23, 2, 7, red)
rect(23, 23, 1, 6, red_light)
skin.putpixel((24, 30), red)
rect(20, 31, 8, 1, (23, 25, 29, 255))

# Classic limbs share a texture and are mirrored by the vanilla renderer.
for x in (40, 44, 48, 52):
    rect(x, 20, 4, 12, face)
    rect(x, 20, 4, 4, shirt)
    rect(x, 24, 4, 1, vest)
    rect(x, 25, 1, 7, face_shadow)
    rect(x+3, 25, 1, 7, face_light)
rect(44, 20, 4, 1, shirt_shadow)
rect(45, 21, 2, 2, shirt_light)
rect(40, 20, 4, 4, shirt_shadow)
rect(52, 20, 4, 4, (206, 211, 213, 255))
rect(44, 16, 4, 4, shirt)
rect(48, 16, 4, 4, face_shadow)
rect(44, 31, 4, 1, (201, 162, 116, 255))

for x in (0, 4, 8, 12):
    rect(x, 20, 4, 12, pants)
    rect(x, 20, 1, 10, (24, 27, 32, 255))
    rect(x+1, 21, 2, 8, (34, 37, 42, 255))
    rect(x, 30, 4, 2, shoe)
    rect(x, 31, 4, 1, (12, 14, 17, 255))
rect(4, 16, 4, 4, pants)
rect(8, 16, 4, 4, shoe)
rect(5, 30, 2, 1, (28, 31, 36, 255))

path = out / 'normal_guy.png'
skin.save(path, optimize=True)
assert skin.size == (64, 32) and path.stat().st_size < 24576

# Orthographic, textured cube renderer for the newly authored skin.
preview = Image.new('RGB', (900, 690), '#1d2635')
draw = ImageDraw.Draw(preview)
for y in range(690):
    shade = int(9 * y / 690)
    draw.line((0,y,899,y), fill=(29-shade,38-shade,53-shade))

font_path = 'C:/Windows/Fonts/segoeui.ttf'
bold_path = 'C:/Windows/Fonts/segoeuib.ttf'
title_font = ImageFont.truetype(bold_path, 30)
label_font = ImageFont.truetype(bold_path, 19)
small_font = ImageFont.truetype(font_path, 17)
draw.text((450, 28), 'NORMAL GUY', fill='#eef0f3', font=title_font, anchor='mt')
draw.text((450, 72), 'Brown hair  /  white shirt  /  red tie  /  charcoal waistcoat', fill='#b9c3d2', font=small_font, anchor='mt')

parts = [((-4,-8,-4),(8,8,8),0,0,8,8,8),
         ((-4,0,-2),(8,12,4),16,16,8,12,4),
         ((-8,0,-2),(4,12,4),40,16,4,12,4),
         ((4,0,-2),(4,12,4),40,16,4,12,4),
         ((-4,12,-2),(4,12,4),0,16,4,12,4),
         ((0,12,-2),(4,12,4),0,16,4,12,4),
         ((-4.25,-8.25,-4.25),(8.5,8.5,8.5),32,0,8,8,8)]

def render(center, yaw_degrees, label):
    yaw, elevation = radians(yaw_degrees), radians(15)
    scale = 12.7
    def project(p):
        x,y,z = p
        depth = x*sin(yaw)+z*cos(yaw)
        return (center+(x*cos(yaw)-z*sin(yaw))*scale,
                255+(y*cos(elevation)-depth*sin(elevation))*scale)
    def depth(p):
        return (p[0]*sin(yaw)+p[2]*cos(yaw))*cos(elevation)+p[1]*sin(elevation)
    camera = (-sin(yaw)*cos(elevation),-sin(elevation),-cos(yaw)*cos(elevation))
    pixels=[]
    for (x,y,z),(w,h,dep),u,v,tw,th,td in parts:
        faces = [((0,0,-1),(u+td,v+td,tw,th),lambda a,b:(x+a*w,y+b*h,z),1.0),
                 ((0,0,1),(u+td+tw+td,v+td,tw,th),lambda a,b:(x+(1-a)*w,y+b*h,z+dep),.9),
                 ((-1,0,0),(u,v+td,td,th),lambda a,b:(x,y+b*h,z+(1-a)*dep),.78),
                 ((1,0,0),(u+td+tw,v+td,td,th),lambda a,b:(x+w,y+b*h,z+a*dep),.84),
                 ((0,-1,0),(u+td,v,tw,td),lambda a,b:(x+a*w,y,z+(1-b)*dep),1.08)]
        for normal,(tx,ty,fw,fh),point,shade in faces:
            if sum(a*b for a,b in zip(normal,camera)) <= 0: continue
            for py in range(fh):
                for px in range(fw):
                    color=skin.getpixel((tx+px,ty+py))
                    if color[3] < 128: continue
                    poly=[point(px/fw,py/fh),point((px+1)/fw,py/fh),point((px+1)/fw,(py+1)/fh),point(px/fw,(py+1)/fh)]
                    distance=sum(depth(p) for p in poly)/4
                    rgb=tuple(min(255,int(c*shade)) for c in color[:3])
                    pixels.append((distance,[project(p) for p in poly],rgb))
    draw.ellipse((center-103,537,center+103,570),fill='#101620')
    for _,points,color in sorted(pixels,key=lambda p:p[0],reverse=True):
        draw.polygon(points,fill=color)
    draw.text((center,592),label,fill='#dde3ec',font=label_font,anchor='mt')

render(260,25,'FRONT')
render(655,205,'BACK')
draw.text((450,646),'Minecraft 1.7.10  •  Classic Steve model  •  64 × 32 PNG',fill='#9ba9bc',font=small_font,anchor='mt')
preview.save(out/'normal_guy_preview.png')
print(path)
print(out/'normal_guy_preview.png')
