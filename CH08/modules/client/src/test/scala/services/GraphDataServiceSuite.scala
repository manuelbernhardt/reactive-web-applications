package services

import biz.enef.angulate.core.HttpPromise
import dashboard._
import org.scalajs.dom._
import utest._

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSExportAll

object GraphDataServiceSuite extends TestSuite {
  val tests = TestSuite {
    "GraphDataService should establish a WebSocket connection at startup" - {
      val growlMock = new GrowlServiceMock
      val mockedWebsocketDataStream = new WebsocketDataStreamMock()
      val mockedWebsocketService: js.Function = {
        (url: String, options: js.UndefOr[js.Dynamic]) =>
          mockedWebsocketDataStream.asInstanceOf[WebsocketDataStream]
      }

      new GraphDataService(
        mockedWebsocketService.asInstanceOf[WebsocketService],
        growlMock.asInstanceOf[GrowlService]
      )

      assert(mockedWebsocketDataStream.isInitialized)
    }
  }
}

@JSExportAll
class GrowlServiceMock

@JSExportAll
class WebsocketDataStreamMock {
  val isInitialized = true
  def send[T](data: js.Any): HttpPromise[T] = ???
  def onMessage(
    callback: js.Function1[MessageEvent, Unit],
    options: UndefOr[js.Dynamic] = js.undefined
  ): Unit = {}
  def onClose(callback: js.Function1[CloseEvent, Unit]): Unit = {}
  def onOpen(callback: js.Function1[js.Dynamic, Unit]): Unit = {}
}
