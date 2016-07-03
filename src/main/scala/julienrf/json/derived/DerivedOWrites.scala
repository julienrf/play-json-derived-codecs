package julienrf.json.derived

import play.api.libs.json.{JsValue, JsObject, Json, OWrites, Writes}
import shapeless.labelled.FieldType
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness}

trait DerivedOWrites[-A] {
  def owrites: OWrites[A]
}

object DerivedOWrites extends DerivedOWritesInstances

trait DerivedOWritesInstances extends DerivedOWritesInstances1 {

  implicit val owritesHNil: DerivedOWrites[HNil] =
    new DerivedOWrites[HNil] {
      val owrites = OWrites[HNil] { _ => Json.obj() }
    }

  implicit def owritesLabelledHListOpt[A, K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    owritesH: Lazy[Writes[H]],
    owritesT: Lazy[DerivedOWrites[T]]
  ): DerivedOWrites[FieldType[K, Option[H]] :: T] =
    new DerivedOWrites[FieldType[K, Option[H]] :: T] {
      val owrites =
        OWrites[FieldType[K, Option[H]] :: T] { case maybeH :: t =>
          val maybeField: Map[String, JsValue] =
            (maybeH: Option[H]) match {
              case Some(h) => Map(fieldName.value.name -> owritesH.value.writes(h))
              case None => Map.empty
            }
          JsObject(maybeField ++ owritesT.value.owrites.writes(t).value)
        }
    }

  implicit val owritesCNil: DerivedOWrites[CNil] =
    new DerivedOWrites[CNil] {
      def owrites: OWrites[CNil] = sys.error("No JSON representation of CNil")
    }

  implicit def owritesCoproduct[K <: Symbol, L, R <: Coproduct](implicit
    typeName: Witness.Aux[K],
    owritesL: Lazy[DerivedOWrites[L]],
    owritesR: Lazy[DerivedOWrites[R]],
    typeTagOWrites: TypeTagOWrites
  ): DerivedOWrites[FieldType[K, L] :+: R] =
    new DerivedOWrites[FieldType[K, L] :+: R] {
      val owrites = OWrites[FieldType[K, L] :+: R] {
        case Inl(l) => typeTagOWrites.owrites(typeName.value.name, owritesL.value.owrites).writes(l)
        case Inr(r) => owritesR.value.owrites.writes(r)
      }
    }


}

trait DerivedOWritesInstances1 extends DerivedOWritesInstances2 {

  implicit def owritesLabelledHList[A, K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    owritesH: Lazy[Writes[H]],
    owritesT: Lazy[DerivedOWrites[T]]
  ): DerivedOWrites[FieldType[K, H] :: T] =
    new DerivedOWrites[FieldType[K, H] :: T] {
      val owrites =
        OWrites[FieldType[K, H] :: T] { case h :: t =>
          JsObject(Map(fieldName.value.name -> owritesH.value.writes(h)) ++ owritesT.value.owrites.writes(t).value)
        }
    }

}

trait DerivedOWritesInstances2 {

  implicit def owritesGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    derivedOWrites: Lazy[DerivedOWrites[R]]
  ): DerivedOWrites[A] =
    new DerivedOWrites[A] {
      lazy val owrites =
        OWrites.contravariantfunctorOWrites.contramap(derivedOWrites.value.owrites, gen.to)
    }

}
