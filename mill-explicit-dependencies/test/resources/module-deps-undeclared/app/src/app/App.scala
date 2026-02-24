package app

import core.CoreModel
import middle.MiddleService

object App {
  def main(args: Array[String]): Unit = {
    val model: CoreModel = MiddleService.create("test")
    println(model)
  }
}
