package piJava.entities;

public class Recette {

    private int id;
    private String titre;
    private String image;
    private int readyInMinutes;
    private int servings;
    private String sourceUrl;
    private double calories;

    public Recette() {
    }

    public Recette(int id, String titre, String image, int readyInMinutes, int servings, String sourceUrl, double calories) {
        this.id = id;
        this.titre = titre;
        this.image = image;
        this.readyInMinutes = readyInMinutes;
        this.servings = servings;
        this.sourceUrl = sourceUrl;
        this.calories = calories;
    }

    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getImage() {
        return image;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public int getServings() {
        return servings;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public double getCalories() {
        return calories;
    }
}