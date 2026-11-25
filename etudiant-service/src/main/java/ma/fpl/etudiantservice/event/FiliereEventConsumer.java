
package ma.fpl.etudiantservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.fpl.etudiantservice.entities.FiliereSnapshot;
import ma.fpl.etudiantservice.repository.FiliereSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FiliereEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(FiliereEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final FiliereSnapshotRepository snapshotRepository;

    public FiliereEventConsumer(
            ObjectMapper objectMapper,
            FiliereSnapshotRepository snapshotRepository) {
        this.objectMapper = objectMapper;
        this.snapshotRepository = snapshotRepository;
    }

    @KafkaListener(
            topics = "faculte.filieres.v1",
            groupId = "etudiant-filiere-projection"
    )
    @Transactional
    public void consommer(String message) throws Exception {

        FiliereEvent event =
                objectMapper.readValue(message, FiliereEvent.class);

        log.info("Kafka event received: {}", event);

        switch (event.type()) {

            case "CREATED", "UPDATED" -> {
                FiliereSnapshot snapshot = snapshotRepository
                        .findById(event.id())
                        .orElseGet(FiliereSnapshot::new);

                snapshot.setId(event.id());
                snapshot.setCode(event.code());
                snapshot.setLibelle(event.libelle());
                snapshot.setActive(true);

                snapshotRepository.save(snapshot);

                log.info("Filiere snapshot synchronized: {}", event.id());
            }

            case "DELETED" -> {
                snapshotRepository.findById(event.id())
                        .ifPresent(snapshot -> {
                            snapshot.setActive(false);
                            snapshotRepository.save(snapshot);
                        });

                log.info("Filiere marked inactive: {}", event.id());
            }

            default -> log.warn(
                    "Unknown Filiere event type: {}",
                    event.type()
            );
        }
    }
}
