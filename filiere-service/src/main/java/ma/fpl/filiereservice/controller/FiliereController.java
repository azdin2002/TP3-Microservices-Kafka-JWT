package ma.fpl.filiereservice.controller;

import jakarta.validation.Valid;
import ma.fpl.filiereservice.dto.RequestFiliereDto;
import ma.fpl.filiereservice.dto.ResponseFiliereDto;
import ma.fpl.filiereservice.service.FiliereService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filieres")
public class FiliereController {

    private final FiliereService filiereService;

    public FiliereController(FiliereService filiereService) {
        this.filiereService = filiereService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseFiliereDto ajouter(
            @Valid @RequestBody RequestFiliereDto dto) {

        return filiereService.ajouter(dto);
    }

    @PutMapping("/{id}")
    public ResponseFiliereDto modifier(
            @PathVariable Long id,
            @Valid @RequestBody RequestFiliereDto dto) {

        return filiereService.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {

        filiereService.supprimer(id);
    }

    @GetMapping("/{id}")
    public ResponseFiliereDto afficherParId(
            @PathVariable Long id) {

        return filiereService.afficherParId(id);
    }

    @GetMapping
    public List<ResponseFiliereDto> afficherToutes() {

        return filiereService.afficherToutes();
    }
}
