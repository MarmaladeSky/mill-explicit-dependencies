package middle

import core.CoreModel

object MiddleService {
  def create(name: String): CoreModel = CoreModel(name, 42)
}
