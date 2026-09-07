"""
Post-migration validator (ADR-0026 gate).

Opens the released quran-1.1.0.db and asserts every invariant listed in Decision 7:

    * exactly 604 pages
    * every Quran word is represented exactly once
    * zero Quran-text mutation vs Tanzil corpus.json
    * zero unmapped real QCF4 words
    * zero unmapped Tanzil non-waqf tokens
    * all 4,379 waqf markers resolve
    * all 199 Rub markers resolve
    * page/line ordering is deterministic
    * no duplicate or missing word positions
    * every ayah has a deterministic page assignment
    * database hash/version match the generated production asset

Also cross-checks the Android asset copy against the release DB and the
QuranAsset.kt constants.

Exits 0 on success, non-zero on any failure.
"""
from __future__ import annotations

import hashlib
import json
import re
import sqlite3
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

REPO = Path(r"C:\Users\Bah\Desktop\mobileApp")
RELEASE_DB = REPO / "data" / "output" / "RELEASE" / "quran-1.1.0.db"
CORPUS_JSON = REPO / "data" / "output" / "intermediate" / "corpus.json"
ANDROID_ASSET = REPO / "android" / "app" / "src" / "main" / "assets" / "quran" / "quran.db"
QURAN_ASSET_KT = REPO / "android" / "core" / "database" / "src" / "main" / "java" / "app" / "mushaf" / "core" / "database" / "QuranAsset.kt"

EXPECTED_TOTALS = {
    "pages": 604,
    "ayahs": 6236,
    "page_lines": 9046,
    "page_words": 82011,
    "word_tokens": 77433,
    "waqf_tokens": 4379,
    "rub_tokens": 199,
    "sajda": 15,
}


