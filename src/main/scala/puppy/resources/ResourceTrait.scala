package puppy.resources

import cats.effect.IO
import net.ruippeixotog.scalascraper.browser.Browser
import puppy.model.Model.Dog

trait ResourceTrait {
  def getPuppies(getFromUrl: String => Browser#DocumentType): IO[List[Dog]]
}
