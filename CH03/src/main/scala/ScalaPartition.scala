object ScalaPartition {

  case class User(firstName: String, lastName: String, age: Int)

  def main(args: String*): Unit = {

    val users = List(
      User("Bob", "Marley", 19),
      User("Jimmy", "Hendrix", 16)
    )

    val (minors, majors) = users.partition(_.age < 18)

  }

}
