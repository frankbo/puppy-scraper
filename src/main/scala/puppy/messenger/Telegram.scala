package puppy.messenger

import java.net.URLEncoder

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.Materializer
import puppy.model.Model.{Dog, ServiceConf}

import scala.concurrent.{ExecutionContext, Future}

trait MessengerTrait {
  def sendUpdate(executeRequest: HttpRequest => Future[HttpResponse],
                 dogs: List[Dog],
                 conf: ServiceConf)(
      implicit m: Materializer,
      ec: ExecutionContext): List[Future[HttpResponse]]
  // TODO ServiceConf relevant here?
}

object Telegram extends MessengerTrait {
  val telegramApiUrl = "https://api.telegram.org"

  override def sendUpdate(executeRequest: HttpRequest => Future[HttpResponse],
                          dogs: List[Dog],
                          conf: ServiceConf)(
      implicit m: Materializer,
      ec: ExecutionContext): List[Future[HttpResponse]] = {
    dogs.map(d => {
      val text = URLEncoder.encode(formatText(d), "UTF-8")
      val uri = telegramApiUrl ++ s"/bot${conf.telegramToken}/sendMessage?chat_id=${conf.telegramChatId}&text=$text&parse_mode=HTML"
      executeRequest(HttpRequest(method = HttpMethods.GET, uri = uri))
    })

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
