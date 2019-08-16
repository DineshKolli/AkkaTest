package example.akka.remote.smsApi;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.SmsApiMessages;

public class SmsApiServiceMain {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("SmsApiService", ConfigFactory.load());
        ActorRef apiService = system.actorOf(Props.create(SmsApiService.class));
        for(int i = 200000; i < 200063; i++) {
            apiService.tell(new SmsApiMessages.Message(i+"", "1234567"+i, "This is my SMS "+i), ActorRef.noSender());
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
