
package ma.fpl.filiereservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FiliereEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(FiliereEventPublisher.class);

    public static final String TOPIC = "faculte.filieres.v1";

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;

    public FiliereEventPublisher(
            KafkaTemplate<String, String> kafka,
            ObjectMapper json) {

        this.kafka = kafka;
        this.json = json;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(FiliereEvent event) {

        try {
            kafka.send(
                    TOPIC,
                    event.id().toString(),
                    json.writeValueAsString(event)
            ).whenComplete((result, error) -> {

                if (error == null) {
                    log.info("Kafka : {} id={}", event.type(), event.id());
                } else {
                    log.error("Publication Kafka echouee", error);
                }
            });

        } catch (JsonProcessingException ex) {
            log.error("Serialisation de l'evenement impossible", ex);
        }
    }
}
