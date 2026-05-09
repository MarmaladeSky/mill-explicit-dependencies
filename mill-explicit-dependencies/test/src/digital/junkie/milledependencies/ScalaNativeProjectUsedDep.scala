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

import mill.testkit.TestRootModule
import mill.api.Discover
import mill.*
import mill.javalib.Dep
import mill.scalanativelib.ScalaNativeModule
import digital.junkie.milledependencies.ExplicitDependencies

object ScalaNativeProjectUsedDep
    extends TestRootModule
    with ScalaNativeModule
    with ExplicitDependencies {

  def scalaVersion = "3.8.3"

  def scalaNativeVersion = "0.5.11"

  lazy val millDiscover = Discover[this.type]

  override def mvnDeps = Seq(Dep.parse("org.typelevel::cats-effect::3.7.0"))
}
