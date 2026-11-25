
package ma.fpl.etudiantservice.mapper;

import ma.fpl.etudiantservice.dto.FiliereDto;
import ma.fpl.etudiantservice.dto.RequestEtudiantDto;
import ma.fpl.etudiantservice.dto.ResponseEtudiantDto;
import ma.fpl.etudiantservice.entities.Etudiant;
import ma.fpl.etudiantservice.entities.FiliereSnapshot;
import org.springframework.stereotype.Component;

@Component
public class EtudiantMapper {

    public Etudiant toEntity(RequestEtudiantDto dto) {
        Etudiant etudiant = new Etudiant();

        etudiant.setNom(dto.nom());
        etudiant.setPrenom(dto.prenom());
        etudiant.setCne(dto.cne());
        etudiant.setFiliereId(dto.filiereId());

        return etudiant;
    }

    public ResponseEtudiantDto toDto(
            Etudiant etudiant,
            FiliereSnapshot filiere) {

        FiliereDto filiereDto = new FiliereDto(
                filiere.getId(),
                filiere.getCode(),
                filiere.getLibelle(),
                filiere.isActive()
        );

        return new ResponseEtudiantDto(
                etudiant.getId(),
                etudiant.getNom(),
                etudiant.getPrenom(),
                etudiant.getCne(),
                filiereDto
        );
    }

    public void updateEntity(
            Etudiant etudiant,
            RequestEtudiantDto dto
    ) {
        etudiant.setNom(dto.nom());
        etudiant.setPrenom(dto.prenom());
        etudiant.setCne(dto.cne());
        etudiant.setFiliereId(dto.filiereId());
    }
}
