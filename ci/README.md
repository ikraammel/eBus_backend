# Tests des images contre la base distante

Le job `tests` du workflow backend attend les deux builds, télécharge leurs images et démarre le backend sur `127.0.0.1:18081` et le frontend sur `127.0.0.1:18082`. Les outils Robot Framework, Playwright et JMeter s'exécutent nativement sur le runner Ubuntu 24.04.

## Secrets requis dans le dépôt backend

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EBUS_USER_EMAIL`, `EBUS_USER_PASSWORD`, `EBUS_ADMIN_EMAIL`, `EBUS_ADMIN_PASSWORD`.

Les trois variables `DB_*` doivent appartenir à **la même base**. L'URL doit être une URL JDBC PostgreSQL externe accessible depuis GitHub, avec les paramètres TLS requis par le fournisseur. Les comptes USER/ADMIN doivent exister dans cette base, pas simplement dans une autre base ou une autre version déployée.

Le secret optionnel `JWT_SECRET` est utilisé s'il existe ; sinon le job génère une clé aléatoire temporaire. Les tests obtiennent de nouveaux tokens en se connectant au conteneur. Ils ne réutilisent pas les tokens Render. Les vraies clés Stripe et les identifiants mail ne sont pas utilisés : ces fonctions ont une configuration factice et ne sont pas validées par cette suite.

## Préservation des données

- Hibernate utilise `ddl-auto=validate`, sans migration ni création de schéma.
- Le pool JDBC demande des connexions en lecture seule.
- Les requêtes des suites sont limitées aux lectures et à `POST /users/login`.
- Aucun seed, achat, utilisateur, ligne, réclamation ou objet n'est créé ou supprimé.
- Le nettoyage supprime uniquement les conteneurs du runner.

Le contrôle JDBC complète les restrictions des scénarios ; il ne remplace pas les droits d'un compte PostgreSQL limité si votre infrastructure en propose un. Si le schéma est incompatible, corriger la configuration ou la version applicative : ne pas passer en `update` pour faire réussir le job sur la base réelle.

## Suites et résultats

| Suite | Cas | Périmètre |
| --- | --- | --- |
| Robot | 12 | Santé, transport, profil et données du USER authentifié |
| Playwright | 3 | Invité/lignes, connexion USER/lignes, connexion ADMIN/accueil |
| JMeter | 7 requêtes, 1 utilisateur, 1 itération | Connexion et lectures ; smoke limité, pas test de capacité |

Le frontend actuellement construit contient l'adresse API Render. Pour garantir que les parcours testent **le backend Docker**, Playwright transmet ces appels au conteneur et restitue ses vraies réponses. Aucune donnée API n'est simulée. Ce routage de test ne valide pas la configuration CORS du déploiement. Les requêtes d'écriture externes sont bloquées.

Les sélecteurs reconnaissent le texte et les noms accessibles du DOM sémantique Flutter. Le parcours des lignes attend la réponse GET réelle avant de vérifier les données reçues : l'apparition du titre de page ne garantit pas que cette réponse soit terminée. Les échecs indiquent l'étape, les statuts API et des compteurs de ressources sans publier de contenu de session. Une capture du parcours invité peut être conservée localement pour le diagnostic ; les captures ne sont pas incluses dans les artefacts GitHub actuels.

Le job utilise explicitement le Chrome installé sur le runner Ubuntu, en mode headless, avec SwiftShader pour le rendu logiciel sur une machine sans GPU. Il publie le canal et la version du navigateur dans le bilan. Si JavaScript échoue avant la création de la vue Flutter, le test signale immédiatement `FrontendBootstrapError` et conserve le message expurgé ; il ne passe pas trois minutes à attendre des sélecteurs absents. Les erreurs détaillées ne sont capturées qu'avant la saisie des identifiants.

Références : [canaux et modes headless Playwright](https://playwright.dev/python/docs/browsers), [rendu logiciel SwiftShader de Chromium](https://github.com/chromium/chromium/blob/main/docs/gpu/swiftshader.md).

Une erreur Robot ne masque pas les exécutions indépendantes Playwright/JMeter : elles continuent si les applications sont disponibles. Le job reste en échec si une suite échoue. Le HTTP 500 anonyme sur `/types-abonnement` provenait d'une conversion du principal anonyme en `User`. Le service gère maintenant explicitement le visiteur et lui renvoie les offres non étudiantes ; le comportement étudiant authentifié est conservé. Le test Robot attend toujours HTTP 200 et un tableau JSON. Huit régressions Java ciblées couvrent ce correctif, sans base de données.

Les rapports sont disponibles dans l'artefact `remote-database-test-reports`. Aucun journal applicatif brut, JWT, capture ou trace navigateur n'est publié. Le diagnostic de démarrage indique une catégorie d'erreur (réseau, authentification, schéma) sans afficher les secrets.

Les résultats des tests de garde-fous et les dry-runs sont distincts de l'exécution fonctionnelle contre GitHub/PostgreSQL. Consulter le résultat réel de la nouvelle exécution avant de conclure que la connexion et les parcours ont réussi.

## Diagnostiquer un démarrage interrompu

`Application containers started` confirme uniquement que Docker a accepté les lancements. Seul `backend readiness: PASS` confirme que le backend répond après son initialisation.

En cas d'arrêt, consulter `startup.json` dans les rapports : il contient le code de sortie, l'indicateur mémoire et les causes Java expurgées. Les erreurs réseau, authentification, TLS, schéma et permissions sont distinguées ; les mots de passe, tokens, adresses et identifiants connus sont masqués. Les journaux bruts restent exclus des artefacts.

`The operation was canceled` correspond à une interruption GitHub/runner et ne prouve pas un défaut de connexion PostgreSQL. Vérifier le statut final (`Cancelled` ou `Timed out`) et la durée du job. Après une mise à jour du diagnostic, utiliser la nouvelle exécution du nouveau commit ; relancer une ancienne exécution conserve son ancien code.
