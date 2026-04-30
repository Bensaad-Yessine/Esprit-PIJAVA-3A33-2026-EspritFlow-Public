# 📚 Architecture et Emplacement des Fichiers - EspritFlow

Ce document fournit une cartographie complète de l'architecture du projet et l'emplacement de tous les fichiers pour chaque fonctionnalité.

---

## 📁 Structure Générale du Projet

```
Esprit-PIJAVA-3A33-2026-EspritFlow-Public/
├── src/
│   ├── main/
│   │   ├── java/piJava/
│   │   │   ├── Controllers/          # 40 contrôleurs JavaFX
│   │   │   ├── entities/             # 11 entités JPA
│   │   │   ├── services/             # 15 services métier
│   │   │   ├── mains/                # Point d'entrée main.java
│   │   │   └── utils/                # Classes utilitaires
│   │   └── resources/
│   │       ├── *.css                 # 52 fichiers CSS
│   │       ├── *.fxml                # 40 fichiers FXML
│   │       └── *.html                # Fichiers HTML pour WebView
├── docs/                              # Documentation technique
├── pom.xml                            # Configuration Maven
└── run-javafx.ps1                     # Script de démarrage
```

---

## 🎯 Fonctionnalités Principales et Emplacement des Fichiers

### 1️⃣ Gestion des Utilisateurs (Backoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `user.java` | `src/main/java/piJava/entities/user.java` | Entité JPA pour les utilisateurs |
| **Controller** | `UserContentController.java` | `src/main/java/piJava/Controllers/backoffice/User/UserContentController.java` | Gestion CRUD des utilisateurs |
| **Service** | `UserServices.java` | `src/main/java/piJava/services/UserServices.java` | Logique métier utilisateurs |
| **View (FXML)** | `UserContent.fxml` | `src/main/resources/backoffice/User/UserContent.fxml` | Interface utilisateur |
| **Style (CSS)** | `users.css` | `src/main/resources/backoffice/User/users.css` | Styles CSS |

**Code clé dans UserContentController.java :**
- `initialize()` - Initialisation de la table et des formulaires
- `loadUsers()` - Chargement des utilisateurs depuis la BDD
- `addUser()` / `editUser()` / `deleteUser()` - Opérations CRUD
- `generateQRCode()` - Génération de QR code pour l'utilisateur

---

### 2️⃣ Gestion des Séances (Backoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `Seance.java` | `src/main/java/piJava/entities/Seance.java` | Entité JPA pour les séances |
| **Controller** | `SeanceContentController.java` | `src/main/java/piJava/Controllers/backoffice/Seance/SeanceContentController.java` | Gestion CRUD des séances |
| **Controller** | `SeanceDashboardController.java` | `src/main/java/piJava/Controllers/backoffice/Seance/SeanceDashboardController.java` | Dashboard des séances |
| **Controller** | `QRCodeDisplayController.java` | `src/main/java/piJava/Controllers/backoffice/Seance/QRCodeDisplayController.java` | Affichage QR code séance |
| **Service** | `SeanceService.java` | `src/main/java/piJava/services/SeanceService.java` | Logique métier séances |
| **View (FXML)** | `SeanceContent.fxml` | `src/main/resources/backoffice/Seance/SeanceContent.fxml` | Interface utilisateur |
| **Style (CSS)** | `seance.css` | `src/main/resources/backoffice/Seance/seance.css` | Styles CSS |

**Code clé dans SeanceContentController.java :**
- `initialize()` - Initialisation des formulaires et calendriers
- `loadSeances()` - Chargement des séances depuis la BDD
- `addSeance()` / `updateSeance()` / `deleteSeance()` - Opérations CRUD
- `generateQRCode()` - Génération de QR code pour la séance
- `exportPDF()` - Export PDF de l'emploi du temps
- `sendEmails()` - Envoi d'emails aux étudiants
- `addToAgenda()` - Ajout à l'agenda Google Calendar

---

### 3️⃣ Gestion des Salles (Backoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `Salle.java` | `src/main/java/piJava/entities/Salle.java` | Entité JPA pour les salles |
| **Controller** | `SallesContentController.java` | `src/main/java/piJava/Controllers/backoffice/Salle/SallesContentController.java` | Gestion CRUD des salles |
| **Service** | `SalleService.java` | `src/main/java/piJava/services/SalleService.java` | Logique métier salles |
| **View (FXML)** | `SallesContent.fxml` | `src/main/resources/backoffice/Salle/SallesContent.fxml` | Interface utilisateur |
| **Style (CSS)** | `salles.css` | `src/main/resources/backoffice/Salle/salles.css` | Styles CSS |

