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
import pureconfig._
import pureconfig.generic.auto._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends IOApp {
  implicit val system: ActorSystem = ActorSystem("puppy-scraper")
  implicit val ec: ExecutionContext = system.dispatcher

  val config: ServiceConf = ConfigSource.default.loadOrThrow[ServiceConf]
  val getFromUrl = JsoupBrowser().get(_)
  val ref = Ref.of[IO, List[Dog]](List.empty)
  val telegram = new Telegram(Http().singleRequest(_), config)

  def fetchShelters(get: String => Browser#DocumentType,
                    listRef: Ref[IO, List[Dog]]): IO[Unit] = {
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

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO(println("Start crawling"))
      initRef <- ref
      _ <- repeat(fetchShelters(getFromUrl, initRef)) //Fiber or not??
    } yield ExitCode.Success
}
