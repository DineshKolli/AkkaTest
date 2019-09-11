package com.dynamic.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.Call;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DynamicActorMain  {

    public static int NUMBER_OF_ACTORS=10000;
    public static int ACTOR_SLEEP = 10;
    public static int CPS = 500;
    public static int ACTORS_PER_CALL = 1;
    public static String MY_IP = "127.0.0.1";
    public static String MY_PORT = "5099";

    public static void main(String[] args) {

        for(int i = 0; i < args.length; i++) {
            if (args[i].toUpperCase().startsWith("CALL_COUNT")) {
                NUMBER_OF_ACTORS = Integer.parseInt(args[i + 1]);
                continue;
            }
            if (args[i].toUpperCase().startsWith("ACTOR_PROCESSING")) {
                ACTOR_SLEEP = Integer.parseInt(args[i + 1]);
                continue;
            }
            if (args[i].toUpperCase().startsWith("CPS")) {
                CPS = Integer.parseInt(args[i + 1]);
                continue;
            }
            if (args[i].toUpperCase().startsWith("ACTORS_PER_CALL")) {
                ACTORS_PER_CALL = Integer.parseInt(args[i + 1]);
                continue;
            }
        }

        System.out.println("Trying with call count " +  NUMBER_OF_ACTORS);

        ActorSystem system = ActorSystem.create("DynamicActorService", ConfigFactory.load());
        ActorRef apiService1 = system.actorOf(Props.create(DynamicActorService.class).withDispatcher("my-dispatcher"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Dynamic Actor Call Creation Started time is " + dtf.format(now));



        int sleep_time = ((CPS/5)*2)/ACTORS_PER_CALL;

        for(int i = 1; i < NUMBER_OF_ACTORS*ACTORS_PER_CALL; i++) {
             if(i%100 == 0)
             {
                 try {
                     Thread.sleep(sleep_time);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
             apiService1.tell(new Call.CreateCall(i+"", "1234567"+i, ""+i), ActorRef.noSender());
        }
        now = LocalDateTime.now();
        System.out.println("Dynamic Actor Call Creation Completed time is " + dtf.format(now));


        apiService1.tell("GetCount", ActorRef.noSender());

        for(int i = 1; i < NUMBER_OF_ACTORS*ACTORS_PER_CALL; i++) {

            apiService1.tell(new Call.DeleteCall(i+"", "1234567"+i, ""+i), ActorRef.noSender());
        }
        now = LocalDateTime.now();
        System.out.println("Dynamic Actor Call Deletion Completed time is " + dtf.format(now));

        while(DynamicActorService.actorCount != 0) {

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        now = LocalDateTime.now();
        System.out.println("Dynamic Actor Call Deletion Freeup time is " + dtf.format(now));
        apiService1.tell("GetCount", ActorRef.noSender());
    }


}
