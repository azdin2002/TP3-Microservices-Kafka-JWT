
package ma.fpl.etudiantservice.service;

import ma.fpl.etudiantservice.dto.RequestEtudiantDto;
import ma.fpl.etudiantservice.dto.ResponseEtudiantDto;
import ma.fpl.etudiantservice.entities.Etudiant;
import ma.fpl.etudiantservice.entities.FiliereSnapshot;
import ma.fpl.etudiantservice.mapper.EtudiantMapper;
import ma.fpl.etudiantservice.repository.EtudiantRepository;
import ma.fpl.etudiantservice.repository.FiliereSnapshotRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final FiliereSnapshotRepository filiereSnapshotRepository;
    private final EtudiantMapper etudiantMapper;

    public EtudiantService(
            EtudiantRepository etudiantRepository,
            FiliereSnapshotRepository filiereSnapshotRepository,
            EtudiantMapper etudiantMapper) {

        this.etudiantRepository = etudiantRepository;
        this.filiereSnapshotRepository = filiereSnapshotRepository;
        this.etudiantMapper = etudiantMapper;
    }

    private Etudiant trouverEtudiant(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Etudiant introuvable : " + id
                ));
    }

    private FiliereSnapshot trouverFiliereActive(Long id) {
        return filiereSnapshotRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Filiere absente ou inactive : " + id
                ));
    }

    @Transactional
    public ResponseEtudiantDto ajouter(RequestEtudiantDto dto) {

        if (etudiantRepository.existsByCne(dto.cne())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CNE deja utilise"
            );
        }

        FiliereSnapshot filiere =
                trouverFiliereActive(dto.filiereId());

        Etudiant etudiant = etudiantMapper.toEntity(dto);

        Etudiant etudiantEnregistre =
                etudiantRepository.save(etudiant);

        return etudiantMapper.toDto(etudiantEnregistre, filiere);
    }

    @Transactional
    public ResponseEtudiantDto modifier(Long id, RequestEtudiantDto dto) {

        Etudiant etudiant = trouverEtudiant(id);

        if (etudiantRepository.existsByCneAndIdNot(dto.cne(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CNE deja utilise"
            );
        }

        FiliereSnapshot filiere =
                trouverFiliereActive(dto.filiereId());

        etudiantMapper.updateEntity(etudiant, dto);

        Etudiant etudiantModifie =
                etudiantRepository.save(etudiant);

        return etudiantMapper.toDto(etudiantModifie, filiere);
    }

    @Transactional
    public void supprimer(Long id) {
        Etudiant etudiant = trouverEtudiant(id);
        etudiantRepository.delete(etudiant);
    }

    @Transactional(readOnly = true)
    public ResponseEtudiantDto afficherParId(Long id) {

        Etudiant etudiant = trouverEtudiant(id);

        FiliereSnapshot filiere =
                filiereSnapshotRepository.findById(etudiant.getFiliereId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Filiere non synchronisee : " + etudiant.getFiliereId()
                        ));

        return etudiantMapper.toDto(etudiant, filiere);
    }

    @Transactional(readOnly = true)
    public List<ResponseEtudiantDto> afficherTous() {

        return etudiantRepository.findAll()
                .stream()
                .map(etudiant -> {
                    FiliereSnapshot filiere =
                            filiereSnapshotRepository.findById(etudiant.getFiliereId())
                                    .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.CONFLICT,
                                            "Filiere non synchronisee : " + etudiant.getFiliereId()
                                    ));

                    return etudiantMapper.toDto(etudiant, filiere);
                })
                .toList();
    }
}
