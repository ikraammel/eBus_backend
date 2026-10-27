# eBus Backend

API de transport eBus développée en Java 17 avec Spring Boot 3.2.6 et PostgreSQL. Elle fournit les fonctions de transport, comptes et rôles JWT, objets perdus, réclamations et abonnements, avec intégration Stripe et notifications mail.

| Composant | Source | Déploiement |
| --- | --- | --- |
| API | [eBus_backend](https://github.com/ikraammel/eBus_backend) | [Render](https://ebus-backend-ao2u.onrender.com) |
| Application Flutter | [eBus_frontend](https://github.com/ikraammel/eBus_frontend) | [Web Render](https://ebus-frontend-1an4.onrender.com/) |

## Structure

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

## Tests unitaires sans base

```powershell
mvn.cmd -B "-Dtest=AbonnementServiceImpTest,ObjetPerduServiceImpTest" test
```

Ces classes utilisent Mockito : 32 cas pour les abonnements et 9 pour les objets perdus. La vérification du 27 octobre 2026 donne **41 tests réussis, 0 échec**. Le rapport Surefire est sous `target/surefire-reports` et JaCoCo sous `target/site/jacoco/index.html`. Le rapport couvre le périmètre exécuté ; il ne constitue pas une couverture globale validée du produit.

## Tests de contexte et d'intégration

`AbstractDatabaseTest` configure la base dédiée `ebus_integration` sur `127.0.0.1:55433`, avec le compte synthétique `ebus_integration` et le mot de passe de test `integration-only`. Créer ce service PostgreSQL et cette base nativement avant l'exécution complète ; ces valeurs sont réservées à l'environnement local de test.

```powershell
mvn.cmd -B verify
```

Les classes de contexte/API héritent de cette configuration et utilisent des paramètres mail/Stripe de test. Ne pas remplacer cette cible par une base réelle. L'exécution antérieure complète comporte 46 cas réussis, dont 5 cas contexte/intégration ; la vérification courante de 41 tests ne relance pas ces cinq cas et ne prétend pas les revalider.

Les tests navigateur, API externes, mobiles et performance sont maintenus dans le dépôt QA séparé. Les scénarios distants conservent les utilisateurs et les lignes existants et n'effectuent aucun paiement réel validé.
