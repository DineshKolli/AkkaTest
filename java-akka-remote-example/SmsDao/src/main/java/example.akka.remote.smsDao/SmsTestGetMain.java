package example.akka.remote.smsDao;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.SmsDaoMessage;

import java.util.Set;

public class SmsTestGetMain {

    private static Config setupClusterNodeConfig(String port) {

        if(port.equalsIgnoreCase("2565")) {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port))
                    .withFallback(ConfigFactory.load());
        }
        return null;
    }

    public static void main(String args[])
    {
        ActorSystem system  = ActorSystem.create("SmsDaoActor", setupClusterNodeConfig("2565"));
        system.actorOf(Props.create(SmsDaoService.class), "SmsDaoActor");

        ActorSelection selection = system.actorSelection("akka.tcp://SmsDaoActor@127.0.0.1:2565/user/SmsDaoActor");

        selection.tell("fetchAllSMS", ActorRef.noSender());



    }

}
