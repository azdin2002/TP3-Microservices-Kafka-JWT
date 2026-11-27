package ma.fpl.etudiantservice.controller;

import jakarta.validation.Valid;
import ma.fpl.etudiantservice.dto.RequestEtudiantDto;
import ma.fpl.etudiantservice.dto.ResponseEtudiantDto;
import ma.fpl.etudiantservice.service.EtudiantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final EtudiantService etudiantService;

    public EtudiantController(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEtudiantDto ajouter(@Valid @RequestBody RequestEtudiantDto dto) {
        return etudiantService.ajouter(dto);
    }

    @PutMapping("/{id}")
    public ResponseEtudiantDto modifier(
            @PathVariable Long id,
            @Valid @RequestBody RequestEtudiantDto dto) {
        return etudiantService.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        etudiantService.supprimer(id);
    }

    @GetMapping("/{id}")
    public ResponseEtudiantDto afficherParId(@PathVariable Long id) {
        return etudiantService.afficherParId(id);
    }

    @GetMapping
    public List<ResponseEtudiantDto> afficherToutes() {
        return etudiantService.afficherTous();
    }

    @GetMapping("/page")
    public Page<ResponseEtudiantDto> allPaginated(
            @PageableDefault(size = 5, sort = "nom") Pageable pageable) {
        return etudiantService.allPaginated(pageable);
    }

    @GetMapping("/filiere/{filiereId}")
    public Page<ResponseEtudiantDto> byFiliere(
            @PathVariable Long filiereId,
            @PageableDefault(size = 5, sort = "nom") Pageable pageable) {
        return etudiantService.byFiliere(filiereId, pageable);
    }
}