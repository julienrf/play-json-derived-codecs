package julienrf.json

import play.api.libs.json.Reads

package object derived {

  def reads[A](implicit derivedReads: DerivedReads[A]): Reads[A] = derivedReads

}
