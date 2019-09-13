package sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sample.GreetingActor.Greet;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
//import static sample.SpringExtension.SPRING_EXTENSION_PROVIDER;

public class Main {


    public static void main(String args[])
    {

        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfiguration.class);
        ActorSystem actorSystem = (ActorSystem) ctx.getBean("actorSystem");
        SpringExt ext = SpringExtension.SPRING_EXTENSION_PROVIDER.get(actorSystem);

        ActorRef greeter = actorSystem.actorOf(ext.props("greetingActor"), "greeter");
        FiniteDuration duration = FiniteDuration.create(1, TimeUnit.SECONDS);
        Timeout timeout = Timeout.durationToTimeout(duration);
        Future<Object> result = ask(greeter, new Greet("John"), timeout);
        try {
            String myResult = (String)Await.result(result, duration);
            System.out.println("===================== " + myResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        ActorRef counter = actorSystem.actorOf(ext.props("countingactor"), "counter");
        FiniteDuration dur = FiniteDuration.create(1, TimeUnit.SECONDS);
        Timeout timeOut = Timeout.durationToTimeout(duration);
        Future<Object> res = ask(counter, new CountingActor.Count(), timeout);
        try {
            String myResult = (String)Await.result(res, duration);
            System.out.println("===================== " + myResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

    }
}
