package piJava.entities;

public class Quiz {
    private int id;
    private int matiereId;
    private String title;
    private String description;

    public Quiz() {
    }

    public Quiz(int id, int matiereId, String title, String description) {
        this.id = id;
        this.matiereId = matiereId;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(int matiereId) {
        this.matiereId = matiereId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return title;
    }
}

