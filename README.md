# Arkanoid Roguelike

A JavaFX brick-breaker with five progressively harder stages, persistent run upgrades, and a timing-based Perfect Parry. Gameplay uses a fixed 960 × 640 logical playfield and scales with letterboxing when the window is resized.

## Features

- Arcade-style menu, instructions, pause, stage transition, upgrade, game-over, and victory screens.
- Keyboard and mouse paddle movement, sub-stepped ball physics, normal/strong/bonus bricks, score, and lives.
- Six persistent run upgrades: wider paddle, extra life, multiball, bonus score, faster paddle movement, and Parry Mastery.
- Optional sound effects loaded asynchronously; the game remains playable when assets or audio support are unavailable.
- Rolling Logback file logs and JUnit 5 tests for the core mechanics.

## Prerequisites and commands

- JDK 21 or later (the source and Maven compiler target Java 21).
- Maven 3.8.5 or later, or use the included Maven Wrapper.
- A desktop environment capable of running JavaFX.

The Maven project is in `Shadow_Arkanoid`. From the repository root, run these Windows commands:

```powershell
cd Shadow_Arkanoid
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd javafx:run
.\mvnw.cmd clean package
```

On macOS/Linux, use `./mvnw` in place of `.\mvnw.cmd`. A local Maven installation can be used by replacing the wrapper with `mvn`.

The package goal creates `target/shadow-arkanoid-1.0.0.jar`, which is **not** a self-contained JavaFX application. The supported launch command resolves JavaFX and other dependencies through Maven: `mvn javafx:run` (or the wrapper equivalent).

## Controls and rules

| Input | Action |
|---|---|
| A / Left Arrow | Move left |
| D / Right Arrow | Move right |
| Mouse movement | Move the paddle |
| Space / Enter / Left Click | Launch the attached ball |
| Right Mouse Button | Attempt a Perfect Parry |
| Escape / P | Pause or resume |

Destroy every brick to complete a stage and select one of three distinct upgrade choices. Normal bricks take one hit, strong bricks take two, and bonus bricks award extra points. The run ends after three lost lives or a victory after stage five. Paddle, score, extra-life, and Parry Mastery upgrades persist for the current run; multiball adds an additional ball.

### Perfect Parry and piercing

Right-click once as a descending ball approaches the paddle. The default prediction window is 125 ms; the ball must also be on course to hit the paddle. A successful Perfect Parry awards **100 points**, increases ball speed by **1.25×** (up to the configured cap), and activates piercing. The parry cooldown is 1.5 seconds.

While piercing, the ball damages and destroys successive bricks without bouncing from them. Each brick can award destruction points only once, including strong bricks' durability rules. **Piercing remains active through brick hits, brick destruction, and paddle contact. It ends only at the first top, left, or right playfield boundary collision.** The increased speed remains after piercing ends. A missed or early Parry has no score, speed, or power effect.

## Architecture

- `model`: game state, ball, paddle, bricks, and `GameWorld` orchestration.
- `physics`: sub-stepped collision handling and bounce resolution, independent of the JavaFX window.
- `system`: scoring, lives, parry timing, and stage generation.
- `upgrade`: composable upgrade implementations and random distinct choice selection.
- `controller` / `ui`: one JavaFX `AnimationTimer`, input handlers, and a Canvas renderer.
- `audio`: optional asynchronous `AudioClip` loading and playback.

Core physics and game-state rules are covered by headless JUnit tests under `Shadow_Arkanoid/src/test`.

## Logging and optional assets

Logback writes to `Shadow_Arkanoid/logs/arkanoid.log` and rotates compressed daily files with a size limit. The `logs/` directory is ignored by Git.

Optional sound assets are loaded from `Shadow_Arkanoid/src/main/resources/se233/shadow_arkanoid/assets/`. The supplied MP3 files are used for life loss, stage completion, and the Parry cue. To add other effects, add MP3 files under `assets/audio/` using the paths in `SoundManager` (`brick-hit.mp3`, `brick-break.mp3`, `paddle-hit.mp3`, `piercing-end.mp3`, and `upgrade.mp3`). Audio is never required to start the game. The existing picture asset is optional; the game is drawn with JavaFX shapes and does not fetch external assets.
