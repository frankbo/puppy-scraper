package puppy.resources

import net.ruippeixotog.scalascraper.browser.Browser
import puppy.model.DogModel.Dog

import scala.concurrent.{ExecutionContext, Future}

trait PuppiesI {
  def getPuppies(getFromUrl: String => Browser#DocumentType)(
      implicit ec: ExecutionContext): Future[List[Dog]]
}
