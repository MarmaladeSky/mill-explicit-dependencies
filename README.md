# Mill Explicit Dependencies

[![Latest version](https://img.shields.io/badge/mill_explicit_dependencies-1.0.1-green?logo=scala&logoColor=red&label=Maven)](https://central.sonatype.com/artifact/digital.junkie/mill-explicit-dependencies_3/1.0.1)

This is a [Mill](https://github.com/com-lihaoyi/mill) Plugin similar to the most useful [SBT](https://github.com/sbt/sbt) [Plugin you ever saw](https://github.com/cb372/sbt-explicit-dependencies).

## Compatibility

The plugin was tested and verified for any combination of:

| Scala   | Mill  |
|---------|-------|
| 2.13.18 | 1.0.5 |
| 3.3.7   | 1.0.6 |
| 3.8.2   | 1.1.0 |
|         | 1.1.1 |
|         | 1.1.2 |

## Usage

Set up the plugin at your **build.mill**:

```scala
//| mill-version: 1.0.5
//| mvnDeps: [
//|   "digital.junkie::mill-explicit-dependencies:1.0.1"
//| ]

object myproject extends ScalaModule with ExplicitDependencies {

  // [optional] filter out unwanted results
  override def undeclaredCompileDependenciesFilter: Seq[Dep] = Seq.empty
  override def unusedCompileDependenciesFilter: Seq[Dep] = Seq.empty
  override def undeclaredCompileModulesFilter: Seq[Module] = Seq.empty
  override def unusedCompileModulesFilter: Seq[Module] = Seq.empty
}
```

Run the tasks:

```shell
# find all undeclared dependencies
mill __.undeclaredCompileDependencies
# fail if there are undeclared dependencies
mill __.undeclaredCompileDependenciesTest

# find all unused dependencies
mill __.unusedCompileDependencies
# fail if there are unused dependencies
mill __.unusedCompileDependenciesTest
```
