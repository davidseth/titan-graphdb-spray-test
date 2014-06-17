package com.example


import akka.actor.Actor
import com.thinkaurelius.titan.graphdb.blueprints.TitanBlueprintsGraph
import com.thinkaurelius.titan.core. { TitanGraph, TitanFactory, TitanType }
import com.tinkerpop.blueprints. {Vertex, Edge }
import com.thinkaurelius.titan.core.attribute.Geoshape
import com.tinkerpop.gremlin.scala._
import org.apache.commons.configuration.BaseConfiguration
import spray.http.MediaTypes._
import spray.http._
import spray.routing._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  var g: TitanGraph = null

  def getTitanConf = {
    val conf = new BaseConfiguration();
    conf.setProperty("storage.backend","cassandrathrift");
    conf.setProperty("storage.hostname","127.0.0.1");
    conf.setProperty("cache.db-cache","true");
    conf.setProperty("cache.db-cache-clean-wait","20");
    conf.setProperty("cache.db-cache-time","0");
    conf.setProperty("cache.db-cache-size","0.25");

    conf.setProperty("storage.index.search.backend", "elasticsearch");
    conf.setProperty("storage.index.search.hostname", "127.0.0.1");
    conf.setProperty("storage.index.search.client-only", "true");

    conf
  }

    def getTitanConnection = {
      if (g == null || !g.isOpen()) {
        g = TitanFactory.open(getTitanConf);
      }
      g
    }

  //def g = TitanFactory.open(getTitanConf)

  val myRoute2 = path("people" / IntNumber)  { vertexId =>
    get {
      complete {
        var g = getTitanConnection
        val id = vertexId;
        println(id)

        var vertex = g.getVertex(id);
        //return vertex;

        //var json = com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility.jsonFromElement(vertex, null, com.tinkerpop.blueprints.util.io.graphson.GraphSONMode.EXTENDED);

        <html>
          <body>
            {vertex.getProperty("name")}
          </body>
        </html>
      }
    }
  }


  val myRoute =
    path("graph") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {


            //var g: TitanGraph = getTitanConnection// TitanFactory.open(getTitanConf);
            var g = getTitanConnection// TitanFactory.open(getTitanConf);

            //g.newTransaction()

            var nameType = g.getType("name");
            if (nameType == null) {
              g.makeKey("name").dataType(classOf[String]).indexed(classOf[Vertex]).make();
              g.makeLabel("place").make();
              g.makeLabel("married").make();

            }
            var cityType: TitanType = g.getType("city");
            if (cityType == null) {
              g.makeKey("city").dataType(classOf[String]).indexed(classOf[Vertex]).indexed(classOf[Edge]).indexed("search",classOf[Vertex]).indexed("search",classOf[Edge]).make();
            }
            var locationType: TitanType = g.getType("location");
            if (locationType == null) {
              g.makeKey("location").dataType(classOf[Geoshape]).indexed(classOf[Vertex]).indexed(classOf[Edge]).indexed("search",classOf[Vertex]).indexed("search",classOf[Edge]).make();
            }


            val juno = g.addVertex(null);
            juno.setProperty("quote", "I like milk")
            juno.setProperty("name", "David")
            juno.setProperty("city", "bismarck")
            juno.setProperty("content", "why won't this work???")

            val jupiter = g.addVertex(null);
            jupiter.setProperty("name", "jupiter");
            val friends = g.addEdge(null, juno, jupiter, "friends");
            val family = g.addEdge(null, juno, jupiter, "family");


            g.commit()

            //println(juno.getProperty("name"));


            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>! {juno.getId()}</h1>
              </body>
            </html>
          }
        }
      }
    } ~
    path("people" / IntNumber)  { vertexId =>
      get {
        complete {
          var g = getTitanConnection
          val id = vertexId;
          println(id)

          var vertex = g.getVertex(id);
          //return vertex;

          var json = com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility.jsonFromElement(vertex, null, com.tinkerpop.blueprints.util.io.graphson.GraphSONMode.EXTENDED);

          {json}
          <html>
            <body>
              {vertex.getProperty("name")}
            </body>
          </html>
        }
      }
    } ~
    path("/vertex" / IntNumber) { vertexId =>
      respondWithMediaType(`text/html`) {
        complete {
          var g = getTitanConnection
          val id = vertexId;
          println(id)

          var vertex = g.getVertex(id);
          //return vertex;

          //var json = com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility.jsonFromElement(vertex, null, com.tinkerpop.blueprints.util.io.graphson.GraphSONMode.EXTENDED);
          <html>
          <body>
            {vertex.getProperty("name")}
          </body>
          </html>


          //println(vertex.getProperty("name"))

          //json
        }
      }
    }

}