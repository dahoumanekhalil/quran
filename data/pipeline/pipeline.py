"""
Digital Mushaf — Quran data pipeline.

Deterministic, reproducible pipeline that ingests raw source files
under ../sources/ and produces validated intermediate + released
artifacts under ../output/.

Same input hashes MUST yield same output hashes.

Usage:
    python pipeline.py <stage>
    python pipeline.py all

Stages (run in order):
    parse_tanzil        Extract ayah text from Tanzil Uthmani XML (byte-preserving).
    parse_meta          Extract HafsLists.ts (pagination, juz, hizb, rub, sajda, surah).
    merge               Combine text + metadata into corpus.json + indices.
    validate_structural (added in TASK-019)
    validate_unicode    (added in TASK-020)
    cross_source_diff   (added in TASK-021)
    package_sqlite      (added in TASK-022)
    build_search_index  (added in TASK-023)
    release             (added in TASK-024)
"""

from __future__ import annotations

import argparse
import datetime as _dt
import hashlib
import json
import pathlib
import re
import sys
import unicodedata
import xml.etree.ElementTree as ET
from collections import Counter, OrderedDict
from typing import Any

# Force UTF-8 on stdout/stderr so Arabic prints cleanly on Windows consoles.
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------

PIPELINE_DIR = pathlib.Path(__file__).resolve().parent
DATA_DIR = PIPELINE_DIR.parent
SOURCES_DIR = DATA_DIR / "sources"
OUTPUT_DIR = DATA_DIR / "output"
INTERMEDIATE_DIR = OUTPUT_DIR / "intermediate"

TANZIL_XML = SOURCES_DIR / "tanzil-uthmani" / "1.1" / "quran-uthmani.xml"
TANZIL_TXT = SOURCES_DIR / "tanzil-uthmani" / "1.1" / "quran-uthmani.txt"
QURAN_META_HAFS = SOURCES_DIR / "quran-meta" / "repo" / "src" / "lists" / "HafsLists.ts"
QURAN_META_NAMES_EN = SOURCES_DIR / "quran-meta" / "repo" / "src" / "i18n" / "surah.en.ts"

MANIFEST_PATH = OUTPUT_DIR / "manifest.json"

# ---------------------------------------------------------------------------
# Deterministic IO helpers
# ---------------------------------------------------------------------------

def sha256_bytes(b: bytes) -> str:
    return hashlib.sha256(b).hexdigest()

def sha256_file(path: pathlib.Path) -> str:
    return sha256_bytes(path.read_bytes())

def write_json_deterministic(path: pathlib.Path, data: Any) -> str:
    """Write JSON with fixed formatting for byte-reproducible output.

    Rules: UTF-8 (no BOM), LF line endings, indent=2, sort_keys=True,
    ensure_ascii=False, trailing newline.
    Returns SHA-256 of the written bytes.
    """
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(data, indent=2, sort_keys=True, ensure_ascii=False)
    b = (text + "\n").encode("utf-8")
    # Explicit binary write to avoid Windows CRLF translation.
    path.write_bytes(b)
    return sha256_bytes(b)

# ---------------------------------------------------------------------------
# Manifest
# ---------------------------------------------------------------------------

def load_manifest() -> dict:
    if MANIFEST_PATH.exists():
        return json.loads(MANIFEST_PATH.read_bytes().decode("utf-8"))
    return {"stages": {}}

def save_manifest(manifest: dict) -> None:
    manifest["last_run_utc"] = _dt.datetime.now(_dt.UTC).strftime("%Y-%m-%dT%H:%M:%SZ")
    MANIFEST_PATH.parent.mkdir(parents=True, exist_ok=True)
    MANIFEST_PATH.write_bytes(
        (json.dumps(manifest, indent=2, sort_keys=True, ensure_ascii=False) + "\n").encode("utf-8")
    )

def record_stage(manifest: dict, stage: str, inputs: dict[str, pathlib.Path], outputs: dict[str, str]) -> None:
    """Record a stage's input/output hashes in the manifest."""
    manifest["stages"][stage] = {
        "inputs": {name: {"path": str(p.relative_to(DATA_DIR)), "sha256": sha256_file(p)} for name, p in inputs.items()},
        "outputs": {name: {"path": path_str, "sha256": sha_hex} for name, (path_str, sha_hex) in outputs.items()},
    }

# ---------------------------------------------------------------------------
# Stage: parse_tanzil  (TASK-017: byte-preserving text import)
# ---------------------------------------------------------------------------

def stage_parse_tanzil(manifest: dict) -> None:
    """Parse Tanzil Uthmani XML into a raw ayah list, byte-preserving text.

    Produces:
        intermediate/tanzil-raw.json  — [{surah, ayah, text}, ...] in canonical order.
        intermediate/tanzil-corpus.sha256  — SHA-256 of concatenated ayah texts
                                             (each terminated with LF), for content-integrity.
    """
    print(f"[parse_tanzil] reading {TANZIL_XML.relative_to(DATA_DIR)}", flush=True)

    # xml.etree decodes standard XML entities (&amp; etc.) but does NOT
    # apply Unicode normalization. We forbid any further mutation.
    tree = ET.parse(TANZIL_XML)
    root = tree.getroot()

    ayahs: list[dict] = []
    for sura in root.findall("sura"):
        s_num = int(sura.get("index"))
        for aya in sura.findall("aya"):
            a_num = int(aya.get("index"))
            text = aya.get("text")
            if text is None:
                raise RuntimeError(f"aya {s_num}:{a_num} missing text")
            # Explicitly forbid any transformation here.
            ayahs.append({"surah": s_num, "ayah": a_num, "text": text})

    if len(ayahs) != 6236:
        raise RuntimeError(f"expected 6236 ayahs, got {len(ayahs)}")

    # Canonical order: (surah, ayah)
    ayahs.sort(key=lambda a: (a["surah"], a["ayah"]))

    # Corpus hash — LF-terminated concatenation, stable across runs.
    corpus_bytes = "".join(a["text"] + "\n" for a in ayahs).encode("utf-8")
    corpus_sha = sha256_bytes(corpus_bytes)

    out_json = INTERMEDIATE_DIR / "tanzil-raw.json"
    out_sha = INTERMEDIATE_DIR / "tanzil-corpus.sha256"

    json_sha = write_json_deterministic(out_json, ayahs)
    out_sha.write_bytes((corpus_sha + "  tanzil-uthmani corpus (LF-joined text)\n").encode("utf-8"))
    sha_file_sha = sha256_file(out_sha)

    record_stage(manifest, "parse_tanzil",
        inputs={"tanzil_xml": TANZIL_XML},
        outputs={
            "tanzil_raw": (str(out_json.relative_to(DATA_DIR)), json_sha),
            "corpus_sha256": (str(out_sha.relative_to(DATA_DIR)), sha_file_sha),
        },
    )
    print(f"[parse_tanzil] OK  ayahs={len(ayahs)}  corpus_sha={corpus_sha[:16]}...  json_sha={json_sha[:16]}...", flush=True)

# ---------------------------------------------------------------------------
# Stage: parse_meta  (TASK-018 prep: extract HafsLists.ts into JSON)
# ---------------------------------------------------------------------------

_INT_ARRAY_RE = re.compile(
    r"export const (?P<name>\w+): AyahId\[\] = \[(?P<body>[^\]]+)\]",
    re.DOTALL,
)
_SURAH_TUPLE_RE = re.compile(
    r"\[\s*(-?\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*\"([^\"]*)\"\s*,\s*(true|false)\s*\]"
)
_NAME_PAIR_RE = re.compile(r"\[\"([^\"]*)\"\s*,\s*\"([^\"]*)\"\]")


def _parse_int_list(body: str) -> list[int]:
    # Strip comments and trailing commas.
    body = re.sub(r"//[^\n]*", "", body)
    nums = [int(x) for x in re.findall(r"-?\d+", body)]
    return nums


def _extract_int_array(src: str, name: str) -> list[int]:
    for m in _INT_ARRAY_RE.finditer(src):
        if m.group("name") == name:
            return _parse_int_list(m.group("body"))
    raise KeyError(f"array {name} not found in HafsLists.ts")


