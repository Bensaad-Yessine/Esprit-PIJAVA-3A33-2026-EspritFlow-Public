# Esprit Flow — Application JavaFX

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue)
![Maven](https://img.shields.io/badge/Maven-Project-red)
![MySQL](https://img.shields.io/badge/Database-MySQL-lightgrey)
![Status](https://img.shields.io/badge/Academic%20Project-PIDEV-success)

## Description du projet

**Esprit Flow** est une application desktop développée en **JavaFX** dans le cadre du projet académique **PIDEV 3A**. L’application propose une plateforme de gestion scolaire et de bien-être destinée aux étudiants et aux administrateurs.

La partie JavaFX contient deux espaces principaux :

- **Front Office** : espace utilisateur/étudiant permettant de consulter ses informations, ses tâches, sa classe, ses matières, ses objectifs santé, ses suivis bien-être, ses groupes et ses alertes.
- **Back Office** : espace administrateur permettant de gérer les utilisateurs, classes, matières, salles, séances, tâches, groupes, réunions, objectifs santé, suivis et préférences d’alerte.

Le projet applique une architecture organisée autour des **entités**, **services**, **contrôleurs JavaFX**, fichiers **FXML**, feuilles de style **CSS**, et intégrations avec des **API externes**.

---

## Objectifs du projet

- Centraliser la gestion scolaire dans une application desktop moderne.
- Séparer l’espace administrateur et l’espace étudiant.
- Faciliter la gestion des classes, matières, salles, séances et utilisateurs.
- Permettre le suivi des tâches, alertes, groupes et réunions.
- Ajouter un module santé avec objectifs, suivis bien-être, coaching IA, recettes et exercices.
- Utiliser une base de données MySQL via JDBC.
- Intégrer des API externes pour enrichir l’expérience utilisateur.
- Proposer une interface professionnelle basée sur JavaFX, FXML et CSS.

---

## Fonctionnalités principales

### Authentification et gestion de session

- Connexion par email et mot de passe.
- Gestion du rôle utilisateur : administrateur ou étudiant.
- Redirection automatique vers le Back Office ou le Front Office selon le rôle.
- Option **Remember Me** pour mémoriser l’email.
- Gestion globale de l’utilisateur connecté avec `SessionManager`.
- Sécurité du mot de passe avec **BCrypt**.

### Back Office

L’espace Back Office permet à l’administrateur de gérer les différentes parties du système :

- **Dashboard administrateur** : statistiques générales, nombre d’étudiants, enseignants, classes et données de suivi.
- **Gestion des utilisateurs** : affichage et administration des comptes.
- **Gestion des classes** : ajout, modification, suppression, recherche, filtres et pagination.
- **Gestion des matières** : gestion des coefficients, charge horaire, complexité et description.
- **Gestion des salles** : gestion des espaces physiques.
- **Gestion des séances** : organisation des séances et emplois du temps.
- **Gestion des tâches** : création, modification, détails, priorités, statuts et suivi.
- **Gestion des préférences d’alerte** : création, modification et consultation des alertes.
- **Gestion des groupes** : consultation et gestion des groupes de travail.
- **Gestion des propositions de réunion** : création et suivi des propositions.
- **Gestion des objectifs santé** : CRUD complet, recherche, tri, suivi et archivage.
- **Gestion des suivis bien-être** : consultation, ajout, modification et analyse des suivis.
- **Archives santé** : consultation des objectifs archivés avec statut final.

### Front Office

L’espace Front Office est destiné à l’étudiant :

- **Dashboard étudiant** : vue d’ensemble personnalisée.
- **Profil utilisateur** : consultation des informations personnelles.
- **Classe connectée** : affichage de la classe associée à l’étudiant connecté.
- **Matières** : consultation des matières liées à la classe.
- **Emploi du temps** : consultation des séances.
- **Tâches** : consultation, création, modification et détails des tâches.
- **Alertes** : gestion des préférences d’alerte personnelles.
- **Groupes** : consultation des groupes et détails.
- **Objectifs santé** : création, modification, consultation et archivage.
- **Suivis bien-être** : ajout et modification des suivis liés aux objectifs.
- **Détail objectif** : affichage des informations de l’objectif, suivis, coaching IA, recettes ou exercices selon le type d’objectif.

---

## Module Objectif Santé et Bien-être

Le module santé permet de suivre l’évolution personnelle de l’étudiant à travers des objectifs et des suivis.

### Objectif Santé

Un objectif santé peut contenir :

- titre ;
- type : `SOMMEIL`, `SPORT`, `ALIMENTATION` ;
- valeur cible ;
- date de début ;
- date de fin ;
- statut : `EN_COURS`, `ATTEINT`, `ABANDONNE` ;
- priorité : `BASSE`, `MOYENNE`, `HAUTE` ;
- utilisateur associé ;
- date d’archivage ;
- chemin du PDF d’archive.

### Suivi Bien-être

Un suivi permet d’évaluer l’état de l’utilisateur selon plusieurs indicateurs :

- humeur ;
- qualité du sommeil ;
- niveau d’énergie ;
- niveau de stress ;
- qualité d’alimentation ;
- notes libres ;
- score calculé ;
- objectif associé.

### Coaching intelligent

Le projet intègre un service de coaching privé basé sur l’API **Groq**. Le coaching analyse l’objectif et ses suivis pour générer :

- un message de motivation ;
- un résumé d’analyse ;
- un niveau global ;
- une tendance ;
- un point fort ;
- un point faible ;
- des conseils personnalisés ;
- un message de confiance.

### Recettes et exercices

Selon le type d’objectif, l’application peut afficher :

- des **recettes saines** via l’API Spoonacular pour les objectifs de type alimentation ;
- des **exercices sportifs** via Workout API pour les objectifs de type sport.

### Archivage PDF

Le service `PdfArchiveObjectifService` permet de générer un PDF d’archive pour un objectif terminé. Les fichiers PDF sont stockés dans le dossier :

```txt
archives_pdf/
```

L’envoi d’email avec PDF attaché est géré par `BrevoEmailService`.

---

## APIs externes utilisées

| API | Utilisation | Classe Java |
|---|---|---|
| Groq API | Coaching intelligent privé | `GroqClient`, `CoachingPriveService` |
| Spoonacular API | Recherche de recettes saines | `SpoonacularClient` |
| Workout API | Suggestions d’exercices sportifs | `WorkoutApiExerciseClient` |
| Brevo API | Envoi d’emails avec pièce jointe PDF | `BrevoEmailService` |
| iText PDF | Génération des fichiers PDF d’archive | `PdfArchiveObjectifService` |

---

## Technologies utilisées

| Technologie | Rôle |
|---|---|
| Java 17 | Langage principal |
| JavaFX 21.0.2 | Interface graphique desktop |
| FXML | Structure des vues |
| CSS | Design et styles des interfaces |
| Maven | Gestion du projet et des dépendances |
| MySQL | Base de données relationnelle |
| JDBC | Connexion entre Java et MySQL |
| BCrypt | Hachage et vérification des mots de passe |
| org.json | Traitement des réponses JSON |
| dotenv-java | Chargement des variables d’environnement |
| iTextPDF | Génération de PDF |
| IntelliJ IDEA | Environnement de développement |
| SceneBuilder | Conception des interfaces FXML |

---

## Architecture du projet

```txt
pjava/
├── pom.xml
├── README.md
├── .env.example
├── run-javafx.ps1
├── archives_pdf/
├── src/
│   └── main/
│       ├── java/
│       │   └── piJava/
│       │       ├── Controllers/
│       │       │   ├── backoffice/
│       │       │   ├── frontoffice/
│       │       │   ├── AjouterUser.java
│       │       │   └── loginController.java
│       │       ├── entities/
│       │       ├── services/
│       │       │   └── api/
│       │       ├── utils/
│       │       ├── testes/
│       │       └── mains/
│       └── resources/
│           ├── backoffice/
│           ├── frontoffice/
│           ├── images/
│           ├── login.fxml
│           ├── login.css
│           ├── register.css
│           └── AjouterUser.fxml
```

---

## Rôle des principaux packages

### `piJava.Controllers`

Contient les contrôleurs JavaFX qui relient les vues FXML avec la logique métier.

- `backoffice/` : contrôleurs de l’espace administrateur.
- `frontoffice/` : contrôleurs de l’espace étudiant.
- `loginController.java` : gestion de la connexion.
- `AjouterUser.java` : inscription utilisateur.

### `piJava.entities`

Contient les classes modèles représentant les tables ou objets métier :

- `user`
- `Classe`
- `Matiere`
- `Salle`
- `Seance`
- `tache`
- `suiviTache`
- `preferenceAlerte`
- `Groupe`
- `PropositionReunion`
- `ObjectifSante`
- `SuiviBienEtre`
- `Recette`
- `ExerciceSport`

### `piJava.services`

Contient les services responsables de la logique métier et des opérations CRUD avec la base de données.

Exemples :

- `UserServices`
- `ClasseService`
- `MatiereService`
- `SalleService`
- `SeanceService`
- `TacheService`
- `AlerteService`
- `GroupeService`
- `ObjectifSanteService`
- `SuiviBienEtreService`

### `piJava.services.api`

Contient les services liés aux API externes :

- `GroqClient`
- `CoachingPriveService`
- `SpoonacularClient`
- `WorkoutApiExerciseClient`
- `BrevoEmailService`
- `PdfArchiveObjectifService`

### `piJava.utils`

Contient les classes utilitaires :

- `MyDataBase` : connexion MySQL Singleton.
- `SessionManager` : gestion de l’utilisateur connecté.
- `ValidationHelper` : validation des formulaires.
- `TacheValidator` : validation des tâches.
- `PreferenceAlerteValidator` : validation des préférences d’alerte.
- `SchemaDumper` : outil de diagnostic de schéma.

### `src/main/resources`

Contient toutes les vues FXML, fichiers CSS et ressources graphiques.

---

## Prérequis

Avant de lancer le projet, il faut installer :

- JDK 17 ou supérieur ;
- Maven ;
- MySQL Server ;
- IntelliJ IDEA ;
- SceneBuilder, optionnel mais utile pour modifier les interfaces FXML.

Vérification :

```bash
java -version
mvn -version
```

---

## Installation du projet

### 1. Cloner le dépôt

```bash
git clone https://github.com/Bensaad-Yessine/Esprit-PIJAVA-3A33-2026-EspritFlow-Public.git
cd Esprit-PIJAVA-3A33-2026-EspritFlow-Public
```

### 2. Installer les dépendances Maven

```bash
mvn clean install
```

### 3. Configurer la base de données

Créer une base MySQL nommée :

```sql
CREATE DATABASE pidev;
```

La connexion utilisée dans `MyDataBase.java` est :

```txt
URL      : jdbc:mysql://localhost:3306/pidev
Username : root
Password : vide
```

Si votre configuration MySQL est différente, modifier le fichier :

```txt
src/main/java/piJava/utils/MyDataBase.java
```

---

## Configuration du fichier `.env`

Créer un fichier `.env` à la racine du projet.

Exemple :

```env
GROQ_API_KEY=your_groq_api_key
GROQ_MODEL=llama-3.1-8b-instant
SPOONACULAR_API_KEY=your_spoonacular_api_key
WORKOUT_API_KEY=your_workout_api_key
BREVO_API_KEY=your_brevo_api_key
MAIL_FROM=your_email@example.com
```

Important : le fichier `.env` contient des clés privées. Il ne doit jamais être publié sur GitHub.

Le fichier `.gitignore` doit contenir :

```gitignore
.env
target/
.idea/
archives_pdf/
```

---

## Lancement de l’application

### Méthode 1 : Maven

```bash
mvn javafx:run
```

La classe principale configurée dans le `pom.xml` est :

```txt
piJava.mains.Launcher
```

Cette classe lance ensuite :

```txt
piJava.mains.mainFX
```

L’écran de départ est :

```txt
/login.fxml
```

### Méthode 2 : IntelliJ IDEA

1. Ouvrir le projet avec IntelliJ IDEA.
2. Attendre le chargement Maven.
3. Vérifier que le JDK du projet est Java 17.
4. Ouvrir `piJava.mains.Launcher`.
5. Cliquer sur **Run**.

### Méthode 3 : script Windows PowerShell

Le projet contient un script :

```powershell
./run-javafx.ps1
```

Ce script lance l’application avec les dépendances JavaFX présentes dans le repository Maven local.

---

## Identifiants et rôles

L’application redirige l’utilisateur selon son rôle :

- administrateur : `/backoffice/main.fxml`
- étudiant/utilisateur : `/frontoffice/main.fxml`

La vérification se fait après connexion dans :

```txt
loginController.java
SessionManager.java
loginService.java
```

---

## Structure des vues principales

### Back Office

```txt
src/main/resources/backoffice/
├── main.fxml
├── sidebar.fxml
├── dashboard/
├── User/
├── Classe/
├── Matiere/
├── Salle/
├── Seance/
├── taches/
├── preferenceAlerte/
├── group/
├── objectifsante/
└── suivibienetre/
```

### Front Office

```txt
src/main/resources/frontoffice/
├── main.fxml
├── sidebar.fxml
├── dashboard/
├── user/
├── classe/
├── matieres/
├── emploi/
├── salle/
├── taches/
├── preferenceAlerte/
├── group/
├── objectifsante/
└── suivibienetre/
```

---

## Design et interface

L’interface utilise :

- JavaFX pour les composants graphiques ;
- FXML pour structurer les pages ;
- CSS pour les thèmes visuels ;
- cartes, tableaux, badges, boutons d’action et dashboards ;
- séparation claire entre pages Back Office et pages Front Office.

Les fichiers CSS sont organisés par module afin de garder un design cohérent et facile à maintenir.

---

## Sécurité et bonnes pratiques

- Les mots de passe sont protégés avec **BCrypt**.
- Les clés API sont chargées depuis `.env` avec `dotenv-java`.
- Le fichier `.env` ne doit pas être poussé sur GitHub.
- La session utilisateur est centralisée avec `SessionManager`.
- Les validations sont séparées dans des classes utilitaires.
- Le projet suit une séparation logique : entités, services, contrôleurs, vues et styles.

---

## Dépendances Maven principales

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.32</version>
</dependency>

<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.2</version>
</dependency>

<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21.0.2</version>
</dependency>

<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>

<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20240303</version>
</dependency>

<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.2.0</version>
</dependency>
```

---

## Tests et classes de diagnostic

Le projet contient quelques classes de test dans :

```txt
src/main/java/piJava/testes/
```

Exemples :

- `TestBrevo.java` : test d’envoi d’email via Brevo.
- `TestGroq.java` : test de connexion à Groq.
- `TestCoachingPrive.java` : test de génération de coaching.

---

## Problèmes fréquents et solutions

### Erreur de connexion MySQL

Vérifier :

- que MySQL est lancé ;
- que la base `pidev` existe ;
- que le port est `3306` ;
- que le nom d’utilisateur et le mot de passe sont corrects dans `MyDataBase.java`.

### Erreur JavaFX au lancement

Vérifier :

- que Java 17 est sélectionné ;
- que Maven a bien téléchargé les dépendances ;
- que la classe principale est `piJava.mains.Launcher` ;
- que les fichiers FXML existent bien dans `src/main/resources`.

### Erreur avec les APIs externes

Vérifier :

- que le fichier `.env` existe à la racine du projet ;
- que les noms des variables sont corrects ;
- que les clés API sont valides ;
- que la connexion Internet est disponible.

---

## Mots-clés du projet

```txt
Java, JavaFX, Maven, MySQL, JDBC, FXML, CSS, MVC, CRUD, Back Office,
Front Office, SessionManager, BCrypt, API REST, JSON, dotenv, Groq,
Spoonacular, Workout API, Brevo, PDF, iText, Dashboard, Gestion scolaire,
Objectif santé, Suivi bien-être, Coaching IA, Classe connectée.
```

---

## Équipe

Projet réalisé dans le cadre du PIDEV 3A.

| Membre | Module / contribution |
|---|---|
| Adem Mannoubi | Objectifs santé, suivis bien-être, coaching IA, recettes, exercices, archivage PDF |
| Membre 2 | Gestion des classes / matières / salles / séances |
| Membre 3 | Gestion des tâches / alertes |
| Membre 4 | Gestion des groupes / propositions de réunion |
| Membre 5 | Utilisateurs / dashboard / intégration générale |

> À modifier selon la répartition exacte de l’équipe.

---

## Captures d’écran

Ajouter les captures dans un dossier `docs/screenshots/` puis les intégrer ici :

```md
![Login](docs/screenshots/login.png)
![Dashboard](docs/screenshots/dashboard.png)
![Objectifs Santé](docs/screenshots/objectifs.png)
![Tâches](docs/screenshots/taches.png)
![Classes](docs/screenshots/classes.png)
```

---

## Licence

Ce projet est réalisé dans un cadre académique à **Esprit School of Engineering**.

---

## Conclusion

**Esprit Flow JavaFX** est une application desktop complète qui combine gestion scolaire, organisation personnelle, suivi santé, intelligence artificielle et intégration d’API externes. Grâce à son architecture séparée en contrôleurs, services, entités et vues FXML, le projet reste maintenable, évolutif et adapté à une démonstration académique professionnelle.
