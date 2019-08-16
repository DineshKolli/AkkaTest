package example.akka.remote.smsDao;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class SmsDaoServiceMain {

    public static void main(String[] args) {
        //deleteFile();
        if (args.length == 0) {
            startupClusterNodes(Arrays.asList("2900", "2901", "2902", "2903", "2904", "2905","2565"));
        } else {
            startupClusterNodes(Arrays.asList(args));
        }


    }


    private static void startupClusterNodes(List<String> ports) {
        System.out.printf("Start cluster on port(s) %s%n", ports);

        for (String port : ports) {

            if(port.equalsIgnoreCase("2900"))
            {
                ActorSystem system  = ActorSystem.create("SmsDaoCluster", setupClusterNodeConfig("2900"));
                system.actorOf(Props.create(SmsDaoService.class), "SmsDaoService");
            }
            else if(port.equalsIgnoreCase("2565"))
            {
                ActorSystem system2  = ActorSystem.create("SmsDaoCluster", setupClusterNodeConfig("2565"));
                system2.actorOf(Props.create(SmsDaoRouter.class), "SmsDaoRouter");
            }
            else {
                ActorSystem system  = ActorSystem.create("SmsDaoCluster", setupClusterNodeConfig(port));
                system.actorOf(Props.create(SmsDaoService.class), "SmsDaoService");
            }

        }
    }

    private static Config setupClusterNodeConfig(String port) {

        if(port.equalsIgnoreCase("2565")) {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port) + "akka.cluster.roles = [frontend]")
                    .withFallback(ConfigFactory.load("myRouter"));
        }
        else if(port.equalsIgnoreCase("2900")) {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port) +
                            String.format("akka.remote.artery.canonical.port=%s%n", port) + "akka.cluster.roles = [backend]")
                    .withFallback(ConfigFactory.load());
        }
        else {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port) +
                            String.format("akka.remote.artery.canonical.port=%s%n", port) + "akka.cluster.roles = [backend]")
                    .withFallback(ConfigFactory.load());
        }
    }

    public static void deleteFile() {
        File file = new File("D:\\Project\\Akka\\alpakka-slick-h2-test.mv.db");

        if (file.delete()) {
            System.out.println("File alpakka-slick-h2-test.mv.db deleted successfully");
        } else {
            System.out.println("Failed to delete the file alpakka-slick-h2-test.mv.db");
        }
        file = new File("D:\\Project\\Akka\\alpakka-slick-h2-test.trace.db");
        if (file.delete()) {
            System.out.println("File alpakka-slick-h2-test.trace.db deleted successfully");
        } else {
            System.out.println("Failed to delete the file alpakka-slick-h2-test.trace.db");
        }



    }
}
