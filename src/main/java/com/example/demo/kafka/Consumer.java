package com.example.demo.kafka;

/**
 * @author: faker
 * @DATE: 2020/7/27 11:13
 */
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.util.Arrays;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "http://192.168.137.51:9092");
        properties.put("group.id", "group01");
        properties.put("session.timeout.ms", "30000");

        properties.put("enable.auto.commit", "false");
        properties.put("auto.commit.interval.ms", "1000");

        //earliest：在偏移量无效的情况下，消费者将从起始位置读取分区的记录。
        //latest：在偏移量无效的情况下，消费者将从最新位置读取分区的记录
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(properties);

        kafkaConsumer.subscribe(Arrays.asList("mtest"));
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("offset = "+record.offset()+", value = "+record.value());
            }
        }
    }
}