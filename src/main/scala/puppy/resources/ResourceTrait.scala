package puppy.resources

import net.ruippeixotog.scalascraper.browser.Browser
import puppy.model.Model.Dog

import scala.concurrent.{ExecutionContext, Future}

trait ResourceTrait {
  def getPuppies(getFromUrl: String => Browser#DocumentType)(
      implicit ec: ExecutionContext): Future[List[Dog]]
}
