
package ma.fpl.filiereservice.service;

import ma.fpl.filiereservice.dto.RequestFiliereDto;
import ma.fpl.filiereservice.dto.ResponseFiliereDto;
import ma.fpl.filiereservice.entities.Filiere;
import ma.fpl.filiereservice.event.FiliereEvent;
import ma.fpl.filiereservice.mappers.FiliereMapper;
import ma.fpl.filiereservice.repository.FiliereRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FiliereService {

    private final FiliereRepository filiereRepository;
    private final FiliereMapper filiereMapper;
    private final ApplicationEventPublisher events;

    public FiliereService(
            FiliereRepository filiereRepository,
            FiliereMapper filiereMapper,
            ApplicationEventPublisher events) {

        this.filiereRepository = filiereRepository;
        this.filiereMapper = filiereMapper;
        this.events = events;
    }

    private Filiere trouverFiliere(Long id) {

        return filiereRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Filière introuvable : " + id
                        )
                );
    }

    @Transactional
    public ResponseFiliereDto ajouter(RequestFiliereDto dto) {

        if (filiereRepository.existsByCode(dto.code())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code de filiere deja utilise"
            );
        }

        Filiere filiere = filiereMapper.toEntity(dto);
        Filiere filiereEnregistree = filiereRepository.save(filiere);

        publierEvenement("CREATED", filiereEnregistree);

        return filiereMapper.toDto(filiereEnregistree);
    }

    @Transactional
    public ResponseFiliereDto modifier(Long id, RequestFiliereDto dto) {

        Filiere filiere = trouverFiliere(id);

        if (filiereRepository.existsByCodeAndIdNot(dto.code(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Code de filiere deja utilise"
            );
        }

        filiereMapper.updateEntity(filiere, dto);

        Filiere filiereModifiee = filiereRepository.save(filiere);

        publierEvenement("UPDATED", filiereModifiee);

        return filiereMapper.toDto(filiereModifiee);
    }


    @Transactional
    public void supprimer(Long id) {

        Filiere filiere = trouverFiliere(id);

        publierEvenement("DELETED", filiere);

        filiereRepository.delete(filiere);
    }

    public ResponseFiliereDto afficherParId(Long id) {

        Filiere filiere = trouverFiliere(id);
        return filiereMapper.toDto(filiere);
    }

    public List<ResponseFiliereDto> afficherToutes() {

        return filiereRepository.findAll()
                .stream()
                .map(filiereMapper::toDto)
                .toList();
    }

    private void publierEvenement(String type, Filiere filiere) {

        FiliereEvent event = new FiliereEvent(
                type,
                filiere.getId(),
                filiere.getCode(),
                filiere.getLibelle(),
                Instant.now()
        );

        events.publishEvent(event);
    }
}
