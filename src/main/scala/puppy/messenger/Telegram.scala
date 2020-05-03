package puppy.messenger

import java.net.URLEncoder

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import cats.implicits._
import cats.effect.{ContextShift, IO}
import puppy.model.Model.{Dog, ServiceConf}

import scala.concurrent.Future

trait MessengerTrait {
  def sendUpdate(executeRequest: HttpRequest => Future[HttpResponse],
                 dogs: List[Dog])(implicit cs: ContextShift[IO]): IO[Unit]
}

class Telegram(conf: ServiceConf) extends MessengerTrait {
  val telegramApiUrl = "https://api.telegram.org"

  override def sendUpdate(
      executeRequest: HttpRequest => Future[HttpResponse],
      dogs: List[Dog])(implicit cs: ContextShift[IO]): IO[Unit] = {
    dogs
      .map(d => {
        val uri = Uri(
          telegramApiUrl ++ s"/bot${conf.telegramToken}/sendMessage").withQuery(
          Uri.Query("chat_id" -> conf.telegramChatId,
                    "text" -> formatText(d),
                    "parse_mode" -> "HTML"))
        IO.fromFuture(
          IO(executeRequest(HttpRequest(method = HttpMethods.GET, uri = uri))))
      })
      .parSequence
      .as(())
  }

  def formatText(dog: Dog): String = {
    val picHref = dog.pics match {
      case Some(d) => d.headOption.getOrElse("")
      case None    => ""
    }
    s"""
      |<b>${dog.name}</b>
      |<a href="${picHref}">&#8205;</a>
      |<a href="${dog.shelter.url}">Shelter: ${dog.shelter.name}</a>
      |""".stripMargin.replaceAll("\n", "")
  }
}
