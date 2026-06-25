# Repository Guidelines

## Project Structure & Module Organization

Keep source code in `src/`, tests in `tests/`, assets in `assets/`, and contributor notes in `docs/` as the project grows. Keep `.agents/` and `.codex/` for local agent metadata.

## Build, Test, and Development Commands

Document project-specific commands here when a build system is added. Include install, run, test, and lint commands with one-line explanations.

## Coding Style & Naming Conventions

Follow the conventions of the language and framework used in this project. Prefer descriptive module names and small, focused files.

## Testing Guidelines

Add tests with new behavior and mirror the source layout where practical. Name tests after the behavior they verify.

## Commit & Pull Request Guidelines

Use clear imperative commit messages. Pull requests should include a short summary, test results, and relevant screenshots for UI changes.

## High-Risk System Changes

Before changing network topology, routing, firewall policy, remote-access exposure, or dangerous permissions on any Windows or Linux device, present the exact proposed changes, reasons, risks, and rollback plan for user approval. Read-only inspection is allowed first; do not implement until approval is explicit.
