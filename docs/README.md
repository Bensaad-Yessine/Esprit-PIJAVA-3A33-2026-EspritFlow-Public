# 📚 Documentation EspritFlow - Index

Bienvenue dans la documentation technique d'EspritFlow ! Ce dossier contient des descriptions très détaillées de chaque fonctionnalité avancée du système.

---

## 📋 Liste des Fonctionnalités Documentées

### 1. 📄 [QR Code](./01_QR_CODE_FEATURE.md)
**Système de génération et validation de QR codes pour la présence**

- **Technologies** : ZXing (Zebra Crossing), Java AWT, JDBC
- **Fonctionnalités** :
  - Génération de QR codes uniques par séance
  - Tokens UUID sécurisés
  - Dates d'expiration (15 min après fin)
  - Validation avec géolocalisation
  - Protection contre réutilisation
- **Points clés** : Correction d'erreur Medium (15%), Cache d'images, Compression

---

### 2. 📊 [Statistiques Front Office](./02_STATISTIQUES_FRONT.md)
**Tableau de bord interactif avec KPIs et graphiques**

- **Technologies** : JavaFX, BarChart, Stream API, iText
- **Fonctionnalités** :
  - KPIs : Séances, Matières, Salles, Heures
  - Analyse de charge par jour
  - Graphique en barres par jour
  - Calendrier hebdomadaire interactif
  - Navigation temporelle (semaines)
  - Export PDF
- **Points clés** : Stream API, Lazy Loading, Caching

---

### 3. 📄 [Export PDF de l'Emploi du Temps](./03_EXPORT_PDF_EMPLOI.md)
**Génération de PDFs haute qualité à partir de l'interface**

- **Technologies** : iText, JavaFX Snapshot API, SwingFXUtils
- **Fonctionnalités** :
  - Capture d'écran JavaFX
  - Conversion image → PDF
  - Marges automatiques
  - En-têtes et pieds de page personnalisables
  - Compression optionnelle
  - Sauvegarde dans Downloads
- **Points clés** : Mode Paysage, Qualité PNG, Asynchrone

---

### 4. 📧 [Système de Mailing](./04_MAILING_SYSTEM.md)
**Envoi d'emails de notification et rappels**

- **Technologies** : JavaMail API, SMTP, TLS/SSL, HTML Templates
- **Fonctionnalités** :
  - Envoi d'emails via SMTP
  - Templates HTML dynamiques
  - Pièces jointes (MimeMultipart)
  - Rappels automatiques planifiés
  - Gestion des logs d'emails
  - Queue d'envoi asynchrone
- **Points clés** : OAuth 2.0, Placeholders, Monitoring

---

### 5. 📅 [Ajout de Séance à l'Agenda](./05_AJOUT_SEANCE_AGENDA.md)
**Synchronisation avec Google Calendar et export ICS**

- **Technologies** : iCal4j, Google Calendar API, OAuth 2.0
- **Fonctionnalités** :
  - Génération de fichiers ICS (format universel)
  - Intégration Google Calendar API
  - Synchronisation bidirectionnelle
  - Rappels email + popup
  - OAuth 2.0 pour authentification
  - Export en masse (semaine entière)
- **Points clés** : RFC 5545, Refresh Tokens, iCal4j

---

### 6. 🗺️ [Google Maps Intégrée](./06_GOOGLE_MAPS_FEATURE.md)
**Carte interactive avec géolocalisation et réservation**

- **Technologies** : Google Maps JavaScript API, HTML5 Geolocation, JavaFX WebView
- **Fonctionnalités** :
  - Affichage de la carte du campus ESPRIT
  - Marqueurs pour chaque bloc
  - Géolocalisation en temps réel de l'utilisateur
  - Marqueur bleu pour position actuelle
  - Mise à jour automatique (watchPosition)
  - Sélection de bloc sur la carte
  - Réservation de salles depuis la carte
  - Contournement bug JavaFX (OSM tiles)
- **Points clés** : JSObject Bridge, Symboles SVG, OpenStreetMap

---

### 7. 🤖 [Optimisation des Salles par IA](./07_OPTIMISATION_SALLES_IA.md)
**Recommandation intelligente de salles avec Machine Learning**

- **Technologies** : Weka (ML Framework), KNN, Régression Linéaire, Clustering
- **Fonctionnalités** :
  - Algorithmes de Machine Learning (KNN, K-Means)
  - Scoring multi-critères pondéré
  - Critères : Capacité, Disponibilité, Préférences, Équipement, Proximité
  - Apprentissage par feedback utilisateur
  - Recommandation basée sur similarité (KNN)
  - Prédiction de charge de salle
  - Cache des scores pour performances
