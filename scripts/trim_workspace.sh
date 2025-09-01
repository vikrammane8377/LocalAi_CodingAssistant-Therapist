#!/usr/bin/env bash
set -euo pipefail

# Trim local build artifacts to reclaim disk space without changing functionality.
# Safe to run anytime. It only deletes transient build outputs.

ROOT_DIR="$(cd "$(dirname "$0")"/.. && pwd)"
cd "$ROOT_DIR"

echo "Trimming build artifacts under: $ROOT_DIR"

rm -rf "ProgrammingAiLocal/app/build" || true
rm -rf "ProgrammingAiLocal/build" || true
rm -rf "Programming__AiLocal/app/build" || true
rm -rf "Programming__AiLocal/build" || true
rm -rf "gallery/Android/src/app/build" || true

echo "Done. Current sizes:"
du -sh ProgrammingAiLocal/app/build Programming__AiLocal/app/build gallery/Android/src/app/build 2>/dev/null || true


