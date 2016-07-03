package julienrf.json.derived

import play.api.libs.json.{Reads, __, JsError}
import shapeless.labelled.{FieldType, field}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness, Coproduct, :+:, Inr, Inl, CNil}

trait DerivedReads[A] {
  def reads: Reads[A]
}

object DerivedReads extends DerivedReadsInstances

trait DerivedReadsInstances extends DerivedReadsInstances1 {

  implicit val readsCNil: DerivedReads[CNil] =
    new DerivedReads[CNil] {
      val reads = Reads[CNil] { _ => JsError("error.sealed.trait") }
    }

  implicit def readsCoProduct[K <: Symbol, L, R <: Coproduct](implicit
    typeName: Witness.Aux[K],
    readL: Lazy[DerivedReads[L]],
    readR: Lazy[DerivedReads[R]],
    typeTagReads: TypeTagReads
  ): DerivedReads[FieldType[K, L] :+: R] =
    new DerivedReads[FieldType[K, L] :+: R] {
      lazy val reads =
        typeTagReads.reads(typeName.value.name, Reads[L](json => readL.value.reads.reads(json)))
          .map[FieldType[K, L] :+: R](l => Inl(field[K](l)))
          .orElse(readR.value.reads.map(r => Inr(r)))
    }

  implicit val readsHNil: DerivedReads[HNil] =
    new DerivedReads[HNil] {
      val reads = Reads.pure[HNil](HNil)
    }

  implicit def readsLabelledHListOpt[K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    readH: Lazy[Reads[H]],
    readT: Lazy[DerivedReads[T]]
  ): DerivedReads[FieldType[K, Option[H]] :: T] =
    new DerivedReads[FieldType[K, Option[H]] :: T] {
      lazy val reads =
        Reads.applicative.apply(
          (__ \ fieldName.value.name).readNullable(readH.value).map {
            h => { (t: T) => field[K](h) :: t }
          },
          readT.value.reads
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
      lazy val reads =
        Reads.applicative.apply(
          (__ \ fieldName.value.name).read(readH.value).map {
            h => { (t: T) => field[K](h) :: t }
          },
          readT.value.reads
        )
    }

}

trait DerivedReadsInstances2 {

  implicit def readsGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    derivedReads: Lazy[DerivedReads[R]]
  ): DerivedReads[A] =
    new DerivedReads[A] {
      lazy val reads = derivedReads.value.reads.map(gen.from)
    }

}
