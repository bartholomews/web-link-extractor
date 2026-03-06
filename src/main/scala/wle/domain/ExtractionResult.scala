package wle.domain

import sttp.model.Uri

final case class ExtractionResult(uri: Uri, links: List[Hyperlink])
