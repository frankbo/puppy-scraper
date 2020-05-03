package puppy.resources

import cats.effect.IO
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import puppy.model.Model.{Dog, Shelter}

object Olpe extends ResourceTrait {
  val baseUrl: String = "https://www.tierheim-olpe.de"
  val url: String = baseUrl ++ "/hunde/unsere-hunde/index.php"

  override def getPuppies(
      getFromUrl: String => Browser#DocumentType): IO[List[Dog]] =
    IO(getDogs(getFromUrl(url)))

  def getDogs(doc: Browser#DocumentType): List[Dog] = {
    val list = (doc >> elementList("div.paragraph")).lift(1)
    list match {
      case Some(l) => extractSingleDog(l >> elementList("p"))
      case None    => List.empty
    }
  }

  def extractSingleDog(elements: List[Element]): List[Dog] = {
    val (head, tail) = elements.splitAt(3)
    if (head.length != 3) {
      List.empty
    } else {
      Dog(name = head.head >> text("p"),
          shelter = Shelter("Olpe", url),
          pics = Some(extractPicsSrc(head(1))),
          description = head(2) >> text("p")) :: extractSingleDog(tail)
    }
  }

  def extractPicsSrc(element: Element): List[String] = {
    val urls = element >> elementList("img") >?> attr("src")
    urls.flatten.map(v => baseUrl ++ v.drop(5))
  }
}
