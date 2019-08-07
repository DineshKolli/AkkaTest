package com.example;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import example.akka.remote.shared.SmsApiMessages;
import example.akka.remote.shared.SmsValidationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.PathMatchers.longSegment;

public class SmsHttpApi extends AllDirectives {

    static ActorSelection smsApiActor = null;
    static ActorSystem system = null;

    private ActorSelection route = null;

    public static void main(String[] args) throws Exception {
        // boot up server using the route as defined below
        system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are define.
        SmsHttpApi app = new SmsHttpApi();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }


    private CompletionStage<Done> sendSms(final SmsIncomingMessage sms) {
        smsApiActor = system.actorSelection("akka.tcp://SmsApiService@127.0.0.1:2562/user/SmsApiService");

        route = system.actorSelection("akka.tcp://SmsValidationCluster@127.0.0.1:2559/user/SmsValidationRouter");

        System.out.println("DINESH ------- 0");
        //SmsApiMessages.Message newMsg = new SmsApiMessages.Message(sms.getFrom(), sms.getTo(), sms.getSms());

        SmsValidationMessage.Message newMsg = new SmsValidationMessage.Message(sms.getFrom(), sms.getTo(), sms.getSms());
        System.out.println("DINESH ------- 1");

         //smsApiActor.tell(newMsg, ActorRef.noSender());
        route.tell(newMsg, ActorRef.noSender());
        System.out.println("DINESH ------- 2");
        //return CompletableFuture.completedFuture(Done.getInstance());
        return CompletableFuture.completedFuture(Done.getInstance());
    }



    private Route createRoute() {
        return concat(
                get(() ->
                        pathPrefix("item", () ->
                                path(longSegment(), (Long id) -> {
                                    final CompletionStage<Optional<List<SmsIncomingMessage>>> futureMaybeItem = fetchSmsList(id);
                                    return onSuccess(() -> futureMaybeItem, maybeItem ->
                                            maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                                    .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                    );
                                }))),

                 post(() ->
                        path("sendSms", () ->
                                entity(Jackson.unmarshaller(SmsIncomingMessage.class), sms -> {
                                    CompletionStage<Done> futureSaved = sendSms(sms);
                                    return onComplete(futureSaved, done ->
                                            complete("SMS sent")
                                    );
                                })))
        );


    }


    private static class SmsIncomingMessage {

        final String from;

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getSms() {
            return sms;
        }

        final String to;
        final String sms;

        @JsonCreator
        SmsIncomingMessage(@JsonProperty("from") String from,
                           @JsonProperty("to") String to,
                            @JsonProperty("sms") String sms) {
            this.from = from;
            this.to = to;
            this.sms = sms;
        }


    }

    CompletionStage<Optional<List<SmsIncomingMessage>>> fetchSmsList(Long fromNumber)
    {
        String fromString = fromNumber + "";
        ActorSelection selection = system.actorSelection("akka.tcp://SmsDaoActor@127.0.0.1:2565/user/SmsDaoActor");
        selection.tell("fetchAllSMS", ActorRef.noSender());
        List<SmsIncomingMessage> list = new ArrayList<>();
        list.add(new SmsIncomingMessage(fromString, "1234567890", "Reply SMS"));
        return CompletableFuture.completedFuture(Optional.of(list));
    }
}
