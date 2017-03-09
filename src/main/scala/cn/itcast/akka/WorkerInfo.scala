package cn.itcast.akka

/**
  * Created by Jinbin Zhu on 3/8/17.
  */
class WorkerInfo(val id: String, val memory: Int, val cores: Int) {

  //TODO last heart beat
  var heartBeatTime: Long = _
}
