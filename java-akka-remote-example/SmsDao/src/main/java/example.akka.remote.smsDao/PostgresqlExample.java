package example.akka.remote.smsDao;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.slick.javadsl.Slick;
import akka.stream.alpakka.slick.javadsl.SlickRow;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import example.akka.remote.shared.SmsDaoMessage;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PostgresqlExample {
    private static final SlickSession session = SlickSession.forConfig("database.slick-postgres");
    //private static final SlickSession session = SlickSession.forConfig("database.slick-h2");

    private static final String fetchSmsCount =
            "SELECT COUNT(*) FROM SMS_DB";

    static String SQL_SELECT = "Select * from SMS_DB";

    public static String totalCount()
    {
        System.out.println("===================================== 0");
        ActorSystem system  = ActorSystem.create();
        ActorMaterializer materializer = ActorMaterializer.create(system);
        System.out.println("===================================== 1");
        Source<String, NotUsed> slickSource = null;
        String count = "";
        slickSource = Slick.source(
                session, fetchSmsCount, (SlickRow row) -> new String(row.nextString()));
        System.out.println("===================================== 2");
        final CompletionStage<List<String>> foundUsersFuture =
                slickSource.runWith(Sink.seq(), materializer);

        Set<String> foundUsers = null;
        try {
            foundUsers = new HashSet<>(foundUsersFuture.toCompletableFuture().get(3, TimeUnit.SECONDS));
            ArrayList<String> aList = new ArrayList<String>(foundUsers);
            System.out.println("----------------------------> Total Count of SMS in DB " + aList.get(0));

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


        public static void main(String[] args) {

            List<SmsDaoMessage.Message> result = new ArrayList<>();



            // auto close connection and preparedStatement
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/testdb", "postgres", "password");
                 PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {

                    String from = resultSet.getString("USERFROM");
                    String to = resultSet.getString("USERTO");
                    String sms = resultSet.getString("SMS");

                    System.out.println("----------> FROM " + from);
                    System.out.println("----------> TO " + to);
                    System.out.println("----------> SMS " + sms);

                    SmsDaoMessage.Message obj = new SmsDaoMessage.Message(from, to, sms);
                    result.add(obj);

                }
                result.forEach(x -> System.out.println(x));


                totalCount();

            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }