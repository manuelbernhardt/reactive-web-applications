object Match {

  def spellOut(number: Int): String = number match {
    case 1 => "one"
    case 2 => "two"
    case 42 => "forty-two"
    case _ => "unknown number"
  }

  spellOut(42)

}
