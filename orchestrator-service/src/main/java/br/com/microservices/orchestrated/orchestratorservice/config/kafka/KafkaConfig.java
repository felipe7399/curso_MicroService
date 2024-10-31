package br.com.microservices.orchestrated.orchestratorservice.config.kafka;


import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopic;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    private static final Integer PARTITIONS = 1;
    private static final Integer REPLICAS = 1;

    @Bean
    public ConsumerFactory<String, String> consumerFactory () {
        return new DefaultKafkaConsumerFactory<>(consumerProps());
    }

    private Map<String, Object> consumerProps () {

        //HashMap <String, Object> props = new HashMap<>();
        var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory () {
        return new DefaultKafkaProducerFactory<>(producerProps());
    }

    private Map<String, Object> producerProps () {
        //HashMap <String, Object> props = new HashMap<>();
        var props = new HashMap<String, Object>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate (ProducerFactory<String,String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    private NewTopic buildtopic (String name) {

        return TopicBuilder.name(name)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    @Bean
    public NewTopic createStartSagaTopic() {

        return buildtopic(ETopic.START_SAGA.getTopic());
    }

    @Bean
    public NewTopic createBaseOrchestratorTopic() {

        return buildtopic(ETopic.BASE_ORCHESTRATOR.getTopic());
    }

    @Bean
    public NewTopic createFinishSuccessTopic() {

        return buildtopic(ETopic.FINISH_SUCCESS.getTopic());
    }

    @Bean
    public NewTopic createFinishFailTopic() {

        return buildtopic(ETopic.FINISH_FAIL.getTopic());
    }

    @Bean
    public NewTopic createNotifyEndingTopic() {

        return buildtopic(ETopic.NOTIFY_ENDING.getTopic());
    }

    @Bean
    public NewTopic createProductValidationSuccessTopic() {

        return buildtopic(ETopic.PRODUCT_VALIDATION_SUCCESS.getTopic());
    }
    @Bean
    public NewTopic createProductValidationFailTopic() {

        return buildtopic(ETopic.PRODUCT_VALIDATION_FAIL.getTopic());
    }

    @Bean
    public NewTopic createPaymentSuccessTopic() {

        return buildtopic(ETopic.PAYMENT_SUCCESS.getTopic());
    }

    @Bean
    public NewTopic createPaymentFailTopic() {

        return buildtopic(ETopic.PAYMENT_FAIL.getTopic());
    }

    @Bean
    public NewTopic createInventorySuccessTopic() {

        return buildtopic(ETopic.INVENTORY_SUCCESS.getTopic());
    }

    @Bean
    public NewTopic createInventorytFailTopic() {

        return buildtopic(ETopic.INVENTORY_FAIL.getTopic());
    }
}
