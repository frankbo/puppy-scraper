package puppy.messenger

import java.net.URLEncoder
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import cats.implicits._
import cats.effect.{ContextShift, IO}
import puppy.model.Model.{Dog, ServiceConf}
import scala.concurrent.Future

trait MessengerTrait {
  def sendUpdate(
      executeRequest: HttpRequest => Future[HttpResponse],
      dogs: List[Dog],
      conf: ServiceConf)(implicit cs: ContextShift[IO]): IO[List[Unit]]
}

object Telegram extends MessengerTrait {
  val telegramApiUrl = "https://api.telegram.org"

  override def sendUpdate(
      executeRequest: HttpRequest => Future[HttpResponse],
      dogs: List[Dog],
      conf: ServiceConf)(implicit cs: ContextShift[IO]): IO[List[Unit]] = {
    dogs
      .map(d => {
        val text = URLEncoder.encode(formatText(d), "UTF-8")
        val uri = telegramApiUrl ++ s"/bot${conf.telegramToken}/sendMessage?chat_id=${conf.telegramChatId}&text=$text&parse_mode=HTML"
        IO(println(d.name))
//        IO.fromFuture(
//          IO(executeRequest(HttpRequest(method = HttpMethods.GET, uri = uri))))
      })
      .parSequence
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
