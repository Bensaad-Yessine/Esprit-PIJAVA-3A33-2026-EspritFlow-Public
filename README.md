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

