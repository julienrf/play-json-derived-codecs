package julienrf.json.derived

import play.api.libs.json.{Reads, __, JsError}
import shapeless.labelled.{FieldType, field}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness, Coproduct, :+:, Inr, Inl, CNil}

/**
  * Derives a `Reads[A]`
  */
trait DerivedReads[A] {
  /**
    * @param tagReads The strategy to use to deserialize sum types
    * @param adapter Fields naming strategy
    * @return The derived `Reads[A]`
    */
  def reads(tagReads: TypeTagReads, adapter: NameAdapter): Reads[A]
}

object DerivedReads extends DerivedReadsInstances

trait DerivedReadsInstances extends DerivedReadsInstances1 {

  implicit val readsCNil: DerivedReads[CNil] =
    new DerivedReads[CNil] {
      def reads(tagReads: TypeTagReads, adapter: NameAdapter) = Reads[CNil] { _ => JsError("error.sealed.trait") }
    }

  implicit def readsCoProduct[K <: Symbol, L, R <: Coproduct](implicit
    typeName: Witness.Aux[K],
    readL: Lazy[DerivedReads[L]],
    readR: Lazy[DerivedReads[R]]
  ): DerivedReads[FieldType[K, L] :+: R] =
    new DerivedReads[FieldType[K, L] :+: R] {
      def reads(tagReads: TypeTagReads, adapter: NameAdapter) = {
        lazy val derivedReadL = readL.value.reads(tagReads, adapter)
        Reads.alternative.|(
          tagReads.reads(typeName.value.name, Reads[L](json => derivedReadL.reads(json)))
            .map[FieldType[K, L] :+: R](l => {Inl(field[K](l))}),
          readR.value.reads(tagReads, adapter).map(r => Inr(r)))
      }
  }

  implicit val readsHNil: DerivedReads[HNil] =
    new DerivedReads[HNil] {
      def reads(tagReads: TypeTagReads, adapter: NameAdapter) = Reads.pure[HNil](HNil)
    }

  implicit def readsLabelledHListOpt[K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    readH: Lazy[Reads[H]],
    readT: Lazy[DerivedReads[T]]
  ): DerivedReads[FieldType[K, Option[H]] :: T] =
    new DerivedReads[FieldType[K, Option[H]] :: T] {
      def reads(tagReads: TypeTagReads, adapter: NameAdapter) =
        Reads.applicative.apply(
          (__ \ adapter(fieldName.value.name)).readNullable(readH.value).map {
            h => { (t: T) => field[K](h) :: t }
          },
          readT.value.reads(tagReads, adapter)
        )
    }

}

trait DerivedReadsInstances1 extends DerivedReadsInstances2 {

  implicit def readsLabelledHList[K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    readH: Lazy[Reads[H]],
    readT: Lazy[DerivedReads[T]]
  ): DerivedReads[FieldType[K, H] :: T] =
    new DerivedReads[FieldType[K, H] :: T] {
      def reads(tagReads: TypeTagReads, adapter: NameAdapter): Reads[FieldType[K, H] :: T] =
        Reads.applicative.apply(
          (__ \ adapter(fieldName.value.name)).read(readH.value).map {
            h => { (t: T) => field[K](h) :: t }
          },
          readT.value.reads(tagReads, adapter)
        )
    }

}

trait DerivedReadsInstances2 {

  implicit def readsGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    derivedReads: Lazy[DerivedReads[R]]
  ): DerivedReads[A] =
    new DerivedReads[A] {
      def reads(tagReads: TypeTagReads, adapter: NameAdapter) = derivedReads.value.reads(tagReads, adapter).map(gen.from)
    }

}
