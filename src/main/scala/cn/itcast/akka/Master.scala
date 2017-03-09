package cn.itcast.akka

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.concurrent.duration._

/**
  * Created by Jinbin Zhu on 3/8/17.
  */
class Master(masterHost: String, masterPort: Int) extends Actor{

  var idToWorker = new HashMap[String, WorkerInfo]()
  var workers = new mutable.HashSet[WorkerInfo]()
  val CHECK_INTERVAL = 20000

  //test preStar invoked after main constructor
  override def preStart(): Unit = {
    import context.dispatcher
    context.system.scheduler.schedule(0 millis,  CHECK_INTERVAL millis, self, CheckHeartBeat)
  }

  override def receive: Receive = {
  //used to receive messages
    case RegisterWorker(workerId, memory, cores) =>
      if(!idToWorker.contains(workerId)){
        //if worker has not registered yet!
        val workerInfo = new WorkerInfo(workerId, memory, cores)
        idToWorker.put(workerId, workerInfo)
        workers += workerInfo
        sender ! RegisteredWorker(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
      }

    case HeartBeat(workId) =>
      if(idToWorker.contains(workId)){
        val workerInfo = idToWorker(workId)
        val currentTime = System.currentTimeMillis()
        workerInfo.heartBeatTime = currentTime
      }


    case CheckHeartBeat =>
      val currentTime = System.currentTimeMillis()
      val toRemove = workers.filter(currentTime - _.heartBeatTime > CHECK_INTERVAL)
      for(worker <- toRemove){
        println(s"$worker failed")
        workers -= worker
        idToWorker -= worker.id
      }
      println(workers)
  }
}

object Master{

  def main(args: Array[String]): Unit = {

    val masterHost = args(0)
    val masterPort = args(1).toInt
    val configContent =
      s"""
        |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname = "$masterHost"
        |akka.remote.netty.tcp.port = "$masterPort"
      """.stripMargin

    val config = ConfigFactory.parseString(configContent)

    //ActorSystem -- responsible for creating and monitoring actors
    val actorSystem = ActorSystem("MasterSystem", config)

    //create actor
    //actorSystem.actorOf(Props[Master], "Master")
    val master = actorSystem.actorOf(Props(new Master(masterHost, masterPort)), "Master")

    actorSystem.awaitTermination()
  }
}