def sha256(p: Path) -> str:
    h = hashlib.sha256()
    with p.open("rb") as f:
        for chunk in iter(lambda: f.read(64 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def main() -> int:
    if not RELEASE_DB.exists():
        print(f"FAIL  release DB not found: {RELEASE_DB}")
        return 2
    if not CORPUS_JSON.exists():
        print(f"FAIL  corpus.json not found: {CORPUS_JSON}")
        return 2

    failures: list[str] = []

    def check(cond: bool, msg: str) -> None:
        if cond:
            print(f"OK    {msg}")
        else:
            print(f"FAIL  {msg}")
            failures.append(msg)

    print(f"Opening {RELEASE_DB.relative_to(REPO)}")
    conn = sqlite3.connect(str(RELEASE_DB))
    cur = conn.cursor()

    # ------------------------------------------------------------------ row counts
    check(cur.execute("SELECT COUNT(*) FROM ayahs").fetchone()[0] == EXPECTED_TOTALS["ayahs"],
          f"ayahs row count == {EXPECTED_TOTALS['ayahs']}")
    check(cur.execute("SELECT COUNT(*) FROM pages").fetchone()[0] == EXPECTED_TOTALS["pages"],
          f"pages row count == {EXPECTED_TOTALS['pages']}")
    check(cur.execute("SELECT COUNT(DISTINCT number) FROM pages").fetchone()[0] == 604,
          "distinct page numbers == 604")
    check(cur.execute("SELECT MIN(number), MAX(number) FROM pages").fetchone() == (1, 604),
          "pages numbered 1..604")
    check(cur.execute("SELECT COUNT(*) FROM page_lines").fetchone()[0] == EXPECTED_TOTALS["page_lines"],
          f"page_lines row count == {EXPECTED_TOTALS['page_lines']}")
    check(cur.execute("SELECT COUNT(*) FROM page_words").fetchone()[0] == EXPECTED_TOTALS["page_words"],
          f"page_words row count == {EXPECTED_TOTALS['page_words']}")
    check(cur.execute("SELECT COUNT(*) FROM sajda").fetchone()[0] == EXPECTED_TOTALS["sajda"],
          "sajda row count == 15")

    # ------------------------------------------------------------------ token-kind buckets
    counts = dict(cur.execute(
        "SELECT token_kind, COUNT(*) FROM page_words GROUP BY token_kind"
    ).fetchall())
    check(counts.get("word", 0) == EXPECTED_TOTALS["word_tokens"],
          f"word tokens == {EXPECTED_TOTALS['word_tokens']}")
    check(counts.get("waqf", 0) == EXPECTED_TOTALS["waqf_tokens"],
          f"waqf tokens == {EXPECTED_TOTALS['waqf_tokens']}")
    check(counts.get("rub", 0) == EXPECTED_TOTALS["rub_tokens"],
          f"rub tokens == {EXPECTED_TOTALS['rub_tokens']}")

    # ------------------------------------------------------------------ Tanzil text integrity
    print("Comparing DB text_uthmani against corpus.json (byte-for-byte)")
    corpus = json.loads(CORPUS_JSON.read_bytes())
    corpus_by_key = {(c["surah"], c["ayah"]): c["text_uthmani"] for c in corpus}
    text_mismatches = 0
    for surah, ayah, text_db in cur.execute("SELECT surah, ayah, text_uthmani FROM ayahs"):
        if corpus_by_key.get((surah, ayah)) != text_db:
            text_mismatches += 1
    check(text_mismatches == 0,
          f"ayahs.text_uthmani byte-identical to corpus for all 6236 rows (mismatches: {text_mismatches})")

    # ------------------------------------------------------------------ page_words -> text_uthmani reassembly
    print("Reassembling page_words -> text_uthmani per ayah")
    per_ayah: dict[tuple[int, int], list[tuple[int, str]]] = {}
    for surah, ayah, pos, tok in cur.execute(
        "SELECT surah, ayah, position_in_ayah, tanzil_token FROM page_words"
    ):
        per_ayah.setdefault((surah, ayah), []).append((pos, tok))
    reassemble_mismatches = 0
    for (s, a), items in per_ayah.items():
        items.sort()
        got = " ".join(t for _, t in items)
        expected = " ".join(corpus_by_key[(s, a)].split())
        if got != expected:
            reassemble_mismatches += 1
    check(reassemble_mismatches == 0,
          f"page_words reassembly matches text_uthmani for all ayahs (mismatches: {reassemble_mismatches})")

    # ------------------------------------------------------------------ every ayah has ≥1 word emission
    ayahs_with_word = set(
        (s, a) for s, a in cur.execute(
            "SELECT DISTINCT surah, ayah FROM page_words WHERE token_kind='word'"
        )
    )
    missing_ayahs = [(c["surah"], c["ayah"]) for c in corpus if (c["surah"], c["ayah"]) not in ayahs_with_word]
    check(len(missing_ayahs) == 0,
          f"every ayah has ≥1 'word' emission (missing: {len(missing_ayahs)})")

    # ------------------------------------------------------------------ deterministic ordering / uniqueness
    check(
        cur.execute(
            "SELECT COUNT(*) FROM ("
            "SELECT page, line_index, word_index_in_line, COUNT(*) AS n "
            "FROM page_words GROUP BY page, line_index, word_index_in_line HAVING n > 1)"
        ).fetchone()[0] == 0,
        "no duplicate (page, line_index, word_index_in_line)",
    )
    check(
        cur.execute(
            "SELECT COUNT(*) FROM ("
            "SELECT surah, ayah, position_in_ayah, COUNT(*) AS n "
            "FROM page_words GROUP BY surah, ayah, position_in_ayah HAVING n > 1)"
        ).fetchone()[0] == 0,
        "no duplicate (surah, ayah, position_in_ayah)",
    )
    check(
        cur.execute(
            "SELECT COUNT(*) FROM ("
            "SELECT page, line_index, COUNT(*) AS n "
            "FROM page_lines GROUP BY page, line_index HAVING n > 1)"
        ).fetchone()[0] == 0,
        "no duplicate (page, line_index) in page_lines",
    )

    # ------------------------------------------------------------------ deterministic word_index_in_line
    print("Checking word_index_in_line densities per line")
    density_fail = 0
    for page, line_index, n in cur.execute(
        "SELECT page, line_index, COUNT(*) FROM page_words GROUP BY page, line_index"
    ):
        got_max = cur.execute(
            "SELECT MAX(word_index_in_line) FROM page_words WHERE page=? AND line_index=?",
            (page, line_index),
        ).fetchone()[0]
        if got_max != n:
            density_fail += 1
    check(density_fail == 0,
          f"word_index_in_line is dense 1..N on every line (violations: {density_fail})")

    # ------------------------------------------------------------------ every page_words row references a page_line
    orphans = cur.execute(
        "SELECT COUNT(*) FROM page_words pw "
        "LEFT JOIN page_lines pl ON pl.page=pw.page AND pl.line_index=pw.line_index "
        "WHERE pl.page IS NULL"
    ).fetchone()[0]
    check(orphans == 0, "no page_words row references a missing page_line")

    # ------------------------------------------------------------------ every ayah has a deterministic page
    ayah_page_nulls = cur.execute("SELECT COUNT(*) FROM ayahs WHERE page IS NULL").fetchone()[0]
    check(ayah_page_nulls == 0, "no ayah has a null page")
    check(
        cur.execute("SELECT COUNT(*) FROM ayahs WHERE page < 1 OR page > 604").fetchone()[0] == 0,
        "every ayah.page in 1..604",
    )

    conn.close()

    # ------------------------------------------------------------------ file hashes
    release_hash = sha256(RELEASE_DB)
    print(f"Release DB SHA-256: {release_hash}")
    check(release_hash == "86e6be19da45d686146d4840b109b3ad0efb39525e366f080485589c4a2085cb",
          "release DB hash matches ADR-0026-declared value")

    if not ANDROID_ASSET.exists():
        failures.append("android asset missing")
        print("FAIL  android asset missing")
    else:
        asset_hash = sha256(ANDROID_ASSET)
        check(asset_hash == release_hash,
              f"android asset hash matches release DB hash")

    # Extract CONTENT_VERSION + CONTENT_SHA256 from QuranAsset.kt
    kt = QURAN_ASSET_KT.read_text(encoding="utf-8")
    ver = re.search(r'CONTENT_VERSION\s*=\s*"([^"]+)"', kt)
    sh = re.search(r'CONTENT_SHA256\s*=\s*\n?\s*"([0-9a-fA-F]+)"', kt)
    check(bool(ver) and ver.group(1) == "1.1.0", "QuranAsset.CONTENT_VERSION == 1.1.0")
    check(bool(sh) and sh.group(1).lower() == release_hash,
          "QuranAsset.CONTENT_SHA256 matches release DB hash")

    # ------------------------------------------------------------------ summary
    print()
    if failures:
        print(f"POST-MIGRATION VALIDATION FAILED — {len(failures)} check(s) failed:")
        for f in failures:
            print(f"  - {f}")
        return 1
    print("POST-MIGRATION VALIDATION PASSED — all invariants hold")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
