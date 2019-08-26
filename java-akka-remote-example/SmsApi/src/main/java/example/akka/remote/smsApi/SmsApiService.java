package example.akka.remote.smsApi;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.RoundRobinGroup;
import example.akka.remote.shared.Messages;
import example.akka.remote.shared.SmsApiMessages;
import example.akka.remote.shared.SmsDaoMessage;
import example.akka.remote.shared.SmsValidationMessage;

import java.util.Arrays;
import java.util.List;

public class SmsApiService extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);



    private ActorSelection route = getContext().actorSelection("akka.tcp://SmsValidationCluster@" +
            SmsApiServiceMain.VALIDATION_ROUTER_IP + ":"+ SmsApiServiceMain.VALIDATION_ROUTER_PORT +"/user/SmsValidationRouter");
    private ActorSelection daoRouter = getContext().actorSelection("akka.tcp://SmsDaoCluster@" +
            SmsApiServiceMain.DAO_ROUTER_IP + ":" + SmsApiServiceMain.DAO_ROUTER_PORT + "/user/SmsDaoRouter");


    /*

    // Getting the other actor
    private ActorSelection selection1 = getContext().actorSelection("akka.tcp://SmsValidationCluster@127.0.0.1:2557/user/CommonValidationActor");
    private ActorSelection selection2 = getContext().actorSelection("akka.tcp://SmsValidationCluster@127.0.0.1:2556/user/CommonValidationActor");

    List<String> paths = Arrays.asList("akka.tcp://SmsValidationCluster@127.0.0.1:2556/user/CommonValidationActor",
                                        "akka.tcp://SmsValidationCluster@127.0.0.1:2557/user/CommonValidationActor");

    private static int loadBalancer = 0;

    ActorRef router = null;


     */

    @Override
    public void preStart() {
        log.info("Started Api Service Actor");
    }

    @Override
    public void onReceive(Object message) throws Exception {


        if (message instanceof SmsApiMessages.Message) {

            log.info("Got a Sms Api Message, send it to the Validator");

            String from = ((SmsApiMessages.Message) message).getFromNumber();
            String to = ((SmsApiMessages.Message) message).getToNumber();
            String smsMessage = ((SmsApiMessages.Message) message).getSmsMessage();

            //if(!SmsApiServiceMain.DAO_ROUTER_IP.equalsIgnoreCase("127.0.0.1"))
            {
                SmsDaoMessage.Message daoMessage = new SmsDaoMessage.Message(from, to, smsMessage);
                daoRouter.tell(daoMessage, getSelf());
            }
            //else {
            //    SmsValidationMessage.Message newMessage = new SmsValidationMessage.Message(from, to, smsMessage);
            //    route.tell(newMessage, getSelf());
            //}



        }


        else if (message instanceof SmsValidationMessage.ValidationResponse) {
            String result = ((SmsValidationMessage.ValidationResponse) message).getMessage();

            log.info("Got result back Validator: {}", result);


        }
    }
}
