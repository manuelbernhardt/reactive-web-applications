class ForComprehension {

  val aList = List(1, 2, 3)
  val bList = List(4, 5, 6)

  val result = for {
    a <- aList
    if a > 1
    b <- bList
    if b < 6
  } yield a + b


}
