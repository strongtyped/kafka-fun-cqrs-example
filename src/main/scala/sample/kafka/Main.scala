package sample.kafka

import akka.actor.{ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import io.funcqrs.akka.backend.AkkaBackend
import io.funcqrs.config
import io.funcqrs.config.CustomOffsetPersistenceStrategy
import io.funcqrs.projections.{Projection, PublisherFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Random
import io.funcqrs.config.api._
import org.apache.kafka.common.serialization.StringDeserializer
import org.reactivestreams.Publisher

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

object Main {

  val actorSys = ActorSystem("test")
  implicit val materializer = ActorMaterializer()(actorSys)

  val logger = LoggerFactory.getLogger(this.getClass)

  type KafkaMsg = ConsumerMessage.CommittableMessage[String, String]

  def main(args: Array[String]): Unit = {
    
    val configu = ConfigFactory.load()

    val kafkaConfig = configu.getConfig("kafka-events-and-commands")
    
    val source =
      Consumer
        .committableSource(
          settings = ConsumerSettings(kafkaConfig, new StringDeserializer, new StringDeserializer),
          subscription = Subscriptions.topics(kafkaConfig.getStringList("topics").toSet)
        )

    val backend = new KafkaBackend

    backend.configure {
      projection(
        projection = new KafkaProjection,
        
        // a publisherFactory that will start the kafka source
        publisherFactory = new PublisherFactory[CommittableOffset, String] {
          
          override def from(offset: Option[CommittableOffset]): Publisher[(CommittableOffset, String)] = {
            source
              .collect {
                // send the commitableOffset to be used by the OffsetPersistenceStrategy
                case e => (e.committableOffset, e.record.value())
              }
              .runWith(Sink.asPublisher(false))
          }

        }
      )
      .withOffsetPersistenceStrategy(
        new CustomOffsetPersistenceStrategy[CommittableOffset] {
          override def saveCurrentOffset(offset: CommittableOffset): Future[Unit] = {
            // commit the offset
            offset.commitScaladsl().map(_ => ())
          }
            
          override def readOffset: Future[Option[CommittableOffset]] = {
            // do nothing on read, kafka takes care of it
            // we will be commiting the consumed offset to the topic, so no need to read the offset
            Future.successful(None) 
          }
            
        }
      )
    }

    Thread.sleep(120000)

  }

  class KafkaBackend extends AkkaBackend {
    override val actorSystem: ActorSystem = actorSys
  }

  class KafkaProjection extends Projection[String] {
    // using sync.HandleEvent as we don't do nothing special
    override def handleEvent = sync.HandleEvent {
      case any => println(s"got this one! $any")
    }
  }

}
