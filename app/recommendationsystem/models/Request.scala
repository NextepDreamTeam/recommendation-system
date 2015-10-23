package recommendationsystem.models

case class Request(
  id: String,
  user: User,
  tags: Option[List[Tag]],
  mandatoryTags: Option[List[Tag]],
  date: Long) {

  /**
   * Set the user of request
   */
  def setUser(user: User) = this match {
    //    case Request(id, None, tags, date) => Request(id, Some(user), tags, date)
    case Request(id, oldUser, tags, mandatoryTags, date) =>
      Request(id, User(user.id, oldUser.email), tags, mandatoryTags, date)
  }
}
/*
object Request{

  implicit val requestFormat = formatters.json.RequestFormatters.storageFormatter
}

object Requests extends MongoObj[Request] {
  val collectionName = "requests"//"recommendation.requests"
  implicit val storageFormat = formatters.json.RequestFormatters.storageFormatter
}
*/