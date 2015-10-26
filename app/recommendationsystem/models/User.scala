package recommendationsystem.models


import play.api.libs.json._
import recommendationsystem.models.storage.{UsersOdb, UsersDao}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class Anag(name: String, lastName: String)

object Anag {
  implicit val anagFormat = Json.format[Anag]
}

/**
 * A class that represent a user
 *
 * @param id
 * @param email
 * @param anag
 * @param tags
 */
case class User(
                 id: String,
                 email: Option[String] = None,
                 anag: Option[Anag] = None,
                 tags: Option[List[(Tag, Double, Long)]] = None) {
  /**
   * Update the weight of tag, this means that user 'like' _tags
   * @param newTags tags that user likes with value of 'like'
   * @return new user with new value
   */
  def addTags(newTags: List[Tag]): User = {
    val minRating = 1D
    val step = 1 /*0.1D*/
    val now = System.currentTimeMillis
    val updatedTags = tags match {
      case Some(userTags) =>
        val userSetTags = userTags.map(p => p._1).toSet
        val (tagsToUpdate, tagsToAdd) = (newTags.toSet).partition(userSetTags.contains(_))
        val updatedUserTagts = userTags.map { t =>
          if (tagsToUpdate.contains(t._1) /*&& t._2 < 10*/ ) (t._1, t._2 + step, now)
          else t
        }
        updatedUserTagts ++ tagsToAdd.map((_, minRating, now))
      case None =>
        newTags.map((_, minRating, now))
    }
    copy(tags = Some(updatedTags))
  }

  def merge(that: User) = {
    that match {
      case User(id, None, None, tags) =>
        copy(tags = tags)
      case User(id, email, None, tags) =>
        copy(email = email, tags = tags)
      case User(id, None, anag, tags) =>
        copy(anag = anag, tags = tags)
      case User(id, email, anag, tags) =>
        copy(anag = anag, email = email, tags = tags)
    }
  }

}


/** Companion object for class User */
object User {

  implicit val userFormat: Format[User] = recommendationsystem.formatters.json.UserFormatters.storageFormatter

  def applyWithToken(id: String, _email: Option[String] = None, token: Option[String], anag: Option[Anag] = None, tags: Option[List[(Tag, Double, Long)]] = None) = {
    val email = _email.orElse(token)
    User(id, email, anag, tags)
  }

}


 // Companion object for manage class User, contains utility method and databases access method

object Users extends UsersDao {
/**
   * Update tags weights of user
   *
   * @param user User that tags have to be updated
   * @return a Future with new user updated
   */
    def updateWeight(user: User): Future[User] = {
      val futureUsers = this.find(user.id)
      val futureUser = futureUsers.map(users => users.head)
      futureUser.map { u =>
        u.tags match {
          case Some(t) => u.addTags(t.map(t => t._1))
          case None => u
        }
      }
    }

  override def count: Future[Long] = UsersOdb.count

  override def all: Future[List[User]] = UsersOdb.all

  override def remove(e: User): Future[Boolean] = UsersOdb.remove(e)

  override def save(e: User, upsert: Boolean): Future[Boolean] = UsersOdb.save(e,upsert)

  override def find(id: String): Future[Option[User]] = UsersOdb.find(id)

  override def update(e: User): Future[Boolean] = UsersOdb.update(e)
}


