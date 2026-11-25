package ma.fpl.filiereservice.mappers;

import ma.fpl.filiereservice.dto.RequestFiliereDto;
import ma.fpl.filiereservice.dto.ResponseFiliereDto;
import ma.fpl.filiereservice.entities.Filiere;
import org.springframework.stereotype.Component;

@Component
public class FiliereMapper {

    public Filiere toEntity(RequestFiliereDto dto){
        Filiere filiere = new Filiere();

        filiere.setCode(dto.code());
        filiere.setLibelle(dto.libelle());

        return filiere;
    }

    public ResponseFiliereDto toDto(Filiere filiere){

        return new ResponseFiliereDto(
                filiere.getId(),
                filiere.getCode(),
                filiere.getLibelle());
    }

    public void updateEntity(
            Filiere filiere,
            RequestFiliereDto dto) {

        filiere.setCode(dto.code());
        filiere.setLibelle(dto.libelle());
    }
}
