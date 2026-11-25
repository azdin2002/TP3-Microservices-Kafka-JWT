
package ma.fpl.etudiantservice.repository;

import ma.fpl.etudiantservice.entities.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    boolean existsByCne(String cne);

    boolean existsByCneAndIdNot(String cne, Long id);
}
