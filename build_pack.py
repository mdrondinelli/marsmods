#!/usr/bin/env python3
"""Build the local mod projects and collect pack jars in ./build."""

from __future__ import annotations

import argparse
import os
import shutil
import subprocess
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parent
PACK_BUILD_DIR = REPO_ROOT / "build"
EXTERNAL_DIR = REPO_ROOT / "external"
DEFAULT_JAVA_HOME = Path("/usr/lib/jvm/java-25-openjdk-amd64")

MOD_PROJECTS = (
    "marsflinttool",
    "marshemp",
    "marsstoneage",
    "marsfoodspoilage",
    "marsworbloodfx",
    "marscampfires",
    "marstorches",
    "marssleepingoverhaul",
    "marswildcrops",
)

NON_PACK_JAR_SUFFIXES = ("-sources.jar", "-javadoc.jar", "-dev.jar", "-plain.jar")


def parse_gradle_properties(path: Path) -> dict[str, str]:
    properties: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        properties[key.strip()] = value.strip()
    return properties


def gradle_environment(gradle_user_home: Path | None) -> dict[str, str]:
    env = os.environ.copy()
    if gradle_user_home is not None:
        env["GRADLE_USER_HOME"] = str(gradle_user_home)
    if "JAVA_HOME" not in env and DEFAULT_JAVA_HOME.is_dir():
        env["JAVA_HOME"] = str(DEFAULT_JAVA_HOME)
        env["PATH"] = f"{DEFAULT_JAVA_HOME / 'bin'}{os.pathsep}{env.get('PATH', '')}"
    return env


def run_gradle_build(project_dir: Path, gradle_user_home: Path | None) -> None:
    subprocess.run(
        ["./gradlew", "build"],
        cwd=project_dir,
        env=gradle_environment(gradle_user_home),
        check=True,
    )


def expected_pack_jar(project_dir: Path) -> Path:
    properties = parse_gradle_properties(project_dir / "gradle.properties")
    mod_id = properties.get("mod_id")
    mod_version = properties.get("mod_version")

    if mod_id and mod_version:
        expected = project_dir / "build" / "libs" / f"{mod_id}-{mod_version}.jar"
        if expected.exists():
            return expected

    jars = sorted(
        jar
        for jar in (project_dir / "build" / "libs").glob("*.jar")
        if not jar.name.endswith(NON_PACK_JAR_SUFFIXES)
    )
    if len(jars) == 1:
        return jars[0]
    if not jars:
        raise FileNotFoundError(f"No pack jar found in {project_dir / 'build' / 'libs'}")
    raise RuntimeError(
        f"Multiple candidate pack jars found in {project_dir / 'build' / 'libs'}: "
        + ", ".join(jar.name for jar in jars)
    )


def refresh_pack_build_dir() -> None:
    PACK_BUILD_DIR.mkdir(exist_ok=True)
    for jar in PACK_BUILD_DIR.glob("*.jar"):
        jar.unlink()


def copy_external_jars() -> list[Path]:
    if not EXTERNAL_DIR.is_dir():
        return []

    copied: list[Path] = []
    for jar in sorted(EXTERNAL_DIR.glob("*.jar")):
        destination = PACK_BUILD_DIR / jar.name
        shutil.copy2(jar, destination)
        copied.append(destination)
    return copied


def copy_pack_jars(project_dirs: list[Path]) -> list[Path]:
    copied: list[Path] = []
    refresh_pack_build_dir()
    for project_dir in project_dirs:
        jar = expected_pack_jar(project_dir)
        destination = PACK_BUILD_DIR / jar.name
        shutil.copy2(jar, destination)
        copied.append(destination)
    copied.extend(copy_external_jars())
    return copied


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Build all pack mods and collect their distributable jars in ./build."
    )
    parser.add_argument(
        "--skip-build",
        action="store_true",
        help="Copy existing jars from module build directories without running Gradle.",
    )
    parser.add_argument(
        "--gradle-user-home",
        type=Path,
        help="Override GRADLE_USER_HOME. By default, the current environment or Gradle's normal ~/.gradle cache is used.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    project_dirs = [REPO_ROOT / project for project in MOD_PROJECTS]
    missing_projects = [str(path.relative_to(REPO_ROOT)) for path in project_dirs if not path.is_dir()]
    if missing_projects:
        print("Missing mod project directories: " + ", ".join(missing_projects), file=sys.stderr)
        return 1

    if not args.skip_build:
        for project_dir in project_dirs:
            print(f"Building {project_dir.name}...", flush=True)
            run_gradle_build(project_dir, args.gradle_user_home)

    copied = copy_pack_jars(project_dirs)
    print(f"Pack jars assembled in {PACK_BUILD_DIR.relative_to(REPO_ROOT)}:")
    for jar in copied:
        print(f"  {jar.relative_to(REPO_ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