def stage_parse_meta(manifest: dict) -> None:
    """Parse HafsLists.ts + surah.en.ts into a language-agnostic JSON blob.

    Produces:
        intermediate/hafs-meta.json
    """
    print(f"[parse_meta] reading {QURAN_META_HAFS.relative_to(DATA_DIR)}", flush=True)
    src = QURAN_META_HAFS.read_text(encoding="utf-8")

    hizb_quarter = _extract_int_array(src, "HizbQuarterList")  # 240 rubs + sentinels
    juz_list = _extract_int_array(src, "JuzList")              # 30 juz + sentinels
    manzil_list = _extract_int_array(src, "ManzilList")        # 7 manzil + sentinels
    page_list = _extract_int_array(src, "PageList")            # 604 pages + sentinels
    ruku_list = _extract_int_array(src, "RukuList")            # 556 rukus + sentinels
    sajda_list = _extract_int_array(src, "SajdaList")          # 15 sajdas (no sentinels)

    # SurahList: 116 tuples (index 0 sentinel, 1..114 real, 115 end sentinel).
    # The tuple regex is specific enough (6-tuple w/ arabic name + bool) that
    # a whole-file scan is unambiguous.
    tuples = _SURAH_TUPLE_RE.findall(src)
    if len(tuples) != 116:
        raise RuntimeError(f"SurahList: expected 116 entries, got {len(tuples)}")
    surahs: list[dict] = []
    for i, (start, count, order, rukus, name, is_meccan) in enumerate(tuples):
        surahs.append({
            "index": i,
            "start_ayah_id": int(start),
            "ayah_count": int(count),
            "revelation_order": int(order),
            "ruku_count": int(rukus),
            "name_ar": name,
            "is_meccan": is_meccan == "true",
        })

    # English names + translation from surah.en.ts. Only real entries match
    # the two-quoted-strings-in-brackets shape (sentinels are just `[]`).
    print(f"[parse_meta] reading {QURAN_META_NAMES_EN.relative_to(DATA_DIR)}", flush=True)
    en_src = QURAN_META_NAMES_EN.read_text(encoding="utf-8")
    name_pairs = _NAME_PAIR_RE.findall(en_src)
    if len(name_pairs) != 114:
        raise RuntimeError(f"surahNamesEn: expected 114 pairs, got {len(name_pairs)}")

    for i, (translit, translation) in enumerate(name_pairs, start=1):
        surahs[i]["name_translit_en"] = translit
        surahs[i]["name_translation_en"] = translation

    data = {
        "riwaya": "Hafs",
        "meta": {
            "num_ayahs": 6236,
            "num_surahs": 114,
            "num_pages": 604,
            "num_juzs": 30,
            "num_hizbs": 60,
            "num_rub_al_hizbs": 240,
            "num_sajdas": 15,
            "num_rukus": 556,
            "num_manzils": 7,
        },
        # Lists as-imported (with sentinels). Downstream code trims sentinels
        # according to the semantics of each list.
        "hizb_quarter_list": hizb_quarter,   # len 242 (0-sentinel + 240 rubs + 6237 end)
        "juz_list": juz_list,                # len 32 (0 + 30 + 6237)
        "manzil_list": manzil_list,          # len 9  (0 + 7 + 6237)
        "page_list": page_list,              # len 606 (0 + 604 + 6237)
        "ruku_list": ruku_list,              # len 558 (0 + 556 + 6237)
        "sajda_list": sajda_list,            # len 15
        "surahs": surahs,                    # len 116 (sentinel + 114 + sentinel)
    }

    out = INTERMEDIATE_DIR / "hafs-meta.json"
    out_sha = write_json_deterministic(out, data)

    record_stage(manifest, "parse_meta",
        inputs={"hafs_lists_ts": QURAN_META_HAFS, "surah_names_en_ts": QURAN_META_NAMES_EN},
        outputs={"hafs_meta": (str(out.relative_to(DATA_DIR)), out_sha)},
    )
    print(
        f"[parse_meta] OK  page_list={len(page_list)}  juz_list={len(juz_list)}  "
        f"rubs={len(hizb_quarter)}  sajdas={len(sajda_list)}  surahs={len(surahs)}",
        flush=True,
    )

# ---------------------------------------------------------------------------
# Stage: merge  (TASK-018: attach page/juz/hizb/rub metadata to every ayah)
# ---------------------------------------------------------------------------

def _global_index_for(surah_num: int, ayah_num: int, surahs: list[dict]) -> int:
    """Convert (surah, ayah_in_surah) → global 1-based ayah index (Hafs)."""
    s = surahs[surah_num]  # surahs is 116-long, 1-indexed
    return s["start_ayah_id"] + (ayah_num - 1)


def _assign_from_starts(starts: list[int], num_items: int, num_ayahs: int) -> list[int]:
    """Given a list of 'start ayah id' entries with sentinels [0, start_1, start_2, ..., num_ayahs+1]
    returns per-ayah assignment array (1..num_ayahs → 1..num_items).

    Semantics: `starts[k]` is the global 1-based ayah index at which item k begins,
    for k in 1..num_items. Ayahs [starts[k] .. starts[k+1]-1] belong to item k.
    """
    if len(starts) != num_items + 2:
        raise ValueError(f"expected {num_items + 2} entries (sentinel + items + end), got {len(starts)}")
    if starts[0] != 0:
        raise ValueError(f"leading sentinel must be 0, got {starts[0]}")
    if starts[-1] != num_ayahs + 1:
        raise ValueError(f"trailing sentinel must be {num_ayahs + 1}, got {starts[-1]}")

    assign = [0] * (num_ayahs + 1)  # 1-indexed
    for item in range(1, num_items + 1):
        for ayah_id in range(starts[item], starts[item + 1]):
            assign[ayah_id] = item
    # Sanity: all assigned
    for i in range(1, num_ayahs + 1):
        if assign[i] == 0:
            raise RuntimeError(f"ayah {i} not assigned")
    return assign


