# Esprit Flow – Application Desktop JavaFX

## Présentation générale

**Esprit Flow** est une application Java / JavaFX conçue pour gérer et améliorer l’expérience pédagogique au sein d’une école. Elle regroupe un ensemble de modules pour l’administration et les étudiants : gestion des cours et des classes, tâches et notifications, groupes, objectifs de santé, suivi bien‑être, abonnement et paiement, ainsi qu’un assistant intelligent. Le projet s’interface avec plusieurs services web (Groq, OpenWeatherMap, Spoonacular, WorkoutAPI, Stripe, Brevo, RapidAPI, Google OAuth) pour enrichir l’expérience utilisateur et offrir des conseils personnalisés.

## Objectifs et principes

Le but d’Esprit Flow est de proposer une application tout‑en‑un où l’administrateur peut gérer l’ensemble des ressources (profils, classes, matières, salles, sessions, tâches, alertes, groupes, abonnements…), et où les étudiants disposent d’un tableau de bord complet : emploi du temps, tâches, groupes, propositions de réunions, objectifs de santé, recettes et exercices suggérés, suivi bien‑être, cours en ligne et chatbot. L’architecture repose sur des services spécialisés, chaque module étant découpé en entités (modèles), services (logique métier et appels API) et contrôleurs (logique UI et interaction avec FXML).

## Fonctionnalités principales

### Authentification et sécurité

- **Inscription & connexion** : gestion des utilisateurs avec hachage de mot de passe (BCrypt) et vérification des rôles (admin, enseignant, étudiant). 
- **OAuth Google** : intégration du service `GoogleAuthService` pour se connecter via Google. 
- **Réinitialisation de mot de passe** : envoi d’e‑mails de réinitialisation via Brevo et sécurité par jeton. 
- **Gestion des sanctions** : `BanNotificationService` envoie des notifications de bannissement/debannissement par e‑mail.

### Back‑office (administration)

L’interface d’administration permet de gérer toutes les données : 

- **Utilisateurs, classes, matières, salles et séances**.  
- **Tâches et préférences d’alerte** (rappels programmés).  
- **Groupes et propositions de réunion**.  
- **Objectifs de santé et suivi bien‑être**.  
- **Abonnements et packs** (avec intégration Stripe pour le paiement).  
- **Quiz/questions** et **posts de groupes**.  
- **Dashboard** avec statistiques et filtrage.

### Front‑office (étudiant)

- **Tableau de bord** personnel avec aperçu des tâches, du calendrier et des notifications.  
- **Profil** : affichage et édition des informations personnelles.  
- **Emploi du temps** : affichage des séances planifiées.  
- **Tâches** : création, modification et suivi des tâches ; prédiction de réussite grâce à un modèle ML local.  
- **Groupes** : rejoindre/créer des groupes, publier des posts et planifier des réunions.  
- **Objectifs et suivi santé** : définir des objectifs (hydratation, sommeil, sport), suivre ses progrès et recevoir des conseils via IA.  
- **Recettes & Exercices** : suggestions de recettes via Spoonacular et exercices via WorkoutAPI.  
- **Assistant de cours** : recherche de cours en ligne via l’API Udemy et système de streaks/achievements.  
- **Chatbot** : conversation en langage naturel avec un modèle GPT via RapidAPI.  
- **Abonnement** : achat de packs premium via Stripe Checkout et suivi de l’abonnement dans l’application.

### Modules santé et bien‑être

Ce module propose : 

- **Objectifs personnalisés** (eau, sommeil, activité physique, alimentation).  
- **Suivi des objectifs** avec graphiques et génération automatique de PDF via iText.  
- **Coaching IA** : le service `CoachingPriveService` appelle l’API Groq pour générer des conseils personnalisés (sport, alimentation, organisation).  
- **Recettes nutritives** : requêtes à l’API Spoonacular pour rechercher des recettes santé.  
- **Exercices sportifs** : utilisation de l’API WorkoutAPI pour obtenir des exercices filtrés par muscle, équipement et niveau.  
- **Conseils météo** : `WeatherAiService` interroge OpenWeatherMap pour récupérer la météo et génère des recommandations d’habillement et de planning d’étude via un appel Groq.

