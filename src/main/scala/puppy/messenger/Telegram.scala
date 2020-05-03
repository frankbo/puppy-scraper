package puppy.messenger

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import cats.effect.{ContextShift, IO}
import cats.implicits._
import puppy.model.Model.{Dog, ServiceConf}

import scala.concurrent.Future

trait MessengerTrait {
  def sendUpdate(dogs: List[Dog]): IO[Unit]
}

class Telegram(executeRequest: HttpRequest => Future[HttpResponse],
               conf: ServiceConf)(implicit cs: ContextShift[IO])
    extends MessengerTrait {
  val telegramApiUrl = "https://api.telegram.org"

  override def sendUpdate(dogs: List[Dog]): IO[Unit] = {
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
