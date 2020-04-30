package puppy.resources

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import puppy.model.DogModel.Dog

import scala.concurrent.{ExecutionContext, Future}

object Iserlohn extends PuppiesI {
  val baseUrl = "http://www.tierheim-iserlohn.de/"
  override def getPuppies(getFromUrl: String => Browser#DocumentType)(
      implicit ec: ExecutionContext): Future[List[Dog]] = Future {
    getDogs(getFromUrl(baseUrl ++ "hunde.html"))
  }

  def getDogs(doc: Browser#DocumentType): List[Dog] = {
    (doc >> elementList("#imPage div > p.imAlign_center"))
      .filter(v => {
        (v >> elementList("span")).length == 3 &&
        (v >> elementList("span.fb")).length == 1
      })
      .map(v => {
        val name = (v >> texts("span")).head
        val pics =
          (v >> elementList("img") >?> attr("src")).flatten
            .map(v => baseUrl ++ v)
        Dog(name = name, pics = Some(pics))
      })
  }
}