### Smart Assistant (cours et recommandations)

Le module Smart Assistant permet aux étudiants de rechercher des cours en ligne, de s’inscrire et de suivre leur progression :

- **Recherche de cours** : `CourseService` interroge l’API Udemy (via RapidAPI) pour trouver des cours selon un mot‑clé.  
- **Gestion des inscriptions** : `UserCourseService` gère l’inscription et l’avancement dans les cours.  
- **Streaks & Achievements** : `StreakService` et `AchievementService` motivent l’utilisateur avec des séries d’actions continues et des trophées.  
- **Limitation de débit** : `RateLimitService` protège l’API contre un trop grand nombre de requêtes.  
- **Recommandations personnalisées** : `RecommendationService` et `StudentIntelligenceProfileService` suggèrent des cours en se basant sur le style d’apprentissage de l’étudiant.

### Analyses et prédictions

- **Analyse de comportement** : `BehaviorAnalysisService` calcule des statistiques sur les tâches (durée moyenne, deadlines, avancement) et interroge l’API Groq pour générer un rapport et des conseils.  
- **Prédiction de tâche** : `TaskPredictionService` appelle un serveur Flask local qui retourne la probabilité de réussite d’une tâche (modèle ML).  
- **Notification des délais** : `NotifsService` envoie des rappels et des notifications par e‑mail (Brevo) pour les tâches en retard ou à venir.  
- **Face Recognition** : `FaceRecognitionService` compare les visages à partir d’images pour vérifier l’identité.  
- **Weather AI** : `WeatherAiService` combine la météo d’OpenWeatherMap et un modèle Groq pour proposer une tenue et un planning d’étude en fonction de la météo du jour.

### Chatbot & notifications

- **Chat en langage naturel** : `AcademicChatService` utilise RapidAPI pour envoyer les messages de l’étudiant à un modèle GPT‑4 et afficher les réponses.  
- **Notifications en temps réel** : ajout de notifications dans l’application (rappels, invitations, nouveaux messages) et envoi d’e‑mails via `BrevoEmailService`.

### Abonnements & paiements

- **Packs d’abonnement** : chargement des packs depuis la base de données (gratuit, premium).  
- **Paiement Stripe** : `StripeCheckoutService` crée une session de paiement Stripe et redirige l’utilisateur vers une page sécurisée.  Le processus s’appuie sur la création d’une *Checkout Session* et sur une page hébergée par Stripe où l’utilisateur saisit ses informations de paiement et est renvoyé ensuite vers l’application.  
- **Suivi et renouvellement** : enregistrement de l’abonnement et date d’expiration dans la base de données et rappels automatiques.

### Autres services intégrés

- **Reconnaissance faciale** : comparaison des visages pour la validation d’identité.  
- **Profils d’intelligence de l’étudiant** : le service `StudentIntelligenceProfileService` analyse les préférences d’apprentissage pour proposer des recommandations adaptées.  
- **Archivage PDF** : génération de rapports et d’archives PDF avec `PdfArchiveObjectifService` (iText).  
- **Gestion des posts de groupe et des commentaires** : création, modification et suppression avec notifications.  
- **Module quiz** : création de questionnaires, gestion des questions et évaluation.

## Services API externes utilisés

