package example.akka.remote.smsApi;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.SmsApiMessages;
import example.akka.remote.shared.SmsDaoMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SmsApiServiceMain {

    public static String VALIDATION_ROUTER_IP = "";
    public static String DAO_ROUTER_IP = "";
    public static String VALIDATION_ROUTER_PORT = "";
    public static String DAO_ROUTER_PORT = "";
    public static int PUMPSMS_COUNT = 20;
    public static int SLEEP_INT = 300;
    public static int BREAK_INT = 1000;

    public static void main(String[] args) {
        for(int i = 0; i < args.length; i++)
        {
            if(args[i].toUpperCase().startsWith("VALIP"))
            {
                VALIDATION_ROUTER_IP = args[i+1];
                continue;
            }
            else if(args[i].toUpperCase().startsWith("VALPORT"))
            {
                VALIDATION_ROUTER_PORT = args[i+1];
                continue;
            }
            else if(args[i].toUpperCase().startsWith("DAOIP"))
            {
                DAO_ROUTER_IP = args[i+1];
                continue;
            }
            else if(args[i].toUpperCase().startsWith("DAOPORT"))
            {
                DAO_ROUTER_PORT = args[i+1];
                continue;
            }
            else if(args[i].toUpperCase().startsWith("SMSCOUNT"))
            {
                PUMPSMS_COUNT = Integer.parseInt(args[i+1]);
                continue;
            }
            else if(args[i].toUpperCase().startsWith("SLEEP"))
            {
                SLEEP_INT = Integer.parseInt(args[i+1]);
                continue;
            }
            else if(args[i].toUpperCase().startsWith("BREAK"))
            {
                BREAK_INT = Integer.parseInt(args[i+1]);
                continue;
            }
        }
        if(VALIDATION_ROUTER_IP.isEmpty())
        {
            VALIDATION_ROUTER_IP = "127.0.0.1";
        }
        if(DAO_ROUTER_IP.isEmpty())
        {
            DAO_ROUTER_IP = "127.0.0.1";
        }
        if(VALIDATION_ROUTER_PORT.isEmpty())
        {
            VALIDATION_ROUTER_PORT = "2559";
        }
        if(DAO_ROUTER_PORT.isEmpty())
        {
            DAO_ROUTER_PORT = "5001";
        }

        ActorSystem system = ActorSystem.create("SmsApiService", ConfigFactory.load());
        ActorRef apiService1 = system.actorOf(Props.create(SmsApiService.class).withDispatcher("my-dispatcher"));

        ActorSelection daoRouter = system.actorSelection("akka.tcp://SmsDaoCluster@" +
                SmsApiServiceMain.DAO_ROUTER_IP + ":" + SmsApiServiceMain.DAO_ROUTER_PORT + "/user/SmsDaoRouter");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Start time is " + dtf.format(now));
        ActorRef apiService;

        for(int i = 1; i < PUMPSMS_COUNT; i++) {

            if( i%BREAK_INT == 0)
            {
                try {
                    now = LocalDateTime.now();
                    if(i%10000 == 0 )
                        System.out.println("Pumped messages till now " + i + " current time"  + dtf.format(now));
                    Thread.sleep(SLEEP_INT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /*
            if(i%3 == 0)
            {
                apiService = apiService1;
            }
            else if(i%3 == 1)
            {
                apiService = apiService2;
            }
            else
            {
                apiService = apiService3;
            }



*/
            apiService1.tell(new SmsApiMessages.Message(i+"", "1234567"+i, "This is my SMS "+i), ActorRef.noSender());


            //daoRouter.tell(new SmsDaoMessage.Message(i+"", "1234567"+i, "This is my SMS "+i), ActorRef.noSender());
        }
        now = LocalDateTime.now();
        System.out.println("END time is " + dtf.format(now));


    }
}
