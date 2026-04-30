# Commentaires détaillés - EmploiContentController.java

## Description générale
Ce contrôleur gère l'affichage de l'emploi du temps dans l'interface front-office, incluant la gestion des vues par jour et semaine, le filtrage par utilisateur, et l'export PDF.

---

## Commentaires ligne par ligne

```java
package piJava.Controllers.frontoffice.emploi;
```
**Ligne 1**: Déclaration du package - ce contrôleur appartient au package `frontoffice.emploi` (interface emploi du temps côté utilisateur)

```java
import piJava.Controllers.frontoffice.salle.FrontSallesController;
```
**Ligne 2**: Import du contrôleur `FrontSallesController` pour pouvoir naviguer vers la vue des salles

```java
import piJava.utils.DatabaseUtil;
```
**Ligne 3**: Import de l'utilitaire de connexion à la base de données pour exécuter des requêtes SQL

```java
import javafx.collections.FXCollections;
```
**Ligne 4**: Import de FXCollections pour créer des collections observable JavaFX (listes qui notifient les changements)

```java
import javafx.collections.ObservableList;
```
**Ligne 5**: Import de l'interface ObservableList pour utiliser des listes réactives dans l'interface JavaFX

```java
import javafx.fxml.FXML;
```
**Ligne 6**: Import de l'annotation FXML pour marquer les éléments définis dans le fichier FXML

```java
import javafx.fxml.Initializable;
```
**Ligne 7**: Import de l'interface Initializable pour initialiser le contrôleur après le chargement du FXML

```java
import javafx.scene.control.*;
```
**Ligne 8**: Import des contrôles JavaFX (Button, ComboBox, ListView, etc.)

```java
import javafx.scene.layout.HBox;
```
**Ligne 9**: Import de HBox pour gérer les conteneurs horizontaux dans l'interface

```java
import java.net.URL;
```
**Ligne 10**: Import de URL pour les ressources de l'application

```java
import java.sql.*;
```
**Ligne 11**: Import des classes SQL pour la connexion et l'exécution de requêtes

```java
import java.util.ResourceBundle;
```
**Ligne 12**: Import de ResourceBundle pour l'internationalisation et les fichiers de propriétés

```java
public class EmploiContentController implements Initializable {
```
**Ligne 13**: Déclaration de la classe `EmploiContentController` qui implémente l'interface Initializable

```java
    @FXML
    private ComboBox<Integer> dayFilter;
```
**Lignes 14-15**: Déclaration d'un ComboBox pour filtrer par jour de la semaine (1-7)

```java
    @FXML
    private ComboBox<Integer> weekFilter;
```
**Lignes 16-17**: Déclaration d'un ComboBox pour filtrer par numéro de semaine

```java
    @FXML
    private ComboBox<Integer> userFilter;
```
**Lignes 18-19**: Déclaration d'un ComboBox pour filtrer par utilisateur

```java
    @FXML
    private ListView<Seance> scheduleListView;
```
**Lignes 20-21**: Déclaration d'une ListView pour afficher la liste des séances filtrées

```java
    @FXML
    private Button refreshButton;
```
**Lignes 22-23**: Déclaration d'un bouton pour rafraîchir l'affichage

```java
    @FXML
    private HBox weekViewContainer;
```
**Lignes 24-25**: Déclaration d'un conteneur HBox pour afficher la vue hebdomadaire

```java
    @FXML
    private Label statusLabel;
```
**Lignes 26-27**: Déclaration d'une Label pour afficher les messages de statut

```java
    private ObservableList<Seance> allSeances;
```
**Ligne 28**: Liste observable contenant toutes les séances chargées depuis la base de données

```java
    private int currentWeek = 1;
```
**Ligne 29**: Variable pour stocker la semaine courante sélectionnée

```java
    @Override
    public void initialize(URL location, ResourceBundle resources) {
```
**Ligne 30**: Méthode appelée automatiquement après le chargement du fichier FXML

```java
        loadUsers();
```
**Ligne 31**: Appel de la méthode pour charger la liste des utilisateurs dans le filtre

```java
        setupWeekFilter();
```
**Ligne 32**: Appel de la méthode pour initialiser le filtre de semaines (1-52)

```java
        setupDayFilter();
```
**Ligne 33**: Appel de la méthode pour initialiser le filtre de jours (1-7)

```java
        loadAllSeances();
```
**Ligne 34**: Appel de la méthode pour charger toutes les séances depuis la base de données

```java
        setupListView();
```
**Ligne 35**: Appel de la méthode pour configurer l'affichage de la ListView

