package puppy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import puppy.messenger.Telegram
import puppy.model.Model.{Dog, ServiceConf}
import puppy.resources.Olpe
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Config {
  import pureconfig._
  import pureconfig.generic.auto._

  def readConfig(): IO[ServiceConf] = {
    IO(ConfigSource.default.loadOrThrow[ServiceConf])
  }
}

object Main extends IOApp {
  implicit val system: ActorSystem = ActorSystem("puppy-scraper")
  implicit val ec: ExecutionContext = system.dispatcher

  def fetchShelters(get: String => Browser#DocumentType,
                    listRef: Ref[IO, List[Dog]],
                    telegram: Telegram): IO[Unit] = {
    val shelters = List(Olpe.getPuppies(get))
    for {
      dogs <- shelters
        .parTraverse(_.recoverWith { case _ => IO(List.empty) })
        .map(_.flatten)
      currentDogs <- listRef.get
      diffedDogs = dogs.diff(currentDogs)
      _ <- listRef.set(dogs)
      _ <- telegram.sendUpdate(diffedDogs)
    } yield ()
  }

  def repeat(io: IO[Unit]): IO[Nothing] =
    io >> IO.sleep(1.minute) >> IO.suspend(repeat(io)) // use >> instead of *> for stack safety

  override def run(args: List[String]): IO[ExitCode] = {
    val getFromUrl = JsoupBrowser().get(_)
    for {
      _ <- IO(println("Start crawling"))
      config <- Config.readConfig()
      telegramClient = new Telegram(Http().singleRequest(_), config)
      initRef <- Ref.of[IO, List[Dog]](List.empty)
      _ <- repeat(fetchShelters(getFromUrl, initRef, telegramClient)) //Fiber or not??
    } yield ExitCode.Success
  }
}
