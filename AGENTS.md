# AGENTS.md – VolApp

> Version du fichier : 1.0 (10 novembre 2026).
> Ce fichier sert d'instructions communes à tous les agents de code (OpenCode, Qwen, Claude…).
> Pour que les mesures de l'étude de cas restent comparables, **ne pas le modifier entre deux mesures** :
> le versionner dans Git et noter sa version dans le journal.

## 1. Le projet

VolApp est une application web de **recherche, réservation et suivi de vols**.
Les offres, les commandes et les annulations viennent de l'**API Duffel en mode test** :
la compagnie « Duffel Airways » (code `ZZ`) est fictive et aucun paiement réel n'a lieu.

- Back-end : **Java 25**, **Spring Boot 3.5.7**, **Spring Cloud 2025.0.0**, projet **Maven multi-modules**.
- Front-end : **Vue 3** + **Vite**, sans routeur (navigation par état dans `App.vue`).
- Infrastructure (Docker Compose) : **Redis 7**, **RabbitMQ 3**, **PostgreSQL 16**.
- Poste de développement : Windows, IntelliJ IDEA, PowerShell, Docker Desktop, Node.js via fnm.

## 2. Architecture

```
Navigateur ── Vue 3 (5173) ──/api──► Gateway (8080)
                                      ├── /api/recherche/**    ──► recherche-service   (8081) ──► Duffel
                                      └── /api/reservations/** ──► reservation-service (4000) ──► Duffel
recherche-service   ──► Redis       (cache des recherches, 5 min)
reservation-service ──► PostgreSQL  (base "reservations")
reservation-service ──► RabbitMQ    (exchange "volapp.evenements")
```

| Module | Port | État | Rôle |
|---|---|---|---|
| `gateway` | 8080 | Fait | Point d'entrée unique, routage (Spring Cloud Gateway WebFlux) |
| `recherche-service` | 8081 | Fait | Recherche d'offres chez Duffel, cache Redis |
| `reservation-service` | 4000 | Fait | Réservation, paiement simulé, annulation, événements |
| `frontend` | 5173 | Fait | Recherche, réservation, confirmation, mes réservations |
| `suivi-service` | à définir (8083 conseillé) | À faire | Surveillance des vols réservés |
| `notification-service` | à définir | À faire | E-mails au voyageur |
| `utilisateurs-service` | à définir | À faire | Comptes, authentification JWT vérifiée par la gateway |

Le port de `reservation-service` est **4000** (8082 était occupé sur le poste) : ne pas le remettre à 8082.

### Règles d'architecture (obligatoires)

1. Le front-end n'appelle **que la gateway** (`/api/...`), jamais un service directement.
2. Seuls les services métier appellent Duffel. Le client Duffel est isolé dans le package `duffel/` de chaque service.
3. **Une base par service.** Un service ne lit jamais la base d'un autre ; il passe par une API ou un événement.
4. Chaque service traduit le format Duffel vers **son propre format** (records du package `api/`). Ne jamais renvoyer le JSON Duffel brut au front-end.
5. **Synchrone (HTTP)** quand l'utilisateur attend une réponse ; **asynchrone (RabbitMQ)** pour le reste.
6. Le jeton Duffel vient de la variable `DUFFEL_ACCESS_TOKEN`. **Ne jamais l'écrire dans le code, un fichier de configuration versionné ou un test.**
7. Pas d'appel à Duffel à l'intérieur d'une transaction de base de données.

## 3. Arborescence

```
vol-app/
├── pom.xml                  parent Maven (modules, versions Java / Spring)
├── docker-compose.yml       redis, rabbitmq, postgres + services
├── Dockerfile               commun : docker build --build-arg MODULE=<module>
├── infra/postgres-init.sql  crée les bases reservations, suivi, utilisateurs
├── .env.example             modèle du fichier .env (jeton Duffel) – .env est ignoré par Git
├── gateway/
├── recherche-service/
├── reservation-service/
└── frontend/
```

### Organisation d'un service (à reproduire pour les nouveaux services)

```
com.volapp.<service>/
├── <Service>Application.java   @SpringBootApplication (+ @ConfigurationPropertiesScan)
├── api/          contrôleur REST, records de requête/réponse, GestionErreurs (@RestControllerAdvice)
├── service/      logique métier, mapper Duffel -> format interne, exceptions métier
├── duffel/       DuffelClient (RestClient) et DuffelDtos (records du JSON Duffel)
├── domaine/      entités JPA et repositories (si le service a une base)
├── evenements/   records d'événements et publication RabbitMQ (si besoin)
└── config/       DuffelProperties, DuffelClientConfig, cache, RabbitMQ
```

