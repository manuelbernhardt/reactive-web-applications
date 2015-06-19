package dashboard

import biz.enef.angulate._
import org.scalajs.dom._
import scala.scalajs.js
import scalajs.js.Dynamic

class DashboardCtrl($scope: Dynamic, graphDataService: GraphDataService) extends ScopeController {
  graphDataService.fetchGraph(GraphType.MonthlySubscriptions, { (graphData: js.Dynamic) =>
    console.log(graphData)
    $scope.monthlySubscriptions = graphData
  })
}