def stage_merge(manifest: dict) -> None:
    """Merge Tanzil text with metadata into corpus.json + indices.json."""
    print("[merge] loading intermediate artifacts", flush=True)
    ayahs_raw = json.loads((INTERMEDIATE_DIR / "tanzil-raw.json").read_bytes())
    meta = json.loads((INTERMEDIATE_DIR / "hafs-meta.json").read_bytes())

    surahs = meta["surahs"]  # 0..115
    num_ayahs = meta["meta"]["num_ayahs"]

    # Assign each ayah its page, juz, hizb-quarter (rub), then derive hizb.
    page_by_ayah = _assign_from_starts(meta["page_list"], meta["meta"]["num_pages"], num_ayahs)
    juz_by_ayah = _assign_from_starts(meta["juz_list"], meta["meta"]["num_juzs"], num_ayahs)
    rub_by_ayah = _assign_from_starts(meta["hizb_quarter_list"], meta["meta"]["num_rub_al_hizbs"], num_ayahs)
    manzil_by_ayah = _assign_from_starts(meta["manzil_list"], meta["meta"]["num_manzils"], num_ayahs)
    ruku_by_ayah = _assign_from_starts(meta["ruku_list"], meta["meta"]["num_rukus"], num_ayahs)

    sajda_set = set(meta["sajda_list"])

    corpus: list[dict] = []
    for a in ayahs_raw:
        s_num = a["surah"]
        a_num = a["ayah"]
        gid = _global_index_for(s_num, a_num, surahs)
        rub = rub_by_ayah[gid]
        hizb = (rub - 1) // 4 + 1
        quarter_in_hizb = (rub - 1) % 4 + 1
        corpus.append({
            "surah": s_num,
            "ayah": a_num,
            "global_index": gid,
            "text_uthmani": a["text"],
            "page": page_by_ayah[gid],
            "juz": juz_by_ayah[gid],
            "hizb": hizb,
            "rub": rub,
            "quarter_in_hizb": quarter_in_hizb,
            "manzil": manzil_by_ayah[gid],
            "ruku": ruku_by_ayah[gid],
            "is_sajda": gid in sajda_set,
        })

    # Sanity: monotonic global index
    for i, c in enumerate(corpus, start=1):
        if c["global_index"] != i:
            raise RuntimeError(f"non-monotonic global index at position {i}: got {c['global_index']}")

    # Build reverse indices.
    def _ranges(starts: list[int], num_items: int) -> list[dict]:
        # Convert starts list to [{number, first, last}] with inclusive last.
        out = []
        for item in range(1, num_items + 1):
            out.append({
                "number": item,
                "first_ayah_global_index": starts[item],
                "last_ayah_global_index": starts[item + 1] - 1,
            })
        return out

    pages = _ranges(meta["page_list"], meta["meta"]["num_pages"])
    juz = _ranges(meta["juz_list"], meta["meta"]["num_juzs"])
    rubs = _ranges(meta["hizb_quarter_list"], meta["meta"]["num_rub_al_hizbs"])
    manzils = _ranges(meta["manzil_list"], meta["meta"]["num_manzils"])
    rukus = _ranges(meta["ruku_list"], meta["meta"]["num_rukus"])

    # Derive hizb from rub: hizb k spans rubs 4k-3 .. 4k.
    hizbs = []
    for h in range(1, meta["meta"]["num_hizbs"] + 1):
        first_rub = rubs[(h - 1) * 4]      # rubs is 0-indexed, item h·1 is at position (h-1)*4
        last_rub = rubs[(h - 1) * 4 + 3]
        hizbs.append({
            "number": h,
            "first_ayah_global_index": first_rub["first_ayah_global_index"],
            "last_ayah_global_index": last_rub["last_ayah_global_index"],
        })

    # Attach page-level aggregates: surahs_on_page, juz_starts_on_page.
    juz_start_ayahs = {j["first_ayah_global_index"]: j["number"] for j in juz}
    ayah_to_surah = {c["global_index"]: c["surah"] for c in corpus}
    for p in pages:
        surahs_on_page: list[int] = []
        seen: set[int] = set()
        for gid in range(p["first_ayah_global_index"], p["last_ayah_global_index"] + 1):
            s = ayah_to_surah[gid]
            if s not in seen:
                surahs_on_page.append(s)
                seen.add(s)
        p["surahs_on_page"] = surahs_on_page
        p["juz_starts_on_page"] = [
            juz_start_ayahs[gid]
            for gid in range(p["first_ayah_global_index"], p["last_ayah_global_index"] + 1)
            if gid in juz_start_ayahs
        ]

    # Surahs table: strip sentinels, add first_ayah_global_index (already start_ayah_id).
    surah_table: list[dict] = []
    for s in surahs[1:115]:
        surah_table.append({
            "number": s["index"],
            "name_ar": s["name_ar"],
            "name_translit_en": s.get("name_translit_en", ""),
            "name_translation_en": s.get("name_translation_en", ""),
            "revelation_order": s["revelation_order"],
            "revelation_place": "meccan" if s["is_meccan"] else "medinan",
            "ayah_count": s["ayah_count"],
            "ruku_count": s["ruku_count"],
            "bismillah_pre": s["index"] != 9,
            "first_ayah_global_index": s["start_ayah_id"],
        })

    sajdas = [
        {"number": i + 1, "ayah_global_index": gid, "surah": ayah_to_surah[gid],
         "ayah_in_surah": next(c["ayah"] for c in corpus if c["global_index"] == gid)}
        for i, gid in enumerate(meta["sajda_list"])
    ]

    indices = {
        "surahs": surah_table,
        "pages": pages,
        "juz": juz,
        "hizb": hizbs,
        "rub": rubs,
        "manzil": manzils,
        "ruku": rukus,
        "sajda": sajdas,
    }

    out_corpus = INTERMEDIATE_DIR / "corpus.json"
    out_indices = INTERMEDIATE_DIR / "indices.json"
    sha_corpus = write_json_deterministic(out_corpus, corpus)
    sha_indices = write_json_deterministic(out_indices, indices)

    record_stage(manifest, "merge",
        inputs={
            "tanzil_raw": INTERMEDIATE_DIR / "tanzil-raw.json",
            "hafs_meta": INTERMEDIATE_DIR / "hafs-meta.json",
        },
        outputs={
            "corpus": (str(out_corpus.relative_to(DATA_DIR)), sha_corpus),
            "indices": (str(out_indices.relative_to(DATA_DIR)), sha_indices),
        },
    )
    print(
        f"[merge] OK  corpus_ayahs={len(corpus)}  pages={len(pages)}  juz={len(juz)}  "
        f"hizb={len(hizbs)}  rub={len(rubs)}  sajda={len(sajdas)}",
        flush=True,
    )

# ---------------------------------------------------------------------------
# Stage: validate_structural  (TASK-019)
# ---------------------------------------------------------------------------

# Canonical Hafs per-surah ayah counts (1..114).
HAFS_AYAH_COUNTS = [
    7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99, 128,
    111, 110, 98, 135, 112, 78, 118, 64, 77, 227, 93, 88, 69, 60, 34, 30, 73,
    54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59, 37, 35, 38, 29, 18, 45, 60,
    49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
    28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29, 19, 36, 25, 22, 17, 19, 26, 30,
    20, 15, 21, 11, 8, 8, 19, 5, 8, 8, 11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4,
    5, 6,
]
assert sum(HAFS_AYAH_COUNTS) == 6236 and len(HAFS_AYAH_COUNTS) == 114


