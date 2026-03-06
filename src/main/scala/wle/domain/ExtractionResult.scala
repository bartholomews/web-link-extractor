package wle.domain

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import sttp.model.Uri

final case class ExtractionResult(uri: Uri, links: List[Hyperlink])

object ExtractionResult {
  implicit val uriEncoder: Encoder[Uri] = Encoder.encodeString.contramap(_.toString())
  implicit val encoder: Encoder[ExtractionResult] = deriveEncoder
}
