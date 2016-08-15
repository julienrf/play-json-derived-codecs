package julienrf.json

import play.api.libs.json.{OFormat, OWrites, Reads}
import shapeless.Lazy

package object derived {

  def reads[A](adapter: Adapter = Adapter.identity)(implicit derivedReads: Lazy[DerivedReads[A]]): Reads[A] = derivedReads.value.reads(TypeTagReads.nested, adapter)

  def owrites[A](adapter: Adapter = Adapter.identity)(implicit derivedOWrites: Lazy[DerivedOWrites[A]]): OWrites[A] = derivedOWrites.value.owrites(TypeTagOWrites.nested, adapter)

  def oformat[A](adapter: Adapter = Adapter.identity)(implicit derivedReads: Lazy[DerivedReads[A]], derivedOWrites: Lazy[DerivedOWrites[A]]): OFormat[A] =
    OFormat(derivedReads.value.reads(TypeTagReads.nested, adapter), derivedOWrites.value.owrites(TypeTagOWrites.nested, adapter))

  object flat {

    def reads[A](typeName: Reads[String], adapter: Adapter = Adapter.identity)(implicit derivedReads: Lazy[DerivedReads[A]]): Reads[A] =
      derivedReads.value.reads(TypeTagReads.flat(typeName), adapter)

    def owrites[A](typeName: OWrites[String], adapter: Adapter = Adapter.identity)(implicit derivedOWrites: Lazy[DerivedOWrites[A]]): OWrites[A] =
      derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName), adapter)

    def oformat[A](typeName: OFormat[String], adapter: Adapter = Adapter.identity)(implicit derivedReads: Lazy[DerivedReads[A]], derivedOWrites: Lazy[DerivedOWrites[A]]): OFormat[A] =
      OFormat(derivedReads.value.reads(TypeTagReads.flat(typeName), adapter), derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName), adapter))

  }

}
