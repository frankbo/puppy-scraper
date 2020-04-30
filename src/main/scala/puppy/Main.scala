package puppy

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import puppy.model.DogModel.Dog
import puppy.resources.{Iserlohn, Olpe}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

object Main extends App {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val getFromUrl: String => Browser#DocumentType = JsoupBrowser().get
  val puppiesIserlohn = Iserlohn.getPuppies(getFromUrl)
  val puppiesOlpe = Olpe.getPuppies(getFromUrl)
  val result = for {
    iserlohn <- puppiesIserlohn
    olpe <- puppiesOlpe
  } yield iserlohn ++ olpe // TODO applicative. Check that one of all resolved

  result.onComplete({
    case Success(puppies) => puppies.map(println(_)) //akka client
    case Failure(error)   => println(error)
  })

  StdIn.readLine() // let it run until user presses return
}
