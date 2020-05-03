package puppy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import puppy.messenger.Telegram
import puppy.model.Model.{Dog, ServiceConf}
import puppy.resources.Olpe
import pureconfig._
import pureconfig.generic.auto._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("puppy-scraper")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timer: Timer[IO] = IO.timer(ec)
  implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ec)

  val config: ServiceConf = ConfigSource.default.load[ServiceConf] match {
    case Right(c) => c
    case Left(e)  => throw new Error(e.toString)
  }

  val getFromUrl = JsoupBrowser().get(_)
  val ref = Ref.of[IO, List[Dog]](List.empty)

  def fetchShelters(get: String => Browser#DocumentType,
                    conf: ServiceConf,
                    listRef: Ref[IO, List[Dog]]): IO[List[Unit]] = {
    val shelters = List(Olpe.getPuppies(get))

    shelters
      .parTraverse(_.recoverWith({ case _ => IO(List.empty) }))
      .map(_.flatten)
      .flatMap(dogs => {
        val listOfDogs = listRef.get.map(_.diff(dogs))
        listRef.set(dogs)
        listOfDogs
      })
      .flatMap(d => Telegram.sendUpdate(Http().singleRequest(_), d, conf))
  }

  def repeat(io: IO[List[Unit]]): IO[Nothing] =
    io >> IO.sleep(1.minute) >> IO.suspend(repeat(io)) // use >> instead of *> for stack safety

  val app = for {
    _ <- IO(println("Start crawling"))
    initRef <- ref
    threads <- repeat(fetchShelters(getFromUrl, config, initRef)).start
  } yield threads

  app.unsafeRunSync() // Move to IOApp and use IO.never

  StdIn.readLine() // let it run until user presses return

  // Cancel the threads afterwards.
}
