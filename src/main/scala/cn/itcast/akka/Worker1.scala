package cn.itcast.akka

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._


/**
  * Created by Jinbin Zhu on 3/8/17.
  */
class Worker1(val masterHost: String, val masterPort: Int, val memory: Int, val cores: Int) extends Actor{

  val workerId = UUID.randomUUID().toString
  var master: ActorSelection = _
  val HEART_INTERVAL = 10000

  override def preStart(): Unit = {
    //register information to Master
    master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
    master ! RegisterWorker(workerId, memory, cores)
  }

  override def receive: Receive = {
    case RegisteredWorker(masterURL) =>
      println(masterURL)
      //start timer used to send hear beat
      //0 millis: send immediately; CHECK_INTERVAL millis: send every CHECK_INTERVAL
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, HEART_INTERVAL millis, self, SendHeartBeat)

    case SendHeartBeat =>
      println("send heart beat to Master...")
      master ! HeartBeat(workerId)
  }
}

object Worker1{

  def main(args: Array[String]): Unit = {

    val workerHost = args(0)
    val workerPort = args(1).toInt
    val masterHost = args(2)
    val masterPort = args(3).toInt

    val memory = args(4).toInt
    val cores = args(5).toInt

    val configContent =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$workerHost"
         |akka.remote.netty.tcp.port = "$workerPort"
      """.stripMargin

    val config = ConfigFactory.parseString(configContent)

    //ActorSystem -- responsible for creating and monitoring actors
    val actorSystem = ActorSystem("WorkerSystem", config)

    //create actor
    //actorSystem.actorOf(Props[Worker], "Worker")
    val worker1 = actorSystem.actorOf(Props(new Worker1(masterHost, masterPort, memory, cores)), "Worker1")
    actorSystem.awaitTermination()
  }
}
