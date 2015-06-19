package dashboard

import biz.enef.angulate._
import org.scalajs.dom._
import scala.scalajs.js.{Dynamic, JSON}
import scala.collection._

class GraphDataService($websocket: WebsocketService) extends Service {
  val dataStream = $websocket("ws://localhost:9000/graphs")

  private val callbacks =
    mutable.Map.empty[GraphType.Value, Dynamic => Unit]

  def fetchGraph(graphType: GraphType.Value, callback: Dynamic => Unit) = {
    callbacks += graphType -> callback
    dataStream.send(graphType.toString)
  }

  dataStream.onMessage { (event: MessageEvent) =>
    val json: Dynamic = JSON.parse(event.data.toString)
    val graphType = GraphType.withName(json.graph_type.toString)
    callbacks.get(graphType).map { callback =>
      callback(json)
    } getOrElse {
      console.log(s"Unknown graph type $graphType")
    }
  }
}

object GraphType extends Enumeration {
  val MonthlySubscriptions = Value
}