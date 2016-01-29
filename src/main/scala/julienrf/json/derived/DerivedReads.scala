package julienrf.json.derived

import play.api.libs.json.{JsResult, JsValue, Reads}
import shapeless.{Lazy, LabelledGeneric}

trait DerivedReads[A] extends Reads[A]

object DerivedReads extends DerivedReadsInstances

trait DerivedReadsInstances extends DerivedReadsInstances1 {

  // TODO Instances for products and coproducts

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
