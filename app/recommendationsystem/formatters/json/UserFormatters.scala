/*package recommendationsystem.formatters.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import recommendationsystem.models._
import play.api.data.validation.ValidationError

object UserFormatters {

  implicit val storageFormatter: Format[User] = {
    val userReads: Reads[User] = (
      (__ \ "id").readNullable[String].map(_.getOrElse("")) ~
      (__ \ "email").readNullable[String](email) ~
      (__ \ "anag").readNullable[Anag] ~
      (__ \ "tags").readNullable(
        list[(Tag, Double, Long)](
          (__ \ "tag").read[String].map(str => Tag(str, None)) ~
            (__ \ "weight").read[Double] ~
            (__ \ "lastInsert").read[Long] tupled)))(User.apply _)

    import play.api.libs.json.Writes._
    val userWrites: Writes[User] = (
      (__ \ "id").write[String] ~
      (__ \ "email").writeNullable[String] ~
      (__ \ "anag").writeNullable[Anag] ~
      (__ \ "tags").writeNullable(
        Writes.traversableWrites[(Tag, Double, Long)](
          (__ \ "tag").write[String].contramap[Tag](tag => tag.flatten) ~
            (__ \ "weight").write[Double] ~
            (__ \ "lastInsert").write[Long] tupled)))(unlift(User.unapply))
    Format(userReads, userWrites)
  }

  implicit val restFormatter: Format[User] = {

    def converToEmail(implicit reads: Reads[String]) = {
      val converted = reads.map { str => new String(new sun.misc.BASE64Decoder().decodeBuffer(str))}
      email(converted)
    }

    val userReads: Reads[User] = (
      (__ \ "id").readNullable[String].map(_.getOrElse("")) ~
      (__ \ "email").readNullable[String](email) ~
      (__ \ "token").readNullable[String](converToEmail) ~
      (__ \ "anag").readNullable[Anag] ~
      (__ \ "tags").readNullable(
        list[(Tag, Double, Long)](
          (__ \ "tag").read[String].map(str => Tag(str, None)) ~
            (__ \ "weight").read[Double] ~
            (__ \ "lastInsert").read[Long] tupled)))(User.applyWithToken _)

    import play.api.libs.json.Writes._
    val userWrites: Writes[User] = (
      (__ \ "id").write[String] ~
      //(__ \ "email").writeNullable[String] ~
      (__ \ "token").writeNullable[String].contramap[Option[String]](a => a match { case Some(s) => Some(new sun.misc.BASE64Encoder().encode(s.getBytes())) case None => None }) ~
      (__ \ "anag").writeNullable[Anag] ~
      (__ \ "tags").writeNullable(
        Writes.traversableWrites[(Tag, Double, Long)](
          (__ \ "tag").write[String].contramap[Tag](tag => tag.flatten) ~
            (__ \ "weight").write[Double] ~
            (__ \ "lastInsert").write[Long] tupled)))(unlift(User.unapply))
    Format(userReads, userWrites)
  }

}
*/
