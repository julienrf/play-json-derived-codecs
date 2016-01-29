package julienrf.json.derived

import play.api.libs.json.{Reads, __}
import shapeless.labelled.{FieldType, field}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

trait DerivedReads2[A] {
  def reads: Reads[A]
}

object DerivedReads2 extends DerivedReads2Instances

trait DerivedReads2Instances extends DerivedReads2Instances1 {

  implicit val readsHNil: DerivedReads2[HNil] =
    new DerivedReads2[HNil] {
      val reads = Reads.pure[HNil](HNil)
    }

  implicit def readsLabelledHList[A, K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    decodeH: Lazy[Reads[H]],
    decodeT: Lazy[DerivedReads2[T]]
  ): DerivedReads2[FieldType[K, H] :: T] =
    new DerivedReads2[FieldType[K, H] :: T] {
      val reads =
        for {
          h <- (__ \ fieldName.value.name).read(decodeH.value)
          t <- decodeT.value.reads
        } yield field[K](h) :: t
    }
}

trait DerivedReads2Instances1 {

  implicit def readsGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    derivedReads: Lazy[DerivedReads2[R]]
  ): DerivedReads2[A] =
    new DerivedReads2[A] {
      val reads = derivedReads.value.reads.map(gen.from)
    }

}
