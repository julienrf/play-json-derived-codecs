package julienrf.json

import play.api.libs.json.{OFormat, OWrites, Reads}
import shapeless.Lazy

package object derived {

  def reads[A](adapter: NameAdapter = NameAdapter.identity)(implicit derivedReads: Lazy[DerivedReads[A]]): Reads[A] = derivedReads.value.reads(TypeTagReads.nested, adapter)

  def owrites[A](adapter: NameAdapter = NameAdapter.identity)(implicit derivedOWrites: Lazy[DerivedOWrites[A]]): OWrites[A] = derivedOWrites.value.owrites(TypeTagOWrites.nested, adapter)

  def oformat[A](adapter: NameAdapter = NameAdapter.identity)(implicit derivedReads: Lazy[DerivedReads[A]], derivedOWrites: Lazy[DerivedOWrites[A]]): OFormat[A] =
    OFormat(derivedReads.value.reads(TypeTagReads.nested, adapter), derivedOWrites.value.owrites(TypeTagOWrites.nested, adapter))

  object flat {

    def reads[A](typeName: Reads[String], adapter: NameAdapter = NameAdapter.identity)(implicit derivedReads: Lazy[DerivedReads[A]]): Reads[A] =
      derivedReads.value.reads(TypeTagReads.flat(typeName), adapter)

    def owrites[A](typeName: OWrites[String], adapter: NameAdapter = NameAdapter.identity)(implicit derivedOWrites: Lazy[DerivedOWrites[A]]): OWrites[A] =
      derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName), adapter)

    def oformat[A](typeName: OFormat[String], adapter: NameAdapter = NameAdapter.identity)(implicit derivedReads: Lazy[DerivedReads[A]], derivedOWrites: Lazy[DerivedOWrites[A]]): OFormat[A] =
      OFormat(derivedReads.value.reads(TypeTagReads.flat(typeName), adapter), derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName), adapter))

  }

  object own {

    def reads[A](typeName: Reads[String], typeNameTransformer: String => String)(implicit derivedReads: Lazy[DerivedReads[A]]): Reads[A] =
      derivedReads.value.reads(TypeTagReads.own(typeName, typeNameTransformer))

    def owrites[A](typeName: OWrites[String], typeNameTransformer: String => String)(implicit derivedOWrites: Lazy[DerivedOWrites[A]]): OWrites[A] =
      derivedOWrites.value.owrites(TypeTagOWrites.own(typeName, typeNameTransformer))

    def oformat[A](typeName: OFormat[String], typeNameTransformer: String => String)(implicit derivedReads: Lazy[DerivedReads[A]], derivedOWrites: Lazy[DerivedOWrites[A]]): OFormat[A] =
      OFormat(
        derivedReads.value.reads(TypeTagReads.own(typeName, typeNameTransformer)),
        derivedOWrites.value.owrites(TypeTagOWrites.own(typeName, typeNameTransformer))
      )
  }

}
