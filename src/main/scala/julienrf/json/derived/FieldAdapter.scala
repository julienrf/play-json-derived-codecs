package julienrf.json.derived

trait Adapter extends (String => String)

object Adapter {
  private def toSnakeCase(s: String): String =
    s.foldLeft(new StringBuilder) {
      case (s, c) if Character.isUpperCase(c) && s.nonEmpty => s append "_" append (Character toLowerCase c)
      case (s, c) => s append c
    }.toString

  val snakeCase = Adapter {
    (v1: String) => {
      toSnakeCase(v1)
    }
  }

  val identity = Adapter(s => s)

  implicit val default = identity

  def apply(f: (String => String)): Adapter = new Adapter {
    override def apply(v1: String): String = f(v1)
  }


}
