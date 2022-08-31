package com.skaria.aws.kafka.producer;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.kafka.clients.producer.*;

import java.util.Map;

public class KafkaProducerService {

    private Map<String, Object> properties;

    private String topic;

    private LambdaLogger logger;

    private KafkaProducer<String, String> producer;

    public KafkaProducerService(Map<String, Object> properties, String topic, LambdaLogger logger,
                                KafkaProducer<String, String> producer) {
        this.properties = properties;
        this.topic = topic;
        this.logger = logger;
        this.producer = producer;
    }

    public APIGatewayProxyResponseEvent sendMessages() {
        this.logger.log("inside KafkaProducerService sendMessages()....");

        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
        String status = "500";
        try {
            sendMessagesInLoop(topic, logger);
            apiGatewayProxyResponseEvent.withStatusCode(200)
                    .withBody("Messages sent to Kafka topic on " + properties.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));

        } catch (Exception exp) {
            this.logger.log("exception...."+ exp.getMessage());
            apiGatewayProxyResponseEvent.withStatusCode(500)
                    .withBody("Error sending Messages sent to Kafka topic on " + exp.getMessage() + "\n"
                            + exp.getCause());

        } finally {
            producer.flush();
            //producer.close();
        }
        this.logger.log("Message sent successfully!...."+ apiGatewayProxyResponseEvent.toString());
        return apiGatewayProxyResponseEvent;
    }

    public void sendMessagesInLoop(String topic, LambdaLogger logger) {
        final LambdaLogger finalLogger = logger;
        this.logger.log("inside KafkaProducerService sendMessagesInLoop()....");
        int n = 1;
        ProducerRecord<String, String> record = null;
        for (int i = 1; i <= n; i++) {
            record = new ProducerRecord<String, String>(topic, "key-" + i, "value-" + i);
            final ProducerRecord<String, String> finalRecord = record;
            this.producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception == null) {
                        finalLogger.log("Topic:: " + metadata.topic());
                        finalLogger.log("Partition:: " + metadata.partition());
                        finalLogger.log("Offset:: " + metadata.offset());
                        finalLogger.log("Key:: " + finalRecord.key());
                        finalLogger.log("Timestamp:: " + metadata.timestamp());
                    } else {
                        finalLogger.log("exception = " + exception.getMessage());
                    }
                }
            });
        }
    }

}
