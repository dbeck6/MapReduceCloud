package client

import akka.actor.{Actor, Address, Props, RootActorPath}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.MemberUp
import com.typesafe.config.ConfigFactory
import common._

class MasterActor extends Actor {
  
  // set configurations of intended map and reduce actors
  val numberMappers  = ConfigFactory.load.getInt("number-mappers")
  val numberReducers  = ConfigFactory.load.getInt("number-reducers")
  var pending = numberReducers
  var mapActors = List.empty[Address]

  // get the cluster reference
  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case mapActor:Member =>
      println("************add map actor****************")
      mapActors = mapActor.address :: mapActors
    case Textbook(title: String, url: String) =>
      val index = Math.abs((title.hashCode()) % mapActors.length)
      context.actorSelection(RootActorPath(mapActors(index)) / "user" / "MapActor") ! Textbook(title, url)
    case Flush =>
      for(mapActor<-mapActors){
        context.actorSelection(RootActorPath(mapActor) / "user" / "MapActor") ! Flush
      }
    case Done =>
      println("Received Done from" + sender)
      pending -= 1
      if (pending == 0)
//        context.system.terminate
        print("MapReduce Client Down")
  }
}
