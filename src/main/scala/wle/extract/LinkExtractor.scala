package wle.extract

import org.jsoup.Jsoup
import sttp.model.Uri
import wle.domain.{Hyperlink, RawMarkup}

import scala.jdk.CollectionConverters._

object LinkExtractor {
  def extract[F[_]]: fs2.Pipe[F, RawMarkup, (Uri, List[Hyperlink])] =
    _.map(markup => {
      (
        markup.uri,
        Jsoup
          .parse(markup.content)
          .select("a[href], area[href]")
          .asScala
          .map(el => Hyperlink(el.attr("href")))
          .toList
      )
    })
}
