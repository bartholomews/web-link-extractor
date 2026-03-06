package wle.domain

import sttp.model.Uri

final case class ExtractionResult(uri: Uri, links: List[Hyperlink]) {
  def render: String =
    (uri.toString :: links.map(l => s"  ${l.value}")).mkString("\n")
}
