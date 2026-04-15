package piJava.entities;

import java.sql.Timestamp;

public class Salle {
    private int id;
    private String block;
    private int number;
    private String name;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int etage;
    private int capacite;

    // Construteur complet
    public Salle(int id, String block, int number, String name, Timestamp createdAt, Timestamp updatedAt, int etage, int capacite) {
        this.id = id;
        this.block = block;
        this.number = number;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.etage = etage;
        this.capacite = capacite;
    }

    // Constructeur d'insertion
    public Salle(String block, int number, String name, int etage, int capacite) {
        this.block = block;
        this.number = number;
        this.name = name;
        this.etage = etage;
        this.capacite = capacite;
    }

    public Salle() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public int getEtage() { return etage; }
    public void setEtage(int etage) { this.etage = etage; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    @Override
    public String toString() {
        return name;
    }

    public String displaySalle() {
        return "Salle{id=" + id + ", name='" + name + "', block='" + block +
                "', etage=" + etage + ", capacite=" + capacite + "}";
    }
}
