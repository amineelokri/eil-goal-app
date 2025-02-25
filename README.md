# EILCOGOAL App

Application Android qui permet de :

- Gérer l’authentification des utilisateurs (connexion, inscription) via Firebase.
- Afficher les matchs de football pour les équipes favorites, en récupérant les données depuis l’API-Football.

## Fonctionnalités

1. **Authentification Firebase**  
   - Connexion par email/mot de passe.
   - Inscription d’un nouvel utilisateur (stockage dans Firebase Authentication).

2. **Liste de matchs favoris**  
   - Les IDs d’équipes sont stockés dans Firebase Realtime Database (ou définis statiquement).
   - Les données de matchs (score, logo, etc.) sont récupérées via [API-Football](https://www.api-football.com/).
   - Affichage dans un `RecyclerView` avec logos et scores.

3. **Filtrage par ID**  
   - L’application ne charge que les matchs correspondant aux IDs d’équipes définis comme favoris.

## Prérequis

- **Android Studio** (version récente).
- **Compte Firebase** configuré (avec `google-services.json` dans le dossier `app/`).
- **Clé API** pour API-Football (voir [api-football.com](https://www.api-football.com/)).

## Installation

1. **Cloner le dépôt** :
   ```bash
   git clone [https://github.com/votre-user/eilco-app](https://github.com/amineelokri/eil-goal-app).git
```
