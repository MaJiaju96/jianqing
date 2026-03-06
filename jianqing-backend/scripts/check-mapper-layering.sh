#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAIN_JAVA_DIR="$BASE_DIR/src/main/java"

errors=0

while IFS= read -r -d '' file; do
  file_path="${file#${MAIN_JAVA_DIR}/}"

  if [[ "$file_path" == *"/mapper/"* ]]; then
    continue
  fi

  if [[ "$file_path" == *"/service/impl/"* ]]; then
    continue
  fi

  if grep -qE "import .*\.mapper\..*Mapper;" "$file"; then
    echo "[ERROR] 非 service.impl 层禁止直接引用 Mapper：$file"
    errors=$((errors + 1))
  fi
done < <(find "$MAIN_JAVA_DIR" -type f -name "*.java" -print0)

if [[ $errors -gt 0 ]]; then
  echo "[FAIL] Mapper 分层约束检查未通过，共发现 $errors 处违规。"
  exit 1
fi

echo "[PASS] Mapper 分层约束检查通过（仅 service.impl 可引用 Mapper）。"
