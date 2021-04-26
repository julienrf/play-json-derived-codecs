package julienrf.json

import play.api.libs.json.{OFormat, OWrites, Reads}
import shapeless.Lazy

package object derived {

  def reads[A](
    adapter: NameAdapter = NameAdapter.identity
  )(implicit
    derivedReads: Lazy[DerivedReads[A, TypeTag.ShortClassName]]
  ): Reads[A] =
    derivedReads.value.reads(TypeTagReads.nested, adapter)

  def owrites[A](
    adapter: NameAdapter = NameAdapter.identity
  )(implicit
    derivedOWrites: Lazy[DerivedOWrites.Nested[A, TypeTag.ShortClassName]]
  ): OWrites[A] =
    derivedOWrites.value.owrites(TypeTagOWrites.nested, adapter)

  def oformat[A](
    adapter: NameAdapter = NameAdapter.identity
  )(implicit
    derivedReads: Lazy[DerivedReads[A, TypeTag.ShortClassName]],
    derivedOWrites: Lazy[DerivedOWrites.Nested[A, TypeTag.ShortClassName]]
  ): OFormat[A] =
    OFormat(derivedReads.value.reads(TypeTagReads.nested, adapter), derivedOWrites.value.owrites(TypeTagOWrites.nested, adapter))

  object flat {

    def reads[A](
      typeName: Reads[String],
      adapter: NameAdapter = NameAdapter.identity
    )(implicit
      derivedReads: Lazy[DerivedReads[A, TypeTag.ShortClassName]]
    ): Reads[A] =
      derivedReads.value.reads(TypeTagReads.flat(typeName), adapter)

    def owrites[A](
      typeName: OWrites[String],
      adapter: NameAdapter = NameAdapter.identity
    )(implicit
      derivedOWrites: Lazy[DerivedOWrites.Flat[A, TypeTag.ShortClassName]]
    ): OWrites[A] =
      derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName), adapter)

    def oformat[A](
      typeName: OFormat[String],
      adapter: NameAdapter = NameAdapter.identity
    )(implicit
      derivedReads: Lazy[DerivedReads[A, TypeTag.ShortClassName]],
      derivedOWrites: Lazy[DerivedOWrites.Flat[A, TypeTag.ShortClassName]]
    ): OFormat[A] =
      OFormat(derivedReads.value.reads(TypeTagReads.flat(typeName), adapter), derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName), adapter))

  }

  // Ideally, we would just have a `typeTagSetting: TypeTagSetting` parameter
  // in the previous methods, with a default value of `TypeTagSetting.ShortClassName`.
  // However, this is not supported by Scala 2 (see https://github.com/scala/bug/issues/11571).
  object withTypeTag {
    /** Derives a `Reads[A]` instance, using the given `typeTagSetting`, `adapter`,
      * and `typeTagReads` configuration.
      */
    def reads[A](
      typeTagSetting: TypeTagSetting,
      adapter: NameAdapter = NameAdapter.identity
    )(implicit
      derivedReads: Lazy[DerivedReads[A, typeTagSetting.Value]]
    ): Reads[A] =
      derivedReads.value.reads(TypeTagReads.nested, adapter)

    /**
      * Derives an `OWrites[A]` instance, using the given `typeTagSetting`, `adapter`,
      * and `typeTagOWrites` configuration.
      */
    def owrites[A](
      typeTagSetting: TypeTagSetting,
      adapter: NameAdapter = NameAdapter.identity
    )(implicit
      derivedOWrites: Lazy[DerivedOWrites.Nested[A, typeTagSetting.Value]]
    ): OWrites[A] =
      derivedOWrites.value.owrites(TypeTagOWrites.nested, adapter)

    /**
      * Derives an `OFormat[A]` instance, using the given `typeTagSetting`, `adapter`,
      * and `typeTagOFormat` configuration.
      */
    def oformat[A](
      typeTagSetting: TypeTagSetting,
      adapter: NameAdapter = NameAdapter.identity
    )(implicit
      derivedReads: Lazy[DerivedReads[A, typeTagSetting.Value]],
      derivedOWrites: Lazy[DerivedOWrites.Nested[A, typeTagSetting.Value]]
    ): OFormat[A] =
      OFormat(derivedReads.value.reads(TypeTagOFormat.nested, adapter), derivedOWrites.value.owrites(TypeTagOFormat.nested, adapter))

    // Due to the same limitations as mentioned above, we can't have default arguments for
    // TypeTagOWrites/TypeTagOFormat, hence more duplication

    object flat {
      /** Derives a `Reads[A]` instance, using the given `typeTagSetting`, `adapter`,
        * and `typeTagReads` configuration.
        */
      def reads[A](
        typeTagSetting: TypeTagSetting,
        typeName: Reads[String],
        adapter: NameAdapter = NameAdapter.identity
      )(implicit
        derivedReads: Lazy[DerivedReads[A, typeTagSetting.Value]]
      ): Reads[A] =
        derivedReads.value.reads(TypeTagReads.flat(typeName), adapter)

      /**
        * Derives an `OWrites[A]` instance, using the given `typeTagSetting`, `adapter`,
        * and `typeTagOWrites` configuration.
        */
      def owrites[A](
        typeTagSetting: TypeTagSetting,
        typeName: OWrites[String],
        adapter: NameAdapter = NameAdapter.identity
      )(implicit
        derivedOWrites: Lazy[DerivedOWrites.Flat[A, typeTagSetting.Value]]
      ): OWrites[A] =
        derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName), adapter)

      /**
        * Derives an `OFormat[A]` instance, using the given `typeTagSetting`, `adapter`,
        * and `typeTagOFormat` configuration.
        */
      def oformat[A](
        typeTagSetting: TypeTagSetting,
        typeName: OFormat[String],
        adapter: NameAdapter = NameAdapter.identity
      )(implicit
        derivedReads: Lazy[DerivedReads[A, typeTagSetting.Value]],
        derivedOWrites: Lazy[DerivedOWrites.Flat[A, typeTagSetting.Value]]
      ): OFormat[A] =
        OFormat(
          derivedReads.value.reads(TypeTagReads.flat(typeName), adapter),
          derivedOWrites.value.owrites(TypeTagOWrites.flat(typeName), adapter))
    }
  }
}
