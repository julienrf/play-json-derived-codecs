package julienrf.json.derived

import play.api.libs.json.{JsValue, JsObject, Json, OWrites, Writes}
import shapeless.labelled.FieldType
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness}

/**
  * Derives an `OWrites[A]`
  */
trait DerivedOWrites[-A] {
  /**
    * @param tagOwrites The strategy to use to serialize sum types
    * @param adapter The fields naming strategy
    * @return The derived `OWrites[A]`
    */
  def owrites(tagOwrites: TypeTagOWrites, adapter: NameAdapter): OWrites[A]
}

object DerivedOWrites extends DerivedOWritesInstances

trait DerivedOWritesInstances extends DerivedOWritesInstances1 {

  implicit val owritesHNil: DerivedOWrites[HNil] =
    new DerivedOWrites[HNil] {
      def owrites(tagOwrites: TypeTagOWrites, adapter: NameAdapter) = OWrites[HNil] { _ => Json.obj() }
    }

  implicit def owritesLabelledHListOpt[A, K <: Symbol, H, T <: HList](implicit
    fieldName: Witness.Aux[K],
    owritesH: Lazy[Writes[H]],
    owritesT: Lazy[DerivedOWrites[T]]
  ): DerivedOWrites[FieldType[K, Option[H]] :: T] =
    new DerivedOWrites[FieldType[K, Option[H]] :: T] {
      def owrites(tagOwrites: TypeTagOWrites, adapter: NameAdapter) = {
        val adaptedName = adapter(fieldName.value.name)
        val derivedOwriteT = owritesT.value.owrites(tagOwrites, adapter)
        OWrites[FieldType[K, Option[H]] :: T] { case maybeH :: t =>
          val maybeField: Map[String, JsValue] =
            (maybeH: Option[H]) match {
              case Some(h) => Map(adaptedName -> owritesH.value.writes(h))
              case None => Map.empty
            }
          JsObject(maybeField ++ derivedOwriteT.writes(t).value)
        }
      }
    }

  implicit val owritesCNil: DerivedOWrites[CNil] =
    new DerivedOWrites[CNil] {
      def owrites(tagOwrites: TypeTagOWrites, adapter: NameAdapter): OWrites[CNil] = OWrites {
        _ => sys.error("No JSON representation of CNil")
      }
    }

  implicit def owritesCoproduct[K <: Symbol, L, R <: Coproduct](implicit
    typeName: Witness.Aux[K],
    owritesL: Lazy[DerivedOWrites[L]],
    owritesR: Lazy[DerivedOWrites[R]]
  ): DerivedOWrites[FieldType[K, L] :+: R] =
    new DerivedOWrites[FieldType[K, L] :+: R] {
      def owrites(tagOwrites: TypeTagOWrites, adapter: NameAdapter) = {
        val derivedOwriteL = owritesL.value.owrites(tagOwrites, adapter)
        val derivedOwriteR = owritesR.value.owrites(tagOwrites, adapter)
        OWrites[FieldType[K, L] :+: R] {
          case Inl(l) => tagOwrites.owrites(typeName.value.name, derivedOwriteL).writes(l)
          case Inr(r) => derivedOwriteR.writes(r)
        }
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
      def owrites(tagOwrites: TypeTagOWrites, adapter: NameAdapter) = {
        val adaptedName = adapter(fieldName.value.name)
        val derivedOwritesT = owritesT.value.owrites(tagOwrites, adapter)
        OWrites[FieldType[K, H] :: T] { case h :: t =>
          JsObject(Map(adaptedName -> owritesH.value.writes(h)) ++ derivedOwritesT.writes(t).value)
        }
      }
    }

}

trait DerivedOWritesInstances2 {

  implicit def owritesGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    derivedOWrites: Lazy[DerivedOWrites[R]]
  ): DerivedOWrites[A] =
    new DerivedOWrites[A] {
      def owrites(tagOwrites: TypeTagOWrites, adapter: NameAdapter) =
        OWrites.contravariantfunctorOWrites.contramap(derivedOWrites.value.owrites(tagOwrites, adapter), gen.to)
    }

}
