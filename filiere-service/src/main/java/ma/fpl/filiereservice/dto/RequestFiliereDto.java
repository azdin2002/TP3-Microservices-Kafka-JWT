package ma.fpl.filiereservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestFiliereDto(

        @NotBlank(message = "le code est obligatoire")
        String code,

        @NotBlank(message = "le libelle est obligatoire")
        String libelle
) {
}
