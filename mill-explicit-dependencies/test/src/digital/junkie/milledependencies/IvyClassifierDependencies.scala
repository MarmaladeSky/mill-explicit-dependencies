/*
 * Copyright 2026 David Akermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package digital.junkie.milledependencies

import mill.*
import mill.api.Discover
import mill.javalib.Dep
import mill.javalib.PublishModule
import mill.javalib.publish.{License, PomSettings, PublishInfo, VersionControl}
import mill.scalalib.ScalaModule
import mill.testkit.TestRootModule

object IvyClassifierDependencies extends TestRootModule {

  object lib extends ScalaModule with PublishModule {

    def scalaVersion = "3.7.3"

    def publishVersion = "1.0.0"

    def pomSettings = PomSettings(
      description = "Test library for ivy classifier unit test",
      organization = "test.ivy.classifier",
      url = "https://example.com",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("test", "test"),
      developers = Nil
    )

    // Sub-module whose jar is published as the tests classifier artifact
    object tests extends ScalaModule {
      def scalaVersion = "3.7.3"
      override def moduleDeps = Seq(lib)
    }

    override def extraPublish: T[Seq[PublishInfo]] = Task {
      Seq(
        PublishInfo(
          file = tests.jar(),
          classifier = Some("tests"),
          ext = "jar",
          ivyConfig = "test",
          ivyType = "jar"
        )
      )
    }
  }

  object app extends ScalaModule with ExplicitDependencies {
    def scalaVersion = "3.7.3"
    override def mvnDeps = Seq(
      // base dep — its classes are used in source
      Dep.parse("test.ivy.classifier:lib_3:1.0.0"),
      // tests classifier dep — its classes are also used in source, should not be unused
      Dep.parse("test.ivy.classifier:lib_3:1.0.0;classifier=tests")
    )
  }

  lazy val millDiscover = Discover[this.type]
}
