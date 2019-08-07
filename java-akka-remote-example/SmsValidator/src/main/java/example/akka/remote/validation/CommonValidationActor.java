package example.akka.remote.validation;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.akka.remote.shared.SmsDaoMessage;
import example.akka.remote.shared.SmsValidationMessage;

import java.util.HashMap;
import java.util.Map;

public class CommonValidationActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef creditActor = getContext().actorOf(Props.create(CreditCheckService.class), "CreditCheckActor");
    private ActorRef dncActor = getContext().actorOf(Props.create(DncCheckService.class), "DncCheckActor");


    private ActorSelection selection = getContext().actorSelection("akka.tcp://SmsValidationInterfaceDispatcherCluster@127.0.0.1:2558/user/SmsValidationInterfaceDispatcher");

    static ActorSystem actorSystem = null;
    static ActorRef publisher = null;

    private static Config setupClusterNodeConfig(String port) {

        if(port.equalsIgnoreCase("2559")) {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port) + "akka.cluster.roles = [frontend]")
                    .withFallback(ConfigFactory.load("myRouter"));
        }
        else {
            return ConfigFactory.parseString(
                    String.format("akka.remote.netty.tcp.port=%s%n", port) +
                            String.format("akka.remote.artery.canonical.port=%s%n", port) + "akka.cluster.roles = [backend]")
                    .withFallback(ConfigFactory.load());
        }
    }


    //public CommonValidationActor(ActorSystem actorSystem)
    public CommonValidationActor()
    {
        /*
        if(actorSystem == null) {
            ActorSystem actorSystem2 = ActorSystem.create("SmsValidationCluster", setupClusterNodeConfig("0"));
            actorSystem2.actorOf(Props.create(SmsValidationInterfaceDispatcher.class), "CommonValidationActor");
            //ActorRef mediator = DistributedPubSub.get(actorSystem).mediator();
            publisher = actorSystem2.actorOf(Props.create(Publisher.class), "sender");
        }
        else
        {
            //ActorSystem actorSystem2 = ActorSystem.apply("SmsValidationInterfaceDispatcherCluster");
             //= ActorSystem.create("SmsValidationInterfaceDispatcherCluster", setupClusterNodeConfig("2558"));
            //actorSystem2.actorOf(Props.create(SmsValidationInterfaceDispatcher.class), "SmsValidationInterfaceDispatcher");
            publisher = getContext().system().actorOf(Props.create(Publisher.class), "sender");
        }
            //mediator.tell(new DistributedPubSubMediator.Put(publisher), publisher);
       //this.publisher = publisher;

         */
    }


    ActorRef getMyPublisher()
    {
        return publisher;
        /*
        if(publisher == null)
        {
            publisher = getContext().actorOf(Props.create(Publisher.class), "sender");
        }
        return publisher;

         */
    }


    public boolean performValidation(SmsValidationMessage.Message message)
    {
        boolean validationResult = true;
        if(message.getFromNumber().length() > 6)
        {
            validationResult = false;
        }
        else if(message.getToNumber().length() < 6)
        {
            validationResult = false;
        }
        else if(message.getSmsMessage().length() == 0)
        {
            validationResult = false;
        }

        return validationResult;

    }

    public static Map<String, String> fromToCount = new HashMap<String, String>();
    public static Map<String, SmsValidationMessage.Message> fromToMessage = new HashMap<String, SmsValidationMessage.Message>();
    public static Map<String, ActorRef> fromToActor = new HashMap<String, ActorRef>();

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("onReceive({} and my node is {})", message, getSelf().toString());
        if (message instanceof SmsValidationMessage.Message)
        {
            //getMediator();
            log.info("got a SmsMessage for validation for from " + ((SmsValidationMessage.Message) message).getFromNumber());
            if(performValidation((SmsValidationMessage.Message)message))
            {

                fromToCount.put(((SmsValidationMessage.Message) message).getFromNumber(), "2");
                //actorToValidator.put(dncActor, ((SmsValidationMessage.Message) message).getFromNumber());
                //actorToValidator.put(creditActor, ((SmsValidationMessage.Message) message).getFromNumber());
                fromToMessage.put(((SmsValidationMessage.Message) message).getFromNumber(), ((SmsValidationMessage.Message)message));
                fromToActor.put(((SmsValidationMessage.Message) message).getFromNumber(), getSender());

                dncActor.tell(message, getSelf());
                creditActor.tell(message, getSelf());

            }
            else
            {
                log.info("SMS Validation Failed");
                getSender().tell(new SmsValidationMessage.ValidationResponse("Validation Failed for from " + ((SmsValidationMessage.Message) message).getFromNumber()), getSelf());
            }

        }
        else if (message instanceof SmsDaoMessage.Response) {
            String result = ((SmsValidationMessage.ValidationResponse) message).getMessage();
            log.info("Got result back Validator: {}", result);
        }
        else if (message instanceof SmsValidationMessage.DncResponse) {
            String result = ((SmsValidationMessage.DncResponse) message).getMessage();
            log.info("Got result back to from DNC Validator: {}", result);
            String from = ((SmsValidationMessage.DncResponse) message).getFrom();
            if(result.equalsIgnoreCase("success"))
            {
                String count = fromToCount.get(from);

                if(!count.equalsIgnoreCase("2"))
                {
                    log.info("----------------------------------> DONE DncResponse " + count);
                    log.info("----------------------------------> DONE DncResponse from " + from);
                    fromToCount.remove(from);

                    if(from != null) {
                        SmsValidationMessage.Message origMessage = (SmsValidationMessage.Message) fromToMessage.get(from);
                        ActorRef apiActor = fromToActor.get(from);

                        fromToMessage.remove(from);
                        fromToActor.remove(from);
                        apiActor.tell(new SmsValidationMessage.ValidationResponse("Validation Success for from " + from), getSelf());
                        selection.tell(new SmsDaoMessage.Message(origMessage.getFromNumber(), origMessage.getToNumber(), origMessage.getSmsMessage()), getSelf());


                        //mediator.tell(new DistributedPubSubMediator.Put(publisher), publisher);
                        //getMyPublisher().tell(new SmsDaoMessage.Message(origMessage.getFromNumber(), origMessage.getToNumber(), origMessage.getSmsMessage()), getMyPublisher());

                    }
                }
                else
                {
                    fromToCount.remove(from);
                    fromToCount.put(from,"1");
                }
            }
            else
            {
                fromToCount.remove(from);
                ActorRef apiActor = fromToActor.get(from);

                fromToMessage.remove(from);
                fromToActor.remove(from);
                getSender().tell(new SmsValidationMessage.ValidationResponse("DNC Check Failed for from " + from ), getSelf());
            }
        }
        else if (message instanceof SmsValidationMessage.CreditCheckResponse) {
            String result = ((SmsValidationMessage.CreditCheckResponse) message).getMessage();
            log.info("Got result back to from CreditCheck Validator: {}", result);
            String from = ((SmsValidationMessage.CreditCheckResponse) message).getFrom();
            if(result.equalsIgnoreCase("success"))
            {
                String count = fromToCount.get(from);
                if(!count.equalsIgnoreCase("2"))
                {
                    fromToCount.remove(from);
                    log.info("----------------------------------> DONE CreditCheckResponse " + count);
                    log.info("----------------------------------> DONE CreditCheckResponse from " + from);
                    SmsValidationMessage.Message origMessage  = (SmsValidationMessage.Message)fromToMessage.get(from);
                    if(from != null) {
                        ActorRef apiActor = fromToActor.get(from);

                        fromToMessage.remove(from);
                        fromToActor.remove(from);
                        apiActor.tell(new SmsValidationMessage.ValidationResponse("Validation Success for from " + from), getSelf());
                        selection.tell(new SmsDaoMessage.Message(origMessage.getFromNumber(), origMessage.getToNumber(), origMessage.getSmsMessage()), getSelf());


                        //mediator.tell(new DistributedPubSubMediator.Put(publisher), publisher);
                        //getMyPublisher().tell(new SmsDaoMessage.Message(origMessage.getFromNumber(), origMessage.getToNumber(), origMessage.getSmsMessage()), getMyPublisher());

                        /*
                        sender.tell(new SmsDaoMessage.Message(origMessage.getFromNumber(), origMessage.getToNumber(), origMessage.getSmsMessage()), null);
                        /*
                        boolean localAffinity = false;
                        mediator.tell(
                                new DistributedPubSubMediator.Send("/system/SmsValidationInterfaceDispatcher",
                                        new SmsDaoMessage.Message(origMessage.getFromNumber(), origMessage.getToNumber(), origMessage.getSmsMessage()),
                                        localAffinity),
                                getSelf());

                         */
                    }
                }
                else
                {
                    fromToCount.remove(from);
                    fromToCount.put(from,"1");
                }
            }
            else
            {
                fromToCount.remove(from);
                ActorRef apiActor = fromToActor.get(from);

                fromToMessage.remove(from);
                fromToActor.remove(from);
                getSender().tell(new SmsValidationMessage.ValidationResponse("Credit Check Failed for from " + from), getSelf());
            }
        }
        else {
            unhandled(message);
        }
    }

}
