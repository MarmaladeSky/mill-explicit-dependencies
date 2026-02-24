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

object ScalaNativeProject
    extends TestRootModule
    with ScalaNativeModule
    with ExplicitDependencies {

  def scalaVersion = "3.7.3"

  def scalaNativeVersion = "0.5.10"

  lazy val millDiscover = Discover[this.type]

  override def mvnDeps = Seq(Dep.parse("co.fs2::fs2-core:3.12.2"))
}
