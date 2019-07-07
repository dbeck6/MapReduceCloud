package server

import akka.actor.{Actor, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import com.typesafe.config.ConfigFactory
import common._

import scala.collection.mutable.HashMap

class ReduceActor extends Actor {

  println(self.path)

  // get the cluster reference
  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  var remainingMappers = 0
  var reduceMap = HashMap[String, List[String]]()

  def receive = {
    case MemberUp(member) =>
      if(member.hasRole("MapActor")){
        remainingMappers += 1
      }
    case Pair(name:String, title:String) =>
      if (reduceMap.contains(name)) {
        // when name exists
	      reduceMap += (name -> (title::reduceMap(name)))
      }
      else
      {
        // the first time that get this name
	      reduceMap += (name -> List(title))
      }
    case Flush =>
      remainingMappers -= 1
      if (remainingMappers == 0) {
        println(self.path.toStringWithoutAddress + " : " + reduceMap)
        // close the master actor when all reduce actors are done
//        context.actorSelection("../..") ! Done
        println("Reduce is done")
      }
  }
}

 object ReduceActor{
   def main(args:Array[String]): Unit = {

    implicit val system = ActorSystem("Appka", ConfigFactory.parseString("""
       akka.loglevel = INFO
       akka.actor.provider = cluster
    """)
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = [ReduceActor]"))
    .withFallback(ConfigFactory.load()))

     AkkaManagement(system).start()
     ClusterBootstrap(system).start()
     val ReduceActor = system.actorOf(Props[ReduceActor], name = "ReduceActor")

   }
 }
