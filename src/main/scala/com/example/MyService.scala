package com.example


import akka.actor.Actor
import com.thinkaurelius.titan.graphdb.blueprints.TitanBlueprintsGraph
import com.thinkaurelius.titan.core. { TitanGraph, TitanFactory, TitanType, TitanVertex }
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.gremlin.scala.GremlinScalaPipeline
import com.tinkerpop.gremlin.scala.ScalaGraph
import com.tinkerpop.gremlin.scala.ScalaGraph.unwrap
import com.tinkerpop.gremlin.scala.ScalaPipeFunction
import com.tinkerpop.gremlin.scala.ScalaVertex.wrap
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

    def getTitanConnection : TitanGraph = {

      if (g == null || !g.isOpen()) {
        g = TitanFactory.open(getTitanConf);
      }

      g
    }

  //def g = TitanFactory.open(getTitanConf)

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {


            var g: TitanGraph = getTitanConnection// TitanFactory.open(getTitanConf);

            //g.newTransaction()

            var nameType: TitanType = g.getType("name");
            if (nameType == null) {
              g.makeKey("name");
              g.makeKey("name").dataType(String).indexed(Vertex).make();
              g.makeLabel("place").make();
              g.makeLabel("married").make();

            }
            var cityType: TitanType = g.getType("city");
            if (cityType == null) {
              g.makeKey("city").dataType(String).indexed(Vertex).indexed(Edge).indexed("search",Vertex).indexed("search",Edge).make();
            }
            var locationType: TitanType = g.getType("location");
            if (locationType == null) {
              g.makeKey("location").dataType(Geoshape).indexed(Vertex).indexed(Edge).indexed("search",Vertex).indexed("search",Edge).make();
            }


            val juno = g.addVertex(null);
            juno.setProperty("quote", "I like milk")
            juno.setProperty("name", "David")
            juno.setProperty("city", "bismarck")
            juno.setProperty("content", "why won't this work???")

            val jupiter = g.addVertex(null);
            jupiter.setProperty("name", "jupiter");
            Edge friends = g.addEdge(null, juno, jupiter, "friends");
            Edge family = g.addEdge(null, juno, jupiter, "family");


            g.commit()

            //println(juno.getProperty("name"));


            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>! {juno.getProperty("name")}</h1>
              </body>
            </html>
          }
        }
      }
    }
  path("/vertex") {
    respondWithMediaType(`application/json`) {
      complete {
        var g: TitanGraph = getTitanConnection
        val id: Long = 2;

        var vertex: TitanVertex = g.getVertex(id);
        //return vertex;

        var json: JSONObject = com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility.jsonFromElement(vertex, null, GraphSONMode.EXTENDED);
        System.out.println(json.toString());
        return json.toString();
      }
    }
  }
}