package julienrf.json.derived

import play.api.libs.json.{Json, OFormat, OWrites, Reads, __}

trait TypeTagOWrites {
  def owrites[A](typeName: String, owrites: OWrites[A]): OWrites[A]
}

object TypeTagOWrites extends NestedTypeTagOWrites

trait NestedTypeTagOWrites {

  implicit val nestedTypeTagOWrites: TypeTagOWrites =
    new TypeTagOWrites {
      def owrites[A](typeName: String, owrites: OWrites[A]): OWrites[A] =
        OWrites[A](a => Json.obj(typeName -> owrites.writes(a)))
    }

}

trait FlatTypeTagOWrites {

  val tagOWrites: OWrites[String]

  implicit lazy val flatTypeTagOWrites: TypeTagOWrites =
    new TypeTagOWrites {
      def owrites[A](typeName: String, owrites: OWrites[A]): OWrites[A] =
        OWrites[A](a => tagOWrites.writes(typeName) ++ owrites.writes(a))
    }

}


trait TypeTagReads {
  def reads[A](typeName: String, reads: Reads[A]): Reads[A]
}

object TypeTagReads extends NestedTypeTagReads

trait NestedTypeTagReads {
  implicit val nestedTypeTagReads: TypeTagReads =
    new TypeTagReads {
      def reads[A](typeName: String, reads: Reads[A]): Reads[A] =
        (__ \ typeName).read(reads)
    }
}

trait FlatTypeTagReads {

  val tagReads: Reads[String]

  implicit lazy val flatTypeTagReads: TypeTagReads =
    new TypeTagReads {
      def reads[A](typeName: String, reads: Reads[A]): Reads[A] =
        tagReads.filter(_ == typeName).flatMap(_ => reads)
    }
}

trait FlatTypeTagOFormat extends FlatTypeTagReads with FlatTypeTagOWrites {

  val tagOFormat: OFormat[String]

  final lazy val tagReads = tagOFormat

  final lazy val tagOWrites = tagOFormat

}