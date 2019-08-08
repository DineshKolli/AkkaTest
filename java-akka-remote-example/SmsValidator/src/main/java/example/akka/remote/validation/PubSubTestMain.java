package example.akka.remote.validation;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.SmsDaoMessage;

public class PubSubTestMain {




    public static void main(String... args) {



        ActorSystem actorSystem = ActorSystem.create("SmsValidationCluster", setupClusterNodeConfig("2800"));
        //ActorSystem actorSystem = ActorSystem.create("SmsValidationInterfaceDispatcherCluster", setupClusterNodeConfig("2800"));
        actorSystem.actorOf(Props.create(SmsValidationInterfaceDispatcher.class), "SmsValidationInterfaceDispatcher");

        ActorRef mediator = DistributedPubSub.get(actorSystem).mediator();
        ActorRef publisher = actorSystem.actorOf(Props.create(Publisher.class), "sender");
        mediator.tell(new DistributedPubSubMediator.Put(publisher), publisher);


        ActorSystem actorSystem2 = ActorSystem.create("SmsValidationCluster", setupClusterNodeConfig("2556"));
        actorSystem2.actorOf(Props.create(CommonValidationActor.class), "CommonValidationActor");
        actorSystem2.log().info("Akka node {}", actorSystem2.provider().getDefaultAddress());

      //  ActorRef mediator2 = DistributedPubSub.get(actorSystem2).mediator();
        ActorRef publisher2 = actorSystem2.actorOf(Props.create(Publisher.class), "sender2");
        //mediator2.tell(new DistributedPubSubMediator.Put(mediator2), publisher2);



        try {
            Thread.sleep(5000);
        }
        catch(Exception e)
        {
            actorSystem.log().info("Error ====== ");
        }
        publisher.tell(new SmsDaoMessage.Message("5678","1234567890", "This is my SMS"), publisher);
        publisher2.tell(new SmsDaoMessage.Message("1234","1234567890", "This is my SMS"), null);

    }

    private static Config setupClusterNodeConfig(String port) {
        if(port.equalsIgnoreCase("2559")) {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port) + "akka.cluster.roles = [frontend]")
                    .withFallback(ConfigFactory.load("myRouter"));
        }
        else if(port.equalsIgnoreCase("2800")) {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port))
                    .withFallback(ConfigFactory.load());
        }
        else {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port) +
                            String.format("akka.remote.artery.canonical.port=%s%n", port) + "akka.cluster.roles = [backend]")
                    .withFallback(ConfigFactory.load());
        }
    }
}
