package teste.ecommerce.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import teste.ecommerce.Message;

import java.io.IOException;

public interface ConsumerService<T> {
    void parse(ConsumerRecord<String, Message<T>> record) throws IOException;
    String getTopic();
    String getConsumerGroup();
}
