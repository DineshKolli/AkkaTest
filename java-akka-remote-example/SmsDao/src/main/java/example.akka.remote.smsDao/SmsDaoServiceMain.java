package example.akka.remote.smsDao;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;


public class SmsDaoServiceMain {

    public static void main(String[] args) {
        //deleteFile();
        ActorSystem system  = ActorSystem.create("SmsDaoActor", setupClusterNodeConfig("2565"));
        system.actorOf(Props.create(SmsDaoService.class), "SmsDaoActor");
    }

    private static Config setupClusterNodeConfig(String port) {

        if(port.equalsIgnoreCase("2565")) {
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
