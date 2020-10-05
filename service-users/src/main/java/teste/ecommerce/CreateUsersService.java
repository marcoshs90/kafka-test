package teste.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CreateUsersService {

    private final Connection connection;

    CreateUsersService() throws SQLException {
        String url = "jdbc:sqlite:users_database.db";
        this.connection = DriverManager.getConnection(url);
        try{
            connection.createStatement().execute("create table users(" +
                    "uuid varchar(200) primary key," +
                    "email varchar(200))");
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException {
        var createUsersService = new CreateUsersService();
        try(var service = new KafkaService<Order>( CreateUsersService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER",
                createUsersService::parse,
                Order.class,
                new HashMap<>())) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, Order> record) throws SQLException {
        System.out.println("Processing new order, checking for new User");
        System.out.println(record.value());
        var order = record.value();
        if(isNewUser(order.getEmail())){
            insertNewUser(order.getEmail());
        }
    }

    private void insertNewUser(String email) throws SQLException {
        var insert = connection.prepareStatement("insert into users (uuid, email) " +
                "values (?,?)");
        insert.setString(1, UUID.randomUUID().toString());
        insert.setString(2, email);
        insert.execute();
        System.out.println("Usuario uuid e " + email + "adicionado");
    }

    private boolean isNewUser(String email) throws SQLException {
        var exists = connection.prepareStatement("select uuid from users " +
                "where email = ? limit 1");
        exists.setString(1, email);
        var results = exists.executeQuery();
        return !results.next();
    }
}
