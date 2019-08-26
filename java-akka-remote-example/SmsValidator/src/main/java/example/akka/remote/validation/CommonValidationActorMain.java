package example.akka.remote.validation;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.SmsDaoMessage;

import java.util.Arrays;
import java.util.List;

public class CommonValidationActorMain {

    public static String DAO_IP = "172.27.6.78";
    public static int DAO_PORT = 5001;

    public static void main(String... args) {

        int additionalConfigPresent = 0;

        for(int i = 0; i < args.length; i++)
        {
            if(args[i].toUpperCase().startsWith("DAOIP"))
            {
                DAO_IP = args[i+1];
                additionalConfigPresent = additionalConfigPresent + 2;
                continue;
            }
            else if(args[i].toUpperCase().startsWith("DAOPORT"))
            {
                DAO_PORT = (int)Integer.parseInt(args[i+1]);
                additionalConfigPresent = additionalConfigPresent + 2;
                continue;
            }

        }


        if (args.length == 0 || args.length == additionalConfigPresent) {
            //startupClusterNodes(Arrays.asList("2556", "2559", "2558", "2557"));
            //startupClusterNodes(Arrays.asList("2556", "2558", "2800", "2559"));
            //startupClusterNodes(Arrays.asList("2556", "2558", "2559"));
            startupClusterNodes(Arrays.asList("2800", "2559", "2556"));
        } else {


            String newArgs[] = new String[args.length-additionalConfigPresent];
            for(int i = 0; i < newArgs.length; i++)
            {
                newArgs[i] = args[additionalConfigPresent+i];
            }

            startupClusterNodes(Arrays.asList(newArgs));
        }

     }
    private static void startupClusterNodes(List<String> ports) {
        System.out.printf("Start cluster on port(s) %s%n", ports);

        for (String port : ports) {

            if(port.equalsIgnoreCase("2558"))
            {
                ActorSystem actorSystem = ActorSystem.create("SmsValidationInterfaceDispatcherCluster", setupClusterNodeConfig(port));
                actorSystem.actorOf(Props.create(SmsValidationInterfaceDispatcher.class), "SmsValidationInterfaceDispatcher");
                actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
            }
            else if(port.equalsIgnoreCase("2559"))
            {
                ActorSystem actorSystem = ActorSystem.create("SmsValidationCluster", setupClusterNodeConfig(port));
                actorSystem.actorOf(Props.create(SmsValidationRouter.class), "SmsValidationRouter");
                actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
            }
            else if(port.equalsIgnoreCase("2800")) {
                ActorSystem actorSystem = ActorSystem.create("SmsValidationCluster", setupClusterNodeConfig(port));
                actorSystem.actorOf(Props.create(SmsValidationInterfaceDispatcher.class), "SmsValidationInterfaceDispatcher");
                actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());

                ActorRef mediator = DistributedPubSub.get(actorSystem).mediator();
                ActorRef publisher = actorSystem.actorOf(Props.create(Publisher.class), "sender");
                mediator.tell(new DistributedPubSubMediator.Put(publisher), publisher);

            }
            else {
                ActorSystem actorSystem = ActorSystem.create("SmsValidationCluster", setupClusterNodeConfig(port));
                actorSystem.actorOf(Props.create(CommonValidationActor.class, actorSystem), "CommonValidationActor");
                actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
            }

        }
    }

    private static Config setupClusterNodeConfig(String port) {

        if(port.equalsIgnoreCase("2559")) {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port) +  "akka.cluster.roles = [frontend]")
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
