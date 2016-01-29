package julienrf.json.derived

import play.api.libs.json._
import shapeless.labelled.{field, FieldType}
import shapeless.{Witness, HList, HNil, Lazy, LabelledGeneric, Coproduct, CNil, ::, :+:, Inr, Inl}

trait DerivedReads[A] extends Reads[A]

object DerivedReads extends DerivedReadsInstances

trait DerivedReadsInstances extends DerivedReadsInstances1 {

  implicit def readsCNil: DerivedReads[CNil] =
    new DerivedReads[CNil] {
      def reads(json: JsValue) = JsError("Unable to read this type")
    }

  implicit def readsHNil: DerivedReads[HNil] =
    new DerivedReads[HNil] {
      def reads(json: JsValue) = JsSuccess(HNil)
    }

  implicit def readsCoProduct[K <: Symbol, L, R <: Coproduct](implicit
    typeName: Witness.Aux[K],
    readL: Lazy[Reads[L]],
    readR: Lazy[DerivedReads[R]]
  ): DerivedReads[FieldType[K, L] :+: R] = new DerivedReads[FieldType[K, L] :+: R] {
    def reads(json: JsValue) = {
      val typeFieldReads = (__ \ typeName.value.name).read(readL.value)
      typeFieldReads.reads(json).fold(
        { errors => readR.value.reads(json).map { r => Inr(r) } },
        { l => JsSuccess(Inl(field[K](l))) }
      )
    }
  }

  implicit def readsLabelledHList[K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    readH: Lazy[Reads[H]],
    readT: Lazy[DerivedReads[T]]
  ): DerivedReads[FieldType[K, H] :: T] =
    new DerivedReads[FieldType[K, H] :: T] {
      def reads(json: JsValue) = {
        val fieldReads = (__ \ fieldName.value.name).read(readH.value)
        val hJsResult = fieldReads.reads(json)
        val tJsResult = readT.value.reads(json)
        for {
          h <- hJsResult
          t <- tJsResult
        } yield (field[K](h) :: t)
      }
    }

}

trait DerivedReadsInstances1 {

  implicit def readsGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    derivedReads: Lazy[DerivedReads[R]]
  ): DerivedReads[A] =
    new DerivedReads[A] {
      def reads(json: JsValue): JsResult[A] = derivedReads.value.reads(json).map(gen.from)
    }

}
