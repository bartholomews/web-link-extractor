package wle.producer

import cats.data.EitherT
import cats.effect.Async
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import sttp.client4._
import sttp.model.Uri
import wle.domain.RawMarkup

trait UrlFetcher[F[_]] {
  def fetch: fs2.Pipe[F, Uri, RawMarkup]
}

object UrlFetcher {
  def impl[F[_]: Async: Logger](backend: Backend[F]): UrlFetcher[F] =
    new UrlFetcher[F] {
      override val fetch: fs2.Pipe[F, Uri, RawMarkup] =
        _.evalMapFilter(uri => {
          basicRequest
            .get(uri)
            .response(asString)
            .send(backend)
            .attemptT
            .flatMap[Throwable, RawMarkup](res =>
              res.body match {
                case Right(str) => EitherT.rightT(RawMarkup(uri, str))
                case Left(err) =>
                  EitherT.leftT(
                    new Exception(s"status: [${res.statusText}], error: [$err]")
                  )
              }
            )
            .leftSemiflatTap(err =>
              Logger[F].error(err)(
                s"An error occurred while fetching uri: [${uri.toString()}]"
              )
            )
            .toOption
            .value
        })
    }
}
