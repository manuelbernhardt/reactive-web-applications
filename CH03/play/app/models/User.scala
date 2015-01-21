case class User(id: Long, firstName: String, lastName: String)

object User {
  def findOneById(id: Long): Option[User] = None
  def updateFirstName(id: Long, newFirstName: String) = ()
}
