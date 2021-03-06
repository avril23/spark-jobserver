package spark.jobserver

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import com.typesafe.config.{ Config, ConfigFactory, ConfigValueFactory }
import akka.testkit.TestProbe
import spark.jobserver.common.CommonMessages
import CommonMessages.{ JobErroredOut, JobResult }
import spark.jobserver.common.io.JobDAOActor
import collection.JavaConversions._

class NamedObjectsJobSpec extends JobSpecBase(JobManagerSpec.getNewSystem) {

  //private val emptyConfig = ConfigFactory.parseString("spark.jobserver.named-object-creation-timeout = 60 s")

  before {
    dao = new InMemoryDAO
    daoActor = system.actorOf(JobDAOActor.props(dao))
    manager = system.actorOf(JobManagerActor.props(JobManagerSpec.getContextConfig(adhoc = false)))
    supervisor = TestProbe().ref

    manager ! JobManagerActor.Initialize(daoActor, None)
    
    expectMsgClass(classOf[JobManagerActor.Initialized])

    uploadTestJar()

  }

  val jobName = "spark.jobserver.NamedObjectsTestJob"

  private def getCreateConfig(createDF: Boolean, createRDD: Boolean, createBroadcast: Boolean = false) : Config = {
    ConfigFactory.parseString("spark.jobserver.named-object-creation-timeout = 60 s, " +
        NamedObjectsTestJobConfig.CREATE_DF + " = " + createDF + ", " +
        NamedObjectsTestJobConfig.CREATE_RDD + " = " + createRDD + ", " +
        NamedObjectsTestJobConfig.CREATE_BROADCAST + " = " + createBroadcast)
  }
  
  private def getDeleteConfig(names: List[String]) : Config = {
    ConfigFactory.parseString("spark.jobserver.named-object-creation-timeout = 60 s, " + 
        NamedObjectsTestJobConfig.DELETE+" = [" + names.mkString(", ") + "]")
  }
  
  describe("NamedObjects (RDD)") {
    it("should survive from one job to another one") {

      manager ! JobManagerActor.StartJob("demo", jobName, getCreateConfig(false, true), errorEvents ++ syncEvents)
      val JobResult(_, names: Array[String]) = expectMsgClass(classOf[JobResult])
      names should contain("rdd1")

      manager ! JobManagerActor.StartJob("demo", jobName, getCreateConfig(false, false), errorEvents ++ syncEvents)
      val JobResult(_, names2: Array[String]) = expectMsgClass(classOf[JobResult])

      names2 should contain("rdd1")
      names2 should not contain("df1")

      //clean-up
      manager ! JobManagerActor.StartJob("demo", jobName, getDeleteConfig(List("rdd1")), errorEvents ++ syncEvents)
      val JobResult(_, names3: Array[String]) = expectMsgClass(classOf[JobResult])

      names3 should not contain("rdd1")
      names3 should not contain("df1")
    }
  }

  describe("NamedObjects (DataFrame)") {
    it("should survive from one job to another one") {

      manager ! JobManagerActor.StartJob("demo", jobName, getCreateConfig(true, false), errorEvents ++ syncEvents)
      val JobResult(_, names: Array[String]) = expectMsgClass(classOf[JobResult])

      names should contain("df1")

      manager ! JobManagerActor.StartJob("demo", jobName, getCreateConfig(false, false), errorEvents ++ syncEvents)
      val JobResult(_, names2: Array[String]) = expectMsgClass(classOf[JobResult])

      names2 should equal(names)
      
      //clean-up
      manager ! JobManagerActor.StartJob("demo", jobName, getDeleteConfig(List("df1")), errorEvents ++ syncEvents)
      val JobResult(_, names3: Array[String]) = expectMsgClass(classOf[JobResult])
    }
  }

  describe("NamedObjects (DataFrame + RDD)") {
    it("should survive from one job to another one") {

      manager ! JobManagerActor.StartJob("demo", jobName, getCreateConfig(true, true), errorEvents ++ syncEvents)
      val JobResult(_, names: Array[String]) = expectMsgClass(classOf[JobResult])
      
      names should contain("rdd1")
      names should contain("df1")

      manager ! JobManagerActor.StartJob("demo", jobName, getCreateConfig(false, false), errorEvents ++ syncEvents)
      val JobResult(_, names2: Array[String]) = expectMsgClass(classOf[JobResult])

      names2 should equal(names)
      
      //clean-up
      manager ! JobManagerActor.StartJob("demo", jobName, getDeleteConfig(List("rdd1", "df1")), errorEvents ++ syncEvents)
      val JobResult(_, names3: Array[String]) = expectMsgClass(classOf[JobResult])
    }
  }

  describe("NamedObjects (Broadcast)") {
    it("should survive from one job to another one") {

      manager ! JobManagerActor.StartJob("demo", jobName, getCreateConfig(true, true, true), errorEvents ++ syncEvents)
      val JobResult(_, names: Array[String]) = expectMsgClass(classOf[JobResult])
      
      names should contain("rdd1")
      names should contain("df1")
      names should contain("broadcast1")

      manager ! JobManagerActor.StartJob("demo", jobName, getCreateConfig(false, false, false), errorEvents ++ syncEvents)
      val JobResult(_, names2: Array[String]) = expectMsgClass(classOf[JobResult])

      names2 should equal(names)
      
      //clean-up
      manager ! JobManagerActor.StartJob("demo", jobName, getDeleteConfig(List("rdd1", "df1", "broadcast1"))
        , errorEvents ++ syncEvents)
      val JobResult(_, names3: Array[String]) = expectMsgClass(classOf[JobResult])
    }
  }
}
