package dashboard

import scala.scalajs.js.JSApp
import org.scalajs.dom._

object DashboardApp extends JSApp {

  def main(): Unit = {
    document.getElementById("scalajs").innerHTML = "Hello form ScalaJS!"
  }

}