## 4. Commandes

Toutes les commandes se lancent depuis la racine `vol-app/` sauf indication contraire.
Le projet n'a **pas de Maven Wrapper** : utiliser `mvn` (ou le panneau Maven d'IntelliJ).

| Action | Commande |
|---|---|
| Démarrer l'infrastructure | `docker compose up -d redis rabbitmq postgres` |
| Compiler et tester tout le back-end | `mvn clean verify` |
| Tester un seul module | `mvn -pl reservation-service -am test` |
| Lancer un service | `mvn -pl recherche-service spring-boot:run` (avec `DUFFEL_ACCESS_TOKEN` défini) |
| Installer le front-end | `cd frontend` puis `npm install` |
| Lancer le front-end | `cd frontend` puis `npm run dev` → http://localhost:5173 |
| Vérifier que le front-end compile | `cd frontend` puis `npm run build` |
| Tout lancer dans Docker | `docker compose up --build` (jeton dans `.env`) |

Sous PowerShell, `curl` est un alias d'`Invoke-WebRequest` : utiliser `Invoke-RestMethod` ou `curl.exe`.

**Ordre de démarrage en développement :** infrastructure Docker → `RechercheApplication` et `ReservationApplication` (avec le jeton) → `GatewayApplication`.
Après l'ajout d'une route dans la gateway, **relancer la gateway**.

### Vérification attendue après chaque modification

1. `mvn clean verify` passe.
2. `npm run build` passe si le front-end a changé.
3. Les tests unitaires existants passent (`OffreMapperTest`, `ReservationMapperTest`) et les nouveaux mappers ont leurs tests.

## 5. Configuration

| Variable | Utilisée par | Valeur en développement |
|---|---|---|
| `DUFFEL_ACCESS_TOKEN` | recherche, réservation | jeton de test `duffel_test_...` |
| `REDIS_HOST` | recherche | `localhost` |
| `POSTGRES_HOST` | réservation | `localhost` |
| `RABBITMQ_HOST` | réservation | `localhost` |
| `RECHERCHE_URL` | gateway | `http://localhost:8081` |
| `RESERVATION_URL` | gateway | `http://localhost:4000` |

PostgreSQL : utilisateur `volapp`, mot de passe `volapp`, port 5432.
RabbitMQ : `guest` / `guest`, interface web http://localhost:15672.
Hibernate est en `ddl-auto: update` : **les tables sont créées à partir des entités Java**. Ne pas modifier le schéma à la main (pgAdmin, SQL) ; modifier l'entité. Toute nouvelle colonne doit être **nullable** ou avoir une valeur par défaut (des lignes existent déjà).

## 6. API exposées (via la gateway, http://localhost:8080)

| Méthode | Adresse | Rôle |
|---|---|---|
| POST | `/api/recherche/vols` | Rechercher : `{ origine, destination, dateDepart, dateRetour?, adultes, cabine? }` |
| GET | `/api/reservations/offres/{offreId}` | Relire une offre : prix à jour, `expireLe`, `passagers[].id` |
| POST | `/api/reservations` | Réserver : `{ offreId, montantAttendu, email, telephone, passagers[] }` |
| GET | `/api/reservations` | Lister les réservations |
| GET | `/api/reservations/{id}` | Lire une réservation |
| POST | `/api/reservations/{id}/annulation` | Devis d'annulation (rien n'est annulé) |
| POST | `/api/reservations/{id}/annulation/{annulationId}/confirmer` | Annuler pour de bon |

Les erreurs sont renvoyées au format **ProblemDetail (RFC 7807)** avec un champ `detail` lisible en français, affiché tel quel par le front-end.
Codes utilisés : 400, 404, 409 (prix modifié, déjà annulée), 410 (offre ou devis expiré), 422 (refus compagnie), 429, 502 (jeton refusé, erreur compagnie avec `code = "erreur_compagnie"`), 504.

## 7. Événements RabbitMQ

Exchange **topic** `volapp.evenements`, messages en JSON (dates ISO-8601).

| Clé de routage | Record Java | Contenu |
|---|---|---|
| `reservation.confirmee` | `ReservationConfirmee` | `reservationId, referenceReservation, email, vols[{numeroVol, origine, destination, depart}]` |
| `reservation.annulee` | `ReservationAnnulee` | `reservationId, referenceReservation, email, montantRembourse, devise, vols[{numeroVol, depart}]` |
| `vol.statut-modifie` (à créer) | `StatutVolModifie` | à définir par `suivi-service` |