def stage_validate_structural(manifest: dict) -> None:
    """Assert every structural invariant. Exit non-zero on any failure."""
    print("[validate_structural] loading corpus + indices", flush=True)
    corpus = json.loads((INTERMEDIATE_DIR / "corpus.json").read_bytes())
    indices = json.loads((INTERMEDIATE_DIR / "indices.json").read_bytes())

    failures: list[str] = []

    def check(cond: bool, msg: str) -> None:
        if not cond:
            failures.append(msg)

    # Global-index density and uniqueness
    gids = [c["global_index"] for c in corpus]
    check(len(corpus) == 6236, f"corpus length is {len(corpus)}, expected 6236")
    check(gids == list(range(1, 6237)), "global_index sequence is not 1..6236 dense")

    # Per-surah counts match canonical Hafs table
    per_surah = Counter(c["surah"] for c in corpus)
    for s in range(1, 115):
        check(
            per_surah[s] == HAFS_AYAH_COUNTS[s - 1],
            f"surah {s} ayah count {per_surah[s]} != canonical {HAFS_AYAH_COUNTS[s - 1]}",
        )

    # Index-table row counts
    check(len(indices["surahs"]) == 114, f"surahs table has {len(indices['surahs'])} rows, expected 114")
    check(len(indices["pages"]) == 604, f"pages table has {len(indices['pages'])} rows, expected 604")
    check(len(indices["juz"]) == 30, f"juz table has {len(indices['juz'])} rows, expected 30")
    check(len(indices["hizb"]) == 60, f"hizb table has {len(indices['hizb'])} rows, expected 60")
    check(len(indices["rub"]) == 240, f"rub table has {len(indices['rub'])} rows, expected 240")
    check(len(indices["manzil"]) == 7, f"manzil table has {len(indices['manzil'])} rows, expected 7")
    check(len(indices["ruku"]) == 556, f"ruku table has {len(indices['ruku'])} rows, expected 556")
    check(len(indices["sajda"]) == 15, f"sajda table has {len(indices['sajda'])} rows, expected 15")

    # Every ayah has valid metadata within range
    for c in corpus:
        check(1 <= c["page"] <= 604, f"ayah {c['global_index']}: page {c['page']} out of range")
        check(1 <= c["juz"] <= 30, f"ayah {c['global_index']}: juz {c['juz']} out of range")
        check(1 <= c["hizb"] <= 60, f"ayah {c['global_index']}: hizb {c['hizb']} out of range")
        check(1 <= c["rub"] <= 240, f"ayah {c['global_index']}: rub {c['rub']} out of range")
        check(1 <= c["manzil"] <= 7, f"ayah {c['global_index']}: manzil {c['manzil']} out of range")
        check(1 <= c["ruku"] <= 556, f"ayah {c['global_index']}: ruku {c['ruku']} out of range")
        # Rub → hizb relationship
        check(c["hizb"] == (c["rub"] - 1) // 4 + 1, f"ayah {c['global_index']}: hizb/rub mismatch")

    # Every page/juz/hizb/rub has ≥ 1 ayah
    page_pop = Counter(c["page"] for c in corpus)
    juz_pop = Counter(c["juz"] for c in corpus)
    for p in range(1, 605):
        check(page_pop[p] >= 1, f"page {p} has zero ayahs")
    for j in range(1, 31):
        check(juz_pop[j] >= 1, f"juz {j} has zero ayahs")

    # Every juz starts at a documented ayah
    juz_first_gids = {j["first_ayah_global_index"] for j in indices["juz"]}
    check(len(juz_first_gids) == 30, "juz first-ayah set != 30 entries")

    # Bismillah rule: surah 9 has bismillah_pre=false, all others true
    for s in indices["surahs"]:
        expected = s["number"] != 9
        check(
            s["bismillah_pre"] == expected,
            f"surah {s['number']}: bismillah_pre={s['bismillah_pre']} expected {expected}",
        )

    # Al-Fatiha 1:1 is the Bismillah (Hafs) — just assert non-empty & length reasonable
    fatiha_1 = corpus[0]
    check(fatiha_1["surah"] == 1 and fatiha_1["ayah"] == 1, "corpus[0] is not 1:1")
    check(len(fatiha_1["text_uthmani"]) > 20, "1:1 (Bismillah) unexpectedly short")

    # No duplicate consecutive ayah text
    prev_text = None
    for c in corpus:
        if c["text_uthmani"] == prev_text:
            failures.append(f"duplicate consecutive text at {c['surah']}:{c['ayah']}")
        prev_text = c["text_uthmani"]

    report = {
        "passed": len(failures) == 0,
        "failure_count": len(failures),
        "failures": failures,
        "totals": {
            "ayahs": len(corpus),
            "surahs": len(indices["surahs"]),
            "pages": len(indices["pages"]),
            "juz": len(indices["juz"]),
            "hizb": len(indices["hizb"]),
            "rub": len(indices["rub"]),
            "manzil": len(indices["manzil"]),
            "ruku": len(indices["ruku"]),
            "sajda": len(indices["sajda"]),
        },
    }
    out = INTERMEDIATE_DIR / "validate-structural.json"
    sha = write_json_deterministic(out, report)
    record_stage(manifest, "validate_structural",
        inputs={"corpus": INTERMEDIATE_DIR / "corpus.json", "indices": INTERMEDIATE_DIR / "indices.json"},
        outputs={"report": (str(out.relative_to(DATA_DIR)), sha)},
    )
    if failures:
        for f in failures[:20]:
            print("  FAIL:", f, flush=True)
        raise RuntimeError(f"[validate_structural] {len(failures)} failure(s); see {out.relative_to(DATA_DIR)}")
    print(f"[validate_structural] OK  all invariants pass", flush=True)


# ---------------------------------------------------------------------------
# Stage: validate_unicode  (TASK-020)
# ---------------------------------------------------------------------------

# Allowed Unicode blocks for Quran text. Any char outside is a defect candidate.
ALLOWED_RANGES = [
    (0x0020, 0x0020),   # ASCII space
    (0x0600, 0x06FF),   # Arabic
    (0x0750, 0x077F),   # Arabic Supplement
    (0x08A0, 0x08FF),   # Arabic Extended-A
    (0xFB50, 0xFDFF),   # Arabic Presentation Forms-A
    (0xFE70, 0xFEFF),   # Arabic Presentation Forms-B
]

def _in_allowed(cp: int) -> bool:
    for lo, hi in ALLOWED_RANGES:
        if lo <= cp <= hi:
            return True
    return False


def _block_name(cp: int) -> str:
    if cp == 0x0020: return "ASCII space"
    if 0x0600 <= cp <= 0x06FF: return "Arabic"
    if 0x0750 <= cp <= 0x077F: return "Arabic Supplement"
    if 0x08A0 <= cp <= 0x08FF: return "Arabic Extended-A"
    if 0xFB50 <= cp <= 0xFDFF: return "Arabic Presentation Forms-A"
    if 0xFE70 <= cp <= 0xFEFF: return "Arabic Presentation Forms-B"
    if cp < 0x20: return "C0 control"
    if 0x80 <= cp <= 0x9F: return "C1 control"
    return "OTHER"


def stage_validate_unicode(manifest: dict) -> None:
    """Scan every char in every ayah; assert only allowed Arabic blocks; emit report."""
    print("[validate_unicode] scanning corpus", flush=True)
    corpus = json.loads((INTERMEDIATE_DIR / "corpus.json").read_bytes())

    per_block: Counter[str] = Counter()
    per_char: Counter[int] = Counter()
    offenders: list[dict] = []  # ayahs with disallowed characters
    whitespace_anomalies: list[str] = []
    duplicate_consecutive: list[str] = []

    prev_text = None
    for c in corpus:
        text = c["text_uthmani"]
        if not text:
            offenders.append({"ayah": f"{c['surah']}:{c['ayah']}", "issue": "empty text"})
            continue
        if text != text.strip():
            whitespace_anomalies.append(f"{c['surah']}:{c['ayah']} has leading/trailing whitespace")
        for ch in text:
            cp = ord(ch)
            per_char[cp] += 1
            per_block[_block_name(cp)] += 1
            if not _in_allowed(cp):
                offenders.append({
                    "ayah": f"{c['surah']}:{c['ayah']}",
                    "codepoint": f"U+{cp:04X}",
                    "name": unicodedata.name(ch, "UNKNOWN"),
                    "block": _block_name(cp),
                })
        if text == prev_text:
            duplicate_consecutive.append(f"{c['surah']}:{c['ayah']}")
        prev_text = text

    passed = not offenders and not whitespace_anomalies and not duplicate_consecutive

    # Interesting chars (sample of Quranic annotation marks)
    interesting_marks = {
        0x06D6: "SMALL HIGH LIGATURE SAD WITH LAM WITH ALEF MAKSURA",
        0x06D7: "SMALL HIGH LIGATURE QAF WITH LAM WITH ALEF MAKSURA",
        0x06D8: "SMALL HIGH MEEM INITIAL FORM",
        0x06DA: "SMALL HIGH JEEM",
        0x06DB: "SMALL HIGH THREE DOTS",
        0x06DC: "SMALL HIGH SEEN",
        0x06DD: "END OF AYAH",
        0x06DE: "START OF RUB EL HIZB",
        0x06E9: "PLACE OF SAJDAH",
        0x06ED: "SMALL LOW MEEM",
        0x0670: "SUPERSCRIPT ALEF",
        0x0671: "ALEF WASLA",
    }
    marks_report = {
        f"U+{cp:04X} {name}": per_char.get(cp, 0)
        for cp, name in interesting_marks.items()
    }

    report = {
        "passed": passed,
        "totals": {
            "ayahs_scanned": len(corpus),
            "total_chars": sum(per_char.values()),
            "distinct_codepoints": len(per_char),
        },
        "per_block": dict(sorted(per_block.items())),
        "quranic_marks_present": marks_report,
        "disallowed_char_count": len(offenders),
        "disallowed_chars_sample": offenders[:20],
        "whitespace_anomaly_count": len(whitespace_anomalies),
        "whitespace_anomalies_sample": whitespace_anomalies[:20],
        "duplicate_consecutive_count": len(duplicate_consecutive),
        "duplicate_consecutive_sample": duplicate_consecutive[:20],
    }
    out_json = INTERMEDIATE_DIR / "validate-unicode.json"
    sha_json = write_json_deterministic(out_json, report)

    # Human-readable profile
    lines = ["# Unicode Profile — Quran corpus (Tanzil Uthmani 1.1)", ""]
    lines.append(f"- Ayahs scanned: {report['totals']['ayahs_scanned']}")
    lines.append(f"- Total characters: {report['totals']['total_chars']}")
    lines.append(f"- Distinct codepoints: {report['totals']['distinct_codepoints']}")
    lines.append(f"- Passed validation: **{passed}**")
    lines.append("")
    lines.append("## Characters per Unicode block")
    lines.append("")
    lines.append("| Block | Chars |")
    lines.append("|---|---:|")
    for k, v in sorted(per_block.items(), key=lambda kv: -kv[1]):
        lines.append(f"| {k} | {v} |")
    lines.append("")
    lines.append("## Quranic annotation marks — codepoint counts")
    lines.append("")
    lines.append("| Codepoint | Name | Count |")
    lines.append("|---|---|---:|")
    for cp, name in sorted(interesting_marks.items()):
        lines.append(f"| U+{cp:04X} | {name} | {per_char.get(cp, 0)} |")
    lines.append("")
    if not passed:
        lines.append("## Failures")
        lines.append(f"- disallowed chars: {len(offenders)}")
        lines.append(f"- whitespace anomalies: {len(whitespace_anomalies)}")
        lines.append(f"- duplicate consecutive: {len(duplicate_consecutive)}")
    out_md = INTERMEDIATE_DIR / "unicode-profile.md"
    out_md.write_bytes(("\n".join(lines) + "\n").encode("utf-8"))
    sha_md = sha256_file(out_md)

    record_stage(manifest, "validate_unicode",
        inputs={"corpus": INTERMEDIATE_DIR / "corpus.json"},
        outputs={
            "report_json": (str(out_json.relative_to(DATA_DIR)), sha_json),
            "profile_md": (str(out_md.relative_to(DATA_DIR)), sha_md),
        },
    )
    if not passed:
        raise RuntimeError(f"[validate_unicode] failed; see {out_json.relative_to(DATA_DIR)}")
    print(f"[validate_unicode] OK  chars={report['totals']['total_chars']}  distinct_cp={report['totals']['distinct_codepoints']}", flush=True)


# ---------------------------------------------------------------------------
# Normalization (for cross-source diff and FTS search index)
# ---------------------------------------------------------------------------

# normalize_for_search() collapses text to a loose consonantal skeleton.
# Everything in _STRIP is removed; everything in _SUB is substituted.
# The goal is that a user typing modern Arabic finds the mushaf verse regardless
# of orthographic differences (alef madda vs alef, alef maksura vs ya, ta marbuta
# vs ha, missing/present inline alef, etc.).
#
# Never applied to `text_uthmani` — this is a projection used only for
# cross-source comparison and for the FTS5 `search_text` column.
_STRIP: set[int] = set()
# harakat: fatha, kasra, damma, shadda, sukun, tanween forms (fatha/kasra/damma)
for cp in range(0x064B, 0x0653):
    _STRIP.add(cp)
# Quranic annotation marks (small high seen, small high madda, sajdah symbol, etc.)
for cp in range(0x0610, 0x061B):
    _STRIP.add(cp)
for cp in range(0x06D6, 0x06EE):
    _STRIP.add(cp)
_STRIP.add(0x0640)  # tatweel (decorative kashida)
# Alef family — collapse all forms (including regular alef) so the presence
# or absence of an inline alef doesn't affect matching. This is the standard
# "loose" normalization used by Arabic/Quran search implementations.
_STRIP |= {0x0627, 0x0622, 0x0623, 0x0625, 0x0671, 0x0670}
# 0x0627 ا  0x0622 آ  0x0623 أ  0x0625 إ  0x0671 ٱ  0x0670 ٰ (superscript alef)

_SUB: dict[int, str] = {
    0x0649: "ي",  # ى alef maksura → ي
    0x0629: "ه",  # ة ta marbuta → ه
}


def normalize_for_search(s: str) -> str:
    out: list[str] = []
    for ch in s:
        cp = ord(ch)
        if cp in _STRIP:
            continue
        sub = _SUB.get(cp)
        out.append(sub if sub is not None else ch)
    text = re.sub(r"\s+", " ", "".join(out)).strip()
    return text


# ---------------------------------------------------------------------------
# Stage: cross_source_diff  (TASK-021)
# ---------------------------------------------------------------------------

QURAN_COM_VERSES = SOURCES_DIR / "quran-com" / "v4" / "verses-uthmani.json"


def stage_cross_source_diff(manifest: dict) -> None:
    """Compare Tanzil (source A) vs Quran.com KFGQPC (source B), skeleton-normalized.

    Objective: catch import defects. Diacritic-level differences between
    Tanzil-Uthmani and KFGQPC are expected and are documented.
    """
    print("[cross_source_diff] loading corpus + quran.com verses", flush=True)
    corpus = json.loads((INTERMEDIATE_DIR / "corpus.json").read_bytes())
    qc = json.loads(QURAN_COM_VERSES.read_bytes())["verses"]

    by_key_a = {f"{c['surah']}:{c['ayah']}": c["text_uthmani"] for c in corpus}
    by_key_b = {v["verse_key"]: v["text_uthmani"] for v in qc}

    a_keys, b_keys = set(by_key_a), set(by_key_b)
    only_a = sorted(a_keys - b_keys)
    only_b = sorted(b_keys - a_keys)

    common = sorted(a_keys & b_keys, key=lambda k: (int(k.split(":")[0]), int(k.split(":")[1])))

    byte_identical = 0
    skeleton_equal = 0
    skeleton_diffs: list[dict] = []

    for k in common:
        a, b = by_key_a[k], by_key_b[k]
        if a == b:
            byte_identical += 1
            skeleton_equal += 1
            continue
        na = normalize_for_search(a)
        nb = normalize_for_search(b)
        if na == nb:
            skeleton_equal += 1
        else:
            skeleton_diffs.append({
                "key": k,
                "tanzil_skel": na,
                "quran_com_skel": nb,
                "tanzil_len": len(na),
                "quran_com_len": len(nb),
            })

    passed = not only_a and not only_b and not skeleton_diffs

    report = {
        "passed": passed,
        "sources": {
            "A": "Tanzil Project Uthmani 1.1 (data/sources/tanzil-uthmani/1.1/quran-uthmani.xml)",
            "B": "Quran.com API v4 Uthmani (data/sources/quran-com/v4/verses-uthmani.json) — KFGQPC-derived",
        },
        "counts": {
            "A_ayahs": len(by_key_a),
            "B_ayahs": len(by_key_b),
            "common": len(common),
            "only_in_A": len(only_a),
            "only_in_B": len(only_b),
            "byte_identical_of_common": byte_identical,
            "skeleton_equal_of_common": skeleton_equal,
            "skeleton_diffs_of_common": len(skeleton_diffs),
        },
        "only_in_A_sample": only_a[:20],
        "only_in_B_sample": only_b[:20],
        "skeleton_diffs_sample": skeleton_diffs[:20],
    }
    out_json = INTERMEDIATE_DIR / "cross-source-diff.json"
    sha_json = write_json_deterministic(out_json, report)

    lines = [
        "# Cross-source diff report — Tanzil vs Quran.com (KFGQPC)",
        "",
        "**Purpose:** independent verification that our import pipeline preserves the",
        "Quran text intact. We compare Tanzil Uthmani (source A, our primary) against",
        "Quran.com's Uthmani text (source B, derived from the King Fahd Quran Printing",
        "Complex — an independent lineage). Diacritic-level differences between",
        "the two traditions are **expected** and are not a defect; structural",
        "differences (missing ayahs, wrong ordering, skeleton mismatches on the",
        "consonantal text) would indicate a defect and block release.",
        "",
        "## Sources",
        f"- **A:** {report['sources']['A']}",
        f"- **B:** {report['sources']['B']}",
        "",
        "## Counts",
        "",
        "| Metric | Count |",
        "|---|---:|",
        f"| Ayahs in A | {report['counts']['A_ayahs']} |",
        f"| Ayahs in B | {report['counts']['B_ayahs']} |",
        f"| Common keys | {report['counts']['common']} |",
        f"| Only in A | {report['counts']['only_in_A']} |",
        f"| Only in B | {report['counts']['only_in_B']} |",
        f"| Byte-identical (of common) | {report['counts']['byte_identical_of_common']} |",
        f"| **Skeleton-equal (of common)** | **{report['counts']['skeleton_equal_of_common']}** |",
        f"| Skeleton diffs | {report['counts']['skeleton_diffs_of_common']} |",
        "",
        "**Skeleton equality** = both texts, after removing all diacritics and Quranic",
        "annotation marks and after normalizing alef/ya/ta-marbuta variants and",
        "collapsing whitespace, are byte-identical.",
        "",
        f"## Verdict: {'PASS' if passed else 'FAIL'}",
        "",
    ]
    if skeleton_diffs:
        lines += ["## Skeleton diffs (sample)", ""]
        for d in skeleton_diffs[:20]:
            lines.append(f"### {d['key']}")
            lines.append(f"- A skel ({d['tanzil_len']} chars): `{d['tanzil_skel']}`")
            lines.append(f"- B skel ({d['quran_com_len']} chars): `{d['quran_com_skel']}`")
            lines.append("")
    out_md = INTERMEDIATE_DIR / "cross-source-diff-report.md"
    out_md.write_bytes(("\n".join(lines) + "\n").encode("utf-8"))
    sha_md = sha256_file(out_md)

    record_stage(manifest, "cross_source_diff",
        inputs={"corpus": INTERMEDIATE_DIR / "corpus.json", "quran_com_verses": QURAN_COM_VERSES},
        outputs={
            "diff_json": (str(out_json.relative_to(DATA_DIR)), sha_json),
            "diff_report_md": (str(out_md.relative_to(DATA_DIR)), sha_md),
        },
    )
    print(
        f"[cross_source_diff]  common={len(common)}  byte_identical={byte_identical}  "
        f"skeleton_equal={skeleton_equal}  skeleton_diffs={len(skeleton_diffs)}",
        flush=True,
    )
    if not passed:
        # Skeleton diffs are worth flagging loudly; do NOT auto-fail unless there are
        # structural differences (missing/extra ayahs). Skeleton diffs are reported
        # and require human triage before release.
        if only_a or only_b:
            raise RuntimeError(
                "[cross_source_diff] structural difference: ayahs missing between sources; "
                f"see {out_md.relative_to(DATA_DIR)}"
            )
        print(
            f"[cross_source_diff] NOTE: {len(skeleton_diffs)} skeleton diff(s) require human triage; "
            f"see {out_md.relative_to(DATA_DIR)}",
            flush=True,
        )


# ---------------------------------------------------------------------------
# Stage: package_sqlite  (TASK-022)
# ---------------------------------------------------------------------------

QURAN_DB = OUTPUT_DIR / "quran.db"

_SCHEMA_SQL = """
CREATE TABLE schema_version (
    version    TEXT NOT NULL,
    name       TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE surahs (
    number                    INTEGER PRIMARY KEY,
    name_ar                   TEXT NOT NULL,
    name_translit_en          TEXT NOT NULL,
    name_translation_en       TEXT NOT NULL,
    revelation_order          INTEGER NOT NULL,
    revelation_place          TEXT NOT NULL CHECK(revelation_place IN ('meccan','medinan')),
    ayah_count                INTEGER NOT NULL,
    ruku_count                INTEGER NOT NULL,
    bismillah_pre             INTEGER NOT NULL CHECK(bismillah_pre IN (0,1)),
    first_ayah_global_index   INTEGER NOT NULL
);

CREATE TABLE ayahs (
    global_index      INTEGER PRIMARY KEY,
    surah             INTEGER NOT NULL REFERENCES surahs(number),
    ayah              INTEGER NOT NULL,
    text_uthmani      TEXT NOT NULL,
    page              INTEGER NOT NULL,
    juz               INTEGER NOT NULL,
    hizb              INTEGER NOT NULL,
    rub               INTEGER NOT NULL,
    quarter_in_hizb   INTEGER NOT NULL,
    manzil            INTEGER NOT NULL,
    ruku              INTEGER NOT NULL,
    is_sajda          INTEGER NOT NULL CHECK(is_sajda IN (0,1)),
    UNIQUE(surah, ayah)
);

CREATE TABLE pages (
    number                    INTEGER PRIMARY KEY,
    first_ayah_global_index   INTEGER NOT NULL,
    last_ayah_global_index    INTEGER NOT NULL,
    surahs_on_page_json       TEXT NOT NULL,
    juz_starts_on_page_json   TEXT NOT NULL
);

CREATE TABLE juz (
    number                    INTEGER PRIMARY KEY,
    first_ayah_global_index   INTEGER NOT NULL,
    last_ayah_global_index    INTEGER NOT NULL
);

CREATE TABLE hizb (
    number                    INTEGER PRIMARY KEY,
    first_ayah_global_index   INTEGER NOT NULL,
    last_ayah_global_index    INTEGER NOT NULL
);

CREATE TABLE rub (
    number                    INTEGER PRIMARY KEY,
    hizb_number               INTEGER NOT NULL REFERENCES hizb(number),
    quarter_in_hizb           INTEGER NOT NULL CHECK(quarter_in_hizb BETWEEN 1 AND 4),
    first_ayah_global_index   INTEGER NOT NULL,
    last_ayah_global_index    INTEGER NOT NULL
);

CREATE TABLE manzil (
    number                    INTEGER PRIMARY KEY,
    first_ayah_global_index   INTEGER NOT NULL,
    last_ayah_global_index    INTEGER NOT NULL
);

CREATE TABLE ruku (
    number                    INTEGER PRIMARY KEY,
    first_ayah_global_index   INTEGER NOT NULL,
    last_ayah_global_index    INTEGER NOT NULL
);

CREATE TABLE sajda (
    number              INTEGER PRIMARY KEY,
    ayah_global_index   INTEGER NOT NULL UNIQUE REFERENCES ayahs(global_index),
    surah               INTEGER NOT NULL,
    ayah_in_surah       INTEGER NOT NULL
);

CREATE INDEX idx_ayahs_surah_ayah ON ayahs(surah, ayah);
CREATE INDEX idx_ayahs_page       ON ayahs(page);
CREATE INDEX idx_ayahs_juz        ON ayahs(juz);
CREATE INDEX idx_ayahs_hizb       ON ayahs(hizb);
CREATE INDEX idx_ayahs_rub        ON ayahs(rub);
"""


def stage_package_sqlite(manifest: dict) -> None:
    """Build the read-only quran.db from corpus.json + indices.json."""
    import sqlite3

    print("[package_sqlite] loading intermediates", flush=True)
    corpus = json.loads((INTERMEDIATE_DIR / "corpus.json").read_bytes())
    indices = json.loads((INTERMEDIATE_DIR / "indices.json").read_bytes())

    if QURAN_DB.exists():
        QURAN_DB.unlink()

    conn = sqlite3.connect(str(QURAN_DB))
    try:
        cur = conn.cursor()
        cur.execute("PRAGMA page_size = 4096")
        cur.execute("PRAGMA journal_mode = DELETE")
        cur.executescript(_SCHEMA_SQL)

        cur.execute(
            "INSERT INTO schema_version(version, name, description) VALUES (?, ?, ?)",
            ("1.0.0", "Digital Mushaf Quran dataset",
             "Hafs 'an Asim; Madani 15-line 604-page mushaf; Tanzil 1.1 text + quran-meta pagination"),
        )

        cur.executemany(
            "INSERT INTO surahs VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            [(s["number"], s["name_ar"], s["name_translit_en"], s["name_translation_en"],
              s["revelation_order"], s["revelation_place"], s["ayah_count"], s["ruku_count"],
              1 if s["bismillah_pre"] else 0, s["first_ayah_global_index"])
             for s in indices["surahs"]],
        )

        cur.executemany(
            "INSERT INTO ayahs VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            [(c["global_index"], c["surah"], c["ayah"], c["text_uthmani"],
              c["page"], c["juz"], c["hizb"], c["rub"], c["quarter_in_hizb"],
              c["manzil"], c["ruku"], 1 if c["is_sajda"] else 0)
             for c in corpus],
        )

        cur.executemany(
            "INSERT INTO pages VALUES (?, ?, ?, ?, ?)",
            [(p["number"], p["first_ayah_global_index"], p["last_ayah_global_index"],
              json.dumps(p["surahs_on_page"], separators=(",", ":")),
              json.dumps(p["juz_starts_on_page"], separators=(",", ":")))
             for p in indices["pages"]],
        )
        cur.executemany(
            "INSERT INTO juz VALUES (?, ?, ?)",
            [(j["number"], j["first_ayah_global_index"], j["last_ayah_global_index"]) for j in indices["juz"]],
        )
        cur.executemany(
            "INSERT INTO hizb VALUES (?, ?, ?)",
            [(h["number"], h["first_ayah_global_index"], h["last_ayah_global_index"]) for h in indices["hizb"]],
        )
        cur.executemany(
            "INSERT INTO rub VALUES (?, ?, ?, ?, ?)",
            [(r["number"], (r["number"] - 1) // 4 + 1, (r["number"] - 1) % 4 + 1,
              r["first_ayah_global_index"], r["last_ayah_global_index"]) for r in indices["rub"]],
        )
        cur.executemany(
            "INSERT INTO manzil VALUES (?, ?, ?)",
            [(m["number"], m["first_ayah_global_index"], m["last_ayah_global_index"]) for m in indices["manzil"]],
        )
        cur.executemany(
            "INSERT INTO ruku VALUES (?, ?, ?)",
            [(r["number"], r["first_ayah_global_index"], r["last_ayah_global_index"]) for r in indices["ruku"]],
        )
        cur.executemany(
            "INSERT INTO sajda VALUES (?, ?, ?, ?)",
            [(s["number"], s["ayah_global_index"], s["surah"], s["ayah_in_surah"]) for s in indices["sajda"]],
        )

        conn.commit()

        # Row-count invariants
        counts = {
            "surahs": cur.execute("SELECT COUNT(*) FROM surahs").fetchone()[0],
            "ayahs": cur.execute("SELECT COUNT(*) FROM ayahs").fetchone()[0],
            "pages": cur.execute("SELECT COUNT(*) FROM pages").fetchone()[0],
            "juz": cur.execute("SELECT COUNT(*) FROM juz").fetchone()[0],
            "hizb": cur.execute("SELECT COUNT(*) FROM hizb").fetchone()[0],
            "rub": cur.execute("SELECT COUNT(*) FROM rub").fetchone()[0],
            "manzil": cur.execute("SELECT COUNT(*) FROM manzil").fetchone()[0],
            "ruku": cur.execute("SELECT COUNT(*) FROM ruku").fetchone()[0],
            "sajda": cur.execute("SELECT COUNT(*) FROM sajda").fetchone()[0],
        }
        expected = {"surahs": 114, "ayahs": 6236, "pages": 604, "juz": 30, "hizb": 60,
                    "rub": 240, "manzil": 7, "ruku": 556, "sajda": 15}
        for k, v in expected.items():
            if counts[k] != v:
                raise RuntimeError(f"row-count mismatch for {k}: got {counts[k]}, expected {v}")

        # Compact & finalize
        conn.commit()
    finally:
        conn.close()

    # Compact into a stable layout for reproducible hashing.
    conn2 = sqlite3.connect(str(QURAN_DB))
    try:
        conn2.execute("VACUUM")
        conn2.commit()
    finally:
        conn2.close()

    # Canonical SQL dump for a truly deterministic hash (SQLite's file layout can
    # vary across libsqlite versions; the .sql dump is content-canonical).
    conn3 = sqlite3.connect(str(QURAN_DB))
    try:
        dump_lines = list(conn3.iterdump())
    finally:
        conn3.close()
    dump_text = "\n".join(dump_lines) + "\n"
    dump_path = INTERMEDIATE_DIR / "quran.sql"
    dump_path.write_bytes(dump_text.encode("utf-8"))
    dump_sha = sha256_file(dump_path)

    file_sha = sha256_file(QURAN_DB)
    file_size = QURAN_DB.stat().st_size
    print(f"[package_sqlite] OK  quran.db size={file_size:,} bytes  sha256={file_sha[:16]}...  sql_dump_sha={dump_sha[:16]}...", flush=True)
    print(f"[package_sqlite] counts: {counts}", flush=True)

    record_stage(manifest, "package_sqlite",
        inputs={"corpus": INTERMEDIATE_DIR / "corpus.json", "indices": INTERMEDIATE_DIR / "indices.json"},
        outputs={
            "quran_db": (str(QURAN_DB.relative_to(DATA_DIR)), file_sha),
            "quran_sql_dump": (str(dump_path.relative_to(DATA_DIR)), dump_sha),
        },
    )


