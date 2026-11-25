package ma.fpl.etudiantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RequestEtudiantDto(

        @NotBlank(message = "le nom est obligatoire")
        String nom,

        @NotBlank(message = "le prenom est obligatoire")
        String prenom,

        @NotBlank(message = "le cne est obligatoire")
        String cne,

        @NotNull
        @Positive
        Long filiereId
) {
}
