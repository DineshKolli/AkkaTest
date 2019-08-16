package example.akka.remote.smsApi;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.SmsApiMessages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SmsApiServiceMain {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("SmsApiService", ConfigFactory.load());
        ActorRef apiService = system.actorOf(Props.create(SmsApiService.class));


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Start time is " + dtf.format(now));

        for(int i = 1; i < 100000; i++) {
            apiService.tell(new SmsApiMessages.Message(i+"", "1234567"+i, "This is my SMS "+i), ActorRef.noSender());
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        now = LocalDateTime.now();
        System.out.println("END time is " + dtf.format(now));


    }
}
