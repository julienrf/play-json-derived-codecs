package julienrf.json.derived

trait NameAdapter extends (String => String)

object NameAdapter {

  val snakeCase = NameAdapter { (s: String) =>
    {
      val builder = new StringBuilder
      s foreach {
        case c if Character.isUpperCase(c) && builder.nonEmpty =>
          builder append "_" append (Character toLowerCase c)
        case c => builder append c
      }
      builder.toString
    }
  }

  val identity = NameAdapter(s => s)

  def apply(f: (String => String)): NameAdapter = new NameAdapter {
    override def apply(v1: String): String = f(v1)
  }


}