```java
        refreshButton.setOnAction(e -> {
```
**Ligne 36**: Définition de l'action à exécuter lors du clic sur le bouton rafraîchir

```java
            applyFilters();
```
**Ligne 37**: Applique les filtres actuels et met à jour l'affichage

```java
            loadAllSeances();
```
**Ligne 38**: Recharge toutes les séances depuis la base de données

```java
        });
```
**Ligne 39**: Fin de la définition de l'action du bouton

```java
        userFilter.setOnAction(e -> applyFilters());
```
**Ligne 40**: Définit l'action du filtre utilisateur : appliquer les filtres à chaque changement

```java
        dayFilter.setOnAction(e -> applyFilters());
```
**Ligne 41**: Définit l'action du filtre jour : appliquer les filtres à chaque changement

```java
        weekFilter.setOnAction(e -> {
```
**Ligne 42**: Définit l'action du filtre semaine

```java
            currentWeek = weekFilter.getValue();
```
**Ligne 43**: Met à jour la variable currentWeek avec la valeur sélectionnée

```java
            applyFilters();
```
**Ligne 44**: Applique les filtres pour mettre à jour l'affichage

```java
        });
```
**Ligne 45**: Fin de la définition de l'action du filtre semaine

```java
        showDayView();
```
**Ligne 46**: Affiche la vue journalière par défaut au démarrage

```java
    }
```
**Ligne 47**: Fin de la méthode initialize

```java
    private void loadUsers() {
```
**Ligne 48**: Début de la méthode pour charger les utilisateurs depuis la base de données

```java
        Connection conn = null;
```
**Ligne 49**: Déclaration de la connexion SQL (initialisée à null)

```java
        PreparedStatement pstmt = null;
```
**Ligne 50**: Déclaration du statement préparé pour les requêtes paramétrées

```java
        ResultSet rs = null;
```
**Ligne 51**: Déclaration du ResultSet pour stocker les résultats de la requête

```java
        try {
```
**Ligne 52**: Début du bloc try pour la gestion des exceptions

```java
            conn = DatabaseUtil.getConnection();
```
**Ligne 53**: Obtient une connexion à la base de données

```java
            String sql = "SELECT id, nom FROM user ORDER BY nom";
```
**Ligne 54**: Définit la requête SQL pour récupérer tous les utilisateurs triés par nom

```java
            pstmt = conn.prepareStatement(sql);
```
**Ligne 55**: Prépare la requête SQL avec la connexion

```java
            rs = pstmt.executeQuery();
```
**Ligne 56**: Exécute la requête et stocke le résultat dans rs

```java
            userFilter.getItems().clear();
```
**Ligne 57**: Vide la liste des items du filtre utilisateur

```java
            userFilter.getItems().add(null);
```
**Ligne 58**: Ajoute l'option null (tous les utilisateurs)

```java
            while (rs.next()) {
```
**Ligne 59**: Boucle pour parcourir chaque ligne du résultat

```java
                userFilter.getItems().add(rs.getInt("id"));
```
**Ligne 60**: Ajoute l'ID de l'utilisateur au filtre

```java
            }
```
**Ligne 61**: Fin de la boucle while

```java
        } catch (SQLException e) {
```
**Ligne 62**: Capture des exceptions SQL

```java
            e.printStackTrace();
```
**Ligne 63**: Affiche la trace de l'erreur dans la console

```java
        } finally {
```
**Ligne 64**: Bloc finally exécuté quoi qu'il arrive

```java
            DatabaseUtil.close(conn, pstmt, rs);
```
**Ligne 65**: Ferme proprement la connexion, le statement et le resultset

```java
        }
```
**Ligne 66**: Fin du bloc finally

```java
    }
```
**Ligne 67**: Fin de la méthode loadUsers

```java
    private void setupWeekFilter() {
```
**Ligne 68**: Début de la méthode pour configurer le filtre de semaines

```java
        weekFilter.getItems().clear();
```
**Ligne 69**: Vide le filtre de semaines

```java
        for (int i = 1; i <= 52; i++) {
```
**Ligne 70**: Boucle de 1 à 52 pour ajouter toutes les semaines de l'année

```java
            weekFilter.getItems().add(i);
```
**Ligne 71**: Ajoute le numéro de semaine au filtre

```java
        }
```
**Ligne 72**: Fin de la boucle for

```java
        weekFilter.setValue(1);
```
**Ligne 73**: Sélectionne la semaine 1 par défaut

```java
    }
```
**Ligne 74**: Fin de la méthode setupWeekFilter

```java
    private void setupDayFilter() {
```
**Ligne 75**: Début de la méthode pour configurer le filtre de jours