**Code clé dans SallesContentController.java :**
- `initialize()` - Initialisation de la table des salles
- `loadSalles()` - Chargement des salles depuis la BDD
- `addSalle()` / `editSalle()` / `deleteSalle()` - Opérations CRUD

---

### 4️⃣ Gestion des Matières (Backoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `Matiere.java` | `src/main/java/piJava/entities/Matiere.java` | Entité JPA pour les matières |
| **Controller** | `MatiereContentController.java` | `src/main/java/piJava/Controllers/backoffice/Matiere/MatiereContentController.java` | Gestion CRUD des matières |
| **Service** | `MatiereService.java` | `src/main/java/piJava/services/MatiereService.java` | Logique métier matières |
| **View (FXML)** | `MatiereContent.fxml` | `src/main/resources/backoffice/Matiere/MatiereContent.fxml` | Interface utilisateur |
| **Style (CSS)** | `matieres.css` | `src/main/resources/backoffice/Matiere/matieres.css` | Styles CSS |

---

### 5️⃣ Gestion des Classes (Backoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `Classe.java` | `src/main/java/piJava/entities/Classe.java` | Entité JPA pour les classes |
| **Controller** | `ClasseContentController.java` | `src/main/java/piJava/Controllers/backoffice/Classe/ClasseContentController.java` | Gestion CRUD des classes |
| **Service** | `ClasseService.java` | `src/main/java/piJava/services/ClasseService.java` | Logique métier classes |
| **View (FXML)** | `ClassesContent.fxml` | `src/main/resources/backoffice/Classe/ClassesContent.fxml` | Interface utilisateur |
| **Style (CSS)** | `classes.css` | `src/main/resources/backoffice/Classe/classes.css` | Styles CSS |

---

### 6️⃣ Emploi du Temps (Frontoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Controller** | `EmploiContentController.java` | `src/main/java/piJava/Controllers/frontoffice/emploi/EmploiContentController.java` | Affichage de l'emploi du temps |
| **Service** | `SeanceService.java` | `src/main/java/piJava/services/SeanceService.java` | Récupération des séances |
| **View (FXML)** | `EmploiContent.fxml` | `src/main/resources/frontoffice/emploi/EmploiContent.fxml` | Interface utilisateur |
| **Style (CSS)** | `emploi.css` | `src/main/resources/frontoffice/emploi/emploi.css` | Styles CSS |

**Code clé dans EmploiContentController.java :**
- `initialize()` - Initialisation du calendrier hebdomadaire
- `loadEmploiDuTemps()` - Chargement des séances de l'utilisateur
- `renderWeeklySchedule()` - Affichage du calendrier en grille
- `exportToPDF()` - Export PDF de l'emploi du temps
- `sendEmailNotification()` - Envoi d'emails de notification

---

### 7️⃣ Scan QR Code (Frontoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Controller** | `ScanQRCodeController.java` | `src/main/java/piJava/Controllers/frontoffice/emploi/ScanQRCodeController.java` | Scan de QR code |
| **Service** | `AttendanceService.java` | `src/main/java/piJava/services/AttendanceService.java` | Gestion des présences |
| **Entity** | `Attendance.java` | `src/main/java/piJava/entities/Attendance.java` | Entité JPA pour les présences |

**Code clé dans ScanQRCodeController.java :**
- `initialize()` - Initialisation de la caméra
- `startCamera()` / `stopCamera()` - Gestion de la caméra
- `scanQRCode()` - Détection et validation du QR code
- `markAttendance()` - Marquage de la présence

---

### 8️⃣ Carte Campus et Réservation (Frontoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Controller** | `FrontCampusMapController.java` | `src/main/java/piJava/Controllers/frontoffice/salle/FrontCampusMapController.java` | Gestion de la carte interactive |
| **Controller** | `FrontSallesController.java` | `src/main/java/piJava/Controllers/frontoffice/salle/FrontSallesController.java` | Liste des salles avec accès à la carte |
| **Service** | `SeanceService.java` | `src/main/java/piJava/services/SeanceService.java` | Création des réservations |
| **Entity** | `Seance.java` | `src/main/java/piJava/entities/Seance.java` | Entité pour les séances de révision |
| **View (FXML)** | `FrontSallesContent.fxml` | `src/main/resources/frontoffice/salle/FrontSallesContent.fxml` | Interface de liste des salles |
| **HTML Map** | `map.html` | `src/main/resources/frontoffice/salle/map.html` | Carte Google Maps interactive |
| **Style (CSS)** | `front_salles.css` | `src/main/resources/frontoffice/salle/front_salles.css` | Styles CSS |

