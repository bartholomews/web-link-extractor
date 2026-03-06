package wle.domain

import io.circe.Encoder

final case class Hyperlink(value: String) extends AnyVal
object Hyperlink {
  implicit val encoder: Encoder[Hyperlink] = Encoder[String].contramap(_.value)
}
