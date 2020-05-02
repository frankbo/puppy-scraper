package puppy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import puppy.messenger.Telegram
import puppy.model.Model.ServiceConf
import puppy.resources.Olpe
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("puppy-scraper")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config = ConfigSource.default.load[ServiceConf] match {
    case Right(c) => c
    case Left(e)  => throw new Error(e.toString)
  }

  val getFromUrl = JsoupBrowser().get(_)
  val animalShelters =
    List(Olpe.getPuppies(getFromUrl))
  val result =
    Future
      .traverse(animalShelters)(_.recoverWith({
        case _ => Future { List.empty }
      }))
      .map(_.flatten)

  result.onComplete({
    case Success(puppies) =>
      Future
        .sequence(Telegram.sendUpdate(Http().singleRequest(_), puppies, config))
        .onComplete({ case Failure(error) => println(error) })
    case Failure(error) => println(error)
  })

  StdIn.readLine() // let it run until user presses return
}
