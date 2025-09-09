# Dungeons and AI

Gameplay [video](https://www.youtube.com/watch?v=5Vwz1Q-SAQg&ab_channel=AgrYpn1a).

## Project tools and versions
The output of `java --version` below:
```
openjdk 21.0.7 2025-04-15
OpenJDK Runtime Environment Homebrew (build 21.0.7)
OpenJDK 64-Bit Server VM Homebrew (build 21.0.7, mixed mode, sharing)
```

You may use
```
./gradlew eclipse
```
to generate project files for Eclipse or Emacs (if you're using eglot) and here's minimal config that works for me in Doom Emacs:
```elisp
;; Java
(use-package eglot-java
  :ensure t
  :hook (java-mode . eglot-java-mode))

(add-hook 'java-mode-hook 'eglot-java-mode)
(with-eval-after-load 'eglot-java
  (define-key eglot-java-mode-map (kbd "C-c l n") #'eglot-java-file-new)
  (define-key eglot-java-mode-map (kbd "C-c l x") #'eglot-java-run-main)
  (define-key eglot-java-mode-map (kbd "C-c l t") #'eglot-java-run-test)
  (define-key eglot-java-mode-map (kbd "C-c l N") #'eglot-java-project-new)
  (define-key eglot-java-mode-map (kbd "C-c l T") #'eglot-java-project-build-task)
  (define-key eglot-java-mode-map (kbd "C-c l R") #'eglot-java-project-build-refresh))

(setq eglot-events-buffer-size 2000000)
(setq eglot-connect-timeout 120)
(setq eglot-java-debug t)
```

The [eglot-java](https://github.com/yveszoundi/eglot-java) relies on Eclipse JDT Language Server.

This project uses `gradle` build system and is desktop only.

## How to run the project
You may run the project from CLI from the root directory by executing:
```
./gradlew lwjgl3:run --args="--offline"
```

In order to run multiplayer first run the server:
```
./gradlew lwjgl3:run --args="--server"
```

Then run two clients with:
```
./gradlew lwjgl3:run
```

Clients should connect to the server and matchmaking should automatically happen. Observe server log for any errors.

-----

# LibGDX default README below

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
