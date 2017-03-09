package cn.itcast.akka

/**
  * Created by Jinbin Zhu on 3/8/17.
  * communication across network (different process), the message (case classes) must be serialized
  */
trait RemoteMessage extends Serializable{

}

//Worker --> Master message, used to encapsulate information about worker
case class RegisterWorker(workerId: String, memory: Int, cores: Int) extends RemoteMessage

//Master --> Worker message
case class RegisteredWorker(masterURL: String) extends RemoteMessage

//Worker --> Worker heart beat instruction
case object SendHeartBeat
case class HeartBeat(workerId: String) //Worker --> Master heart beat message

//Master --> Master check heart beat
case object CheckHeartBeat