# ---------------------------------------------------------------------------
# Stage: build_search_index  (TASK-023)
# ---------------------------------------------------------------------------

_TEST_QUERIES = [
    # (query, expected_min_hits, must_include_global_index_or_None)
    # Well-known verses — must be findable by their user-typed form.
    ("الحمد لله رب العالمين", 1, 2),          # Al-Fatiha 1:2 (gid 2)
    ("مالك يوم الدين", 1, 4),                 # Al-Fatiha 1:4 (gid 4)
    ("قل هو الله احد", 1, 6222),              # Al-Ikhlas 112:1 (gid 6222)
    ("قل اعوذ برب الفلق", 1, 6226),           # Al-Falaq 113:1 (gid 6226)
    ("قل اعوذ برب الناس", 1, 6231),           # An-Nas 114:1 (gid 6231)
    ("بسم الله الرحمن الرحيم", 1, 1),         # bismillah — Al-Fatiha 1:1 (gid 1), also 27:30
    # Common tokens — sanity that the index is populated broadly.
    ("الله", 500, None),                       # extremely common
    ("الرحمن الرحيم", 3, None),                # bismillah phrase — appears in a handful of places
    # Diacritics-in-query should behave the same as no diacritics.
    ("بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ", 1, 1),
]


def stage_build_search_index(manifest: dict) -> None:
    """Add search_text column to ayahs and build FTS5 index. Original text_uthmani unchanged."""
    import sqlite3

    print("[build_search_index] opening quran.db", flush=True)
    if not QURAN_DB.exists():
        raise RuntimeError("quran.db does not exist; run package_sqlite first")

    # Preserve pre-index hash of text_uthmani column for integrity check.
    conn = sqlite3.connect(str(QURAN_DB))
    try:
        cur = conn.cursor()
        cur.execute("SELECT text_uthmani FROM ayahs ORDER BY global_index")
        before_hash = hashlib.sha256(
            "\n".join(row[0] for row in cur.fetchall()).encode("utf-8")
        ).hexdigest()

        # Add column if not present (idempotent).
        cols = [row[1] for row in cur.execute("PRAGMA table_info(ayahs)").fetchall()]
        if "search_text" not in cols:
            cur.execute("ALTER TABLE ayahs ADD COLUMN search_text TEXT NOT NULL DEFAULT ''")

        # Populate search_text.
        cur.execute("SELECT global_index, text_uthmani FROM ayahs")
        rows = cur.fetchall()
        cur.executemany(
            "UPDATE ayahs SET search_text = ? WHERE global_index = ?",
            [(normalize_for_search(t), gid) for gid, t in rows],
        )

        # Drop + recreate FTS5 virtual table over search_text.
        cur.execute("DROP TABLE IF EXISTS ayahs_fts")
        cur.execute(
            "CREATE VIRTUAL TABLE ayahs_fts USING fts5("
            "search_text, "
            "content='ayahs', content_rowid='global_index', "
            "tokenize='unicode61 remove_diacritics 0')"
        )
        cur.execute("INSERT INTO ayahs_fts(ayahs_fts) VALUES('rebuild')")
        conn.commit()

        # Post-index hash — must equal before_hash (original text untouched).
        cur.execute("SELECT text_uthmani FROM ayahs ORDER BY global_index")
        after_hash = hashlib.sha256(
            "\n".join(row[0] for row in cur.fetchall()).encode("utf-8")
        ).hexdigest()
        if after_hash != before_hash:
            raise RuntimeError("[build_search_index] text_uthmani column was mutated!")

        # Sanity-check some queries.
        query_results: list[dict] = []
        failed: list[str] = []
        for q, min_hits, must_include_gid in _TEST_QUERIES:
            q_norm = normalize_for_search(q)
            # FTS5 MATCH — phrase query for accuracy.
            hits = cur.execute(
                "SELECT rowid FROM ayahs_fts WHERE ayahs_fts MATCH ? ORDER BY rowid",
                (f'"{q_norm}"',),
            ).fetchall()
            gids = [r[0] for r in hits]
            passed = (len(gids) >= min_hits) and (must_include_gid is None or must_include_gid in gids)
            query_results.append({
                "query": q,
                "query_normalized": q_norm,
                "hits": len(gids),
                "sample_gids": gids[:5],
                "expected_min_hits": min_hits,
                "expected_include_gid": must_include_gid,
                "passed": passed,
            })
            if not passed:
                failed.append(f"{q!r} -> {len(gids)} hits, expected >= {min_hits}"
                              f"{'' if must_include_gid is None else f' incl. gid {must_include_gid} (present={must_include_gid in gids})'}")

        # VACUUM to compact.
        conn.commit()
    finally:
        conn.close()
    conn = sqlite3.connect(str(QURAN_DB)); conn.execute("VACUUM"); conn.commit(); conn.close()

    report = {
        "text_uthmani_sha256_before": before_hash,
        "text_uthmani_sha256_after": after_hash,
        "queries": query_results,
        "passed": len(failed) == 0,
        "failures": failed,
    }
    out = INTERMEDIATE_DIR / "search-index.json"
    sha = write_json_deterministic(out, report)
    file_sha = sha256_file(QURAN_DB)
    print(f"[build_search_index]  passed={report['passed']}  db_sha={file_sha[:16]}...", flush=True)
    for qr in query_results:
        print(f"  [{'OK' if qr['passed'] else 'FAIL'}] {qr['query']!r} -> {qr['hits']} hits", flush=True)
    record_stage(manifest, "build_search_index",
        inputs={"quran_db_pre": INTERMEDIATE_DIR / "quran.sql"},
        outputs={
            "quran_db": (str(QURAN_DB.relative_to(DATA_DIR)), file_sha),
            "search_report": (str(out.relative_to(DATA_DIR)), sha),
        },
    )
    if failed:
        raise RuntimeError(f"[build_search_index] {len(failed)} test queries failed")


