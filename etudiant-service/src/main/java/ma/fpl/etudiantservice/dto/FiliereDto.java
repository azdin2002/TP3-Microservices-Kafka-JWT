package ma.fpl.etudiantservice.dto;

public record FiliereDto(
        Long id,
        String code,
        String libelle,
        boolean active
) {
}