Aucun service n'écoute encore ces événements : un service consommateur doit **déclarer sa propre file durable** (ex. `suivi.reservations`) et la lier à l'exchange avec la ou les clés voulues.
Une publication qui échoue est journalisée sans faire échouer l'opération métier (pattern « outbox » prévu plus tard).

## 8. Spécificités Duffel (à respecter)

- En-têtes : `Authorization: Bearer <jeton>`, `Duffel-Version: v2`, `Accept: application/json`. Les réponses sont enveloppées dans `{ "data": ... }`.
- Recherche : `POST /air/offer_requests?return_offers=true`.
- Avant de réserver, **relire l'offre** (`GET /air/offers/{id}`) : le prix change et l'offre expire (`expires_at`). Les identifiants des passagers (`pas_...`) sont obligatoires pour la commande.
- Commande : `POST /air/orders`, `type: "instant"`, paiement `{ type: "balance", currency, amount }` avec **exactement** le montant et la devise de l'offre.
- Annulation en deux temps : `POST /air/order_cancellations` (devis), puis `POST /air/order_cancellations/{id}/actions/confirm`.
- Heures des segments (`departing_at`, `arriving_at`) : **heure locale de l'aéroport, sans fuseau** → `LocalDateTime`. Dates techniques (`expires_at`) : UTC avec fuseau → `Instant`.
- Durées au format ISO-8601 (`PT8H05M`). Montants en texte (`"412.30"`) → `BigDecimal`.
- En mode test, seules les offres **Duffel Airways** sont fiables ; les offres d'autres sources (ex. Travelport) peuvent échouer avec `airline_error`.
- Les vols Duffel Airways sont fictifs : une API de suivi réelle (AeroDataBox) ne les trouvera pas. Le suivi en mode test doit utiliser une **source de statut simulée**.

## 9. Conventions de code

- **Langue** : noms de classes, méthodes, variables, messages et commentaires **en français** (`RechercheService`, `lireOffre`, `montantAttendu`). Les noms qui reprennent le JSON Duffel restent en anglais dans `DuffelDtos`.
- Commentaires Java sans accents (habitude du projet) ; messages destinés à l'utilisateur avec accents.
- **Records** pour les DTO et les événements ; classes uniquement pour les entités JPA (constructeur `protected` vide obligatoire).
- Injection par **constructeur**, jamais par champ (`@Autowired` sur un attribut interdit).
- Client HTTP : `RestClient` (pas `RestTemplate`, pas `WebClient` dans les services MVC).
- Validation des entrées par annotations Jakarta (`@NotBlank`, `@Pattern`, `@FutureOrPresent`…) + `@Valid` sur le contrôleur.
- Les erreurs métier sont des exceptions dédiées (classe `Exceptions`) traduites en ProblemDetail dans `GestionErreurs`.
- Front-end : toutes les URL de l'API sont définies **une seule fois**, dans `frontend/src/api.js`. Formatage des dates, heures, durées et prix dans `frontend/src/format.js`. Couleurs et polices via les variables CSS de `style.css` (`--encre`, `--papier`, `--balise`…).
- Pas de nouvelle dépendance (Maven ou npm) sans nécessité claire.

## 10. Interdits

- Écrire un jeton, un mot de passe réel ou une clé dans le code ou dans Git.
- Passer en mode live Duffel (jeton `duffel_live_`) : chaque réservation achèterait un vrai billet.
- Faire appeler Duffel ou un service autre que la gateway par le front-end.
- Partager une base de données entre deux services.
- Modifier les ports existants sans mettre à jour la gateway, `docker-compose.yml` et ce fichier.
- Supprimer ou réécrire des tests existants pour les faire passer.

## 11. Prochaines tâches (backlog)

1. **suivi-service** : écouter `reservation.confirmee` et `reservation.annulee`, enregistrer les vols à surveiller (base `suivi`), vérifier périodiquement leur statut (`@Scheduled`), publier `StatutVolModifie`, exposer `GET /api/suivi/...` ; route à ajouter dans la gateway.
2. **notification-service** : écouter les trois événements et envoyer des e-mails (Mailpit en développement).
3. **utilisateurs-service** + authentification JWT vérifiée par la gateway ; filtrer les réservations par utilisateur.
4. Lancement complet avec Docker, tests d'intégration, pipeline CI/CD (build + tests), mise en production.
5. Paiement réel (Stripe en capture manuelle) – seulement après validation.

## 12. Limites connues

- Pas de comptes : toutes les réservations sont visibles par tous.
- Paiement simulé : le voyageur n'est jamais débité.
- Si l'enregistrement en base échoue après la commande Duffel, la commande existe chez Duffel sans être enregistrée (saga à prévoir).
- Un événement peut être perdu si RabbitMQ est arrêté au moment de la publication.
