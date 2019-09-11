package example.akka.remote.smsDao;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.SmsApiMessages;
import example.akka.remote.shared.SmsDaoMessage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


public class SmsDaoServiceMain {


    //public static String DAO_PORT = "2565";
    public static String DAO_PORT = "5001";
    public static int ACTOR_COUNT = 4;
    public static int TEST_MODE = 0;
    public static String DISPATCHER = "my-dispatcher";

    public static int PUMP_MESSAGES = 1000000;

    static ActorRef localRouter = null;


    public static void main(String[] args) {
//        deleteFile();


        int additionalConfigPresent = 0;
        for(int i = 0; i < args.length; i++) {
            if (args[i].toUpperCase().startsWith("ACTOR_COUNT")) {
                ACTOR_COUNT = Integer.parseInt(args[i + 1]);
                additionalConfigPresent = additionalConfigPresent + 2;
                continue;
            }
            else if (args[i].toUpperCase().startsWith("TEST_MODE")) {
                TEST_MODE = Integer.parseInt(args[i + 1]);
                additionalConfigPresent = additionalConfigPresent + 2;
                continue;
            }
            else if (args[i].toUpperCase().startsWith("PUMP_MESSAGES")) {
                PUMP_MESSAGES = Integer.parseInt(args[i + 1]);
                additionalConfigPresent = additionalConfigPresent + 2;
                continue;
            }
            else if (args[i].toUpperCase().startsWith("DISPATCHER")) {
                DISPATCHER = args[i + 1];
                additionalConfigPresent = additionalConfigPresent + 2;
                continue;
            }
        }
        if (args.length == 0) {
            startupClusterNodes(Arrays.asList("2900",  DAO_PORT));
            //startupClusterNodes(Arrays.asList("2900",  DAO_PORT));
        } else {
            String newArgs[] = new String[args.length-additionalConfigPresent];
            for(int i = 0; i < newArgs.length; i++)
            {
                newArgs[i] = args[additionalConfigPresent+i];
            }
            startupClusterNodes(Arrays.asList(newArgs));
        }

        if(TEST_MODE == 1) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Starting in Test Mode. =============================== ");
            for(int i = 0; i < PUMP_MESSAGES; i++) {
                localRouter.tell(new SmsDaoMessage.Message(i+"", "1234567"+i, "This is my SMS "+i), ActorRef.noSender());
            }

        }
    }


    private static void startupClusterNodes(List<String> ports) {
        System.out.printf("Start cluster on port(s) %s%n", ports);

        for (String port : ports) {

            if(port.equalsIgnoreCase("2900"))
            {
                ActorSystem system  = ActorSystem.create("SmsDaoCluster", setupClusterNodeConfig("2900"));
                localRouter = system.actorOf(Props.create(SmsDaoWorkerRouter.class).withDispatcher(DISPATCHER), "SmsDaoWorkerRouter");
            }
            else if(port.equalsIgnoreCase(DAO_PORT))
            {
                ActorSystem system2  = ActorSystem.create("SmsDaoCluster", setupClusterNodeConfig(DAO_PORT));
                system2.actorOf(Props.create(SmsDaoRouter.class).withDispatcher(DISPATCHER), "SmsDaoRouter");
            }
            else {
                ActorSystem localRouterSystem  = ActorSystem.create("SmsDaoCluster", setupClusterNodeConfig(port));
                localRouter = localRouterSystem.actorOf(Props.create(SmsDaoWorkerRouter.class).withDispatcher(DISPATCHER), "SmsDaoWorkerRouter");
            }

        }
    }

    private static Config setupClusterNodeConfig(String port) {

        if(port.equalsIgnoreCase(DAO_PORT)) {
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
