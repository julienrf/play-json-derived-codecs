package julienrf.json

import play.api.libs.json.Reads

package object derived {

  def reads[A](implicit derivedReads: DerivedReads[A]): Reads[A] = derivedReads

  def reads2[A](implicit derivedReads2: DerivedReads2[A]): Reads[A] = derivedReads2.reads

}
