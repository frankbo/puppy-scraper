package puppy.model

object DogModel {
  case class Dog(name: String,
                 description: String = "",
                 pics: Option[Seq[String]] = None,
                 shelter: String)
}
