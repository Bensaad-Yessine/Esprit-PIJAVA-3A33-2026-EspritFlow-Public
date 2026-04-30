# 🗳️ Système de Vote - Configuration Front Office

## ✅ Implémentation Mise à Jour

Le système de vote a été **adapté pour le FRONT OFFICE** (pour les membres).

---

## 📁 Fichiers Créés/Modifiés (Front Office)

### Nouveau
- ✅ `Controllers/frontoffice/group/FrontPropositionReunionController.java`
- ✅ `resources/frontoffice/group/propositions-reunion.fxml`

### Modifié
- ✏️ `Controllers/frontoffice/group/GroupContentController.java`
  - Ajout bouton "📋 Propositions Réunion" sur chaque groupe
  - Ajout méthode `handleShowPropositions()`

---

## 🎯 Flux Utilisateur (Front Office)

### Parcours Utilisateur

```
1. Ouvrir Application
   ↓
2. Front Office (Dashboard → Mes Groupes)
   ↓
3. Voir la liste des groupes avec 2 boutons:
   - "Voir details" (groupe details)
   - "📋 Propositions Réunion" ← NOUVEAU SYSTÈME DE VOTE
   ↓
4. Cliquer sur "📋 Propositions Réunion"
   ↓
5. Voir les propositions du groupe
   ↓
6. Pour chaque proposition (si vote actif):
   - [👍 Pour]
   - [👎 Contre]
   - [⊘ Abstention]
   - [📊 Résultats]
   ↓
7. Cliquer "👍 Pour" pour voter
   ↓
8. Message: "Votre vote 'pour' a été enregistré!"
   ↓
9. Statut de la proposition se met à jour automatiquement
```

---

## 🔍 Où Accéder au Vote (Front Office)

### Localisation
```
Front Office
└── Mes Groupes
    └── Chaque groupe affiche 2 boutons:
        ├── "Voir details"
        └── "📋 Propositions Réunion" ← CLIQUER ICI

            └── Fenêtre "Propositions Réunion"
                └── Liste propositions avec:
                    ├── [👍 Pour]
                    ├── [👎 Contre]
                    ├── [⊘ Abstention]
                    ├── [📊 Résultats]
```

---

## 📊 Interface Propositions (Front Office)

### Vue générale
```
╔════════════════════════════════════════╗
║  📋 Propositions Réunion               ║
║  [Groupes › Vote des Membres]          ║
╠════════════════════════════════════════╣
║  [🔍 Rechercher...]       [Statistiques]║
╠════════════════════════════════════════╣
║  Stats:                                ║
║  📋 Total: 5  | ✓ Acceptées: 2        ║
║  ⏳ En attente: 2                      ║
╠════════════════════════════════════════╣
║                                        ║
║  [Card Proposition 1]                  ║
║  Titre: Réunion Projet                ║
║  [👍 Pour] [👎 Contre] [⊘ Abst]       ║
║  [📊 Résultats] | [... autres btns]   ║
║                                        ║
║  [Card Proposition 2]                  ║
║  ...                                   ║
║                                        ║
╠════════════════════════════════════════╣
║  5 propositions | [← Retour aux Groupes]║
╚════════════════════════════════════════╝
```

---

## 🔄 Différence Back Office vs Front Office

| Aspect | Back Office | Front Office |
|--------|------------|--------------|
| **Accès** | Administrateurs | Membres du groupe |
| **Actions** | ➕ Ajouter prop | ❌ Pas d'ajout |
|  | ✎ Modifier prop | ❌ Pas de modif |
|  | 🗑 Supprimer prop | ❌ Pas de suppression |
| **Vote** | ✅ Oui | ✅ Oui |
|  | 👍 Pour | 👍 Pour |
|  | 👎 Contre | 👎 Contre |
|  | ⊘ Abstention | ⊘ Abstention |
|  | 📊 Résultats | 📊 Résultats |

### ✨ Résumé
- **Back Office:** Gestion complète (CRUD) + Vote
- **Front Office:** Vote uniquement (lecture seule)

---

## 🚀 Navigation (Code)

### Avant (Direct en back office)
```
BackOffice/Groupes → PropositionReunion (back office)
```

### Maintenant (Via front office)
```
Front Office/Groupes
    ↓
[Groupe Card]
    ├─ "Voir details" → GroupDetails
    └─ "📋 Propositions Réunion" → FrontPropositionReunion (NOUVEAU!)
        └─ Vote système intégré
```

---

## 📋 Contrôleurs

### FrontPropositionReunionController.java
- Affiche propositions pour un groupe
- Gère les votes (handleVote)
- Affiche résultats (showVoteStats)
- ❌ Pas d'ajout/édition/suppression
- ✅ Lecture + Vote uniquement

### Lié à
- PropositionReunionService (lecture seule)
- VoteService (CRUD vote)

---

## 🔐 Sécurité

✅ Les membres ne peuvent que voter  
✅ Pas d'accès à la modification des propositions  
✅ 1 vote par member par proposition  
✅ Vote modifiable (changement d'avis)  

---

## ✅ Configuration Requise

### Base de Données
- ✅ Table `vote` (voir SQL_VOTE_SETUP.sql)

### Code
- ✅ FrontPropositionReunionController.java créé
- ✅ propositions-reunion.fxml créé
- ✅ GroupContentController modifié (ajout bouton)
- ✅ Imports FrontPropositionReunionController ajoutés

---

## 🧪 Test Rapide (Front Office)

### Étapes
1. Ouvrir application (userId=1)
2. Aller en Front Office
3. Voir "Mes Groupes"
4. Cliquer bouton "📋 Propositions Réunion" d'un groupe
5. **NOUVEAU:** Voir la fenêtre "Propositions Réunion"
6. Créer une proposition avec dateFinVote future (en back office)
7. Voir les boutons de vote apparaître
8. Voter! 👍

---

## 🚨 Points Importants

⚠️ **userId toujours hardcodé à 1**
```java
private static final int CURRENT_USER_ID = 1;
```
→ À remplacer par userId authentifié réel

✅ **Pas de modification BD existante**
- Groupe, PropositionReunion intactes
- Seule nouvelle table: vote

✅ **Deux interfaces maintenant**
- Back Office: CRUD + Vote (admins)
- **Front Office: Vote uniquement (membres)** ← NOUVEAU

---

## 📞 Support

**Question:** Où voter en Front Office?  
**Réponse:** Front Office → Mes Groupes → [Cliquer groupe] → "📋 Propositions Réunion"

**Question:** Je ne vois pas le bouton "Propositions Réunion"?  
**Réponse:** Vérifiez compilation (`mvn clean compile`)

**Question:** Les votes en back et front office sont-ils séparés?  
**Réponse:** Non, la table `vote` est partagée. Même vote visible partout.

---

**Status:** ✅ **READY FOR FRONT OFFICE**

Lancements:
1. `mvn clean compile`
2. Tester avec userId=1 en front office
3. Profiter du système de vote! 🗳️
