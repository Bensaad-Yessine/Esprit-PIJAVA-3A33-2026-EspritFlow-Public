# Commentaires détaillés - Attendance.java

## Description générale
Cette entité représente les présences (attendances) dans le système. Elle stocke les informations sur la présence des utilisateurs aux séances, y compris la méthode de vérification (QR Code, manuelle, etc.) et l'horodatage.

---

## Commentaires ligne par ligne

```java
package piJava.entities;
```
**Ligne 1**: Déclaration du package - cette entité appartient au package `piJava.entities` qui contient toutes les classes de données

```java
import java.time.LocalDateTime;
```
**Ligne 2**: Import de `LocalDateTime` pour gérer les dates et heures avec précision (date + heure + fuseau horaire)

```java
public class Attendance {
```
**Ligne 3**: Déclaration de la classe `Attendance` qui représente une présence à une séance

```java
    private Integer id;
```
**Ligne 4**: Identifiant unique de l'enregistrement de présence (clé primaire)

```java
    private Integer seanceId;
```
**Ligne 5**: Identifiant de la séance à laquelle la présence est enregistrée (clé étrangère vers la table seance)

```java
    private Integer userId;
```
**Ligne 6**: Identifiant de l'utilisateur qui est présent (clé étrangère vers la table user)

```java
    private LocalDateTime checkInTime;
```
**Ligne 7**: Date et heure exacte de l'arrivée/pointage de l'utilisateur

```java
    private LocalDateTime checkOutTime;
```
**Ligne 8**: Date et heure de départ de l'utilisateur (optionnel, peut être null si l'utilisateur ne part pas)

```java
    private String verificationMethod;
```
**Ligne 9**: Méthode utilisée pour vérifier la présence (ex: "QR_CODE", "MANUAL", "BIOMETRIC", "GPS")

```java
    private String status;
```
**Ligne 10**: Statut de la présence (ex: "PRESENT", "ABSENT", "LATE", "EXCUSED")

