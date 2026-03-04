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

object ClassifierDependencies extends TestRootModule with ExplicitDependencies {

  def scalaVersion = "3.7.3"

  lazy val millDiscover = Discover[this.type]

  override def mvnDeps = Seq(
    // valid dependency
    Dep.parse("io.netty:netty-transport-classes-epoll:4.1.115.Final"),
    // must be runtime
    Dep.parse(
      "io.netty:netty-transport-native-epoll:4.1.115.Final;classifier=linux-x86_64"
    ),
    Dep.parse(
      "io.netty:netty-transport-native-epoll:4.1.115.Final;classifier=linux-aarch_64"
    )
  )

}
