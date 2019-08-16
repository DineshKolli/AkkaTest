package com.example;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import example.akka.remote.shared.SmsValidationMessage;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static akka.http.javadsl.server.PathMatchers.longSegment;

public class SmsHttpApi extends AllDirectives {

    static ActorSelection smsApiActor = null;
    static ActorSystem system = null;

    ActorSelection route = system.actorSelection("akka.tcp://SmsValidationCluster@127.0.0.1:2559/user/SmsValidationRouter");
    ActorSelection selection = system.actorSelection("akka.tcp://SmsDaoCluster@127.0.0.1:2565/user/SmsDaoRouter");

    public static void main(String[] args) throws Exception {
        // boot up server using the route as defined below
        system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are define.
        SmsHttpApi app = new SmsHttpApi();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8082), materializer);

        System.out.println("Server online at http://localhost:8082/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }


    private CompletionStage<Done> sendSms(final SmsIncomingMessage sms) {
        SmsValidationMessage.Message newMsg = new SmsValidationMessage.Message(sms.getFrom(), sms.getTo(), sms.getSms());
        route.tell(newMsg, ActorRef.noSender());
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private Route createRoute() {
        return concat(
                get(() ->
                        pathPrefix("getSms", () ->
                            path(longSegment(), (Long id) -> {
                            final CompletionStage<Optional<List<SmsIncomingMessage>>> futureMaybeItem = fetchSms(id);
                            return onSuccess(() -> futureMaybeItem, maybeItem ->
                                    maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                            .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                            );
                        }))),
                get(() ->
                        pathPrefix("emptyDatabase", () -> {
                               // path(longSegment(), (Long id) -> {
                                    final CompletionStage<Optional<String>> futureMaybeItem = emptyDatabase();
                                    return onSuccess(() -> futureMaybeItem, maybeItem ->
                                            maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                                    .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                    );
                                })),
                get(() ->
                        pathPrefix("smsTotalCount", () -> {
                            // path(longSegment(), (Long id) -> {
                            final CompletionStage<Optional<String>> futureMaybeItem = getSmsTotalCount();
                            return onSuccess(() -> futureMaybeItem, maybeItem ->
                                    maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                            .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                            );
                        })),
                get(() ->
                        pathPrefix("getAllSms", () -> {
                                //path(longSegment(), (Long id) -> {
                                    final CompletionStage<Optional<List<SmsIncomingMessage>>> futureMaybeItem = fetchSmsList();
                                    return onSuccess(() -> futureMaybeItem, maybeItem ->
                                            maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                                    .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                    );
                                })),

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
        final String to;
        final String sms;

        public String getFrom() {
            return from;
        }
        public String getTo() {
            return to;
        }
        public String getSms() {
            return sms;
        }
        @JsonCreator
        SmsIncomingMessage(@JsonProperty("from") String from,
                           @JsonProperty("to") String to,
                            @JsonProperty("sms") String sms) {
            this.from = from;
            this.to = to;
            this.sms = sms;
        }
    }

    CompletionStage<Optional<List<SmsIncomingMessage>>> fetchSms(Long from)
    {
        String fromString = from + "";
        Timeout timeout = new Timeout(100000, TimeUnit.MILLISECONDS);
        Future<Object> future = Patterns.ask(selection, fromString, timeout);

        try {
            List<SmsIncomingMessage> reply = (ArrayList<SmsIncomingMessage>) Await.result(future, timeout.duration());
            System.out.println("Total SMS requests count in DB is " + reply.size());
            return CompletableFuture.completedFuture(Optional.of(reply));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    CompletionStage<Optional<String>> getSmsTotalCount()
    {
        Timeout timeout = new Timeout(100000, TimeUnit.MILLISECONDS);
        Future<Object> future = Patterns.ask(selection, "getSmsCount", timeout);

        try {
            String reply = (String) Await.result(future, timeout.duration());
            System.out.println("Total SMS requests count in DB is " + reply);
            return CompletableFuture.completedFuture(Optional.of(reply));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    CompletionStage<Optional<String>> emptyDatabase()
    {
        Timeout timeout = new Timeout(100000, TimeUnit.MILLISECONDS);
        Future<Object> future = Patterns.ask(selection, "delete", timeout);

        try {
            String reply = (String) Await.result(future, timeout.duration());
            System.out.println("Delete operation is successfull");
            return CompletableFuture.completedFuture(Optional.of(reply));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    CompletionStage<Optional<List<SmsIncomingMessage>>> fetchSmsList()
    {
        Timeout timeout = new Timeout(100000, TimeUnit.MILLISECONDS);
        Future<Object> future = Patterns.ask(selection, "fetchAllSMS", timeout);

        try {
            List<SmsIncomingMessage> reply = (ArrayList<SmsIncomingMessage>) Await.result(future, timeout.duration());
            System.out.println("Total SMS requests count in DB is " + reply.size());
            return CompletableFuture.completedFuture(Optional.of(reply));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
