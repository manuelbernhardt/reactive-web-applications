package dashboard

import biz.enef.angulate._
import org.scalajs.dom._
import scalajs.js.Dynamic

class DashboardCtrl($scope: Dynamic) extends ScopeController {
  $scope.hello = "Hello, world"
  $scope.helloBack = () => console.log("Hi")
}
