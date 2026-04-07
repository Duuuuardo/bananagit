# BananaGit

**Git descomplicado para macacos 🐒** — a simplified Git GUI for people who just want to get things done. No terminal, no memorizing commands.

---

## Features

| Git command       | BananaGit          | Description                             |
|-------------------|--------------------|-----------------------------------------|
| `git add`         | Preparar           | Stage files for the next commit         |
| `git commit`      | Salvar Ponto       | Save a checkpoint in history            |
| `git commit --amend` | Emendar         | Fix the last commit                     |
| `git push`        | Enviar             | Send commits to the remote              |
| `git pull`        | Trazer             | Fetch and merge remote changes          |
| `git branch`      | Branch             | Create, switch, delete, merge branches  |
| `git tag`         | Tag                | Create and manage tags                  |
| `git stash`       | Guardar/Restaurar  | Temporarily shelve changes              |
| `git checkout --` | Descartar          | Discard unstaged file changes           |
| `git clone`       | Clonar             | Download a remote repository            |
| `git init`        | Novo Repositorio   | Create a repository from scratch        |
| `git status`      | Status             | See modified files with color coding    |
| `git log`         | Historico          | Browse past commits                     |
| `git diff`        | Diff               | View file changes with syntax coloring  |

## Interface

- **Home** -- Open, clone, or create repos. Recently opened repos saved for quick access.
- **Status tab** -- Visual file status, stage/unstage, discard changes, commit with message.
- **History tab** -- Scroll through past commits.
- **Branches tab** -- Create, switch, delete, merge branches. Create and manage tags.
- **Diff tab** -- View staged and unstaged changes per file with color-coded lines.
- **Stash** -- Toolbar buttons to save and restore work in progress.
- **Commit suggestions** -- Auto-generated message based on staged files.
- **Amend** -- Checkbox to fix the last commit message or content.
- **Settings** -- Configure GitHub token for private repo access.
- **Dark and light themes** -- Toggle with `Ctrl+T`.

## Tech stack

| Component              | Role                     |
|------------------------|--------------------------|
| Kotlin                 | Language                 |
| Compose for Desktop    | UI framework             |
| Material 3             | Design system            |
| JGit                   | Git operations           |
| Kotlinx Coroutines     | Async operations         |
| Gradle (Kotlin DSL)    | Build system             |

## Requirements

- Java 17+

## Running

```bash
git clone https://github.com/your-user/bananagit.git
cd bananagit
./gradlew run
```

Windows: `gradlew.bat run`

## Building installers

```bash
./gradlew packageDmg   # macOS
./gradlew packageMsi   # Windows
./gradlew packageDeb   # Linux
```

## Project structure

```
src/main/kotlin/com/bananagit/
    Main.kt                          Entry point
    theme/BananaTheme.kt             Dark and light color schemes
    git/GitModels.kt                 Domain models
    git/GitRepository.kt             JGit wrapper
    persistence/RecentRepos.kt       Recent repos storage
    persistence/Settings.kt          App settings storage
    viewmodel/RepositoryViewModel.kt State management (MVVM)
    ui/screens/HomeScreen.kt         Landing page
    ui/screens/RepositoryScreen.kt   Main workspace
    ui/components/Sidebar.kt         Tab navigation
    ui/components/FileStatusList.kt  Changed files with discard
    ui/components/CommitPanel.kt     Commit + amend
    ui/components/BranchSelector.kt  Branch dropdown
    ui/components/DiffView.kt        Diff viewer
    ui/dialogs/CloneDialog.kt        Clone dialog
    ui/dialogs/SettingsDialog.kt     Settings dialog
```

## Architecture

```
UI (Compose) --> ViewModel (AppState) --> Git Layer (JGit)
                                     --> Persistence (~/.bananagit/)
```

Single `AppState` data class drives the entire UI. The ViewModel dispatches async
operations via coroutines. The Git layer wraps JGit. Settings and recent repos are
persisted to `~/.bananagit/`.

## Keyboard shortcuts

| Shortcut            | Action           |
|---------------------|------------------|
| `Ctrl+R` / `F5`    | Refresh          |
| `Ctrl+Shift+P`     | Push             |
| `Ctrl+Shift+L`     | Pull             |
| `Ctrl+Shift+S`     | Stash            |
| `Ctrl+1`           | Status tab       |
| `Ctrl+2`           | History tab      |
| `Ctrl+3`           | Branches tab     |
| `Ctrl+4`           | Diff tab         |
| `Ctrl+T`           | Toggle theme     |

## Contributing

1. Fork
2. Create a branch
3. Commit
4. Open a PR

## License

MIT
