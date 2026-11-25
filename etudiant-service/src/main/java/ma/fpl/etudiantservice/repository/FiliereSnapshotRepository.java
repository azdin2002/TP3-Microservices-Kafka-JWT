
package ma.fpl.etudiantservice.repository;

import ma.fpl.etudiantservice.entities.FiliereSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiliereSnapshotRepository
        extends JpaRepository<FiliereSnapshot, Long> {

    Optional<FiliereSnapshot> findByIdAndActiveTrue(Long id);
}
