# Claude-Specific Documentation

This directory contains documentation specifically organized for Claude AI's consumption when working on the PotatoClient codebase.

## Contents

- **kotlin-subprocess.md** - Detailed documentation about the Kotlin subprocess architecture, including:
  - Video streaming implementation
  - GStreamer pipeline details
  - Performance optimizations
  - Hardware decoder configuration
  - Build integration

## Purpose

The main `CLAUDE.md` file in the project root focuses on Clojure development and overall project architecture. Language-specific implementation details are kept in separate files within this directory to:

1. Keep the main documentation focused and manageable
2. Allow Claude to load relevant context based on the task
3. Organize technical details by concern

## Usage

When working on:
- **Clojure/UI/General tasks** - Claude reads the main `CLAUDE.md`
- **Kotlin subprocess tasks** - Claude also reads `kotlin-subprocess.md`
- **Future specialized tasks** - Additional documentation can be added here

This structure helps Claude maintain appropriate context without being overwhelmed by irrelevant implementation details.