**Code clé dans FrontCampusMapController.java :**
- `initialize()` - Initialisation de la WebView et chargement de la carte
- `loadMap()` - Chargement du fichier HTML de la carte
- `bookSlot()` - Réservation d'un créneau horaire
- `handleBlockSelection()` - Sélection d'un bloc ESPRIT

**Code clé dans map.html :**
- Fonctions JavaScript pour Google Maps API
- `updateUserPosition()` - Affichage de la position GPS utilisateur
- Création des marqueurs pour chaque bloc ESPRIT
- Gestion des créneaux horaires

---

### 9️⃣ Statistiques Dashboard (Frontoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Controller** | `FrontDashboardController.java` | `src/main/java/piJava/Controllers/frontoffice/FrontDashboardController.java` | Dashboard statistiques |
| **Service** | `SeanceService.java` | `src/main/java/piJava/services/SeanceService.java` | Données pour les statistiques |
| **View (FXML)** | `dashboard-content.fxml` | `src/main/resources/frontoffice/dashboard/dashboard-content.fxml` | Interface dashboard |
| **Style (CSS)** | `dashboard.css` | `src/main/resources/frontoffice/dashboard/dashboard.css` | Styles CSS |

**Code clé dans FrontDashboardController.java :**
- `initialize()` - Initialisation des graphiques
- `loadStatistics()` - Chargement des données statistiques
- `updateWeeklyChart()` - Mise à jour du graphique hebdomadaire
- `updateAttendanceChart()` - Mise à jour du graphique de présence

---

### 🔟 Gestion des Tâches (Frontoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `tache.java` | `src/main/java/piJava/entities/tache.java` | Entité JPA pour les tâches |
| **Controller** | `TachesController.java` | `src/main/java/piJava/Controllers/frontoffice/taches/TachesController.java` | Liste des tâches |
| **Controller** | `TacheNewController.java` | `src/main/java/piJava/Controllers/frontoffice/taches/TacheNewController.java` | Création de tâche |
| **Controller** | `TacheEditController.java` | `src/main/java/piJava/Controllers/frontoffice/taches/TacheEditController.java` | Édition de tâche |
| **Controller** | `TachesDetailsController.java` | `src/main/java/piJava/Controllers/frontoffice/taches/TachesDetailsController.java` | Détails de tâche |
| **Service** | `TacheService.java` | `src/main/java/piJava/services/TacheService.java` | Logique métier tâches |
| **View (FXML)** | `taches-content.fxml` | `src/main/resources/frontoffice/taches/taches-content.fxml` | Interface liste |
| **View (FXML)** | `tache-new.fxml` | `src/main/resources/frontoffice/taches/tache-new.fxml` | Interface création |
| **View (FXML)** | `tache-edit.fxml` | `src/main/resources/frontoffice/taches/tache-edit.fxml` | Interface édition |
| **Style (CSS)** | `taches.css` | `src/main/resources/frontoffice/taches/taches.css` | Styles CSS |

---

### 1️⃣1️⃣ Objectifs de Santé (Frontoffice & Backoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `ObjectifSante.java` | `src/main/java/piJava/entities/ObjectifSante.java` | Entité JPA pour les objectifs |
| **Controller (FO)** | `AfficherObjectifsController.java` | `src/main/java/piJava/Controllers/frontoffice/objectifsante/AfficherObjectifsController.java` | Liste des objectifs |
| **Controller (FO)** | `AjouterObjectifController.java` | `src/main/java/piJava/Controllers/frontoffice/objectifsante/AjouterObjectifController.java` | Ajout d'objectif |
| **Controller (FO)** | `ModifierObjectifController.java` | `src/main/java/piJava/Controllers/frontoffice/objectifsante/ModifierObjectifController.java` | Modification d'objectif |
| **Controller (BO)** | `AfficherObjectifsController.java` | `src/main/java/piJava/Controllers/backoffice/objectifsante/AfficherObjectifsController.java` | Liste des objectifs (admin) |
| **Service** | `ObjectifSanteService.java` | `src/main/java/piJava/services/ObjectifSanteService.java` | Logique métier objectifs |
| **Style (CSS)** | `style.css` | `src/main/resources/frontoffice/objectifsante/style.css` | Styles CSS FO |
| **Style (CSS)** | `style.css` | `src/main/resources/backoffice/objectifsante/style.css` | Styles CSS BO |

---

