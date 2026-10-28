# eBus Backend

API de transport eBus développée en Java 17 avec Spring Boot 3.2.6 et PostgreSQL. Elle fournit les fonctions de transport, comptes et rôles JWT, objets perdus, réclamations et abonnements, avec intégration Stripe et notifications mail.

| Composant | Source | Déploiement |
| --- | --- | --- |
| API | [eBus_backend](https://github.com/ikraammel/eBus_backend) | [Render](https://ebus-backend-ao2u.onrender.com) |
| Application Flutter | [eBus_frontend](https://github.com/ikraammel/eBus_frontend) | [Web Render](https://ebus-frontend-1an4.onrender.com/) |

## 🏗️ Architecture

The following diagram illustrates the eBus application architecture, its external services, deployment, and QA automation strategy.

<p align="center">
  <img
    src="docs/eBus Platform Architecture Diagram.png"
    alt="eBus application architecture diagram"
    width="100%"
  />
</p>


## Structure

Le workflow `.github/workflows/build-images.yml` construit les images backend/frontend, puis exécute un job Robot/Playwright/JMeter contre le backend connecté à la base distante avec les comptes existants. Voir [la procédure GitHub Actions](docs/build-images.md) et [les secrets et protections des tests](ci/README.md). Aucun identifiant de base n'est requis pour le build ; les secrets sont nécessaires au job de tests.

- `src/main/java/com/gestion/gestion_bus` : contrôleurs, services, modèles, accès aux données et configuration de sécurité.
- `src/main/resources/application.yaml` : paramètres externalisés.
- `src/test/java/com/gestion/gestion_bus` : tests unitaires et tests de contexte/API.
- `pom.xml` : dépendances, compilation Java 17, Surefire et rapport JaCoCo.

## Démarrage natif

Installer Java 17, Maven et PostgreSQL. Fournir les variables de connexion `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, ainsi que `JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `STRIPE_SECRET_KEY` et `STRIPE_WEBHOOK_SECRET` selon la configuration. Ne pas stocker les secrets dans Git. Le port HTTP par défaut est 8080 ; `PORT` permet de le modifier.

```powershell
mvn.cmd -DskipTests package
java -jar target/gestion-bus-0.0.1-SNAPSHOT.jar
```

L'endpoint `/api/health` permet de contrôler la disponibilité. Les tests automatisés n'exigent pas Docker ; utiliser une base PostgreSQL native dédiée aux tests.

## Image Docker et base PostgreSQL distante

Depuis la racine de ce clone backend :

```powershell
docker build -t ebus-backend:local .
Copy-Item .env.remote.example .env.remote
notepad .env.remote
```

Renseigner les valeurs privées dans `.env.remote`, exclu de Git et du contexte Docker. `DB_URL` doit être une URL JDBC PostgreSQL externe accessible depuis le PC, par exemple `jdbc:postgresql://HOTE:5432/BASE?sslmode=require`. L'URL HTTPS de l'API Render n'est pas une URL de base. Une adresse interne Render ne fonctionne pas depuis le poste.

Fournir les identifiants de base et les paramètres JWT, mail et Stripe attendus par l'application. L'image ne contient aucun de ces secrets ; ils sont injectés au démarrage. Sur GitHub Actions, les fournir depuis GitHub Secrets à l'exécution, sans arguments de build contenant des secrets.

Pour un smoke local sans envoi de mail, le poste peut utiliser un secret JWT aléatoire propre au conteneur, des identifiants mail factices et `SPRING_MAIL_HOST=127.0.0.1`, `SPRING_MAIL_PORT=1025`, avec authentification et STARTTLS SMTP désactivés. Cette configuration ne valide pas les envois mail ; les JWT de ce conteneur sont distincts de ceux de Render. La copie privée `.env.remote` du poste utilise ce mode lorsque les paramètres réels manquent.

Une fois le fichier renseigné :

```powershell
docker compose -f compose.remote.yaml up -d
docker compose -f compose.remote.yaml ps
Invoke-RestMethod http://127.0.0.1:18081/api/health
# Arrêt sans effacer les fichiers téléversés :
docker compose -f compose.remote.yaml stop
```

Le backend tourne localement dans Docker et se connecte à la base distante ; aucun PostgreSQL local n'est démarré par ce Compose. Le port `18081` évite le conflit avec l'API QA sur `18080`. Le frontend doit cibler cette API locale pour utiliser ce conteneur.

La configuration impose `ddl-auto=validate` pour vérifier le schéma existant sans lancer sa mise à jour automatique. Un schéma incompatible empêche le démarrage. Les actions métier de l'application peuvent toujours écrire dans la base ; aucun seed ni nettoyage de données n'est lancé par cette procédure.

Le build compile l'application sans exécuter les tests. Il ne contacte pas la base distante. L'image utilise Java 17 JRE et un utilisateur sans privilèges ; les fichiers téléversés persistent dans un volume dédié. Garder les tests Java et la vérification de connexion comme étapes distinctes.

## Tests unitaires sans base

```powershell
mvn.cmd -B "-Dtest=AbonnementServiceImpTest,ObjetPerduServiceImpTest" test
```

Ces classes utilisent Mockito : 32 cas pour les abonnements et 9 pour les objets perdus. La vérification du 27 octobre 2026 donne **41 tests réussis, 0 échec**. Le rapport Surefire est sous `target/surefire-reports` et JaCoCo sous `target/site/jacoco/index.html`. Le rapport couvre le périmètre exécuté ; il ne constitue pas une couverture globale validée du produit.

## Tests de contexte et d'intégration

La régression `TypeAbonnementServiceImpTest` ajoute huit vérifications ciblées du catalogue d'abonnements : visiteur, absence d'authentification, accès étudiant et réponse HTTP du contrôleur. Elles ont réussi le 28 octobre 2026 sans accès à PostgreSQL :

```powershell
mvn.cmd -B "-Dtest=TypeAbonnementServiceImpTest" test
```

`AbstractDatabaseTest` configure la base dédiée `ebus_integration` sur `127.0.0.1:55433`, avec le compte synthétique `ebus_integration` et le mot de passe de test `integration-only`. Créer ce service PostgreSQL et cette base nativement avant l'exécution complète ; ces valeurs sont réservées à l'environnement local de test.

```powershell
mvn.cmd -B verify
```

Les classes de contexte/API héritent de cette configuration et utilisent des paramètres mail/Stripe de test. Ne pas remplacer cette cible par une base réelle. L'exécution antérieure complète comporte 46 cas réussis, dont 5 cas contexte/intégration ; la vérification courante de 41 tests ne relance pas ces cinq cas et ne prétend pas les revalider.

Les tests navigateur, API externes, mobiles et performance sont maintenus dans le dépôt QA séparé. Les scénarios distants conservent les utilisateurs et les lignes existants et n'effectuent aucun paiement réel validé.
