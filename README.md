# Mill Explicit Dependencies

This is a [Mill](https://github.com/com-lihaoyi/mill) Plugin similar to the most useful [SBT](https://github.com/sbt/sbt) [Plugin you ever saw](https://github.com/cb372/sbt-explicit-dependencies).

## Usage

Set up the plugin at your **build.mill**:

```scala
//| mill-version: 1.0.5
//| mvnDeps: [
//|   "digital.junkie::mill-explicit-dependencies:1.0.0-SNAPSHOT"
//| ]
//| repositories:
//|   - https://central.sonatype.com/repository/maven-snapshots

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