### 1️⃣2️⃣ Suivi Bien-être (Frontoffice & Backoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `SuiviBienEtre.java` | `src/main/java/piJava/entities/SuiviBienEtre.java` | Entité JPA pour le suivi |
| **Controller (FO)** | `AfficherSuivisController.java` | `src/main/java/piJava/Controllers/frontoffice/suivibienetre/AfficherSuivisController.java` | Liste des suivis |
| **Controller (FO)** | `AjouterSuiviController.java` | `src/main/java/piJava/Controllers/frontoffice/suivibienetre/AjouterSuiviController.java` | Ajout de suivi |
| **Controller (FO)** | `ModifierSuiviController.java` | `src/main/java/piJava/Controllers/frontoffice/suivibienetre/ModifierSuiviController.java` | Modification de suivi |
| **Controller (BO)** | `AfficherSuivisController.java` | `src/main/java/piJava/Controllers/backoffice/suivibienetre/AfficherSuivisController.java` | Liste des suivis (admin) |
| **Controller (BO)** | `AjouterSuiviController.java` | `src/main/java/piJava/Controllers/backoffice/suivibienetre/AjouterSuiviController.java` | Ajout de suivi (admin) |
| **Controller (BO)** | `ModifierSuiviController.java` | `src/main/java/piJava/Controllers/backoffice/suivibienetre/ModifierSuiviController.java` | Modification de suivi (admin) |
| **Service** | `SuiviBienEtreService.java` | `src/main/java/piJava/services/SuiviBienEtreService.java` | Logique métier suivi |
| **Style (CSS)** | `style.css` | `src/main/resources/frontoffice/suivibienetre/style.css` | Styles CSS FO |
| **Style (CSS)** | `style.css` | `src/main/resources/backoffice/suivibienetre/style.css` | Styles CSS BO |

---

### 1️⃣3️⃣ Alertes et Préférences (Frontoffice)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Entity** | `preferenceAlerte.java` | `src/main/java/piJava/entities/preferenceAlerte.java` | Entité JPA pour les alertes |
| **Controller** | `AlertesController.java` | `src/main/java/piJava/Controllers/frontoffice/preferencealerte/AlertesController.java` | Liste des alertes |
| **Controller** | `AlerteNewController.java` | `src/main/java/piJava/Controllers/frontoffice/preferencealerte/AlerteNewController.java` | Création d'alerte |
| **Controller** | `AlerteEditController.java` | `src/main/java/piJava/Controllers/frontoffice/preferencealerte/AlerteEditController.java` | Édition d'alerte |
| **Controller** | `AlerteDetailsController.java` | `src/main/java/piJava/Controllers/frontoffice/preferencealerte/AlerteDetailsController.java` | Détails d'alerte |
| **Service** | `AlerteService.java` | `src/main/java/piJava/services/AlerteService.java` | Logique métier alertes |
| **View (FXML)** | `alerte-content.fxml` | `src/main/resources/frontoffice/preferenceAlerte/alerte-content.fxml` | Interface liste |
| **Style (CSS)** | `alerte.css` | `src/main/resources/frontoffice/preferenceAlerte/alerte.css` | Styles CSS |

---

### 1️⃣4️⃣ Système d'Email (Service)

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Service** | `EmailService.java` | `src/main/java/piJava/services/EmailService.java` | Service d'envoi d'emails |

**Code clé dans EmailService.java :**
- `sendEmail()` - Envoi d'un email simple
- `sendEmailWithAttachment()` - Envoi d'email avec pièce jointe
- `sendBulkEmail()` - Envoi d'emails en masse
- Configuration SMTP TLS

---

### 1️⃣5️⃣ Google Calendar Service

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Service** | `GoogleCalendarService.java` | `src/main/java/piJava/services/GoogleCalendarService.java` | Service Google Calendar |

**Code clé dans GoogleCalendarService.java :**
- `addToCalendar()` - Ajout d'un événement au calendrier
- `exportToICS()` - Export au format iCalendar (ICS)
- `deleteFromCalendar()` - Suppression d'un événement

---

### 1️⃣6️⃣ Sidebar et Navigation

