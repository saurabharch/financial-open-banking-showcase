package showcase.financial.banking.ml;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.connection.SimplePropertyValueConnectionNameStrategy;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.EnvironmentBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.config.StreamRabbitListenerContainerFactory;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;

@Configuration
@Slf4j
public class RabbitMQConfig {


    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Value("${spring.rabbitmq.host:localhost}")
    private String hostname;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public ConnectionNameStrategy cns() {
        return new SimplePropertyValueConnectionNameStrategy("spring.application.name");
    }

    @Bean
    public EnvironmentBuilderCustomizer envConfig()
    {
        return builder -> builder.id(applicationName);
    }


    @ConditionalOnProperty(name = "rabbitmq.streaming.replay",havingValue = "true")
    @Bean
    ListenerContainerCustomizer<MessageListenerContainer> containerCustomizer() {
        return (cont, dest, group) -> {
            StreamListenerContainer container = (StreamListenerContainer) cont;
            container.setConsumerCustomizer((name, builder) -> {
                builder.subscriptionListener(context ->{
                    log.info("Replaying from the first record in the stream");
                    context.offsetSpecification(OffsetSpecification.first());
                });
            });
        };
    }
//    @Bean
//    ListenerContainerCustomizer<MessageListenerContainer> customizer() {
//        return (cont, dest, group) -> {
//            StreamListenerContainer container = (StreamListenerContainer) cont;
//            container.setConsumerCustomizer((name, builder) -> {
//                builder.subscriptionListener(context ->{
//                    log.info("Replaying from the first record in the stream");
//                    context.offsetSpecification(OffsetSpecification.first());
//                });
//            });
//        };
//    }
}
