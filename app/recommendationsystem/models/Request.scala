package recommendationsystem.models

import recommendationsystem.models.storage.{RequestsOdb, RequestsDao}

import scala.concurrent.Future

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

object Request{

  //implicit val requestFormat = formatters.json.RequestFormatters.storageFormatter
}

object Requests extends RequestsDao {

  //implicit val storageFormat = formatters.json.RequestFormatters.storageFormatter

  override def count: Future[Long] = RequestsOdb.count

  override def all: Future[List[Request]] = RequestsOdb.all

  override def remove(e: Request): Future[Boolean] = RequestsOdb.remove(e)

  override def save(e: Request, upsert: Boolean): Future[Boolean] = RequestsOdb.save(e,upsert)

  override def find(query: String): Future[List[Request]] = Requests.find(query)
}
