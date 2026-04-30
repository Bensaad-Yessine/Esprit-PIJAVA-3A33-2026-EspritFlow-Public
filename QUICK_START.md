# Quick Start - Email Templates

## Configuration Rapide

### 1. Mettre à jour `.env`
```bash
# Ajouter ou mettez à jour les lignes SMTP
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_FROM=your-email@gmail.com
SMTP_STARTTLS=true
SMTP_SSL=false
```

### 2. Tester les Templates

#### Test 1: Réinitialisation de Mot de Passe
```java
// Dans l'application, cliquez sur "Forgot password?" et suivez le flux
// Ou testez directement :
PasswordResetService service = new PasswordResetService();
service.requestReset("test@example.com");
```

#### Test 2: Bannissement d'Utilisateur
```java
// Bannissez un utilisateur depuis l'admin
UserServices userService = new UserServices();
userService.banUser(1, "Spam ou harcèlement");
// L'email est envoyé automatiquement
```

#### Test 3: Déblocage d'Utilisateur
```java
// Débloquz un utilisateur depuis l'admin
userService.unbanUser(1);
// L'email de succès est envoyé automatiquement
```

## Email Templates

### 🔐 Password Reset (Réinitialisation)
- **Couleur** : Rouge (#dc2626)
- **Déclencheur** : "Forgot password?"
- **Contenu** : Code + instructions + sécurité
- **Durée** : 30 minutes

### ⚠️ Ban Notification (Bannissement)
- **Couleur** : Rouge (#dc2626)
- **Déclencheur** : Admin bannit utilisateur
- **Contenu** : Raison + conséquences + appel
- **Auto** : Envoyé automatiquement

### ✅ Unban Notification (Déblocage)
- **Couleur** : Vert (#16a34a)
- **Déclencheur** : Admin débloque utilisateur
- **Contenu** : Confirmation + actions possibles
- **Auto** : Envoyé automatiquement

## Troubleshooting

| Problème | Solution |
|----------|----------|
| Email non envoyé | Vérifiez SMTP_HOST, PORT, USER, PASSWORD dans `.env` |
| Gmail rejette | Générez app password (pas mot de passe de compte) |
| Caractères mal affichés | UTF-8 doit être activé, c'est le cas par défaut |
| Template vide | Vérifiez que MailService compile sans erreur |
| Port SMTP bloqué | Vérifiez pare-feu, essayez port 465 pour SSL |

## Fichiers à Connaître

```
pijava/
├── src/main/java/piJava/services/
│   ├── PasswordResetService.java      ← Password reset template
│   ├── BanNotificationService.java    ← Ban notification (NOUVEAU)
│   ├── UserServices.java              ← Ban/unban avec emails
│   └── MailService.java               ← Envoi SMTP
├── EMAIL_TEMPLATES_README.md          ← Documentation technique
├── email-templates-preview.html       ← Aperçu visuel
├── SMTP_CONFIGURATION.md              ← Guide SMTP
└── IMPLEMENTATION_SUMMARY.md          ← Résumé complet
```

## Checklist

- [ ] `.env` configuré avec SMTP credentials
- [ ] Teste Password Reset
- [ ] Teste Ban Notification
- [ ] Teste Unban Notification
- [ ] Tous les emails arrivent
- [ ] Templates s'affichent bien
- [ ] Caractères français OK

## Support

Pour des questions ou des problèmes :
1. Consultez `EMAIL_TEMPLATES_README.md`
2. Consultez `SMTP_CONFIGURATION.md`
3. Consultez `IMPLEMENTATION_SUMMARY.md`
4. Ouvrez `email-templates-preview.html` pour voir le design
5. Exécutez `BanNotificationServiceTest.java` pour valider

---

**Besoin d'aide ?** Tous les fichiers de documentation sont dans le dossier racine du projet.

