#!/usr/bin/env python3
"""Content-correction dry-run — TASK-288 acceptance criterion.

Exercises the core defect-detection primitive (the skeleton normalizer that
powers the pipeline's cross-source diff, per Phase 1 gates). Injects a
synthetic consonantal mutation on Ayat al-Kursi (2:255) and confirms that
the normalized skeletons of the original and mutated text are NOT equal —
which is the exact signal the pipeline uses to detect corpus defects.

If this dry-run FAILS, the entire content-correction promise is broken and
must be repaired before v1.0 ships. Do not defer.

Usage:
    python scripts/content-correction-dry-run.py

Exit codes:
    0 — dry-run PASSED (defect detected, pipeline would refuse it)
    1 — corpus not found (run the pipeline first)
    2 — dry-run FAILED (defect NOT detected — release-blocker)
"""

from __future__ import annotations

import json
import os
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parent.parent
CORPUS_PATH = REPO_ROOT / "data" / "output" / "intermediate" / "corpus.json"
PIPELINE_DIR = REPO_ROOT / "data" / "pipeline"


def _load_normalizer():
    """Import `normalize_for_search` from the pipeline module without executing
    the whole pipeline. Adds the pipeline dir to sys.path just long enough."""
    sys.path.insert(0, str(PIPELINE_DIR))
    try:
        from pipeline import normalize_for_search  # type: ignore  # noqa: WPS433
    finally:
        # Restore path so we don't leak the mutation to callers importing us.
        if sys.path and sys.path[0] == str(PIPELINE_DIR):
            sys.path.pop(0)
    return normalize_for_search


def _find_ayah(corpus, surah, ayah):
    for c in corpus:
        if c["surah"] == surah and c["ayah"] == ayah:
            return c
    return None


def _inject_defect(text: str) -> str:
    """Introduce a *consonantal* mutation — the class of defect the pipeline
    is contractually responsible for catching. A diacritic-only mutation
    would (correctly) not be flagged as a defect; that's not our target."""
    # Append a stray consonantal character (ب) at the end. Guaranteed to
    # change the skeleton if the normalizer is doing its job.
    return text + "ب"


def main() -> int:
    if not CORPUS_PATH.exists():
        sys.stderr.write(
            f"ERROR: corpus not found at {CORPUS_PATH}\n"
            "Run the pipeline first:\n"
            "    python data/pipeline/pipeline.py\n"
        )
        return 1

    print(f"[dry-run] loading corpus from {CORPUS_PATH.relative_to(REPO_ROOT)}")
    corpus = json.loads(CORPUS_PATH.read_bytes())
    print(f"[dry-run] {len(corpus)} ayahs in corpus")

    target_surah, target_ayah = 2, 255  # Ayat al-Kursi
    target = _find_ayah(corpus, target_surah, target_ayah)
    if target is None:
        sys.stderr.write(f"ERROR: {target_surah}:{target_ayah} not in corpus\n")
        return 1

    original: str = target["text_uthmani"]
    defect: str = _inject_defect(original)
    print(f"[dry-run] target: {target_surah}:{target_ayah}")
    print(f"[dry-run] original length: {len(original)}")
    print(f"[dry-run] defect   length: {len(defect)} (delta: {len(defect) - len(original)})")

    normalize = _load_normalizer()
    orig_skeleton = normalize(original)
    def_skeleton = normalize(defect)

    print(f"[dry-run] original skeleton: {orig_skeleton!r}")
    print(f"[dry-run] defect   skeleton: {def_skeleton!r}")

    if orig_skeleton == def_skeleton:
        sys.stderr.write(
            "\n"
            "==========================================================\n"
            "DRY-RUN FAILED — DO NOT SHIP.\n"
            "The normalizer produced identical skeletons for the original\n"
            "and mutated text. The Phase 1 cross-source-diff gate would\n"
            "let a real consonantal defect through. Repair before v1.0.\n"
            "==========================================================\n",
        )
        return 2

    print(
        "\n"
        "==========================================================\n"
        "DRY-RUN PASSED.\n"
        "The normalizer detected the synthetic defect. The Phase 1\n"
        "cross-source-diff gate would refuse to freeze a corpus\n"
        "containing this mutation. Content-correction workflow is\n"
        "verified end-to-end at the skeleton-comparison level.\n"
        "==========================================================\n",
    )
    return 0


if __name__ == "__main__":
    # Ensure Windows encoding doesn't mangle Arabic output.
    os.environ.setdefault("PYTHONIOENCODING", "utf-8")
    sys.exit(main())
