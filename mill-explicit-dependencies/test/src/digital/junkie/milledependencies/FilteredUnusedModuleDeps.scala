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

object FilteredUnusedModuleDeps extends TestRootModule {

  object core extends ScalaModule with ExplicitDependencies {
    def scalaVersion = "3.7.3"
  }

  object app extends ScalaModule with ExplicitDependencies {
    def scalaVersion = "3.7.3"
    override def moduleDeps = Seq(core)
    // app does NOT use core, but the filter suppresses the unused warning
    override def unusedCompileModulesFilter = Seq(core)
  }

  lazy val millDiscover = Discover[this.type]
}
