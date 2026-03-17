# Local OpenCode / oh-my-opencode Stable Setup

## Current Versions

- OpenCode: `1.2.15`
- oh-my-opencode: `3.11.2`

## Key User Config Files

- OpenCode main config: `~/.config/opencode/opencode.jsonc`
- oh-my-opencode config: `~/.config/opencode/oh-my-opencode.json`

## Plugin Configuration

`opencode.jsonc` should include `oh-my-opencode` in the `plugin` array.

## Verified Working Capabilities

- `gh` authenticated
- `ast-grep` available
- `comment-checker` available
- built-in MCPs visible to doctor: `context7`, `grep_app`
- Java + Vue LSP works in real project files

## Explicit LSP Commands

Configured in `~/.config/opencode/oh-my-opencode.json`:

- TypeScript: `/opt/homebrew/bin/typescript-language-server --stdio`
- Vue: `/opt/homebrew/bin/vue-language-server --stdio`
- ESLint: `/opt/homebrew/bin/vscode-eslint-language-server --stdio`
- Java: `/opt/homebrew/bin/jdtls`

## Useful Verification Commands

```bash
oh-my-opencode doctor --status
gh auth status
opencode models --refresh
opencode run "Reply with exactly OK"
```

## Notes

- `explore` / `librarian` / `oracle` are subagents; do not expect `opencode run --agent <name>` to start them as primary agents.
- `opencode mcp list` shows user-configured MCP servers only. `context7` and `grep_app` are builtin MCPs reported by `oh-my-opencode doctor`.
- `doctor` may still show `LSP 1/4`; real LSP functionality is the authoritative check.

## Acceptance Results

- `/init-deep`: passed
- `/refactor`: passed
- `/start-work`: passed
- `/handoff`: passed with correct empty-session behavior
