"""
Build a self-contained HTML file that previews the frozen Quran corpus
rendered with our two candidate Mushaf fonts (KFGQPC Uthmanic Hafs v2.2
and Amiri Quran 1.003).

Renders the same 7 mushaf pages that TASK-026's Android prototype must
render (pages 1, 2, 3, 50, 300, 600, 604) so that the visual side-by-side
comparison with a printed Madani mushaf reference can happen NOW, without
any Android tooling.

Output: prototype/font-preview/index.html (open directly in a browser).

Fonts are embedded as base64 data URIs so the file is fully offline / portable.
"""

from __future__ import annotations

import base64
import json
import pathlib
import sys
import textwrap

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT = pathlib.Path(__file__).resolve().parents[2]
CORPUS = ROOT / "data" / "output" / "intermediate" / "corpus.json"
INDICES = ROOT / "data" / "output" / "intermediate" / "indices.json"
KFGQPC = ROOT / "data" / "sources" / "fonts" / "kfgqpc-uthman-taha" / "uthmanic_hafs_v22.ttf"
AMIRI = ROOT / "data" / "sources" / "fonts" / "amiri-quran" / "AmiriQuran.ttf"

TARGET_PAGES = [1, 2, 3, 50, 300, 600, 604]


def _b64(path: pathlib.Path) -> str:
    return base64.b64encode(path.read_bytes()).decode("ascii")


def _surah_of(gid: int, surah_ranges: list[tuple[int, int, int]]) -> int:
    # binary-search-free; N=114
    for s_num, first, last in surah_ranges:
        if first <= gid <= last:
            return s_num
    raise ValueError(gid)


def build() -> None:
    corpus = json.loads(CORPUS.read_bytes())
    indices = json.loads(INDICES.read_bytes())

    surah_ranges = []
    for s in indices["surahs"]:
        first = s["first_ayah_global_index"]
        last = first + s["ayah_count"] - 1
        surah_ranges.append((s["number"], first, last))

    ayah_by_gid = {c["global_index"]: c for c in corpus}
    surah_meta = {s["number"]: s for s in indices["surahs"]}

    # Build per-page render data
    pages_data = []
    for p in indices["pages"]:
        if p["number"] not in TARGET_PAGES:
            continue
        # Group ayahs by surah so we can insert surah headings + bismillahs
        groups: list[dict] = []
        cur_group: dict | None = None
        for gid in range(p["first_ayah_global_index"], p["last_ayah_global_index"] + 1):
            a = ayah_by_gid[gid]
            s_num = a["surah"]
            if cur_group is None or cur_group["surah"] != s_num:
                s = surah_meta[s_num]
                cur_group = {
                    "surah": s_num,
                    "surah_name_ar": s["name_ar"],
                    "surah_name_translit_en": s.get("name_translit_en", ""),
                    "surah_name_translation_en": s.get("name_translation_en", ""),
                    "surah_starts_on_this_page": (a["ayah"] == 1),
                    "bismillah_pre": s["bismillah_pre"],
                    "ayahs": [],
                }
                groups.append(cur_group)
            cur_group["ayahs"].append({"n": a["ayah"], "text": a["text_uthmani"]})
        pages_data.append({
            "number": p["number"],
            "juz_starts_on_page": p["juz_starts_on_page"],
            "groups": groups,
        })

    # Font metadata for the credits line
    kfgqpc_sha = _sha(KFGQPC)[:16]
    amiri_sha = _sha(AMIRI)[:16]

    kfgqpc_b64 = _b64(KFGQPC)
    amiri_b64 = _b64(AMIRI)

    html = _render_html(pages_data, kfgqpc_b64, amiri_b64, kfgqpc_sha, amiri_sha)
    out = pathlib.Path(__file__).parent / "index.html"
    out.write_bytes(html.encode("utf-8"))
    print(f"wrote {out.relative_to(ROOT)}  ({out.stat().st_size:,} bytes)")
    print(f"  pages: {[p['number'] for p in pages_data]}")
    print(f"  KFGQPC sha256[:16] = {kfgqpc_sha}")
    print(f"  Amiri  sha256[:16] = {amiri_sha}")


