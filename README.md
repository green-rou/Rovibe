# Rovibe

A terminal-style sound scripting app for Android. Write short scripts to synthesize and sequence audio in real time - no audio files, no samples, no DAW required.

---

## Overview

Rovibe provides a command-line interface for composing audio. Each line of a script is a sound command. Commands are rendered to PCM audio, mixed, and played back through the Android `AudioTrack` API at 44 100 Hz.

The editor features syntax highlighting, autocomplete suggestions, live parameter sliders, and inline visualizers - all inside a monospace terminal UI.

---

## Command Reference

### Sound generators

| Command | Example | Description |
|---|---|---|
| `play` | `play(440hz, 1s)` | Sine wave at the given frequency and duration |
| `square` | `square(220hz, 0.5s)` | Square wave at the given frequency and duration |
| `noise` | `noise(0.5s)` | White noise for the given duration |
| `pause` | `pause(1s)` | Silence for the given duration |

### Rhythm patterns

Each pattern command takes a space-separated sequence of `1` (hit) and `0` (rest). The tempo is set globally with `tempo()`.

| Command | Default hit type |
|---|---|
| `bit` | Sine wave, 1000 Hz |
| `bass` | Sine wave, 60 Hz |
| `snare` | White noise burst |
| `hihat` | Short white noise |
| `clap` | White noise burst |
| `tom` | Sine wave, 110 Hz |
| `crash` | Long white noise |

Example:

```
tempo(140)
bass(1 0 0 0 1 0 0 0)
snare(0 0 1 0 0 0 1 0)
hihat(1 1 1 1 1 1 1 1)
```

### Piano roll

```
piano(1 5 8 12)
```

Plays a sequence of notes from a 52-key white-key piano starting at C3 (MIDI 48). Notes are numbered 1–52 and played sequentially at the current tempo.

### Modifiers

Modifiers are chained onto a command using dot notation.

```
bit(1 0 1 0 1 0 1 0).volume(0.6).tempo(120)
```

| Modifier | Description |
|---|---|
| `.volume(n)` | Sets output volume. Float `0.0`–`1.0` for attenuation, integer `1`–`100` for amplification |
| `.tempo(bpm)` | Overrides the current tempo for this command |
| `.loop(interval, times)` | Repeats the command with a gap between iterations |
| `.repeat(n)` | Repeats the command immediately, n times |
| `.reverse()` | Reverses the rendered audio buffer |

Modifiers can also be written on a separate line prefixed with `.` to apply to the command on the previous line:

```
bass(1 0 1 0 1 0 1 0)
.volume(0.4)
.repeat(2times)
```

### Global state

```
volume(0.8)
tempo(120)
```

`volume` and `tempo` set state that applies to all subsequent commands until overridden.

### Sequencing

By default all commands play simultaneously (mixed). Use sequencing markers to control timing.

| Command | Behavior |
|---|---|
| `after` | The single command immediately below starts only after the command immediately above finishes |
| `after_all` | All commands below start only after every command above the marker has finished |

```
bass(1 0 1 0)
after
snare(0 1 0 1)
```

```
bass(1 0 1 0 1 0 1 0)
hihat(1 1 1 1 1 1 1 1)
after_all
piano(1 3 5 8 10)
```

### Interactive slider

Wrap any numeric value in `slider()` to replace it with a live drag slider in the editor. Releasing the slider restarts playback with the updated value.

```
play(440hz, 1s).volume(slider(0.5))
```

### Visualizers

Visualizer commands render an animated Canvas overlay spanning three lines directly below the command.

| Command | Visual |
|---|---|
| `wave()` | Animated sine waveform, amplitude-reactive |
| `bars()` | Vertical bar spectrum, amplitude-reactive |
| `piano(...)` | Scrolling FL Studio-style piano roll |

### Comments

Lines beginning with `#` are displayed in gray and ignored during playback.

```
# kick pattern
bass(1 0 0 0 1 0 0 0)
```

---

## Editor

- Start typing a command name and a suggestion bar appears below the keyboard. Tap a suggestion to insert the full command with its arguments selected.
- When the cursor is inside a numeric argument, a live slider appears. Drag it to adjust the value and hear the change on release.
- Tap the play button in the top bar to render and play the script. Tap again to pause or resume.
- Scripts are saved to the device and listed on the home screen. Long-press a script to rename it.
- Prefix a line with `#` to disable it without deleting it.
- Start a line with `.` to attach a modifier to the command on the line above instead of writing everything on one line.

---

## Timing

Each step in a pattern is one eighth note at the current tempo. At 120 BPM one step lasts 250 ms, so an eight-step pattern plays in one bar.

```
tempo(120)
bass(1 0 0 0 1 0 0 0)  # quarter notes on beats 1 and 2
hihat(1 0 1 0 1 0 1 0) # eighth notes on every beat
```

By default all commands are mixed and play from time zero. Use `after` and `after_all` to chain them in time.

---

## Examples

**Minimal beat**

```
tempo(128)
bass(1 0 0 0 1 0 0 0)
snare(0 0 1 0 0 0 1 0)
hihat(1 1 1 1 1 1 1 1)
```

**Melody then bass**

```
tempo(100)
piano(1 3 5 8 10 8 5 3)
after_all
bass(1 0 1 0 1 0 1 0)
```

**Chained melody**

```
tempo(90)
piano(1 3 5)
after
piano(8 10 12)
after
piano(15 17 19)
```

**Beat with crash on the drop**

```
tempo(130)
# build-up
bass(1 0 0 0 1 0 0 0)
hihat(1 0 1 0 1 0 1 0)
after_all
# drop
crash(1 0 0 0 0 0 0 0)
snare(1 1 1 1 1 1 1 1)
```

**Live volume tweaking**

```
tempo(120)
bass(1 0 1 0 1 0 1 0).volume(slider(0.8))
snare(0 0 1 0 0 0 1 0)
```

**Ambient tone**

```
play(220hz, 3s).volume(0.3)
play(330hz, 3s).volume(0.2)
play(440hz, 3s).volume(0.15)
```
