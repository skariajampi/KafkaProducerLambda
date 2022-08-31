package com.skaria.aws.kafka.producer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class KafkaProducerLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public static final String KAFKA_BROKER_LIST = "KAFKA_BROKER_LIST";
    private KafkaProducerService kafkaProducerService;
    private Map<String, Object> properties;
    private Util util;

    public KafkaProducerLambda(Util util) {
        this.util = util;
    }

    public KafkaProducerLambda() {
        this.util = new Util();
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log("Inside main of KafkaJavaProducer....");
        String bootstrapServers = System.getenv(KAFKA_BROKER_LIST);
        String topic = "topic1";
        if (this.properties == null) {
            this.properties = new HashMap<String, Object>();
            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }

        if (this.kafkaProducerService == null) {
            this.kafkaProducerService = this.util.createKafkaProducerService(logger, topic, properties);
        }

        logger.log("Before calling kafkaProducerService.sendMessages()....");
        return this.kafkaProducerService.sendMessages();
    }


}
