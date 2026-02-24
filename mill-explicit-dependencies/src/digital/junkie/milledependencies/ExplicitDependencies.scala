/*
 * Copyright 2026 David Akermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Portions of this code are derived from sbt-explicit-dependencies
 * (https://github.com/cb372/sbt-explicit-dependencies)
 * Licensed under the Apache License 2.0.
 */

package digital.junkie.milledependencies
import mill.*
import sbt.internal.inc.Analysis
import sbt.internal.inc.consistent.ConsistentFileAnalysisStore
import xsbti.compile.analysis.ReadWriteMappers
import mill.javalib.Dep
import java.io.File
import scala.xml.XML
import mill.javalib.Lib
import mill.api.daemon.Result
import mill.api.daemon.Logger

final case class Dependency(
    organization: String,
    fullName: String,
    version: String,
    presentation: String
) {

  override def equals(obj: Any): Boolean = {
    obj match {
      case o: Dependency =>
        o.organization == organization &&
        o.fullName == fullName &&
        o.version == version
      case _ =>
        false
    }
  }

  override def hashCode(): Int = {
    (organization, fullName, version).hashCode()
  }

}

object Dependency {

  implicit val rw: upickle.default.ReadWriter[Dependency] =
    upickle.default.macroRW

}

sealed trait Report

final case class UnusedReport(dependencies: Set[Dependency]) extends Report

final case class UndeclaredReport(dependencies: Set[Dependency]) extends Report

object Report {

  implicit val r: upickle.default.Reader[Report] =
    upickle.default.Reader.derived[Report]

  implicit val w: upickle.default.Writer[Report] =
    upickle.default.Writer.derived[Report]

}

trait ExplicitDependencies extends mill.scalalib.ScalaModule {

  private def checkMillVersion(): Option[String] = {
    val pluginVersion =
      digital.junkie.milledependencies.BuildInfo.version.split("\\.|-")
    val runtimeVersion = mill.api.BuildInfo.millVersion.split("\\.|-")
    val expectedMillVersion = pluginVersion.take(3)
    val versionMatch = runtimeVersion
      .zip(pluginVersion)
      .takeWhile(_ == _)
      .length

    val invalidMatch = pluginVersion.length >= 3 &&
      runtimeVersion.length >= 3 &&
      versionMatch < 3

    Option.when(invalidMatch) {
      s"mill-explicit-dependencies was compiled for Mill ${expectedMillVersion.mkString(".")} " +
        s"but running on Mill ${runtimeVersion.mkString(".")}. " +
        s"This may cause binary incompatibility in transitive dependencies. " +
        s"Consider using ${runtimeVersion.take(3).mkString(".")} version of plugin. " +
        "You can also declare plugin as 'digital.junkie::mill-explicit-dependencies:$MILL_VERSION'."
    }
  }

  private def scalaBinaryVersionOf(sv: String): String = {
    if (sv.startsWith("3.")) "3"
    else sv.split('.').take(2).mkString(".")
  }

  private def makeDependency(
      organization: String,
      name: String,
      version: String,
      sv: String
  ): Dependency = {
    val sbv = scalaBinaryVersionOf(sv)
    if (name.endsWith(s"_$sbv")) {
      val baseName = name.dropRight(sbv.length + 1)
      Dependency(
        organization,
        name,
        version,
        s"$organization::$baseName:$version"
      )
    } else {
      Dependency(
        organization,
        name,
        version,
        s"$organization:$name:$version"
      )
    }
  }

  private def findPomFile(jarFile: File): Option[File] = {
    // pom file should be in the same directory as the jar, with the same filename but a .pom extension
    val filenames = jarFile.getName
      .dropRight(4)
      .split('-')
      .inits
      .filter(_.nonEmpty)
      .map(_.mkString("-") + ".pom")
      .toList
    val pomFiles =
      filenames.map(filename => new File(jarFile.getParentFile, filename))
    pomFiles.find(_.exists)
  }

  private def findIvyFileInIvyCache(jarFile: File): Option[File] = {
    // Ivy file should be in the parent directory, with the filename ivy-$version.xml
    val artifactVersion = jarFile.getName.dropRight(4).split('-').tail
    val potentialVersions =
      (artifactVersion.tails.toList.reverse ++ artifactVersion.inits.toList.tail)
        .filter(_.nonEmpty)
        .map(_.mkString("-"))
    val potentialIvyFiles = potentialVersions.map(version =>
      new File(jarFile.getParentFile.getParentFile, s"ivy-$version.xml")
    )
    potentialIvyFiles.find(_.exists)
  }

  private def findIvyFileInIvyLocal(jarFile: File): Option[File] = {
    // Jar file will be in 'jars' directory. Ivy file should be in the sibling 'ivys' directory, with the filename ivy.xml
    val ivysDirectory = new File(jarFile.getParentFile.getParentFile, "ivys")
    Some(new File(ivysDirectory, "ivy.xml")).filter(_.exists)
  }

  private def parsePomFile(file: File, sv: String): Option[Dependency] = {
    try {
      val xml = XML.loadFile(file)
      val organization = {
        val groupId = (xml \ "groupId").text
        if (groupId.nonEmpty) groupId else (xml \ "parent" \ "groupId").text
      }

      val rawName = (xml \ "artifactId").text

      // We use the parent dir to get the version because it's sometimes not present in the pom file
      val version = file.getParentFile.getName

      Some(makeDependency(organization, rawName, version, sv))
    } catch { case _: Exception => None }
  }

