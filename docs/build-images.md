# Construire les images et tester la base distante dans GitHub Actions

Le workflow `.github/workflows/build-images.yml` appartient au dépôt backend. Il construit l'image backend de la référence sélectionnée et récupère le frontend dans `ikraammel/eBus_frontend` à la référence saisie. Les deux jobs sont indépendants.

Un push backend sur la branche dédiée `ci/docker-images` déclenche également la construction, avec le frontend de la branche `ci/docker-images` et l'API Render. Pousser d'abord la branche frontend. Le workflow ne se déclenche pas sur les autres branches par ce filtre ; aucun merge sur `master` n'est nécessaire pour ce premier build.

## Avant le premier lancement

1. Dans le dépôt frontend, commiter et pousser le `Dockerfile`, `.dockerignore`, `nginx.conf` à la racine sur `ci/docker-images`. L'adaptation `String.fromEnvironment('API_BASE_URL', ...)` est déjà présente sur le frontend distant récupéré le 27 octobre 2026.
2. Dans le dépôt backend, commiter et pousser le Dockerfile, le `.dockerignore` et ce workflow. Pour faire apparaître le lancement manuel, le workflow doit être présent sur la branche par défaut GitHub.
3. Si le frontend est privé, créer le secret `FRONTEND_REPO_TOKEN` dans le dépôt backend avec un accès en lecture au contenu du frontend. Pour un frontend public, ce secret n'est pas nécessaire.

## Lancement manuel

Dans GitHub : dépôt backend → Actions → **Build backend and frontend images** → **Run workflow**. Sélectionner la branche backend, la branche ou le commit frontend et l'URL publique API à compiler dans Flutter Web.

Aucun secret DB, JWT, mail ou Stripe n'est nécessaire à cette construction. Aucun conteneur applicatif n'est lancé et aucune connexion à la base réelle n'est effectuée. Le build Maven ignore les tests : cette étape ne constitue pas une validation des tests Java ou E2E.

Après les deux builds, un troisième job `tests` démarre maintenant les images et exécute Robot, Playwright et JMeter contre le backend connecté à PostgreSQL distant. **Ce job exige les sept secrets documentés dans [ci/README.md](../ci/README.md)**. La phrase ci-dessus concerne uniquement les étapes de construction, pas ce job de démarrage/tests. Aucun seed ou paiement n'est lancé.

## Récupérer les images

Une exécution réussie fournit les artefacts `ebus-backend-image` et `ebus-frontend-image`, conservés trois jours. Après téléchargement et extraction des ZIP :

```powershell
docker load -i ebus-backend.tar
docker load -i ebus-frontend.tar
```

Les tags chargés sont `ebus-backend:ci` et `ebus-frontend:ci`. Ce workflow ne publie pas dans un registre et ne déploie pas sur Render. Le job de tests utilise les images de cette même exécution et publie ses rapports séparément.

Les deux builds ont réussi dans l'exécution GitHub `40591536066` (backend 2 min 1 s, frontend 6 min 7 s), selon le compte rendu fourni. Le nouveau job de tests contre la base distante reste à valider par une nouvelle exécution. Les lancements frontend ne déclenchent pas automatiquement le workflow backend.
