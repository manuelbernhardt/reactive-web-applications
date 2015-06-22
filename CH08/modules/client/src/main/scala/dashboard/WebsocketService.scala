package dashboard

import biz.enef.angulate.core.{HttpPromise, ProvidedService}
import org.scalajs.dom._

import scala.scalajs.js
import scala.scalajs.js.UndefOr

trait WebsocketService extends ProvidedService {
  def apply(url: String, options: UndefOr[Dynamic] = js.undefined): WebsocketDataStream = js.native
}

trait WebsocketDataStream extends js.Object {
  def send[T](data: js.Any): HttpPromise[T] = js.native
  def onMessage(callback: js.Function1[MessageEvent, Unit], options: UndefOr[js.Dynamic] = js.undefined): Unit = js.native
  def onClose(callback: js.Function1[CloseEvent, Unit]): Unit = js.native
  def onOpen(callback: js.Function1[js.Dynamic, Unit]): Unit = js.native
}