| API / service | Utilisation dans l’application | Commentaire |
|--------------|--------------------------------|-------------|
| **Groq** | Génération de texte pour le coaching privé et les analyses de comportement. Les requêtes sont envoyées à l’endpoint `POST https://api.groq.com/openai/v1/chat/completions`. | Nécessite `GROQ_API_KEY` et sélection d’un modèle (par ex. `llama‑3‑8b`). |
| **OpenWeatherMap** | Récupération de la météo actuelle via l’endpoint `api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}`:contentReference[oaicite:1]{index=1} pour générer des conseils d’habillement et d’étude. | Variables `OPENWEATHER_API_KEY`, `OPENWEATHER_CITY`. |
| **Spoonacular** | Recherche de recettes saines par mot‑clé, calories ou temps de préparation. L’API fournit un moteur de recherche sémantique performant et rapide:contentReference[oaicite:2]{index=2}. | Requiert `SPOONACULAR_API_KEY`. |
| **Workout API** | Obtention de programmes d’exercices filtrés par muscle, équipement et intensité ; l’API offre une base riche et une intégration simple sans module supplémentaire:contentReference[oaicite:3]{index=3}. | Requiert `WORKOUT_API_KEY`. |
| **Stripe Checkout** | Paiement sécurisé des abonnements : l’application crée une *Checkout Session*, obtient une URL Stripe, l’utilisateur saisit ses informations sur la page Stripe, puis est redirigé vers l’application:contentReference[oaicite:4]{index=4}. | Variables `STRIPE_PUBLIC_KEY`, `STRIPE_SECRET_KEY`. |
| **Brevo (ex‑Sendinblue)** | Envoi d’e‑mails : notifications de tâches, réinitialisations de mot de passe, ban/unban, PDF en pièces jointes. | Requiert `BREVO_API_KEY` et configuration SMTP. |
| **RapidAPI / ChatGPT** | Interface de chat : envoi de messages à un modèle GPT‑4 via RapidAPI. | Requiert `RAPIDAPI_KEY`. |
| **Google OAuth** | Authentification par compte Google. Nécessite la configuration d’un client OAuth (ID et secret). | Variables `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`. |
| **Udemy (via RapidAPI)** | Recherche de cours en ligne pour le Smart Assistant. | Requiert `UDEMY_API_KEY` et `RAPIDAPI_KEY`. |
| **Serveur ML local (Flask)** | Prédiction de réussite des tâches avec un modèle machine learning. | Utilise un endpoint local. |

## Architecture technique

Le projet est construit en **Java 17** et **JavaFX 21**. L’architecture suit un découpage MVC/MVVM :

- **Entities (models)** : classes représentant les tables de la base de données (User, Task, Group, Objective…).
- **Services** : couches métier responsables des opérations CRUD, des interactions avec la base MySQL (via JDBC) et des appels aux APIs externes.
- **Controllers** : classes JavaFX qui lient l’interface FXML au code et gèrent les actions utilisateur.
- **Interfaces FXML** et **styles CSS** : définition des vues et de l’apparence graphique.
- **Utilities et helpers** : classes utilitaires (validation, cryptage, envoi d’e‑mail, etc.).

Le projet utilise **Maven** pour la gestion des dépendances et s’appuie sur **MySQL** pour persister les données. Le dossier `src/main/java` est organisé en paquets logiques (`controllers`, `entities`, `services`, `api`, `utils`, `mains`). Les vues FXML et les fichiers de ressources (images, CSS) se trouvent dans `src/main/resources`.

## Prérequis et installation

1. **Installer Java 17** et **JavaFX 21**.  
2. **Cloner** le dépôt : `git clone <url>` et se placer dans le répertoire.  
3. **Configurer MySQL** : créer une base de données (par ex. `espritflow`), définir l’utilisateur et le mot de passe dans le fichier `.env`. Importer les scripts SQL fournis si nécessaire.  
4. **Créer le fichier `.env`** à la racine du projet avec les variables suivantes :

   ```env
   DB_URL=jdbc:mysql://localhost:3306/espritflow
   DB_USER=utilisateur
   DB_PASSWORD=motdepasse

   GROQ_API_KEY=XXX
   GROQ_MODEL=llama-3-8b
   OPENWEATHER_API_KEY=XXX
   OPENWEATHER_CITY=Tunis
   SPOONACULAR_API_KEY=XXX
   WORKOUT_API_KEY=XXX
   BREVO_API_KEY=XXX
   BREVO_SENDER=adresse@email.com
   RAPIDAPI_KEY=XXX
   UDEMY_API_KEY=XXX
   STRIPE_PUBLIC_KEY=XXX
   STRIPE_SECRET_KEY=XXX
   GOOGLE_CLIENT_ID=XXX
   GOOGLE_CLIENT_SECRET=XXX
   # autres variables (MAIL_USER, MAIL_PASSWORD, etc.)
