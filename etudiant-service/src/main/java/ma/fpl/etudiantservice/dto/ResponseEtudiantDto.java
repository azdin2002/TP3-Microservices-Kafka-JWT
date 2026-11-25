package ma.fpl.etudiantservice.dto;

public record ResponseEtudiantDto(
        Long id,
        String nom,
        String prenom,
        String cne,
        FiliereDto filiere
) {
}
