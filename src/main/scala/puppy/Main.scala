package puppy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.effect.IO
import cats.effect.concurrent.Ref
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import puppy.messenger.Telegram
import puppy.model.Model.{Dog, ServiceConf}
import puppy.resources.{Iserlohn, Olpe}
import pureconfig._
import pureconfig.generic.auto._

import scala.io.StdIn
import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("puppy-scraper")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config = ConfigSource.default.load[ServiceConf] match {
    case Right(c) => c
    case Left(e)  => throw new Error(e.toString)
  }

  val getFromUrl = JsoupBrowser().get(_)
  val ref = Ref.of[IO, List[Dog]](List.empty)
  system.scheduler.schedule(10.seconds, 5.minutes) {
    val animalShelters =
      List(Olpe.getPuppies(getFromUrl))
    val result =
      Future
        .traverse(animalShelters)(_.recoverWith({
          case _ => Future { List.empty }
        }))
        .map(_.flatten)
        .flatMap(d => Telegram.sendUpdate(Http().singleRequest(_), d, config))

    result.onComplete({
      case Failure(error) => println(error)
      case Success(_)     => println("")
    })
  }

  StdIn.readLine() // let it run until user presses return
}
