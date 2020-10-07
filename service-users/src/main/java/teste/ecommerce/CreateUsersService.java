package teste.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import teste.ecommerce.consumer.ConsumerService;
import teste.ecommerce.consumer.ServiceRunner;
import teste.ecommerce.database.LocalDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CreateUsersService implements ConsumerService<Order> {

    private final LocalDatabase database;

    private CreateUsersService() throws SQLException {
        this.database = new LocalDatabase("users_database");
        this.database.createIfNotExists("create table users(" +
                "uuid varchar(200) primary key," +
                "email varchar(200))");
    }

    public static void main(String[] args) throws SQLException, ExecutionException, InterruptedException {
        new ServiceRunner(CreateUsersService::new).start(1);
    }

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return CreateUsersService.class.getSimpleName();
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws SQLException {
        System.out.println("Processing new order, checking for new User");
        System.out.println(record.value());
        var message = record.value();
        var order = message.getPayload();
        if(isNewUser(order.getEmail())){
            insertNewUser(order.getEmail());
        }
    }

    private void insertNewUser(String email) throws SQLException {
        var uuid = UUID.randomUUID().toString();
        database.update("insert into users (uuid, email) " +
                "values (?,?)", uuid, email);
        System.out.println("Usuario" + uuid + " e " + email + "adicionado");
    }

    private boolean isNewUser(String email) throws SQLException {
        var results = database.query("select uuid from users " +
                "where email = ? limit 1", email);
        return !results.next();
    }
}
