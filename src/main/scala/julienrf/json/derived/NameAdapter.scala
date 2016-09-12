package julienrf.json.derived

trait NameAdapter extends (String => String)

object NameAdapter {

  val snakeCase = NameAdapter {
    (s: String) => {
      s.foldLeft(new StringBuilder) {
        case (s, c) if Character.isUpperCase(c) && s.nonEmpty => s append "_" append (Character toLowerCase c)
        case (s, c) => s append c
      }.toString
    }
  }

  val identity = NameAdapter(s => s)

  val default = identity

  def apply(f: (String => String)): NameAdapter = new NameAdapter {
    override def apply(v1: String): String = f(v1)
  }


}