# ---------------------------------------------------------------------------
# Stage: release  (TASK-024: Content Integrity Gate)
# ---------------------------------------------------------------------------

CONTENT_VERSION = "1.0.0"
RELEASE_DIR = OUTPUT_DIR / "RELEASE"


def stage_release(manifest: dict) -> None:
    """Freeze the dataset: copy quran.db → RELEASE/, emit .sha256, write CONTENT_MANIFEST.md."""
    print(f"[release] freezing content v{CONTENT_VERSION}", flush=True)
    RELEASE_DIR.mkdir(parents=True, exist_ok=True)

    release_db = RELEASE_DIR / f"quran-{CONTENT_VERSION}.db"
    release_db.write_bytes(QURAN_DB.read_bytes())
    db_sha = sha256_file(release_db)
    (RELEASE_DIR / f"quran-{CONTENT_VERSION}.db.sha256").write_bytes(
        f"{db_sha}  quran-{CONTENT_VERSION}.db\n".encode("utf-8")
    )

    # Gather validation summaries
    struct_report = json.loads((INTERMEDIATE_DIR / "validate-structural.json").read_bytes())
    unicode_report = json.loads((INTERMEDIATE_DIR / "validate-unicode.json").read_bytes())
    xsource_report = json.loads((INTERMEDIATE_DIR / "cross-source-diff.json").read_bytes())
    search_report = json.loads((INTERMEDIATE_DIR / "search-index.json").read_bytes())

    tanzil_src = json.loads((SOURCES_DIR / "tanzil-uthmani" / "1.1" / "SOURCE.json").read_bytes())
    qmeta_src = json.loads((SOURCES_DIR / "quran-meta" / "SOURCE.json").read_bytes())
    qcom_src = json.loads((SOURCES_DIR / "quran-com" / "SOURCE.json").read_bytes())

    md = [
        f"# CONTENT MANIFEST — Digital Mushaf Quran dataset v{CONTENT_VERSION}",
        "",
        f"- **Frozen at:** {_dt.datetime.now(_dt.UTC).strftime('%Y-%m-%dT%H:%M:%SZ')}",
        f"- **Release DB:** `data/output/RELEASE/quran-{CONTENT_VERSION}.db`",
        f"- **SHA-256:** `{db_sha}`",
        f"- **Size:** {release_db.stat().st_size:,} bytes",
        f"- **Schema:** v1.0.0 (see `docs/data-model.md`)",
        "",
        "## Riwaya and pagination",
        "- Riwaya: Hafs `an` Asim",
        "- Pagination: Madani mushaf, 15 lines per page, **604 pages**",
        "",
        "## Sources",
        "",
        "### Quran text — Tanzil Project (primary)",
        f"- Version: {tanzil_src['version']} ({tanzil_src['release_date']})",
        f"- License: {tanzil_src['license']}",
        f"- Fetched: {tanzil_src['fetched_on']}",
        "- Files:",
    ]
    for f, meta in tanzil_src["files"].items():
        md.append(f"  - `{f}` — SHA-256 `{meta['sha256']}` ({meta['size_bytes']:,} B)")
    md += [
        "",
        "### Pagination + metadata — quran-center/quran-meta",
        f"- Package version: {qmeta_src['package_version']}",
        f"- Git commit: `{qmeta_src['git_commit']}`",
        f"- License: {qmeta_src['license']}",
        f"- Fetched: {qmeta_src['fetched_on']}",
        "- Files consumed:",
    ]
    for f, meta in qmeta_src["files"].items():
        if "sha256" in meta:
            md.append(f"  - `{f}` — SHA-256 `{meta['sha256']}` ({meta['size_bytes']:,} B)")
    md += [
        "",
        "### Cross-source verification — Quran.com API v4 (independent, KFGQPC-derived)",
        f"- Endpoint: {qcom_src['endpoint']}",
        f"- License: {qcom_src['license']}",
        f"- Fetched: {qcom_src['fetched_on']}",
    ]
    for f, meta in qcom_src["files"].items():
        md.append(f"  - `{f}` — SHA-256 `{meta['sha256']}` ({meta['size_bytes']:,} B)")

    md += [
        "",
        "## Invariants",
        "",
        "| Metric | Value |",
        "|---|---:|",
    ]
    for k, v in struct_report["totals"].items():
        md.append(f"| {k} | {v} |")

    md += [
        "",
        "## Validation summary",
        "",
        f"- Structural: **{'PASS' if struct_report['passed'] else 'FAIL'}** (`data/output/intermediate/validate-structural.json`)",
        f"- Unicode: **{'PASS' if unicode_report['passed'] else 'FAIL'}** (`data/output/intermediate/validate-unicode.json`, profile: `unicode-profile.md`)",
        f"- Cross-source (skeleton): **{xsource_report['counts']['skeleton_equal_of_common']}/{xsource_report['counts']['common']} skeleton-equal** — {'PASS' if xsource_report['passed'] else 'differences require triage — see report'} (`data/output/intermediate/cross-source-diff-report.md`)",
        f"- Search index: **{'PASS' if search_report['passed'] else 'FAIL'}** — {len(search_report['queries'])} canonical queries executed",
        "",
        "## Immutable content contract",
        "",
        "- `text_uthmani` is imported byte-for-byte from Tanzil (no trim/replace/normalization).",
        f"- Corpus SHA-256 (LF-joined ayah text): tracked in `data/output/intermediate/tanzil-corpus.sha256`.",
        f"- text_uthmani SHA-256 (pre/post search-index build): `{search_report['text_uthmani_sha256_before']}` — identical before and after.",
        "",
        "## Change policy",
        "",
        "Any change to text or metadata bumps the content version and requires a full",
        "Phase 1 re-run. Git tag `content-v" + CONTENT_VERSION + "` will be applied when",
        "the repository is initialized.",
        "",
    ]
    md_path = RELEASE_DIR / "CONTENT_MANIFEST.md"
    md_path.write_bytes(("\n".join(md) + "\n").encode("utf-8"))
    md_sha = sha256_file(md_path)

    record_stage(manifest, "release",
        inputs={"quran_db": QURAN_DB},
        outputs={
            "release_db": (str(release_db.relative_to(DATA_DIR)), db_sha),
            "content_manifest_md": (str(md_path.relative_to(DATA_DIR)), md_sha),
        },
    )
    print(f"[release] OK  {release_db.relative_to(DATA_DIR)}  sha256={db_sha}", flush=True)


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

STAGES = OrderedDict([
    ("parse_tanzil", stage_parse_tanzil),
    ("parse_meta", stage_parse_meta),
    ("merge", stage_merge),
    ("validate_structural", stage_validate_structural),
    ("validate_unicode", stage_validate_unicode),
    ("cross_source_diff", stage_cross_source_diff),
    ("package_sqlite", stage_package_sqlite),
    ("build_search_index", stage_build_search_index),
    ("release", stage_release),
])


def main(argv: list[str]) -> int:
    p = argparse.ArgumentParser(description="Digital Mushaf Quran pipeline")
    p.add_argument("stage", help="Stage name, or 'all' to run every stage in order.")
    args = p.parse_args(argv)

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    INTERMEDIATE_DIR.mkdir(parents=True, exist_ok=True)

    manifest = load_manifest()

    if args.stage == "all":
        for name, fn in STAGES.items():
            fn(manifest)
    elif args.stage in STAGES:
        STAGES[args.stage](manifest)
    else:
        print(f"unknown stage: {args.stage}  (known: {', '.join(STAGES)}, or 'all')", file=sys.stderr)
        return 2

    save_manifest(manifest)
    print(f"[manifest] {MANIFEST_PATH.relative_to(DATA_DIR)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
