# Configuration SMTP pour les emails EspritFlow

## Gmail (Recommandé)

Pour utiliser Gmail, vous devez générer un **mot de passe d'application** (app password) :

1. Allez à https://myaccount.google.com/apppasswords
2. Sélectionnez "Mail" et "Windows Computer" (ou votre plateforme)
3. Générez un nouveau mot de passe
4. Copez le mot de passe généré (16 caractères avec espaces)

Configurez ensuite dans `.env` :
```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=votre-email@gmail.com
SMTP_PASSWORD=votre-app-password (sans espaces)
SMTP_FROM=votre-email@gmail.com
SMTP_STARTTLS=true
SMTP_SSL=false
```

## Outlook/Hotmail

```
SMTP_HOST=smtp-mail.outlook.com
SMTP_PORT=587
SMTP_USER=votre-email@outlook.com
SMTP_PASSWORD=votre-mot-de-passe
SMTP_FROM=votre-email@outlook.com
SMTP_STARTTLS=true
SMTP_SSL=false
```

## SendGrid

```
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USER=apikey
SMTP_PASSWORD=SG.votre-api-key
SMTP_FROM=votre-email@votredomaine.com
SMTP_STARTTLS=true
SMTP_SSL=false
```

## Amazon SES

```
SMTP_HOST=email-smtp.region.amazonaws.com
SMTP_PORT=587
SMTP_USER=votre-smtp-username
SMTP_PASSWORD=votre-smtp-password
SMTP_FROM=votre-email@votredomaine.com
SMTP_STARTTLS=true
SMTP_SSL=false
```

## Test rapide

Pour tester votre configuration SMTP sans envoyer d'email :

1. Dans `.env`, mettez une adresse email de test : `SMTP_USER=test@example.com`
2. Appelez la méthode `requestReset()` de `PasswordResetService`
3. Vérifiez les logs de la console pour les erreurs SMTP

Pour envoyer un vrai email :

1. Mettez votre vraie adresse email dans `SMTP_USER`
2. Utilisez votre vrai mot de passe
3. L'application enverra un email au compte demandé

## Dépannage

### "Authentication failed" ou "Invalid credentials"
- Vérifiez que votre mot de passe/token est correct
- Pour Gmail : utilisez le mot de passe d'application, pas votre mot de passe de compte
- Pour SendGrid/SES : vérifiez que votre clé API est valide

### "Connection timed out"
- Vérifiez le HOST SMTP
- Vérifiez le PORT
- Vérifiez que votre pare-feu autorise les connexions sortantes sur ce port

### "550 5.1.1 The email account that you tried to reach does not exist"
- Vérifiez que le destinataire existe
- Vérifiez que votre compte d'envoi peut envoyer des emails

### Les emails n'arrivent pas
- Ils peuvent être bloqués par le filtre anti-spam
- Vérifiez le dossier spam/courrier indésirable du destinataire
- Certains fournisseurs limitent le nombre d'emails par jour/heure