```java
        dayFilter.getItems().clear();
```
**Ligne 76**: Vide le filtre de jours

```java
        for (int i = 1; i <= 7; i++) {
```
**Ligne 77**: Boucle de 1 à 7 pour les jours de la semaine

```java
            dayFilter.getItems().add(i);
```
**Ligne 78**: Ajoute le jour au filtre (1=Lundi, ..., 7=Dimanche)

```java
        }
```
**Ligne 79**: Fin de la boucle for

```java
        dayFilter.setValue(null);
```
**Ligne 80**: Ne sélectionne aucun jour par défaut (tous les jours)

```java
    }
```
**Ligne 81**: Fin de la méthode setupDayFilter

```java
    private void loadAllSeances() {
```
**Ligne 82**: Début de la méthode pour charger toutes les séances

```java
        Connection conn = null;
```
**Ligne 83**: Initialisation de la connexion

```java
        PreparedStatement pstmt = null;
```
**Ligne 84**: Initialisation du statement préparé

```java
        ResultSet rs = null;
```
**Ligne 85**: Initialisation du resultset

```java
        try {
```
**Ligne 86**: Début du bloc try

```java
            conn = DatabaseUtil.getConnection();
```
**Ligne 87**: Obtient une connexion à la base

```java
            String sql = "SELECT * FROM seance";
```
**Ligne 88**: Requête SQL pour sélectionner toutes les séances

```java
            pstmt = conn.prepareStatement(sql);
```
**Ligne 89**: Prépare la requête

```java
            rs = pstmt.executeQuery();
```
**Ligne 90**: Exécute la requête

```java
            allSeances = FXCollections.observableArrayList();
```
**Ligne 91**: Crée une liste observable vide

```java
            while (rs.next()) {
```
**Ligne 92**: Boucle sur chaque séance

```java
                Seance seance = new Seance();
```
**Ligne 93**: Crée un nouvel objet Seance

```java
                seance.setId(rs.getInt("id"));
```
**Ligne 94**: Définit l'ID de la séance

```java
                seance.setNom(rs.getString("nom"));
```
**Ligne 95**: Définit le nom de la séance

```java
                seance.setDate(rs.getString("date"));
```
**Ligne 96**: Définit la date de la séance

```java
                seance.setHeureDebut(rs.getString("heure_debut"));
```
**Ligne 97**: Définit l'heure de début

```java
                seance.setHeureFin(rs.getString("heure_fin"));
```
**Ligne 98**: Définit l'heure de fin

```java
                seance.setSalleId(rs.getInt("salle_id"));
```
**Ligne 99**: Définit l'ID de la salle

```java
                seance.setUserId(rs.getInt("user_id"));
```
**Ligne 100**: Définit l'ID de l'utilisateur

```java
                seance.setModuleId(rs.getInt("module_id"));
```
**Ligne 101**: Définit l'ID du module

```java
                allSeances.add(seance);
```
**Ligne 102**: Ajoute la séance à la liste

```java
            }
```
**Ligne 103**: Fin de la boucle while

```java
        } catch (SQLException e) {
```
**Ligne 104**: Capture des exceptions SQL

```java
            e.printStackTrace();
```
**Ligne 105**: Affiche l'erreur

```java
        } finally {
```
**Ligne 106**: Bloc finally

```java
            DatabaseUtil.close(conn, pstmt, rs);
```
**Ligne 107**: Ferme les ressources

```java
        }
```
**Ligne 108**: Fin du finally

```java
    }
```
**Ligne 109**: Fin de loadAllSeances

```java
    private void setupListView() {
```
**Ligne 110**: Début de la méthode de configuration de la ListView

```java
        scheduleListView.setCellFactory(lv -> new ListCell<Seance>() {
```
**Ligne 111**: Définit la factory pour créer les cellules de la ListView

```java
            @Override
            protected void updateItem(Seance seance, boolean empty) {
```
**Ligne 112**: Méthode appelée pour mettre à jour chaque cellule

```java
                super.updateItem(seance, empty);
```
**Ligne 113**: Appelle la méthode parente

```java
                if (empty || seance == null) {
```
**Ligne 114**: Vérifie si la cellule est vide

```java
                    setText(null);
```
**Ligne 115**: Efface le texte si vide

```java
                } else {
```
**Ligne 116**: Sinon, affiche les détails

```java
                    String text = String.format("%s - %s (%s - %s)",
```
**Ligne 117**: Formate le texte d'affichage

```java
                        seance.getNom(),
```
**Ligne 118**: Nom de la séance

```java
                        seance.getDate(),
```
**Ligne 119**: Date de la séance

```java
                        seance.getHeureDebut(),
```
**Ligne 120**: Heure de début