- **Points clés** : Formule Haversine (distance), Weka, Pondération

---

## 🔍 Comment Utiliser la Documentation

### Pour les Développeurs
Chaque fichier contient :
1. **Vue d'ensemble** : Description de la fonctionnalité
2. **Technologies** : Liste complète des outils et bibliothèques
3. **Architecture** : Schémas de base de données et flux de données
4. **Implémentation** : Code Java détaillé avec explications
5. **Explication du code** : Commentaires pour chaque bloc
6. **Performance** : Optimisations et best practices

### Pour les Architectes
- Consulter les schémas de base de données
- Analyser les flux de traitement
- Comprendre l'architecture globale

### Pour les Étudiants
- Apprendre les technologies utilisées
- Comprendre les algorithmes (IA, géolocalisation)
- Voir des exemples de code réel

---

## 🛠️ Technologies Utilisées dans EspritFlow

### Backend Java
- **Java 8+** : Langage principal
- **JavaFX 21** : Interface graphique
- **JDBC** : Accès base de données
- **Weka** : Machine Learning
- **iText** : Génération PDF
- **JavaMail** : Envoi d'emails
- **iCal4j** : Fichiers ICS
- **ZXing** : QR codes

### Frontend
- **JavaScript** : Google Maps API, Geolocation
- **HTML5** : Carte interactive
- **CSS3** : Styling

### Protocoles et Standards
- **SMTP** : Envoi d'emails
- **TLS/SSL** : Chiffrement
- **OAuth 2.0** : Authentification Google
- **ICS (RFC 5545)** : Format calendrier
- **QR Code** : Standard ISO/IEC 18004

### Bases de Données
- **MySQL** : SGBD principal
- **SQL** : Requêtes

---

## 📊 Statistiques de la Documentation

| Fichier | Lignes | Complexité | Niveau |
|---------|--------|------------|--------|
| QR Code | ~350 | ★★★☆☆ | Avancé |
| Statistiques Front | ~450 | ★★★★☆ | Intermédiaire |
| Export PDF | ~400 | ★★☆☆☆ | Intermédiaire |
| Mailing | ~500 | ★★★★☆ | Avancé |
| Ajout Agenda | ~450 | ★★★★☆ | Avancé |
| Google Maps | ~400 | ★★★☆☆ | Intermédiaire |
| Optimisation IA | ~550 | ★★★★★ | Expert |

**Total** : ~3100 lignes de documentation technique

---

## 🎯 Points Forts d'EspritFlow

✅ **Intégration Multi-Plateforme** : Google Calendar, iOS Calendar, Outlook
✅ **IA Intelligente** : Recommandation de salles avec Machine Learning
✅ **Expérience Utilisateur** : Géolocalisation en temps réel, notifications
✅ **Flexibilité** : Export PDF, ICS, synchronisation
✅ **Sécurité** : QR codes uniques, OAuth 2.0, tokens sécurisés
✅ **Performance** : Caching, Lazy Loading, Queue asynchrone
✅ **Accessibilité** : Mobile-first, responsive design

---

## 🔗 Ressources Externes

### Documentation Officielle
- **JavaFX** : https://openjfx.io/
- **Weka** : https://www.cs.waikato.ac.nz/ml/weka/
- **Google Maps API** : https://developers.google.com/maps
- **Google Calendar API** : https://developers.google.com/calendar
- **iText PDF** : https://itextpdf.com/
- **JavaMail** : https://javaee.github.io/javamail/
- **iCal4j** : https://github.com/ical4j/ical4j
- **ZXing** : https://github.com/zxing/zxing

### Standards
- **RFC 5545** : iCalendar
- **RFC 5321** : SMTP
- **ISO/IEC 18004** : QR Code

---

## 📝 Notes de Version

- **Version Documentation** : 1.0
- **Date de création** : 30 Avril 2026
- **Auteur** : EspritFlow Development Team
- **Projet** : Esprit-PIJAVA-3A33-2026

---

## 🤝 Contribution

Cette documentation est générée pour aider les développeurs à comprendre le code source d'EspritFlow. Pour toute question ou suggestion, contactez l'équipe de développement.

---

**Fin de la documentation** 📚

*EspritFlow - Gestion Universitaire Intelligente*
