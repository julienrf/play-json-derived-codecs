package julienrf.json

import play.api.libs.json.{OFormat, OWrites, Reads}
import shapeless.Lazy

package object derived {

  def reads[A](implicit derivedReads: Lazy[DerivedReads[A]]): Reads[A] = derivedReads.value.reads(TypeTagReads.nested)

  def owrites[A](implicit derivedOWrites: Lazy[DerivedOWrites[A]]): OWrites[A] = derivedOWrites.value.owrites(TypeTagOWrites.nested)

  def oformat[A](implicit derivedReads: Lazy[DerivedReads[A]], derivedOWrites: Lazy[DerivedOWrites[A]]): OFormat[A] =
    OFormat(derivedReads.value.reads(TypeTagReads.nested), derivedOWrites.value.owrites(TypeTagOWrites.nested))

  object flat {

    def reads[A](typeName: Reads[String])(implicit derivedReads: Lazy[DerivedReads[A]]): Reads[A] =
      derivedReads.value.reads(TypeTagReads.flat(typeName))

    def owrites[A](typeName: OWrites[String])(implicit derivedOWrites: Lazy[DerivedOWrites[A]]): OWrites[A] =
      derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName))

    def oformat[A](typeName: OFormat[String])(implicit derivedReads: Lazy[DerivedReads[A]], derivedOWrites: Lazy[DerivedOWrites[A]]): OFormat[A] =
      OFormat(derivedReads.value.reads(TypeTagReads.flat(typeName)), derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName)))

  }

}
