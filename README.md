# 🏦 Plateforme Bancaire Microservices - Test Technique

Ce projet implémente une architecture microservices distribuée pour la gestion bancaire (comptes et transactions), avec tolérance aux pannes, communication synchrone (OpenFeign) et asynchrone (Kafka), configuration centralisée et routage dynamique.

---

## 📐 Architecture Globale

```mermaid
flowchart TD
    Client["Client Web / Mobile / Postman"] -->|Port 8080| GW["API Gateway\n(Spring Cloud Gateway)"]

    subgraph Service Discovery & Config
        Eureka["Discovery Service\n(Eureka - Port 8761)"]
        Config["Config Server\n(Spring Cloud Config - Port 8888)"]
    end

    GW -->|Circuit Breaker + lb| AS["Account Service\n(Port 8081)"]
    GW -->|Circuit Breaker + lb| TS["Transaction Service\n(Port 8082)"]

    GW -.->|Register & Discover| Eureka
    AS -.->|Register & Discover| Eureka
    TS -.->|Register & Discover| Eureka

    AS -.->|Fetch Config| Config
    TS -.->|Fetch Config (max-amount=1000)| Config

    TS -->|OpenFeign + Resilience4J| AS
    TS -->|Publish Events| Kafka[("Kafka Topic:\nbank.transactions.created")]

    AS --> DB_ACC[("PostgreSQL\naccount_db (5432)")]
    TS --> DB_TX[("PostgreSQL\ntransaction_db (5433)")]
```

---

## 🧩 Microservices & Composants

| Service | Rôle | Port | Base de données |
| :--- | :--- | :--- | :--- |
| **`config-server`** | Serveur de configuration centralisée (profil `native`) | `8888` | - |
| **`discovery-service`** | Annuaire de services (Eureka Server) | `8761` | - |
| **`api-gateway`** | Point d'entrée HTTP unique, routage dynamique et Circuit Breaker | `8080` | - |
| **`account-service`** | Gestion du cycle de vie des comptes, débits, crédits et virements | `8081` | PostgreSQL (`account_db`) |
| **`transaction-service`** | Enregistrement des transactions, vérification des plafonds, débit inter-service et publication Kafka | `8082` | PostgreSQL (`transaction_db`) |

---

## ⚖️ Règles Métier Implémentées

1. **Vérification du compte** : Le compte doit exister et avoir le statut `ACTIVE` pour toute opération.
2. **Cohérence des devises** : La devise de la transaction doit correspondre exactement à celle du compte.
3. **Plafond configuré** : Toute transaction dont le montant dépasse la valeur distribuée par `config-server` (`bank.transaction.max-amount=1000`) est refusée (`400 Bad Request`).
4. **Validation du solde** : Le solde du compte est consulté avant chaque transaction pour garantir qu'il est suffisant, puis débité de manière synchronisée.
5. **Virement inter-comptes** : Opération transactionnelle (`@Transactional`) débitant le compte source et créditant le compte récepteur.
6. **Tolérance aux pannes (Resilience4J)** : Disjoncteur actif sur les appels OpenFeign et sur la Gateway avec réponses de repli (*Fallback*) `503 Service Unavailable`.
7. **Événements asynchrones (Kafka)** : Après toute transaction persistée, un événement `TransactionCreated` est publié dans le topic `bank.transactions.created` avec `accountId` comme clé de partitionnement.

---

## 🚀 Démarrage Rapide

### 1. Prérequis
* **Java 21** ou **Java 25**
* **Maven 3.9+**
* **Docker & Docker Compose**

### 2. Démarrer l'infrastructure (Bases de données & Kafka)
```bash
docker compose up -d
```

### 3. Ordre de démarrage des microservices
Pour garantir la résolution des configurations et la découverte de services, démarrez les applications dans cet ordre :

1. `config-server` : `mvn spring-boot:run -pl config-server`
2. `discovery-service` : `mvn spring-boot:run -pl discovery-service` (Vérifier http://localhost:8761)
3. `account-service` : `mvn spring-boot:run -pl account-service`
4. `transaction-service` : `mvn spring-boot:run -pl transaction-service`
5. `api-gateway` : `mvn spring-boot:run -pl api-gateway`

---

## 📡 Documentation des APIs REST (via Gateway `http://localhost:8080`)

### 1. Gestion des Comptes (`account-service`)

#### Créer un compte bancaire
```http
POST /api/v1/accounts
Content-Type: application/json

{
  "customerReference": "CUST-001",
  "currency": "EUR",
  "balance": 1500.00
}
```

#### Consulter un compte par ID
```http
GET /api/v1/accounts/{id}
```

#### Lister tous les comptes (ou par client)
```http
GET /api/v1/accounts
GET /api/v1/accounts?customerReference=CUST-001
```

#### Modifier le statut d'un compte
```http
PATCH /api/v1/accounts/{id}/status?status=BLOCKED
```

#### Effectuer un virement d'un compte vers un autre
```http
POST /api/v1/accounts/transfer
Content-Type: application/json

{
  "sourceAccountId": "UUID-SOURCE",
  "destinationAccountId": "UUID-DESTINATION",
  "amount": 200.00,
  "currency": "EUR"
}
```

---

### 2. Gestion des Transactions (`transaction-service`)

#### Enregistrer une transaction
```http
POST /api/v1/transactions
Content-Type: application/json

{
  "accountId": "UUID-DU-COMPTE",
  "amount": 150.00,
  "currency": "EUR",
  "merchantCategory": "SUPERMARKET",
  "country": "FR"
}
```

#### Consulter une transaction par ID
```http
GET /api/v1/transactions/{id}
```

#### Consulter l'historique d'un compte
```http
GET /api/v1/transactions?accountId=UUID-DU-COMPTE
```

---

## 🛡️ Tolérance aux Pannes & Fallbacks

En cas d'indisponibilité d'un service :
* **Circuit Breaker Gateway** : Si `account-service` ou `transaction-service` ne répond pas, la Gateway redirige automatiquement vers `/fallback/accounts` ou `/fallback/transactions` avec un statut HTTP `503`.
* **Circuit Breaker OpenFeign** : Si `account-service` est inaccessible lors d'une transaction, `TransactionServiceImpl` intercepte l'erreur via son disjoncteur Resilience4J et renvoie un message sécurisé sans planter.

