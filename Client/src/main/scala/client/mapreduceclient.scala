package client

import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Props}
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import common._

object MapReduceClient{

  def main(args: Array[String]): Unit = {
	// set up mapreduce client actor system
    implicit val system = ActorSystem("Appka", ConfigFactory.parseString("""
       akka.loglevel = INFO
       akka.actor.provider = cluster
    """)
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = [MapReduceClient]"))
    .withFallback(ConfigFactory.load()))

	// begin management and bootstrapping process
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

	// establish master actor to handle messaging
    val master = system.actorOf(Props[MasterActor], name = "MasterActor")

	// sleep to allow time for all servers to join cluster
    Thread.sleep(30000)

	// send book messages to messages to master actor to distribute to map actors
    master ! Textbook("The Pickwick Papers", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg580.txt")
    master ! Textbook("Life And Adventures Of Martin Chuzzlewit", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg968.txt")
    master ! Textbook("Hunted Down", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg807.txt")
    master ! Textbook("Great Expectations", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg1400.txt")
    master ! Textbook("A Tale of Two Cities", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg98.txt")
    master ! Textbook("The Cricket on the Hearth", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg20795.txt")
    master ! Textbook("Bleak House", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg1023.txt")
    master ! Textbook("Our Mutual Friend", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg883.txt")
    master ! Textbook("Dombey and Son", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg821.txt")
    master ! Textbook("Oliver Twist", "http://reed.cs.depaul.edu/lperkovic/csc536/homeworks/gutenberg/pg730.txt")

	Thread.sleep(20000)
	// let ReduceServers know that the mapping is done
    master ! Flush
  }

}
