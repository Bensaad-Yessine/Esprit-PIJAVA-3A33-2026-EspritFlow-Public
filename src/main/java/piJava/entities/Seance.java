package piJava.entities;

import java.sql.Timestamp;

public class Seance {
    private int id;
    private String jour;
    private String typeSeance;
    private String mode;
    private Timestamp heureDebut;
    private Timestamp heureFin;
    private Timestamp createdAt;
    private int salleId;
    private int matiereId;
    private int classeId;
    private String qrToken;
    private Timestamp qrExpiresAt;
    private String qrUrl;

    public Seance() {}

    public Seance(int id, String jour, String typeSeance, String mode, Timestamp heureDebut, Timestamp heureFin, Timestamp createdAt, int salleId, int matiereId, int classeId, String qrToken, Timestamp qrExpiresAt, String qrUrl) {
        this.id = id;
        this.jour = jour;
        this.typeSeance = typeSeance;
        this.mode = mode;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.createdAt = createdAt;
        this.salleId = salleId;
        this.matiereId = matiereId;
        this.classeId = classeId;
        this.qrToken = qrToken;
        this.qrExpiresAt = qrExpiresAt;
        this.qrUrl = qrUrl;
    }

    public Seance(String jour, String typeSeance, String mode, Timestamp heureDebut, Timestamp heureFin, int salleId, int matiereId, int classeId) {
        this.jour = jour;
        this.typeSeance = typeSeance;
        this.mode = mode;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salleId = salleId;
        this.matiereId = matiereId;
        this.classeId = classeId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public String getTypeSeance() { return typeSeance; }
    public void setTypeSeance(String typeSeance) { this.typeSeance = typeSeance; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Timestamp getHeureDebut() { return heureDebut; }
    public void setHeureDebut(Timestamp heureDebut) { this.heureDebut = heureDebut; }

    public Timestamp getHeureFin() { return heureFin; }
    public void setHeureFin(Timestamp heureFin) { this.heureFin = heureFin; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }

    public int getMatiereId() { return matiereId; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }

    public int getClasseId() { return classeId; }
    public void setClasseId(int classeId) { this.classeId = classeId; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public Timestamp getQrExpiresAt() { return qrExpiresAt; }
    public void setQrExpiresAt(Timestamp qrExpiresAt) { this.qrExpiresAt = qrExpiresAt; }

    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }

    @Override
    public String toString() {
        return "Seance{" + "id=" + id + ", jour='" + jour + '\'' + ", type='" + typeSeance + '\'' + '}';
    }
}
