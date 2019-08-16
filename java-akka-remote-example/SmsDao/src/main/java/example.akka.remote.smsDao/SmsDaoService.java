package example.akka.remote.smsDao;

import akka.Done;
import akka.NotUsed;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.alpakka.slick.javadsl.Slick;
import akka.stream.alpakka.slick.javadsl.SlickRow;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import example.akka.remote.shared.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class SmsDaoService extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    //private ActorRef loggingActor = getContext().actorOf(Props.create(LoggingActor.class), "LoggingActor");

    private static final SlickSession session = SlickSession.forConfig("database.slick-h2");

    static ActorSystem system  = null;
    static Materializer materializer = null;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");


    private static final Function<SmsDaoMessage.Message, String> insertUser =
            (message) ->
                    "INSERT INTO SMS_DB VALUES ("
                            + "'" + message.getFromNumber() + "'"
                            + ", '" + message.getToNumber() + "'"
                            + ", '" + message.getSmsMessage() + "')";


    private static final String selectAllUsers =
            "SELECT USERFROM, TO, SMS FROM SMS_DB";


    private static final String fetchSmsCount =
            "SELECT COUNT(*) FROM SMS_DB";

    private static final String selectSingleUsers =
            "SELECT USERFROM, TO, SMS FROM SMS_DB WHERE USERFROM =";

    private static final Function<String, String>  emptyDatabase =
            (delete) -> "TRUNCATE TABLE SMS_DB";

    public Set<SmsDaoMessage.Message> fetchSMSDetails(String singleSms)
    {
        Source<SmsDaoMessage.Message, NotUsed> slickSource = null;
        if(singleSms != null && singleSms.length() > 0) {
            slickSource =  Slick.source(
                    session, selectSingleUsers + singleSms, (SlickRow row) -> new SmsDaoMessage.Message(row.nextString(), row.nextString(), row.nextString()));
        }
        else
        {
            slickSource = Slick.source(
                    session, selectAllUsers, (SlickRow row) -> new SmsDaoMessage.Message(row.nextString(), row.nextString(), row.nextString()));
        }
        final CompletionStage<List<SmsDaoMessage.Message>> foundUsersFuture =
                slickSource.runWith(Sink.seq(), materializer);
        Set<SmsDaoMessage.Message> foundUsers = null;
        try {
            foundUsers = new HashSet<>(foundUsersFuture.toCompletableFuture().get(30, TimeUnit.SECONDS));
            log.info("Total number of SMS in DB " + foundUsers.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return foundUsers;
    }

    public String totalCount()
    {
        Source<String, NotUsed> slickSource = null;
        String count = "";
        slickSource = Slick.source(
                session, fetchSmsCount, (SlickRow row) -> new String(row.nextString()));

        final CompletionStage<List<String>> foundUsersFuture =
                slickSource.runWith(Sink.seq(), materializer);

        Set<String> foundUsers = null;
        try {
            foundUsers = new HashSet<>(foundUsersFuture.toCompletableFuture().get(30, TimeUnit.SECONDS));
            ArrayList<String> aList = new ArrayList<String>(foundUsers);
            log.info("Total Count of SMS in DB " + aList.get(0));

            return aList.get(0);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return count;
    }

    public String emptyDatabase(String delete)
    {

        final Sink<String, CompletionStage<Done>> slickSink  = Slick.sink(session, emptyDatabase);
        Set<String> users = new HashSet<>();
        users.add(delete);
        Source<String, NotUsed> usersSource = Source.from(users);


        final CompletionStage<Done> insertionResultFuture =
                usersSource.runWith(slickSink, materializer);
        try {
            insertionResultFuture.toCompletableFuture().get(5, TimeUnit.SECONDS);

            return "Deleted Successfully";

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "Delete operation failed";
    }


    public void insertSMSMessage(SmsDaoMessage.Message message) throws Exception {
        final Sink<SmsDaoMessage.Message, CompletionStage<Done>> slickSink = Slick.sink(session, insertUser);
        Set<SmsDaoMessage.Message> users = new HashSet<>();
        users.add(message);
        Source<SmsDaoMessage.Message, NotUsed> usersSource = Source.from(users);

        final CompletionStage<Done> insertionResultFuture =
                usersSource.runWith(slickSink, materializer);
        insertionResultFuture.toCompletableFuture().get(5, TimeUnit.SECONDS);

    }

    private static void executeStatement(
            String statement, SlickSession session, Materializer materializer) {
        try {
            Source.single(statement)
                    .runWith(Slick.sink(session), materializer)
                    .toCompletableFuture()
                    .get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public void updateSmsCounter(SmsDaoMessage.Message message)
    {
        log.info("All Processing is Done for {}", message.getSmsMessage());

        try {
            insertSMSMessage(message);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("onReceive({})", message);
        if (message instanceof SmsDaoMessage.Message) {
            log.info("Got a Sms Dao Message");
            String from = ((SmsDaoMessage.Message) message).getFromNumber();
            String to = ((SmsDaoMessage.Message) message).getToNumber();
            String smsMessage = ((SmsDaoMessage.Message) message).getSmsMessage();


            LocalDateTime now = LocalDateTime.now();
            //System.out.println("Start time is " + dtf.format(now));

            SmsDaoMessage.Message newMsg = new SmsDaoMessage.Message(from, to , smsMessage + " - time " + dtf.format(now));
            updateSmsCounter((SmsDaoMessage.Message) newMsg);
            getSender().tell(new SmsDaoMessage.Response("DAO Operation Done for from " + from), getSelf());
        }
        else if(message instanceof  String) {
            if(((String) message).equalsIgnoreCase("fetchAllSms"))
            {
                Set<SmsDaoMessage.Message> data = fetchSMSDetails(null);
                ArrayList<SmsDaoMessage.Message> aList = new ArrayList<SmsDaoMessage.Message>(data);
                getSender().tell(aList, self());
                Iterator<SmsDaoMessage.Message> it = data.iterator();
                while(it.hasNext()){
                    SmsDaoMessage.Message smsData = it.next();
                    log.info("From {}, To {}, SMS {}", smsData.getFromNumber(), smsData.getToNumber(), smsData.getSmsMessage() );
                }
                log.info("------------------> Total number of entries in DB " + data.size());
            }
            else if(((String) message).equalsIgnoreCase("delete"))
            {
                String operationResult = emptyDatabase("delete");
                getSender().tell(operationResult, self());
            }
            else if(((String) message).equalsIgnoreCase("getSmsCount"))
            {
                String operationResult = totalCount();
                getSender().tell(operationResult, self());
            }
            else
            {
                Set<SmsDaoMessage.Message> data = fetchSMSDetails((String) message);
                ArrayList<SmsDaoMessage.Message> aList = new ArrayList<SmsDaoMessage.Message>(data);
                getSender().tell(aList, self());
                Iterator<SmsDaoMessage.Message> it = data.iterator();
                while(it.hasNext()){
                    SmsDaoMessage.Message smsData = it.next();
                    log.info("From {}, To {}, SMS {}", smsData.getFromNumber(), smsData.getToNumber(), smsData.getSmsMessage() );
                }
                log.info("------------------> Total number of entries in DB " + data.size());
            }
        }

        else {
            unhandled(message);
        }
    }

    static {
        system  = ActorSystem.create();
        materializer = ActorMaterializer.create(system);
        File temp;
        try
        {
            temp = File.createTempFile("D:\\Project\\Akka\\alpakka-slick-h2-test.mv", ".db");
            boolean exists = temp.exists();
            if(!exists)
            {
                executeStatement(
                        "CREATE TABLE SMS_DB(USERFROM VARCHAR(50), TO VARCHAR(50), SMS VARCHAR(50))",
                        session,
                        materializer);
            }

            System.out.println("Database exists : " + exists);
        } catch (IOException e)
        {
            e.printStackTrace();
        }


    }

}
