package example.akka.remote.smsDao;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.RoundRobinGroup;
import example.akka.remote.shared.SmsDaoMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class SmsDaoWorkerRouter extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    List<String> localWorkerPaths = null; // Arrays.asList("/user/SmsDaoWorkerRouter/w1", "/user/SmsDaoWorkerRouter/w2", "/user/SmsDaoWorkerRouter/w3");
    ActorRef localRouter = null;  //getContext().actorOf(new RoundRobinGroup(paths).props(), "router1");
    @Override
    public void preStart() {
        String myList[] = new String[SmsDaoServiceMain.ACTOR_COUNT];

        for(int i = 0; i < SmsDaoServiceMain.ACTOR_COUNT; i++) {
            getContext().actorOf(Props.create(SmsDaoService.class).withDispatcher("my-dispatcher"), "w" + i);
            myList[i] = "/user/SmsDaoWorkerRouter/w" + i;
        }

        localWorkerPaths = Arrays.asList(myList);

/*
        getContext().actorOf(Props.create(SmsDaoService.class), "w1");
        getContext().actorOf(Props.create(SmsDaoService.class), "w3");
        getContext().actorOf(Props.create(SmsDaoService.class), "w2");

        localWorkerPaths = Arrays.asList("/user/SmsDaoWorkerRouter/w1", "/user/SmsDaoWorkerRouter/w2", "/user/SmsDaoWorkerRouter/w3");
*/

        localRouter = getContext().actorOf(new RoundRobinGroup(localWorkerPaths).props(), "router1");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("Forwarding the message to Worker");

        if(SmsDaoServiceMain.TEST_MODE != 1)
        {
            localRouter.forward(message, getContext());
        }
        else {

            if (message instanceof SmsDaoMessage.Message) {
                if(((SmsDaoMessage.Message) message).getFromNumber().equalsIgnoreCase("1"))
                {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    log.error("Start time is " + dtf.format(now));
                }
                else {
                    localRouter.tell(message, getSelf());
                }
            } else if (message instanceof SmsDaoMessage.Response) {
                if(((SmsDaoMessage.Response) message).getMessage().equalsIgnoreCase("DAO Operation Done for from " +
                        (SmsDaoServiceMain.PUMP_MESSAGES-1)))
                {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    log.error("End time for message {} and time is {}",
                            ((SmsDaoMessage.Response) message).getMessage(), dtf.format(now));

                }
                else {
                    localRouter.forward(message, getContext());
                }
            }
        }

    }
}