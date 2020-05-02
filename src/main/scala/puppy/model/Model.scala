package puppy.model

object Model {
  case class Shelter(name: String, url: String)

  case class Dog(name: String,
                 description: String = "",
                 pics: Option[Seq[String]] = None,
                 shelter: Shelter)

  case class ServiceConf(telegramToken: String, telegramChatId: String)
}
