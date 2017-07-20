package com.blueprint.engine.transform.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.blueprint.engine.transform.config.Config
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import scala.concurrent.Future

/**
  * Example from documentation : http://doc.akka.io/docs/akka-http/current/scala/http/introduction.html#using-akka-http
  */
object WebServerExample {

  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // domain model
  final case class Item(name: String, id: Long)

  final case class Order(items: List[Item])

  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  // (fake) async database query api
  def fetchItem(itemId: Long): Future[Option[Item]] = {
    Future {
      Some(Item("test", itemId))
    }
  }

  def saveOrder(order: Order): Future[Done] = {
    Future {
      Done
    }
  }

  val route: Route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    } ~
      get {
        pathPrefix("item" / LongNumber) { id =>
          // there might be no item for a given id
          val maybeItem: Future[Option[Item]] = fetchItem(id)

          onSuccess(maybeItem) {
            case Some(item) => complete(item)
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~
      post {
        path("sql") {
          entity(as[String]) { sql =>
            val saved: Future[Any] = Future {Config.sparkSession.sql(sql)}
            onComplete(saved) { done =>
              complete("Done")
            }
          }
        }
      }

  def main(args: Array[String]) {

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done

  }
}
