package com.skaria.aws.kafka.producer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Map;
import java.util.concurrent.Future;

import static com.skaria.aws.kafka.producer.KafkaProducerLambda.KAFKA_BROKER_LIST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
public class KafkaProducerLambdaTest {

    @Mock
    private Future future;

    @SystemStub
    private EnvironmentVariables environmentVariables;
    @Mock
    private Context context;
    @Mock
    private KafkaProducerService service;
    @Mock
    private KafkaProducer producer;
    @Mock
    private Util util;

    @BeforeEach
    public void setup() {

        environmentVariables.set(KAFKA_BROKER_LIST, "127.0.0.1:9092");
    }


    @Test
    public void successfulResponse() {
        KafkaProducerLambda kafkaProducerLambda = new KafkaProducerLambda(util);

        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
        apiGatewayProxyResponseEvent.withStatusCode(200)
                .withBody("Messages sent to Kafka topic on 127.0.0.1:9092");

        LambdaLogger logger = getLogger();

        when(context.getLogger()).thenReturn(logger);
        when(util.createKafkaProducerService(any(LambdaLogger.class), anyString(), any(Map.class))).thenReturn(service);
        when(service.sendMessages()).thenReturn(apiGatewayProxyResponseEvent);

        assertEquals("127.0.0.1:9092", System.getenv(KAFKA_BROKER_LIST));
        APIGatewayProxyResponseEvent result = kafkaProducerLambda.handleRequest(null, context);


        assertEquals(200, result.getStatusCode().intValue());

        String content = result.getBody();
        assertNotNull(content);
        assertTrue(content.contains("Messages sent to Kafka topic on 127.0.0.1:9092"));


    }

    private LambdaLogger getLogger() {
        return new LambdaLogger() {
                @Override
                public void log(String s) {

                }

                @Override
                public void log(byte[] bytes) {

                }
            };
    }

}
