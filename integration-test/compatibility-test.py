#!/usr/bin/env python3
"""Matrix compatibility test for mill-explicit-dependencies plugin.

Publishes the plugin locally, then runs a sample integration project
against every (Mill version, Scala version) combination to verify
the plugin works correctly.
"""

import re
import subprocess
import sys
from pathlib import Path

# ── Matrix configuration ─────────────────────────────────────────────
MILL_VERSIONS = ["1.0.5", "1.0.6", "1.1.0", "1.1.1", "1.1.2"]
SCALA_VERSIONS = ["2.13.18", "3.3.7", "3.8.2"]

# ── Paths ─────────────────────────────────────────────────────────────
REPO_ROOT = Path(__file__).resolve().parent / ".."
INTEGRATION_DIR = REPO_ROOT / "./integration-test"
BUILD_FILE = INTEGRATION_DIR / "build.mill"
MILL_VERSION_FILE = INTEGRATION_DIR / ".mill-version"


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


def set_mill_version(version: str):
    """Write .mill-version in the integration test project."""
    MILL_VERSION_FILE.write_text(version + "\n")


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
    """Remove cached Mill output so each combination starts fresh."""
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

    results: list[tuple[str, str, str, bool, str]] = []

    for mill_ver in MILL_VERSIONS:
        for scala_ver in SCALA_VERSIONS:
            header = f"Mill {mill_ver} + Scala {scala_ver}"
            print(f"--- {header} ---")

            set_mill_version(mill_ver)
            set_scala_version(scala_ver)
            clean_integration_project()

            # unusedCompileDependencies — should detect fs2-core
            res = run_plugin_task("__.unusedCompileDependenciesTest")
            ok, msg = check_unused(res)
            status = "PASS" if ok else "FAIL"
            print(f"  unusedCompileDependencies: {status} — {msg}")
            results.append((mill_ver, scala_ver, "unused", ok, msg))

            # undeclaredCompileDependencies — should find nothing undeclared
            res = run_plugin_task("__.undeclaredCompileDependenciesTest")
            ok, msg = check_undeclared(res)
            status = "PASS" if ok else "FAIL"
            print(f"  undeclaredCompileDependencies: {status} — {msg}")
            results.append((mill_ver, scala_ver, "undeclared", ok, msg))

            print()

    # ── Summary ───────────────────────────────────────────────────────
    print("=== Matrix Results ===")
    all_passed = True
    for mill_ver, scala_ver, check, ok, _ in results:
        status = "PASS" if ok else "FAIL"
        if not ok:
            all_passed = False
        print(f"  Mill {mill_ver} + Scala {scala_ver:>8} | {check:>10}: {status}")

    print()
    if all_passed:
        print("All combinations passed!")
        return 0
    else:
        print("Some combinations failed!")
        return 1


if __name__ == "__main__":
    sys.exit(main())
