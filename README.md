# Mill Explicit Dependencies

[![Latest version](https://img.shields.io/badge/mill_explicit_dependencies-1.0.1-green?logo=scala&logoColor=red&label=Maven)](https://central.sonatype.com/artifact/digital.junkie/mill-explicit-dependencies_3/1.0.5)

This is a [Mill](https://github.com/com-lihaoyi/mill) Plugin similar to the most useful [SBT](https://github.com/sbt/sbt) [Plugin you ever saw](https://github.com/cb372/sbt-explicit-dependencies).

## Compatibility

The plugin is available since Mill 1.0.5 and tested against Scala Versions

| Scala   | 
|---------|
| 2.13.18 | 
| 3.3.7   | 
| 3.8.2   |
For older versions compatibility see [the tests results](#integration-tests-results)

## Usage

Set up the plugin at your **build.mill**:

```scala
//| mvnDeps: [
//|   "digital.junkie::mill-explicit-dependencies:$MILL_VERSION"
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

## Versioning
Plugin version are aligned with Mill's versioning, i.e. $MILL_VERSION may be used.
Plugin's bugfixes are delivered as x.y.z.λ.

## Integration Tests Results
| Test Result | Scala   |
|-------------|---------|
| FAIL        | 2.12.0  |
| PASS        | 2.12.1  |
| PASS        | 2.12.2  |
| FAIL        | 2.12.3  |
| FAIL        | 2.12.4  |
| PASS        | 2.12.5  |
| PASS        | 2.12.6  |
| PASS        | 2.12.7  |
| PASS        | 2.12.8  |
| PASS        | 2.12.9  |
| PASS        | 2.12.10 |
| PASS        | 2.12.11 |
| PASS        | 2.12.12 |
| PASS        | 2.12.13 |
| PASS        | 2.12.14 |
| PASS        | 2.12.15 |
| PASS        | 2.12.16 |
| PASS        | 2.12.17 |
| PASS        | 2.12.18 |
| PASS        | 2.12.19 |
| PASS        | 2.12.20 |
| PASS        | 2.12.21 |
| PASS        | 2.13.0  |
| PASS        | 2.13.1  |
| PASS        | 2.13.2  |
| PASS        | 2.13.3  |
| PASS        | 2.13.4  |
| PASS        | 2.13.5  |
| PASS        | 2.13.6  |
| PASS        | 2.13.7  |
| PASS        | 2.13.8  |
| PASS        | 2.13.9  |
| PASS        | 2.13.10 |
| PASS        | 2.13.11 |
| PASS        | 2.13.12 |
| PASS        | 2.13.13 |
| PASS        | 2.13.14 |
| PASS        | 2.13.15 |
| PASS        | 2.13.16 |
| PASS        | 2.13.17 |
| PASS        | 2.13.18 |
| FAIL        | 3.0.0   |
| FAIL        | 3.0.1   |
| FAIL        | 3.0.2   |
| FAIL        | 3.1.0   |
| FAIL        | 3.1.1   |
| FAIL        | 3.1.2   |
| FAIL        | 3.1.3   |
| FAIL        | 3.2.0   |
| FAIL        | 3.2.1   |
| FAIL        | 3.2.2   |
| PASS        | 3.3.0   |
| PASS        | 3.3.1   |
| PASS        | 3.3.3   |
| PASS        | 3.3.4   |
| PASS        | 3.3.5   |
| PASS        | 3.3.6   |
| PASS        | 3.3.7   |
| PASS        | 3.4.0   |
| PASS        | 3.4.1   |
| PASS        | 3.4.2   |
| PASS        | 3.4.3   |
| PASS        | 3.5.0   |
| PASS        | 3.5.1   |
| PASS        | 3.5.2   |
| PASS        | 3.6.2   |
| PASS        | 3.6.3   |
| PASS        | 3.6.4   |
| PASS        | 3.7.0   |
| PASS        | 3.7.1   |
| PASS        | 3.7.2   |
| PASS        | 3.7.3   |
| PASS        | 3.7.4   |
| PASS        | 3.8.0   |
| PASS        | 3.8.1   |
| PASS        | 3.8.2   |
