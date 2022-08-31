package com.skaria.aws.kafka.producer;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Map;

public class Util {

    public KafkaProducerService createKafkaProducerService(LambdaLogger logger, String topic,
                                                           Map<String, Object> properties) {
        return new KafkaProducerService(properties, topic, logger,
                createProducer(properties));
    }

    public KafkaProducer<String, String> createProducer(Map<String, Object> properties) {
        return new KafkaProducer<String, String>(properties);
    }
}