```java
    private String comments;
```
**Ligne 11**: Commentaires ou notes supplémentaires sur cette présence (ex: motif d'absence, remarque du professeur)

```java
    private String location;
```
**Ligne 12**: Lieu où la présence a été enregistrée (ex: coordonnées GPS, nom de la salle)

```java
    private String deviceInfo;
```
**Ligne 13**: Informations sur l'appareil utilisé pour le pointage (ex: type de téléphone, IP address)

```java
    public Attendance() {
```
**Ligne 14**: Constructeur par défaut sans paramètres

```java
    }
```
**Ligne 15**: Fin du constructeur par défaut

```java
    public Attendance(Integer id, Integer seanceId, Integer userId, LocalDateTime checkInTime, 
                     LocalDateTime checkOutTime, String verificationMethod, String status, 
                     String comments, String location, String deviceInfo) {
```
**Ligne 16-17**: Constructeur complet avec tous les paramètres

```java
        this.id = id;
```
**Ligne 18**: Initialise l'identifiant

```java
        this.seanceId = seanceId;
```
**Ligne 19**: Initialise l'ID de la séance

```java
        this.userId = userId;
```
**Ligne 20**: Initialise l'ID de l'utilisateur

```java
        this.checkInTime = checkInTime;
```
**Ligne 21**: Initialise l'heure d'arrivée

```java
        this.checkOutTime = checkOutTime;
```
**Ligne 22**: Initialise l'heure de départ

```java
        this.verificationMethod = verificationMethod;
```
**Ligne 23**: Initialise la méthode de vérification

```java
        this.status = status;
```
**Ligne 24**: Initialise le statut

```java
        this.comments = comments;
```
**Ligne 25**: Initialise les commentaires

```java
        this.location = location;
```
**Ligne 26**: Initialise la localisation

```java
        this.deviceInfo = deviceInfo;
```
**Ligne 27**: Initialise les informations de l'appareil

```java
    }
```
**Ligne 28**: Fin du constructeur complet

```java
    // Getters et Setters
```
**Ligne 29**: Commentaire séparateur pour la section des getters et setters

```java
    public Integer getId() {
```
**Ligne 30**: Getter pour l'ID

```java
        return id;
```
**Ligne 31**: Retourne l'identifiant

```java
    }
```
**Ligne 32**: Fin du getter id

```java
    public void setId(Integer id) {
```
**Ligne 33**: Setter pour l'ID

```java
        this.id = id;
```
**Ligne 34**: Définit l'identifiant

```java
    }
```
**Ligne 35**: Fin du setter id

```java
    public Integer getSeanceId() {
```
**Ligne 36**: Getter pour l'ID de la séance

```java
        return seanceId;
```
**Ligne 37**: Retourne l'identifiant de la séance

```java
    }
```
**Ligne 38**: Fin du getter seanceId

```java
    public void setSeanceId(Integer seanceId) {
```
**Ligne 39**: Setter pour l'ID de la séance

```java
        this.seanceId = seanceId;
```
**Ligne 40**: Définit l'identifiant de la séance

```java
    }
```
**Ligne 41**: Fin du setter seanceId

```java
    public Integer getUserId() {
```
**Ligne 42**: Getter pour l'ID de l'utilisateur

```java
        return userId;
```
**Ligne 43**: Retourne l'identifiant de l'utilisateur

```java
    }
```
**Ligne 44**: Fin du getter userId

```java
    public void setUserId(Integer userId) {
```
**Ligne 45**: Setter pour l'ID de l'utilisateur

```java
        this.userId = userId;
```
**Ligne 46**: Définit l'identifiant de l'utilisateur

```java
    }
```
**Ligne 47**: Fin du setter userId

```java
    public LocalDateTime getCheckInTime() {
```
**Ligne 48**: Getter pour l'heure d'arrivée

```java
        return checkInTime;
```
**Ligne 49**: Retourne la date et heure de pointage d'arrivée

```java
    }
```
**Ligne 50**: Fin du getter checkInTime

```java
    public void setCheckInTime(LocalDateTime checkInTime) {
```
**Ligne 51**: Setter pour l'heure d'arrivée

```java
        this.checkInTime = checkInTime;
```
**Ligne 52**: Définit la date et heure de pointage d'arrivée

```java
    }
```
**Ligne 53**: Fin du setter checkInTime

```java
    public LocalDateTime getCheckOutTime() {
```
**Ligne 54**: Getter pour l'heure de départ

```java
        return checkOutTime;
```
**Ligne 55**: Retourne la date et heure de pointage de départ

```java
    }
```
**Ligne 56**: Fin du getter checkOutTime

```java
    public void setCheckOutTime(LocalDateTime checkOutTime) {
```
**Ligne 57**: Setter pour l'heure de départ

```java
        this.checkOutTime = checkOutTime;
```
**Ligne 58**: Définit la date et heure de pointage de départ

```java
    }
```
**Ligne 59**: Fin du setter checkOutTime

```java
    public String getVerificationMethod() {
```
**Ligne 60**: Getter pour la méthode de vérification

```java
        return verificationMethod;
```
**Ligne 61**: Retourne la méthode utilisée pour la vérification (QR_CODE, MANUAL, etc.)

```java
    }
```
**Ligne 62**: Fin du getter verificationMethod

```java
    public void setVerificationMethod(String verificationMethod) {
```
**Ligne 63**: Setter pour la méthode de vérification

```java
        this.verificationMethod = verificationMethod;
```
**Ligne 64**: Définit la méthode de vérification

```java
    }
```
**Ligne 65**: Fin du setter verificationMethod

```java
    public String getStatus() {
```
**Ligne 66**: Getter pour le statut

```java
        return status;
```
**Ligne 67**: Retourne le statut de la présence (PRESENT, ABSENT, LATE, EXCUSED)

```java
    }
```
**Ligne 68**: Fin du getter status

```java
    public void setStatus(String status) {
```
**Ligne 69**: Setter pour le statut

```java
        this.status = status;
```
**Ligne 70**: Définit le statut de la présence

```java
    }
```
**Ligne 71**: Fin du setter status

```java
    public String getComments() {
```
**Ligne 72**: Getter pour les commentaires

```java
        return comments;
```
**Ligne 73**: Retourne les commentaires associés à cette présence

```java
    }
```
**Ligne 74**: Fin du getter comments

```java
    public void setComments(String comments) {
```
**Ligne 75**: Setter pour les commentaires

```java
        this.comments = comments;
```
**Ligne 76**: Définit les commentaires associés à cette présence

```java
    }
```
**Ligne 77**: Fin du setter comments

```java
    public String getLocation() {
```
**Ligne 78**: Getter pour la localisation

```java
        return location;
```
**Ligne 79**: Retourne le lieu où la présence a été enregistrée

```java
    }
```
**Ligne 80**: Fin du getter location

```java
    public void setLocation(String location) {
```
**Ligne 81**: Setter pour la localisation

```java
        this.location = location;
```
**Ligne 82**: Définit le lieu de la présence

```java
    }
```
**Ligne 83**: Fin du setter location

```java
    public String getDeviceInfo() {
```
**Ligne 84**: Getter pour les informations de l'appareil

```java
        return deviceInfo;
```
**Ligne 85**: Retourne les informations sur l'appareil utilisé

```java
    }
```
**Ligne 86**: Fin du getter deviceInfo

```java
    public void setDeviceInfo(String deviceInfo) {
```
**Ligne 87**: Setter pour les informations de l'appareil

```java
        this.deviceInfo = deviceInfo;
```
**Ligne 88**: Définit les informations sur l'appareil utilisé

```java
    }
```
**Ligne 89**: Fin du setter deviceInfo

```java
    @Override
    public String toString() {
```
**Ligne 90-91**: Redéfinition de la méthode toString() pour afficher l'objet

```java
        return "Attendance{" +
```
**Ligne 92**: Début de la chaîne de caractères de retour

```java
                "id=" + id +
```
**Ligne 93**: Ajoute l'ID à la chaîne

```java
                ", seanceId=" + seanceId +
```
**Ligne 94**: Ajoute l'ID de la séance

```java
                ", userId=" + userId +
```
**Ligne 95**: Ajoute l'ID de l'utilisateur

```java
                ", checkInTime=" + checkInTime +
```
**Ligne 96**: Ajoute l'heure d'arrivée

```java
                ", checkOutTime=" + checkOutTime +
```
**Ligne 97**: Ajoute l'heure de départ

```java
                ", verificationMethod='" + verificationMethod + '\'' +
```
**Ligne 98**: Ajoute la méthode de vérification

```java
                ", status='" + status + '\'' +
```
**Ligne 99**: Ajoute le statut

```java
                ", comments='" + comments + '\'' +
```
**Ligne 100**: Ajoute les commentaires

```java
                ", location='" + location + '\'' +
```
**Ligne 101**: Ajoute la localisation

```java
                ", deviceInfo='" + deviceInfo + '\'' +
```
**Ligne 102**: Ajoute les infos appareil

```java
                '}';
```
**Ligne 103**: Ferme l'accolade

```java
    }
```
**Ligne 104**: Fin de la méthode toString()

```java
}
```
**Ligne 105**: Fin de la classe Attendance

---

## Résumé des fonctionnalités clés

1. **Enregistrement de présence**: Stocke les présences des utilisateurs aux séances
2. **Horodatage précis**: Utilise LocalDateTime pour des dates/heures précises
3. **Méthodes de vérification**: Supporte différents modes (QR Code, manuelle, biométrique, GPS)
4. **Statuts variés**: PRESENT, ABSENT, LATE (retard), EXCUSED (excusé)
5. **Traçabilité**: Enregistre la localisation et l'appareil utilisé
6. **Commentaires**: Permet d'ajouter des notes sur chaque présence
7. **Check-out**: Supporte l'enregistrement de départ en plus de l'arrivée

---

## Utilisation typique

```java
// Création d'une nouvelle présence
Attendance attendance = new Attendance();
attendance.setSeanceId(123);
attendance.setUserId(456);
attendance.setCheckInTime(LocalDateTime.now());
attendance.setVerificationMethod("QR_CODE");
attendance.setStatus("PRESENT");
attendance.setLocation("Salle A123");
attendance.setDeviceInfo("iPhone 14, 192.168.1.100");

// Vérification si l'utilisateur est en retard
if (attendance.getCheckInTime().isAfter(seance.getStartTime().plusMinutes(15))) {
    attendance.setStatus("LATE");
}
```
