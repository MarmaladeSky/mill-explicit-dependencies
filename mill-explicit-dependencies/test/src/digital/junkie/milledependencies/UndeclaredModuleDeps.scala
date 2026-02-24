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
import mill.scalalib.ScalaModule
import mill.testkit.TestRootModule

object UndeclaredModuleDeps extends TestRootModule {

  object core extends ScalaModule with ExplicitDependencies {
    def scalaVersion = "3.7.3"
  }

  object middle extends ScalaModule with ExplicitDependencies {
    def scalaVersion = "3.7.3"
    override def moduleDeps = Seq(core)
  }

  object app extends ScalaModule with ExplicitDependencies {
    def scalaVersion = "3.7.3"
    override def moduleDeps = Seq(middle)
    // app uses core.CoreModel directly but does NOT declare core in moduleDeps
  }

  lazy val millDiscover = Discover[this.type]
}