| Type | Fichier | Chemin Complet | Description |
|------|---------|----------------|-------------|
| **Controller (FO)** | `FrontSidebarController.java` | `src/main/java/piJava/Controllers/frontoffice/FrontSidebarController.java` | Navigation frontoffice |
| **Controller (BO)** | `SidebarController.java` | `src/main/java/piJava/Controllers/backoffice/SidebarController.java` | Navigation backoffice |
| **Controller (FO)** | `FrontController.java` | `src/main/java/piJava/Controllers/frontoffice/FrontController.java` | Main controller FO |
| **Controller (BO)** | `MainController.java` | `src/main/java/piJava/Controllers/backoffice/MainController.java` | Main controller BO |
| **View (FXML)** | `sidebar.fxml` | `src/main/resources/frontoffice/sidebar.fxml` | Interface sidebar FO |
| **View (FXML)** | `sidebar.fxml` | `src/main/resources/backoffice/sidebar.fxml` | Interface sidebar BO |
| **Style (CSS)** | `front-sidebar.css` | `src/main/resources/frontoffice/front-sidebar.css` | Styles sidebar FO |
| **Style (CSS)** | `sidebar.css` | `src/main/resources/backoffice/sidebar.css` | Styles sidebar BO |

---

## 🗃️ Base de Données - Entités (Entities)

| Entity | Chemin | Description |
|--------|--------|-------------|
| **user.java** | `src/main/java/piJava/entities/user.java` | Utilisateurs du système |
| **Seance.java** | `src/main/java/piJava/entities/Seance.java` | Séances (cours, TD, TP, Révision) |
| **Salle.java** | `src/main/java/piJava/entities/Salle.java` | Salles de l'établissement |
| **Matiere.java** | `src/main/java/piJava/entities/Matiere.java` | Matières enseignées |
| **Classe.java** | `src/main/java/piJava/entities/Classe.java` | Classes d'étudiants |
| **tache.java** | `src/main/java/piJava/entities/tache.java` | Tâches personnelles |
| **ObjectifSante.java** | `src/main/java/piJava/entities/ObjectifSante.java` | Objectifs de santé |
| **SuiviBienEtre.java** | `src/main/java/piJava/entities/SuiviBienEtre.java` | Suivi du bien-être |
| **Attendance.java** | `src/main/java/piJava/entities/Attendance.java` | Présences (scan QR code) |
| **preferenceAlerte.java** | `src/main/java/piJava/entities/preferenceAlerte.java` | Préférences d'alertes |
| **suiviTache.java** | `src/main/java/piJava/entities/suiviTache.java` | Suivi des tâches |

---

## 🔧 Services Métier

| Service | Chemin | Responsabilité |
|---------|--------|----------------|
| **UserServices.java** | `src/main/java/piJava/services/UserServices.java` | Gestion utilisateurs |
| **SeanceService.java** | `src/main/java/piJava/services/SeanceService.java` | Gestion séances |
| **SalleService.java** | `src/main/java/piJava/services/SalleService.java` | Gestion salles |
| **MatiereService.java** | `src/main/java/piJava/services/MatiereService.java` | Gestion matières |
| **ClasseService.java** | `src/main/java/piJava/services/ClasseService.java` | Gestion classes |
| **TacheService.java** | `src/main/java/piJava/services/TacheService.java` | Gestion tâches |
| **ObjectifSanteService.java** | `src/main/java/piJava/services/ObjectifSanteService.java` | Gestion objectifs santé |
| **SuiviBienEtreService.java** | `src/main/java/piJava/services/SuiviBienEtreService.java` | Gestion suivi bien-être |
| **AttendanceService.java** | `src/main/java/piJava/services/AttendanceService.java` | Gestion présences |
| **AlerteService.java** | `src/main/java/piJava/services/AlerteService.java` | Gestion alertes |
| **EmailService.java** | `src/main/java/piJava/services/EmailService.java` | Envoi d'emails |
| **GoogleCalendarService.java** | `src/main/java/piJava/services/GoogleCalendarService.java` | Google Calendar API |
| **SuiviTacheService.java** | `src/main/java/piJava/services/SuiviTacheService.java` | Suivi des tâches |
| **loginService.java** | `src/main/java/piJava/services/loginService.java` | Authentification |
| **ICrud.java** | `src/main/java/piJava/services/ICrud.java` | Interface CRUD générique |

---

## 📊 Statistiques du Projet

| Catégorie | Nombre |
|-----------|--------|
| **Contrôleurs** | 40 |
| **Entités** | 11 |
| **Services** | 15 |
| **FXML** | 22 (src) |
| **CSS** | 26 (src) |
| **HTML** | 1 |
| **Total fichiers Java** | 66 |

---

## 🚀 Comment Naviguer

1. **Pour une fonctionnalité donnée** : Consultez la section correspondante pour trouver tous les fichiers associés
2. **Pour modifier l'interface** : Allez dans le fichier FXML et CSS correspondants
3. **Pour modifier la logique** : Allez dans le Controller correspondant
4. **Pour modifier les données** : Allez dans l'Entity et Service correspondants

---

**Dernière mise à jour** : 30 avril 2026
