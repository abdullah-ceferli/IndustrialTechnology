from pathlib import Path
from html.parser import HTMLParser
from urllib.request import Request, build_opener, HTTPCookieProcessor
from http.cookiejar import CookieJar
from urllib.parse import urljoin
from PIL import Image
import io
import json

source = 'https://superminecraftskins.com/other/casual-outfit'
stage = Path(__file__).resolve().parents[1] / 'build/skins/casual-outfit'
stage.mkdir(parents=True, exist_ok=True)
opener = build_opener(HTTPCookieProcessor(CookieJar()))
class Links(HTMLParser):
    def __init__(self):
        super().__init__()
        self.links = []
    def handle_starttag(self, tag, attrs):
        for key, value in attrs:
            if value and key in ('href', 'src', 'rel') and ('.png' in value.lower() or 'download' in value.lower() or 'minecraft.net' in value.lower()):
                self.links.append(urljoin(source, value))

def fetch(url):
    request = Request(url, headers={'User-Agent': 'Mozilla/5.0', 'Referer': source + '/download'})
    with opener.open(request, timeout=25) as response:
        data = response.read(1024 * 1024 + 1)
        if len(data) > 1024 * 1024:
            raise ValueError('Unexpectedly large response')
        return data, response.geturl(), {key:value for key,value in response.headers.items() if key.lower() in ('content-type','content-disposition','location','refresh')}

fetch(source + '/download')
data, final_url, headers = fetch(source + '/install')
if data.startswith(b'\x89PNG\r\n\x1a\n'):
    image = Image.open(io.BytesIO(data))
    image.load()
    print(json.dumps({'downloaded_dimensions': image.size, 'bytes': len(data)}))
    if image.size not in ((64,32),(64,64)):
        (stage / 'website-texture-preview.png').write_bytes(data)
        raise SystemExit('Saved original website preview for inspection; no game files changed.')
    assert len(data) <= 24576
    path = stage / 'casual_outfit.png'
    path.write_bytes(data)
    (stage / 'SOURCE.json').write_text(json.dumps({'page': source, 'download': final_url, 'size': image.size}, indent=2))
    print(json.dumps({'saved': str(path), 'size': image.size, 'bytes': len(data)}))
else:
    page = data.decode('utf-8', errors='replace')
    (stage / 'download-page.html').write_text(page, encoding='utf-8')
    parser = Links()
    parser.feed(page)
    print(json.dumps({'page': final_url, 'headers': headers, 'links': list(dict.fromkeys(parser.links))}, indent=2))
