# 🗳️ Système de Vote - Guide d'Utilisation

## Vue d'ensemble
Système complet de vote pour les propositions de réunion dans l'application EspritFlow.

### Caractéristiques
✅ 1 seul vote par utilisateur par proposition  
✅ Vote modifiable (l'utilisateur peut changer son vote)  
✅ Abstentions exclues du calcul de majorité  
✅ Vote automatiquement désactivé après `dateFinVote`  
✅ Calcul automatique du statut de la proposition  
✅ Interface visuelle intuitive avec résultats en temps réel  

---

## Règles Métier

### Types de Vote
- **👍 Pour** — Vota pour la proposition
- **👎 Contre** — Vote contre la proposition  
- **⊘ Abstention** — N'affecte pas le calcul de majorité
- **📊 Résultats** — Affiche les statistiques du vote

### Calcul du Statut
Basé sur les votes "pour" et "contre" (abstentions exclues):

| Cas | Statut |
|-----|--------|
| > 50% pour | **Acceptée** ✅ |
| > 50% contre | **Rejetée** ❌ |
| Égalité exacte (50/50) | **Reportée** ⏳ |
| Aucun vote | **En attente** ⌛ |

### Activation/Désactivation
Le vote est **ACTIF** si et seulement si:
```
dateFinVote >= aujourd'hui
```

Après `dateFinVote`, le bouton de vote disparaît et affiche "🔒 Vote fermé"

---

## Architecture Technique

### Entités
**Vote.java** — Représente un vote unique
```java
- id (PK)
- userId (FK)
- propositionId (FK)
- type (pour/contre/abstention)
- createdAt
- updatedAt
```

### Service
**VoteService.java** — Gestion des votes et statistiques
- `add(Vote)` — Ajouter un nouveau vote
- `edit(Vote)` — Modifier un vote existant
- `getByUserAndProposition()` — Récupérer le vote d'un utilisateur
- `getByPropositionId()` — Lister tous les votes d'une proposition
- `calculateVoteStats(propositionId)` — Calculer les statistiques

**VoteService.VoteStats** — Résultats du vote
```java
- pour (count)
- contre (count)
- abstention (count)
- getTotalVotants() → pour + contre
- checkProposalStatus() → détermine le statut
```

### Contrôleur
**PropositionReunionController.java**
- Affiche les boutons de vote dans la carte proposition
- Gère les clics de vote (`handleVote()`)
- Met à jour automatiquement le statut de la proposition
- Affiche les résultats (`showVoteStats()`)

---

## Installation

### 1️⃣ Créer la Table de Base de Données
Exécutez le script SQL fourni:
```sql
-- Fichier: SQL_VOTE_SETUP.sql
CREATE TABLE IF NOT EXISTS `vote` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `proposition_id` INT NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `unique_user_proposition_vote` (`user_id`, `proposition_id`),
  KEY `idx_proposition_id` (`proposition_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2️⃣ Fichiers Créés/Modifiés
✅ `entities/Vote.java` — Entité Vote  
✅ `services/VoteService.java` — Service de gestion  
✅ `Controllers/backoffice/group/PropositionReunionController.java` — Interface utilisateur  

### 3️⃣ Compilation
```bash
mvn clean compile
```

---

## Utilisation Interface Utilisateur

### Pour les Propositions Actives (avant dateFinVote)
Chaque proposition affiche 4 boutons:

```
[👍 Pour] [👎 Contre] [⊘ Abstention] [📊 Résultats] | [✎ Modifier] [🗑 Supprimer]
```

**Exemple - Interagir avec un vote:**

1. **Voter:**
   - Cliquez sur "👍 Pour"
   - Message: "Votre vote 'pour' a été enregistré!"
   - Le statut de la proposition se met à jour automatiquement

2. **Modifier son vote:**
   - Votez initialement: "👍 Pour"
   - Changez d'avis et cliquez: "👎 Contre"
   - Message: "Votre vote a été modifié en: contre"
   - Le statut recalcule immédiatement

3. **Voir les résultats:**
   - Cliquez sur "📊 Résultats"
   - Fenêtre modale affichant:
     - 👍 Pour: X votes
     - 👎 Contre: Y votes
     - ⊘ Abstention: Z votes
     - Total (excl. abstention): X+Y
     - Pourcentages: X.X% für X.X% contre
     - Statut calcul: Acceptée/Rejetée/Reportée/En attente
     - Votre vote (si effectué)

### Pour les Propositions Expirées (après dateFinVote)
```
🔒 Vote fermé
```
Aucun bouton de vote n'est affiché. Seuls les boutons "Modifier" et "Supprimer" la proposition restent.

---

## Configuration

### Utilisateur Hardcodé
Actuellement, userId=1 est hardcodé dans:
```java
// PropositionReunionController.java
private static final int CURRENT_USER_ID = 1;
```

**Pour changer l'ID utilisateur:**
1. Remplacez la valeur du CURRENT_USER_ID
2. Ou intégrez le système d'authentification existant

---

## Exemples de Cas d'Usage

### Cas 1: Proposition Acceptée (75% pour)
```
Total votes: 4
- Pour: 3
- Contre: 1
- Abstention: 2

Calcul: 3 / (3+1) = 75% > 50%
Résultat: Acceptée ✅
```

### Cas 2: Proposition Reportée (50/50)
```
Total votes: 4
- Pour: 2
- Contre: 2
- Abstention: 1

Calcul: 2 / (2+2) = 50% = 50%
Résultat: Reportée ⏳
```

### Cas 3: Proposition En Attente (aucun vote pour/contre)
```
Total votes: 3
- Pour: 0
- Contre: 0
- Abstention: 3

Calcul: Aucun vote significatif
Résultat: En attente ⌛
```

---

## Journalisation & Débogage

### Logs disponibles
```java
[VOTE] Proposition 5 updated to status: Acceptée (Pour: 3, Contre: 1, Abstention: 2)
[VoteService] Error getting by proposition: [message d'erreur]
[PropositionReunionController] Proposition 5 loaded with status: Acceptée
```

### Vérifier les votes en BD
```sql
SELECT * FROM vote WHERE proposition_id = 1;
```

---

## Limitations Actuelles

⚠️ **userId hardcodé à 1** — Pas d'intégration d'authentification  
⚠️ **Aucune persistance des votes** — Les votes en mémoire ne survivent pas aux redémarrages  
⚠️ **Aucune authentification réelle** — Pas de vérification d'identité utilisateur  

### Améliorations Futures
- [ ] Intégrer le système d'authentification existant
- [ ] Ajouter des audits/logs détaillés
- [ ] Exporter les résultats (PDF/Excel)
- [ ] Notifications en temps réel
- [ ] Délégation de vote
- [ ] Votes pondérés (par rôle)

---

## Dépannage

### Erreur: "Table 'vote' doesn't exist"
**Solution:** Exécutez SQL_VOTE_SETUP.sql sur la base de données

### Erreur SQL: "Duplicate entry for key 'unique_user_proposition_vote'"
**Cause:** L'utilisateur a déjà voté pour cette proposition
**Comportement normal:** Le système détecte cela et MODIFIE le vote existant

### Boutons de vote n'apparaissent pas
**Vérifier:**
1. `dateFinVote` de la proposition est-elle >= aujourd'hui?
2. La proposition a-t-elle une `dateFinVote` définie?

```java
// Dans PropositionReunionController.java
boolean isVotingOpen = prop.getDateFinVote() != null && 
                       prop.getDateFinVote().isAfter(LocalDate.now());
```

---

## Support
Pour issues ou questions:
1. Vérifiez les logs de débogage
2. Inspectez la table `vote` en base de données
3. Assurez-vous que `dateFinVote` est correctement définie

---

**Dernière mise à jour:** 2026-04-25  
**Version:** 1.0.0
