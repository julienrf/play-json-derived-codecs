package julienrf.json.derived

import shapeless.Witness

class FieldAdapter[K <: Symbol](w: Witness.Aux[K], a: Adapter) {
  val name = a(w.value.name)
}


object FieldAdapter {
  implicit def witnessAdapter[K <: Symbol](implicit w: Witness.Aux[K], a: Adapter): FieldAdapter[K] = {
    new FieldAdapter[K](w, a)
  }
}

trait Adapter extends (String => String)

object Adapter {
  private def toSnakeCase(s: String): String =
    s.foldLeft(new StringBuilder) {
      case (s, c) if Character.isUpperCase(c) && s.nonEmpty => s append "_" append (Character toLowerCase c)
      case (s, c) => s append c
    }.toString

  def snakeCase = new Adapter {
    override def apply(v1: String): String = toSnakeCase(v1)
  }

  def identity = new Adapter {
    override def apply(v1: String): String = v1
  }

  implicit val default = identity

  def apply(f: (String => String)): Adapter = new Adapter {
    override def apply(v1: String): String = f(v1)
  }


}
