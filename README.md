# EspritFlow Front Office - Classe connectée

## Fonctionnalité ajoutée

Dans le Front Office, le bouton **Classe** charge désormais un écran dédié qui :

- récupère l’utilisateur connecté via `SessionManager`
- lit son `classe_id`
- charge la classe correspondante depuis la base
- charge les matières liées à cette classe via la relation `Classe -> Matiere`
- affiche :
  - le nom de la classe
  - le niveau
  - l’année universitaire
  - la liste des matières associées

## Fichiers principaux

- `src/main/java/piJava/Controllers/frontoffice/FrontSidebarController.java`
- `src/main/java/piJava/Controllers/frontoffice/classe/ClasseContentController.java`
- `src/main/java/piJava/services/MatiereService.java`
- `src/main/resources/frontoffice/classe/classe-content.fxml`

## Navigation

Le bouton **Classe** du sidebar Front Office ouvre :

`/frontoffice/classe/classe-content.fxml`

## Configuration base de données

1. Copiez `/.env.example` vers `/.env`.
2. Renseignez vos vraies valeurs dans `/.env`.

Par défaut, l'application essaie de se connecter à :

- URL : `jdbc:mysql://localhost:3306/pidev`
- Utilisateur : `root`
- Mot de passe : vide

Si votre MySQL utilise d'autres paramètres, vous pouvez les fournir via :

- variables/clé `.env` : `DB_URL`, `DB_USER`, `DB_PASSWORD`
- variables d'environnement système : `DB_URL`, `DB_USER`, `DB_PASSWORD`
- propriétés JVM : `-DDB_URL=...`, `-DDB_USER=...`, `-DDB_PASSWORD=...`

## Configuration Google OAuth

Les identifiants Google sont lus depuis `/.env` :

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_OAUTH_PORT` (optionnel, défaut `8001`, avec secours automatique sur un autre port libre)

## Abonnements et paiement Stripe

Le Front Office contient désormais un écran **Abonnements** qui lit la table `subscription_pack` et ouvre un paiement Stripe Checkout.

Devises supportées par l'application et Stripe :

- `EUR`
- `USD`

Si vos packs existants utilisent encore `TND`, mettez-les à jour dans MySQL avant le paiement :

```sql
UPDATE subscription_pack SET currency = 'EUR' WHERE currency = 'TND';
```

Variables à ajouter dans `/.env` :

- `STRIPE_SECRET_KEY`
- `STRIPE_PUBLISHABLE_KEY`
- `STRIPE_WEBHOOK_SECRET`

Navigation Front Office :

- bouton **Abonnements** du sidebar
- vue chargée : `/frontoffice/subscription/subscription-content.fxml`

> Remarque : la clé secrète Stripe est utilisée côté client JavaFX pour créer la session Checkout. Pour une mise en production, il est préférable de déléguer la création de session à un backend.

## Mot de passe oublié

Le lien **Forgot password?** de l'écran de login ouvre un flux de réinitialisation par e-mail :

1. l'utilisateur saisit son e-mail
2. l'application crée une demande dans `reset_password_request`
3. un code de réinitialisation est envoyé par e-mail
4. l'utilisateur saisit le code + le nouveau mot de passe

Fichiers principaux :

- `src/main/java/piJava/Controllers/ForgotPasswordController.java`
- `src/main/java/piJava/Controllers/ResetPasswordController.java`
- `src/main/java/piJava/services/PasswordResetService.java`
- `src/main/java/piJava/services/MailService.java`
- `src/main/resources/forgot-password.fxml`
- `src/main/resources/reset-password.fxml`

Variables SMTP à ajouter dans `/.env` :

- `SMTP_HOST`
- `SMTP_PORT`
- `SMTP_USER`
- `SMTP_PASSWORD`
- `SMTP_FROM`
- `SMTP_STARTTLS`
- `SMTP_SSL`

Le design des écrans de réinitialisation réutilise `login.css` pour rester identique à l'interface de login.

## Templates d'Email

L'application envoie des emails HTML avec un design cohérent en rouge et blanc pour plusieurs événements :

### 1. Email de Réinitialisation de Mot de Passe
- **Déclenché** : quand l'utilisateur demande la réinitialisation
- **Design** : en-tête rouge gradient, code de réinitialisation affiché en boîte surlignée
- **Contenu** : instructions étape par étape, avertissement d'expiration (30 min)
- **Fichier** : `PasswordResetService.java`

### 2. Email de Notification de Bannissement
- **Déclenché** : quand un administrateur bannit un utilisateur
- **Design** : en-tête rouge gradient, avertissement en gras
- **Contenu** : motif de suspension, conséquences, lien pour contester
- **Fichier** : `BanNotificationService.java` (nouveau)

### 3. Email de Déblocage de Compte
- **Déclenché** : quand un administrateur débloque un utilisateur banni
- **Design** : en-tête vert gradient, message de succès
- **Contenu** : confirmation que le compte est réactivé, lien pour se connecter
- **Fichier** : `UserServices.java` (méthode `unbanUser()`)

**Voir aussi** : `EMAIL_TEMPLATES_README.md` et `email-templates-preview.html` pour des détails techniques et un aperçu visuel.

