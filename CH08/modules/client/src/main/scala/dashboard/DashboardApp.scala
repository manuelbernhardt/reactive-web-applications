package dashboard

import biz.enef.angulate.ext.{Route, RouteProvider}
import biz.enef.angulate._
import scala.scalajs.js.JSApp

object DashboardApp extends JSApp {

  def main(): Unit = {
    val module = angular.createModule("dashboard", Seq("ngRoute", "ngWebSocket", "chart.js", "angular-growl"))

    module.serviceOf[GraphDataService]

    module.controllerOf[DashboardCtrl]

    module.config { ($routeProvider: RouteProvider) =>
      $routeProvider
        .when("/dashboard", Route(templateUrl = "/assets/partials/dashboard.html", controller = "dashboard.DashboardCtrl"))
        .otherwise(Route(redirectTo = "/dashboard"))
    }

  }
}
