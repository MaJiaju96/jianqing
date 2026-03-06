#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GIT_ROOT="$(cd "$BASE_DIR" && git rev-parse --show-toplevel)"
HOOK_DIR="$GIT_ROOT/.git/hooks"
HOOK_FILE="$HOOK_DIR/pre-commit"

mkdir -p "$HOOK_DIR"

cat > "$HOOK_FILE" <<'HOOK'
#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
bash "$REPO_ROOT/jianqing-backend/scripts/pre-commit-check.sh"
HOOK

chmod +x "$HOOK_FILE"

echo "[ok] 已安装 pre-commit hook: $HOOK_FILE"
