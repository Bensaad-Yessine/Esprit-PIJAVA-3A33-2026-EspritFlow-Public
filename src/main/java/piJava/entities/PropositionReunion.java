package piJava.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class PropositionReunion {

    private int id;
    private int propositionId;
    private String titre;
    private LocalDate dateReunion;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String lieu;
    private String description;
    private String statut;
    private LocalDate dateCreation;
    private LocalDate dateFinVote;
    private int nbrVoteAccept;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int idGroupeId;

    // ─── Full Constructor ───────────────────────────────────────
    public PropositionReunion(int id, int propositionId, String titre, LocalDate dateReunion,
                              LocalTime heureDebut, LocalTime heureFin, String lieu, String description,
                              String statut, LocalDate dateCreation, LocalDate dateFinVote, int nbrVoteAccept,
                              LocalDateTime createdAt, LocalDateTime updatedAt, int idGroupeId) {
        this.id = id;
        this.propositionId = propositionId;
        this.titre = titre;
        this.dateReunion = dateReunion;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.lieu = lieu;
        this.description = description;
        this.statut = statut;
        this.dateCreation = dateCreation;
        this.dateFinVote = dateFinVote;
        this.nbrVoteAccept = nbrVoteAccept;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.idGroupeId = idGroupeId;
    }

    // ─── Minimal Constructor ────────────────────────────────────
    public PropositionReunion(String titre, LocalDate dateReunion, String lieu, int idGroupeId) {
        this.titre = titre;
        this.dateReunion = dateReunion;
        this.lieu = lieu;
        this.idGroupeId = idGroupeId;
        this.statut = "En attente";
    }

    // ─── Empty Constructor ───────────────────────────────────────
    public PropositionReunion() {}

    // ─── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPropositionId() { return propositionId; }
    public void setPropositionId(int propositionId) { this.propositionId = propositionId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public LocalDate getDateReunion() { return dateReunion; }
    public void setDateReunion(LocalDate dateReunion) { this.dateReunion = dateReunion; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public LocalDate getDateFinVote() { return dateFinVote; }
    public void setDateFinVote(LocalDate dateFinVote) { this.dateFinVote = dateFinVote; }

    public int getNbrVoteAccept() { return nbrVoteAccept; }
    public void setNbrVoteAccept(int nbrVoteAccept) { this.nbrVoteAccept = nbrVoteAccept; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getIdGroupeId() { return idGroupeId; }
    public void setIdGroupeId(int idGroupeId) { this.idGroupeId = idGroupeId; }

    @Override
    public String toString() {
        return "PropositionReunion{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", dateReunion=" + dateReunion +
                ", lieu='" + lieu + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
