import org.scalatestplus.play._

class ApplicationSpec extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with FirefoxFactory {

  "The Application" must {
    "display a text when clicking on a button" in {
      go to (s"http://localhost:$port")
      pageTitle mustBe "Hello"
      click on find(id("button")).value
      eventually {
        find(id("text")).map(_.text) mustBe app.configuration.getString("text")
      }
    }
  }

}
