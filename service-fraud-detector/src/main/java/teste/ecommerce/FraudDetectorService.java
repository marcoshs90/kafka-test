package teste.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import teste.ecommerce.consumer.ConsumerService;
import teste.ecommerce.consumer.ServiceRunner;
import teste.ecommerce.database.LocalDatabase;
import teste.ecommerce.dispatcher.KafkaDispatcher;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class FraudDetectorService implements ConsumerService<Order> {

    private final LocalDatabase database;

    private FraudDetectorService() throws SQLException {
        this.database = new LocalDatabase("frauds_database");
        this.database.createIfNotExists("create table Orders (" +
                "uuid varchar(200) primary key," +
                "is_fraud boolean)");
    }

    public static void main(String[] args) {
        new ServiceRunner(FraudDetectorService::new).start(1);
    }

    private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return FraudDetectorService.class.getSimpleName();
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException, SQLException {
        System.out.println("Processing new order, checking for fraud");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());

        var message = record.value();
        var order = message.getPayload();
        if(wasProcessed(order)) {
            System.out.println("Order " + order.getOrderId() + " was already processed");
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (isFraud(order)){
            database.update("insert into Orders (uuid, is_fraud) values (?, true)", order.getOrderId());
            System.out.println("Order é uma fraud" + order);
            orderDispatcher.send("ECOMMERCE_ORDER_REJECTED",
                    order.getEmail(),
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()),
                    order);
        }else{
            database.update("insert into Orders (uuid, is_fraud) values (?, false)", order.getOrderId());
            System.out.println("Order Processed" + order);
            orderDispatcher.send("ECOMMERCE_ORDER_APPROVED",
                    order.getEmail(),
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()),
                    order);
        }
    }

    private boolean wasProcessed(Order order) throws SQLException {
        var results = database.query("select uuid from orders where uuid = ? limit 1", order.getOrderId());
        return results.next();
    }

    private boolean isFraud(Order order) {
        return order.getAmount().compareTo(new BigDecimal("4500")) >= 0;
    }
}
