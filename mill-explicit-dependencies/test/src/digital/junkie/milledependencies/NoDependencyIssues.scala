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

import digital.junkie.milledependencies.ExplicitDependencies
import mill.*
import mill.api.Discover
import mill.javalib.Dep
import mill.testkit.TestRootModule

object NoDependencyIssues extends TestRootModule with ExplicitDependencies {

  def scalaVersion = "3.7.3"

  lazy val millDiscover = Discover[this.type]

  override def mvnDeps = Seq(Dep.parse("org.typelevel::cats-effect:3.6.3"))

}
