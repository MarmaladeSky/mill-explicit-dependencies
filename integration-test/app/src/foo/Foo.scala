package foo

import cats.effect._

object Foo extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    IO.pure(ExitCode.Success)
  }
}