def _sha(path: pathlib.Path) -> str:
    import hashlib
    return hashlib.sha256(path.read_bytes()).hexdigest()


def _render_html(pages, kfgqpc_b64, amiri_b64, kfgqpc_sha, amiri_sha) -> str:
    pages_json = json.dumps(pages, ensure_ascii=False, separators=(",", ":"))
    body = f"""<!DOCTYPE html>
<html dir="rtl" lang="ar">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width,initial-scale=1" />
<title>Digital Mushaf — Font Preview</title>
<style>
  @font-face {{
    font-family: 'KFGQPC';
    src: url(data:font/ttf;base64,{kfgqpc_b64}) format('truetype');
    font-display: block;
  }}
  @font-face {{
    font-family: 'AmiriQuran';
    src: url(data:font/ttf;base64,{amiri_b64}) format('truetype');
    font-display: block;
  }}
  :root {{
    --paper: #fdfcf7;
    --page-bg: #fbf7ec;
    --ink: #1e1e1e;
    --muted: #8a8266;
    --accent: #7a5a2e;
    --controls-bg: #ede7d8;
  }}
  * {{ box-sizing: border-box; }}
  html, body {{ margin: 0; padding: 0; background: var(--paper); color: var(--ink); }}
  body {{
    font-family: 'KFGQPC', 'AmiriQuran', serif;
    font-size: 34px;
    line-height: 2.4;
    padding: 0 0 80px 0;
  }}
  header {{
    position: sticky; top: 0; z-index: 10;
    background: var(--controls-bg);
    border-bottom: 1px solid #d5cdb4;
    padding: 12px 20px;
    display: flex; flex-wrap: wrap; gap: 16px; align-items: center;
    direction: ltr;
    font-family: -apple-system, "Segoe UI", Roboto, sans-serif;
    font-size: 13px; color: #3a3a3a;
  }}
  header .title {{ font-weight: 600; margin-right: auto; }}
  header label {{ display: inline-flex; align-items: center; gap: 6px; }}
  header select, header input[type=range] {{ font: inherit; }}
  header .credits {{ opacity: 0.7; font-size: 11px; }}
  .page {{
    max-width: 780px;
    margin: 30px auto;
    background: var(--page-bg);
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.10), 0 0 0 1px #e2dabf inset;
    padding: 44px 40px 44px 40px;
  }}
  .page-hdr {{
    direction: ltr; font-family: -apple-system, "Segoe UI", Roboto, sans-serif;
    text-align: center; font-size: 12px; color: var(--muted);
    letter-spacing: 0.08em; text-transform: uppercase;
    margin-bottom: 24px;
  }}
  .surah-hdr {{
    text-align: center;
    padding: 14px 20px;
    margin: 8px 0 20px 0;
    border: 1px solid #c9bd97;
    border-radius: 6px;
    background: linear-gradient(180deg, rgba(122,90,46,0.06), rgba(122,90,46,0.02));
    color: var(--accent);
    font-size: 32px;
    line-height: 1.4;
  }}
  .surah-hdr .translit {{
    display: block;
    font-family: -apple-system, "Segoe UI", Roboto, sans-serif;
    direction: ltr; font-size: 12px; color: var(--muted);
    letter-spacing: 0.1em; text-transform: uppercase; margin-top: 6px;
  }}
  .bismillah {{ text-align: center; font-size: 32px; margin: 6px 0 18px 0; }}
  .ayahs {{ text-align: justify; text-align-last: center; hyphens: none; }}
  .ayah {{ display: inline; }}
  .ayah-num {{
    display: inline-block;
    font-family: 'KFGQPC', serif;
    color: var(--accent);
    padding: 0 4px;
    margin: 0 4px;
    font-size: 0.7em;
    line-height: 1;
    border: 1px solid var(--accent);
    border-radius: 50%;
    min-width: 1.6em; min-height: 1.6em;
    text-align: center; vertical-align: middle;
    background: rgba(122,90,46,0.06);
  }}
  footer {{
    max-width: 780px; margin: 0 auto; padding: 20px;
    direction: ltr; font-family: -apple-system, "Segoe UI", Roboto, sans-serif;
    font-size: 12px; color: var(--muted);
  }}
</style>
</head>
<body>
<header>
  <div class="title">Digital Mushaf — Font Preview (TASK-025 / ADR-0024)</div>
  <label>Font:
    <select id="font">
      <option value="KFGQPC" selected>KFGQPC Uthmanic Hafs v2.2 (primary)</option>
      <option value="AmiriQuran">Amiri Quran v1.003 (alternate)</option>
    </select>
  </label>
  <label>Size: <input id="size" type="range" min="22" max="56" value="34" />
    <span id="size-val">34px</span>
  </label>
  <label>Line height: <input id="lh" type="range" min="150" max="360" value="240" step="10" />
    <span id="lh-val">2.40</span>
  </label>
  <div class="credits">
    KFGQPC {kfgqpc_sha}… · Amiri {amiri_sha}…
  </div>
</header>
<main id="pages"></main>
<footer>
  Content v1.0.0 · pages 1, 2, 3, 50, 300, 600, 604 · corpus SHA-256 in
  <code>data/output/intermediate/tanzil-corpus.sha256</code>.
  Compare each page to a printed Madani mushaf; report defects as TASK-026 acceptance evidence.
</footer>
<script>
const PAGES = {pages_json};

function render() {{
  const root = document.getElementById('pages');
  root.innerHTML = '';
  for (const p of PAGES) {{
    const el = document.createElement('section');
    el.className = 'page';
    const juz = p.juz_starts_on_page.length
      ? ` · Juz ${{p.juz_starts_on_page.join(', ')}} starts here`
      : '';
    el.innerHTML = `<div class="page-hdr">Page ${{p.number}} of 604${{juz}}</div>`;
    for (const g of p.groups) {{
      if (g.surah_starts_on_this_page) {{
        const h = document.createElement('div');
        h.className = 'surah-hdr';
        h.innerHTML = `${{g.surah_name_ar}} · سورة ${{g.surah}}
          <span class="translit">${{g.surah_name_translit_en}} — ${{g.surah_name_translation_en}}</span>`;
        el.appendChild(h);
        if (g.bismillah_pre && g.surah !== 1) {{
          const b = document.createElement('div');
          b.className = 'bismillah';
          b.textContent = 'بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ';
          el.appendChild(b);
        }}
      }}
      const block = document.createElement('div');
      block.className = 'ayahs';
      block.innerHTML = g.ayahs.map(a =>
        `<span class="ayah">${{a.text}}<span class="ayah-num">${{a.n}}</span></span>`
      ).join(' ');
      el.appendChild(block);
    }}
    root.appendChild(el);
  }}
}}

function applyFont() {{
  document.body.style.fontFamily = `'${{document.getElementById('font').value}}', serif`;
}}
function applySize() {{
  const v = document.getElementById('size').value;
  document.body.style.fontSize = v + 'px';
  document.getElementById('size-val').textContent = v + 'px';
}}
function applyLh() {{
  const v = document.getElementById('lh').value / 100;
  document.body.style.lineHeight = v;
  document.getElementById('lh-val').textContent = v.toFixed(2);
}}
document.getElementById('font').addEventListener('change', applyFont);
document.getElementById('size').addEventListener('input', applySize);
document.getElementById('lh').addEventListener('input', applyLh);

render();
applyFont(); applySize(); applyLh();
</script>
</body>
</html>
"""
    return body


if __name__ == "__main__":
    build()
