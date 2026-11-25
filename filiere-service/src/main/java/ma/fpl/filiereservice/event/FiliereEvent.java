
package ma.fpl.filiereservice.event;

import java.time.Instant;

public record FiliereEvent(
        String type,
        Long id,
        String code,
        String libelle,
        Instant occurredAt
) {
}