  private def parseIvyFile(
      file: File,
      scalaVersion: String
  ): Option[Dependency] = {
    try {
      val xml = XML.loadFile(file)
      val organization = xml \ "info" \@ "organisation"
      val rawName = xml \ "info" \@ "module"
      val version = xml \ "info" \@ "revision"

      Some(makeDependency(organization, rawName, version, scalaVersion))
    } catch { case _: Exception => None }
  }

  private def jarToDependency(
      file: File,
      scalaVersion: String
  ): Option[Dependency] = {
    if (file.getAbsolutePath.endsWith(".jar")) {
      findIvyFileInIvyCache(file)
        .orElse { findIvyFileInIvyLocal(file) }
        .flatMap { parseIvyFile(_, scalaVersion) }
        .orElse { findPomFile(file).flatMap(parsePomFile(_, scalaVersion)) }
    } else {
      None
    }
  }

  private val implicitModules = Seq(
    "scala-library",
    "scalajs-library",
    "scala3-library"
  )

  def undeclaredCompileDependenciesFilter: Seq[Dep] = Seq.empty

  def unusedCompileDependenciesFilter: Seq[Dep] = Seq.empty

  private def dependencyFromMill(dep: Dep, sv: String) = {
    val resolved = Lib.depToDependency(dep, sv)
    val isCross = dep.cross.isBinary || dep.cross.isConstant
    val org = dep.organization
    val name = dep.name
    val version = dep.version

    Dependency(
      resolved.module.organization.value,
      resolved.module.name.value,
      resolved.versionConstraint.asString,
      if (isCross) {
        s"$org::$name:$version"
      } else {
        s"$org:$name:$version"
      }
    )
  }

  private def declared(deps: Seq[Dep], sv: String): Set[Dependency] = {
    deps.map { dependencyFromMill(_, sv) }.toSet
  }

  private def usedDependencies(
      analysis: os.Path,
      sv: String
  ): Set[Dependency] = {
    val store = ConsistentFileAnalysisStore.binary(
      analysis.toIO,
      ReadWriteMappers.getEmptyMappers()
    )
    val analysisContents = store.get()
    if (analysisContents.isPresent) {
      val analysis = analysisContents.get().getAnalysis.asInstanceOf[Analysis]
      analysis.relations.allLibraryDeps
        .map(d => os.Path(d.id()))
        .map(_.toIO)
        .flatMap(jarToDependency(_, sv))
        .toSet
    } else {
      throw new Exception("Zinc analysis not available")
    }
  }

  def undeclaredCompileDependenciesAnon = Task.Anon {
    val analysis = compile().analysisFile
    val declaredDeps = declared(mvnDeps(), scalaVersion())
    val usedDeps = usedDependencies(analysis, scalaVersion())
    val undeclaredDeps = (usedDeps -- declaredDeps)
      .filter { d => !implicitModules.exists(m => d.fullName.startsWith(m)) }
      .filter { d =>
        !undeclaredCompileDependenciesFilter
          .map(dependencyFromMill(_, scalaVersion()))
          .contains(d)
      }
    UndeclaredReport(undeclaredDeps)
  }

  def logUndeclared(log: Logger, report: UndeclaredReport) = {
    if (report.dependencies.isEmpty) {
      log.info(
        "The project explicitly declares all the libraries that it directly depends on for compilation. Good job!"
      )
    } else {
      log.warn(
        "The project depends on the following libraries for compilation but they are not declared:"
      )
      report.dependencies.toSeq
        .sortBy(d => (d.organization, d.fullName))
        .foreach { d => log.warn(d.presentation) }
    }
  }

  def undeclaredCompileDependencies() = Task.Command {
    val log = Task.ctx().log

    val warning = checkMillVersion()
    warning.foreach(log.warn)

    val report = undeclaredCompileDependenciesAnon()
    logUndeclared(log, report)
  }

  def undeclaredCompileDependenciesTest = Task {
    val log = Task.ctx().log

    val warning = checkMillVersion()
    warning.foreach(log.warn)

    val report = undeclaredCompileDependenciesAnon()
    logUndeclared(log, report)
    if (report.dependencies.nonEmpty) {
      Result.Failure(
        "Failing the build because undeclared dependencies were found"
      )
    } else {
      Result.Success(report)
    }
  }

  def unusedCompileDependenciesAnon = Task.Anon {
    val sv = scalaVersion()
    val declaredDeps = declared(mvnDeps(), sv)
    val usedDeps = usedDependencies(compile().analysisFile, sv)
    val unusedDeps = (declaredDeps -- usedDeps)
      .filter { d =>
        !unusedCompileDependenciesFilter
          .map(dependencyFromMill(_, scalaVersion()))
          .contains(d)
      }
    UnusedReport(unusedDeps)
  }

  def logUnused(log: Logger, report: UnusedReport) = {
    if (report.dependencies.isEmpty) {
      log.info(
        "The project has no unused dependencies declared in libraryDependencies. Good job!"
      )
    } else {
      log.warn(
        "The following libraries are declared but are not needed for compilation:"
      )
      report.dependencies.toSeq
        .sortBy(d => (d.organization, d.fullName))
        .foreach { d => log.warn(d.presentation) }
    }
  }

  def unusedCompileDependencies() = Task.Command {
    val log = Task.ctx().log

    val warning = checkMillVersion()
    warning.foreach(log.warn)

    val report = unusedCompileDependenciesAnon()
    logUnused(log, report)
  }

  def unusedCompileDependenciesTest = Task {
    val log = Task.ctx().log

    val warning = checkMillVersion()
    warning.foreach(log.warn)

    val report = unusedCompileDependenciesAnon()
    logUnused(log, report)

    if (report.dependencies.nonEmpty) {
      Result.Failure("Failing the build because unused dependencies were found")
    } else {
      Result.Success(report)
    }
  }

}