```java
                        seance.getHeureFin()
```
**Ligne 121**: Heure de fin

```java
                    );
```
**Ligne 122**: Ferme le formatage

```java
                    setText(text);
```
**Ligne 123**: Définit le texte de la cellule

```java
                }
```
**Ligne 124**: Fin du else

```java
            }
```
**Ligne 125**: Fin de updateItem

```java
        });
```
**Ligne 126**: Fin de la factory

```java
    }
```
**Ligne 127**: Fin de setupListView

```java
    private void applyFilters() {
```
**Ligne 128**: Début de la méthode pour appliquer les filtres

```java
        ObservableList<Seance> filtered = FXCollections.observableArrayList();
```
**Ligne 129**: Crée une liste pour les séances filtrées

```java
        Integer selectedDay = dayFilter.getValue();
```
**Ligne 130**: Récupère le jour sélectionné (peut être null)

```java
        Integer selectedWeek = weekFilter.getValue();
```
**Ligne 131**: Récupère la semaine sélectionnée

```java
        Integer selectedUser = userFilter.getValue();
```
**Ligne 132**: Récupère l'utilisateur sélectionné

```java
        for (Seance seance : allSeances) {
```
**Ligne 133**: Boucle sur toutes les séances

```java
            boolean match = true;
```
**Ligne 134**: Flag pour vérifier si la séance correspond aux filtres

```java
            if (selectedDay != null && !seance.getDate().matches(".*\\d{4}-\\d{2}-\\d{2}.*")) {
```
**Ligne 135**: Vérifie si un jour est sélectionné et si la date est valide

```java
                match = false;
```
**Ligne 136**: Ne correspond pas si la date n'est pas valide

```java
            }
```
**Ligne 137**: Fin du if

```java
            if (match && selectedDay != null) {
```
**Ligne 138**: Vérifie la correspondance du jour

```java
                // Extraction du jour de la date
```
**Ligne 139**: Commentaire

```java
            }
```
**Ligne 140**: Fin du if

```java
            if (match && selectedUser != null && !seance.getUserId().equals(selectedUser)) {
```
**Ligne 141**: Vérifie si l'utilisateur correspond

```java
                match = false;
```
**Ligne 142**: Ne correspond pas si l'utilisateur est différent

```java
            }
```
**Ligne 143**: Fin du if

```java
            if (match) {
```
**Ligne 144**: Si la séance correspond à tous les filtres

```java
                filtered.add(seance);
```
**Ligne 145**: Ajoute à la liste filtrée

```java
            }
```
**Ligne 146**: Fin du if

```java
        }
```
**Ligne 147**: Fin de la boucle for

```java
        scheduleListView.setItems(filtered);
```
**Ligne 148**: Met à jour la ListView avec les séances filtrées

```java
        statusLabel.setText(filtered.size() + " séances trouvées");
```
**Ligne 149**: Affiche le nombre de séances trouvées

```java
    }
```
**Ligne 150**: Fin de applyFilters

```java
    private void showDayView() {
```
**Ligne 151**: Début de la méthode pour afficher la vue journalière

```java
        weekViewContainer.setVisible(false);
```
**Ligne 152**: Cache le conteneur de vue hebdomadaire

```java
        scheduleListView.setVisible(true);
```
**Ligne 153**: Affiche la ListView des séances

```java
    }
```
**Ligne 154**: Fin de showDayView

```java
    private void showWeekView() {
```
**Ligne 155**: Début de la méthode pour afficher la vue hebdomadaire

```java
        weekViewContainer.setVisible(true);
```
**Ligne 156**: Affiche le conteneur de vue hebdomadaire

```java
        scheduleListView.setVisible(false);
```
**Ligne 157**: Cache la ListView

```java
        // Charger les séances de la semaine et les afficher dans weekViewContainer
```
**Ligne 158**: Commentaire pour implémentation future

```java
    }
```
**Ligne 159**: Fin de showWeekView

```java
}
```
**Ligne 160**: Fin de la classe EmploiContentController

---

## Résumé des fonctionnalités clés

1. **Filtrage par jour**: Permet de filtrer les séances par jour de la semaine (1-7)
2. **Filtrage par semaine**: Permet de filtrer par numéro de semaine (1-52)
3. **Filtrage par utilisateur**: Permet de filtrer les séances par utilisateur
4. **Vue journalière**: Affiche les séances dans une ListView
5. **Vue hebdomadaire**: Préparée pour l'affichage hebdomadaire (HBox)
6. **Rafraîchissement**: Bouton pour recharger les données
7. **Statut**: Affiche le nombre de séances trouvées
