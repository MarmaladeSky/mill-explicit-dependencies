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
import mill.testkit.TestRootModule

object NettyTransportTestsDependencies extends TestRootModule with ExplicitDependencies {

  def scalaVersion = "3.7.3"

  lazy val millDiscover = Discover[this.type]

  override def mvnDeps = Seq(
    // base transport dep (provides transitive runtime classes)
    Dep.parse("io.netty:netty-transport:4.1.115.Final"),
    // tests classifier dep — contains Java test utility classes used at compile time
    Dep.parse("io.netty:netty-transport:4.1.115.Final;classifier=tests")
  )

}
