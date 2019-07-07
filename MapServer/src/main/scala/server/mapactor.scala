package server

import akka.actor.{Actor, ActorSelection, ActorSystem, Props, RootActorPath}
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.{Cluster, Member}
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import com.typesafe.config.ConfigFactory
import common._

import scala.collection.mutable.HashSet
import scala.io.Source

class MapActor() extends Actor {

  println(self.path)

  // get the cluster reference
  val cluster = Cluster(context.system)
  var mapNodes = List.empty[Member]
  var reduceNodes = List.empty[Member]
  var masterAddress:ActorSelection = context.actorSelection("")

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  val STOP_WORDS_LIST = List("a", "am", "an", "and", "are", "as", "at", "be",
    "do", "go", "if", "in", "is", "it", "of", "on", "the", "to")

  def receive = {
    case Textbook(title: String, url: String) =>
      process(title, url)
    case Flush =>
      for(member<-reduceNodes){
        context.actorSelection(RootActorPath(member.address)/"user"/"ReduceActor") ! Flush
      }
    case MemberUp(member) => register(member)

  }

  def register(member: Member): Unit ={
    if(member.hasRole("MapActor")){
      mapNodes = member :: mapNodes
      println("***************add map node***************")
    }else if(member.hasRole("ReduceActor")){
      reduceNodes = member :: reduceNodes
      println("***************add reduce node***************")
    }else if(member.hasRole("MapReduceClient")){
      masterAddress = context.actorSelection(RootActorPath(member.address)/"user"/"MasterActor")
      println("***************add client node***************")
      for(member<-mapNodes){
        printf("member %s\n", member)
        masterAddress ! member
      }
    }
  }

  def process(title: String, url: String) = {
    // get content via url
    var content = ""
    try {
      content = Source.fromURL(url).mkString
    } catch {
      case e: Exception => content = ""
    }

    // record names that have been sent
    var nameRecorded = HashSet[String]()
    for (name <- content.split("[\\p{Punct}\\s]+"))
      if ((!STOP_WORDS_LIST.contains(name.toLowerCase)) && name.matches("[A-Z][a-z]*") && !nameRecorded.contains(name)) {
        nameRecorded += name
        val index = Math.abs((title.hashCode()) % reduceNodes.length)
        context.actorSelection(RootActorPath(reduceNodes(index).address) / "user" / "ReduceActor") ! Pair(name, title)
      }
  }
}

 object MapActor {
   def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("Appka", ConfigFactory.parseString("""
       akka.loglevel = INFO
       akka.actor.provider = cluster
    """)
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = [MapActor]"))
    .withFallback(ConfigFactory.load()))

     AkkaManagement(system).start()
     ClusterBootstrap(system).start()

     val MapActor = system.actorOf(Props[MapActor], name = "MapActor")

   }
 }

