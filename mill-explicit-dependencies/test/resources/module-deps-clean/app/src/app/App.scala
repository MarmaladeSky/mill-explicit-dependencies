package app

import core.CoreService

object App extends CoreService {
  def main(args: Array[String]): Unit = {
    println(greet)
  }
}
