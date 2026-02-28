#!/usr/bin/env python3
"""Scala version compatibility test for mill-explicit-dependencies plugin.

Publishes the plugin locally, then runs a sample integration project
against every Scala version to verify the plugin works correctly.
"""

import re
import subprocess
import sys
from pathlib import Path

# ── Matrix configuration ─────────────────────────────────────────────
SCALA_VERSIONS = ["2.13.18", "3.3.7", "3.8.2"]

# ── Paths ─────────────────────────────────────────────────────────────
REPO_ROOT = Path(__file__).resolve().parent / ".."
INTEGRATION_DIR = REPO_ROOT / "./integration-test"
BUILD_FILE = INTEGRATION_DIR / "build.mill"


def run(args: list[str], cwd: Path, check: bool = True, **kwargs):
    """Run a subprocess, printing the command for visibility."""
    print(f"  $ {' '.join(args)}")
    return subprocess.run(args, cwd=cwd, check=check, **kwargs)


def publish_plugin_locally():
    """Publish the plugin to the local Ivy repository."""
    print("Publishing plugin locally...")
    run(
        ["./mill", "mill-explicit-dependencies.publishLocal"],
        cwd=REPO_ROOT,
    )
    print()


def set_scala_version(version: str):
    """Rewrite scalaVersion in the integration test build.mill."""
    content = BUILD_FILE.read_text()
    content = re.sub(
        r'def scalaVersion = ".*?"',
        f'def scalaVersion = "{version}"',
        content,
    )
    BUILD_FILE.write_text(content)


def clean_integration_project():
    """Remove cached Mill output so each run starts fresh."""
    out_dir = INTEGRATION_DIR / "out"
    if out_dir.exists():
        run(["rm", "-rf", str(out_dir)], cwd=INTEGRATION_DIR)


def run_plugin_task(task: str) -> subprocess.CompletedProcess:
    """Run a Mill task inside the integration test project."""
    return run(
        ["./mill", task],
        cwd=INTEGRATION_DIR,
        check=False,
        capture_output=True,
        text=True,
    )


def check_unused(result: subprocess.CompletedProcess) -> tuple[bool, str]:
    """Verify that unusedCompileDependenciesTest reports fs2-core."""
    output = result.stdout + result.stderr
    if result.returncode != 0 and "fs2-core" in output:
        return True, "correctly detected fs2-core as unused"
    if result.returncode == 0:
        return False, "expected failure (unused dep) but command succeeded"
    return False, f"command failed unexpectedly:\n{output}"


def check_undeclared(result: subprocess.CompletedProcess) -> tuple[bool, str]:
    """Verify that undeclaredCompileDependenciesTest succeeds (nothing undeclared)."""
    output = result.stdout + result.stderr
    if result.returncode == 0:
        return True, "no undeclared dependencies (as expected)"
    return False, f"unexpected undeclared dependencies:\n{output}"


def main():
    publish_plugin_locally()

    results: list[tuple[str, str, bool, str]] = []

    for scala_ver in SCALA_VERSIONS:
        print(f"--- Scala {scala_ver} ---")

        set_scala_version(scala_ver)
        clean_integration_project()

        # unusedCompileDependencies — should detect fs2-core
        res = run_plugin_task("__.unusedCompileDependenciesTest")
        ok, msg = check_unused(res)
        status = "PASS" if ok else "FAIL"
        print(f"  unusedCompileDependencies: {status} — {msg}")
        results.append((scala_ver, "unused", ok, msg))

        # undeclaredCompileDependencies — should find nothing undeclared
        res = run_plugin_task("__.undeclaredCompileDependenciesTest")
        ok, msg = check_undeclared(res)
        status = "PASS" if ok else "FAIL"
        print(f"  undeclaredCompileDependencies: {status} — {msg}")
        results.append((scala_ver, "undeclared", ok, msg))

        print()

    # ── Summary ───────────────────────────────────────────────────────
    print("=== Results ===")
    all_passed = True
    for scala_ver, check, ok, _ in results:
        status = "PASS" if ok else "FAIL"
        if not ok:
            all_passed = False
        print(f"  Scala {scala_ver:>8} | {check:>10}: {status}")

    print()
    if all_passed:
        print("All versions passed!")
        return 0
    else:
        print("Some versions failed!")
        return 1


if __name__ == "__main__":
    sys.exit(main())
