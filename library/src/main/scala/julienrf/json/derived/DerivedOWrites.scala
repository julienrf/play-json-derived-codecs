package julienrf.json.derived

import play.api.libs.json.{JsValue, JsObject, Json, OWrites, Writes}
import shapeless.labelled.FieldType
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness}

/**
  * Derives an `OWrites[A]`
  *
  * @tparam TT Type of TypeTag to use to discriminate alternatives of sealed traits
  * @tparam W A subtype of `Writes` that this derived value requires for its type-tag generation.
  *           This can either be a `Writes` for the nested format or an `OWrites` for the flat
  *           format. See the comment on [[julienrf.json.derived.TypeTagOWrites.W]].
  *
  */
trait DerivedOWrites[A, TT[A] <: TypeTag[A], W[_]] {
  /**
    * @param tagOwrites The strategy to use to serialize sum types
    * @param adapter The fields naming strategy
    * @return The derived `OWrites[A]`
    */
  def owrites(tagOwrites: TypeTagOWrites.Aux[W], adapter: NameAdapter): OWrites[A]
}

object DerivedOWrites extends DerivedOWritesInstances {
  type Nested[A, TT[A] <: TypeTag[A]] = DerivedOWrites[A, TT, Writes]
  type Flat[A, TT[A] <: TypeTag[A]] = DerivedOWrites[A, TT, OWrites]
}

trait DerivedOWritesInstances extends DerivedOWritesInstances1 {
  /** Supports reading a coproduct where the left branch already has a defined `OWrites` instance.
    * This will avoid using the `Generic` implicit for cases where it is possible to use a pre-existing
    * `Writes`/`OWrites`, thus enabling users to easily plug-in their own `Format`/`OFormat` as well
    * as saving on compile-times.
    */
  implicit def owritesPredefinedCoProduct[K <: Symbol, L, R <: Coproduct, TT[A] <: TypeTag[A], W[A] >: OWrites[A]](implicit
    owritesL: Lazy[W[L]],
    owritesR: Lazy[DerivedOWrites[R, TT, W]],
    typeTag: TT[FieldType[K, L]]
  ): DerivedOWrites[FieldType[K, L] :+: R, TT, W] =
    DerivedOWritesUtil.makeCoProductOWrites(
      (_, _) => owritesL.value,
      owritesR,
      typeTag)
}

trait DerivedOWritesInstances1 extends DerivedOWritesInstances2 {

  implicit def owritesHNil[TT[A] <: TypeTag[A], W[_]]: DerivedOWrites[HNil, TT, W] =
    new DerivedOWrites[HNil, TT, W] {
      def owrites(tagOwrites: TypeTagOWrites.Aux[W], adapter: NameAdapter) = OWrites[HNil] { _ => Json.obj() }
    }

  implicit def owritesLabelledHListOpt[A, K <: Symbol, H, T <: HList, TT[A] <: TypeTag[A], W[A]](implicit
    fieldName: Witness.Aux[K],
    owritesH: Lazy[Writes[H]],
    owritesT: Lazy[DerivedOWrites[T, TT, W]]
  ): DerivedOWrites[FieldType[K, Option[H]] :: T, TT, W] =
    new DerivedOWrites[FieldType[K, Option[H]] :: T, TT, W] {
      def owrites(tagOwrites: TypeTagOWrites.Aux[W], adapter: NameAdapter) = {
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

  implicit def owritesCNil[TT[A] <: TypeTag[A], W[_]]: DerivedOWrites[CNil, TT, W] =
    new DerivedOWrites[CNil, TT, W] {
      def owrites(tagOwrites: TypeTagOWrites.Aux[W], adapter: NameAdapter): OWrites[CNil] =
        _ => sys.error("No JSON representation of CNil")
    }

  implicit def owritesCoproduct[K <: Symbol, L, R <: Coproduct, TT[A] <: TypeTag[A], W[A] >: OWrites[A]](implicit
    owritesL: Lazy[DerivedOWrites[L, TT, W]],
    owritesR: Lazy[DerivedOWrites[R, TT, W]],
    typeTag: TT[FieldType[K, L]]
  ): DerivedOWrites[FieldType[K, L] :+: R, TT, W] =
    DerivedOWritesUtil.makeCoProductOWrites(
      owritesL.value.owrites,
      owritesR,
      typeTag)
}

trait DerivedOWritesInstances2 extends DerivedOWritesInstances3 {

  implicit def owritesLabelledHList[A, K <: Symbol, H, T <: HList, TT[A] <: TypeTag[A], W[A]](implicit
    fieldName: Witness.Aux[K],
    owritesH: Lazy[Writes[H]],
    owritesT: Lazy[DerivedOWrites[T, TT, W]]
  ): DerivedOWrites[FieldType[K, H] :: T, TT, W] =
    new DerivedOWrites[FieldType[K, H] :: T, TT, W] {
      def owrites(tagOwrites: TypeTagOWrites.Aux[W], adapter: NameAdapter) = {
        val adaptedName = adapter(fieldName.value.name)
        val derivedOwritesT = owritesT.value.owrites(tagOwrites, adapter)
        OWrites[FieldType[K, H] :: T] { case h :: t =>
          JsObject(Map(adaptedName -> owritesH.value.writes(h)) ++ derivedOwritesT.writes(t).value)
        }
      }
    }

}

trait DerivedOWritesInstances3 {

  implicit def owritesGeneric[A, R, TT[A] <: TypeTag[A], W[A]](implicit
    gen: LabelledGeneric.Aux[A, R],
    derivedOWrites: Lazy[DerivedOWrites[R, TT, W]]
  ): DerivedOWrites[A, TT, W] =
    new DerivedOWrites[A, TT, W] {
      def owrites(tagOwrites: TypeTagOWrites.Aux[W], adapter: NameAdapter) =
        OWrites.contravariantfunctorOWrites.contramap(derivedOWrites.value.owrites(tagOwrites, adapter), gen.to)
    }

}

private[derived] object DerivedOWritesUtil {
  def makeCoProductOWrites[K <: Symbol, L, R <: Coproduct, TT[A] <: TypeTag[A], W[A] >: OWrites[A]](
    makeOWritesL: (TypeTagOWrites.Aux[W], NameAdapter) => W[L],
    owritesR: Lazy[DerivedOWrites[R, TT, W]],
    typeTag: TT[FieldType[K, L]]
  ): DerivedOWrites[FieldType[K, L] :+: R, TT, W] =
    new DerivedOWrites[FieldType[K, L] :+: R, TT, W] {
      def owrites(tagOwrites: TypeTagOWrites.Aux[W], adapter: NameAdapter) = {
        // we don't want to create the OWrites instance more than once
        val owritesL = makeOWritesL(tagOwrites, adapter)
        val derivedOwriteR = owritesR.value.owrites(tagOwrites, adapter)

        OWrites[FieldType[K, L] :+: R] {
          case Inl(l) => tagOwrites.owrites(typeTag.value, owritesL).writes(l)
          case Inr(r) => derivedOwriteR.writes(r)
        }
      }
    }
}
