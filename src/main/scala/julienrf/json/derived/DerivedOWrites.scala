package julienrf.json.derived

import play.api.libs.json.{JsValue, JsObject, Json, OWrites, Writes}
import shapeless.labelled.FieldType
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness}

trait DerivedOWrites[-A] {
  def owrites(tagOwrites: TypeTagOWrites): OWrites[A]
}

object DerivedOWrites extends DerivedOWritesInstances

trait DerivedOWritesInstances extends DerivedOWritesInstances1 {

  implicit val owritesHNil: DerivedOWrites[HNil] =
    new DerivedOWrites[HNil] {
      def owrites(tagOwrites: TypeTagOWrites) = OWrites[HNil] { _ => Json.obj() }
    }

  implicit def owritesLabelledHListOpt[A, K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    adaptedName: FieldAdapter[K],
    owritesH: Lazy[Writes[H]],
    owritesT: Lazy[DerivedOWrites[T]]
  ): DerivedOWrites[FieldType[K, Option[H]] :: T] =
    new DerivedOWrites[FieldType[K, Option[H]] :: T] {
      def owrites(tagOwrites: TypeTagOWrites) =
        OWrites[FieldType[K, Option[H]] :: T] { case maybeH :: t =>
          val maybeField: Map[String, JsValue] =
            (maybeH: Option[H]) match {
              case Some(h) => Map(adaptedName.name -> owritesH.value.writes(h))
              case None => Map.empty
            }
          JsObject(maybeField ++ owritesT.value.owrites(tagOwrites).writes(t).value)
        }
    }

  implicit val owritesCNil: DerivedOWrites[CNil] =
    new DerivedOWrites[CNil] {
      def owrites(tagOwrites: TypeTagOWrites): OWrites[CNil] = sys.error("No JSON representation of CNil")
    }

  implicit def owritesCoproduct[K <: Symbol, L, R <: Coproduct](implicit
    typeName: Witness.Aux[K],
    adaptedName: FieldAdapter[K],
    owritesL: Lazy[DerivedOWrites[L]],
    owritesR: Lazy[DerivedOWrites[R]]
  ): DerivedOWrites[FieldType[K, L] :+: R] =
    new DerivedOWrites[FieldType[K, L] :+: R] {
      def owrites(tagOwrites: TypeTagOWrites) = OWrites[FieldType[K, L] :+: R] {
        case Inl(l) => tagOwrites.owrites(typeName.value.name, owritesL.value.owrites(tagOwrites)).writes(l)
        case Inr(r) => owritesR.value.owrites(tagOwrites).writes(r)
      }
    }


}

trait DerivedOWritesInstances1 extends DerivedOWritesInstances2 {

  implicit def owritesLabelledHList[A, K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    adaptedName: FieldAdapter[K],
    owritesH: Lazy[Writes[H]],
    owritesT: Lazy[DerivedOWrites[T]]
  ): DerivedOWrites[FieldType[K, H] :: T] =
    new DerivedOWrites[FieldType[K, H] :: T] {
      def owrites(tagOwrites: TypeTagOWrites) =
        OWrites[FieldType[K, H] :: T] { case h :: t =>
          JsObject(Map(adaptedName.name -> owritesH.value.writes(h)) ++ owritesT.value.owrites(tagOwrites).writes(t).value)
        }
    }

}

trait DerivedOWritesInstances2 {

  implicit def owritesGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    derivedOWrites: Lazy[DerivedOWrites[R]]
  ): DerivedOWrites[A] =
    new DerivedOWrites[A] {
      def owrites(tagOwrites: TypeTagOWrites) =
        OWrites.contravariantfunctorOWrites.contramap(derivedOWrites.value.owrites(tagOwrites), gen.to)
    }

}
