# SecureMicroservicesBank — Propriété et gouvernance des données

**Document :** Modèle de propriété des données et frontières de persistance  
**Projet :** SecureMicroservicesBank  
**Version :** 1.0  
**Statut :** Proposition d’architecture à valider  

**Documents parents :**
- `docs/project-scope.md`
- `docs/use-cases.md`
- `docs/business-rules.md`
- `docs/architecture/service-responsibilities.md`

**Référence :** Cahier des charges SecureMicroservicesBank, version 1.0 du 19 juin 2026

---

## 1. Objectif du document

Ce document précise comment les données sont réparties, protégées et échangées entre les microservices de SecureMicroservicesBank.

Il répond notamment aux questions suivantes :

- Quel service est propriétaire de chaque donnée ?
- Quelles tables appartiennent à chaque service ?
- Quelles contraintes doivent être appliquées dans PostgreSQL ?
- Quelles références sont locales et lesquelles sont interservices ?
- Quelles données peuvent être exposées par API ou événement ?
- Quelles données ne doivent jamais quitter leur service ?
- Quelle cohérence peut être garantie immédiatement ?
- Quelles données sont synchronisées de manière éventuelle ?
- Comment éviter les accès directs entre bases de données ?

Ce document constitue la base de la conception des entités JPA, des migrations Flyway, des DTO, des contrats REST et des événements.

---

# 2. Principes obligatoires

## 2.1 Database per service

Chaque microservice possède son propre espace de données logique.

```text
auth-service        → auth_db
customer-service    → customer_db
account-service     → account_db
transaction-service → transaction_db
audit-service       → audit_db
```

Services ajoutés après le MVP :

```text
risk-service         → risk_db
notification-service → notification_db
reporting-service    → reporting_db
```

Un service ne doit jamais lire ou modifier directement les tables d’un autre service.

---

## 2.2 Propriété exclusive

Le service propriétaire est le seul composant autorisé à modifier la donnée.

| Donnée | Service propriétaire |
|---|---|
| Identité de connexion | `auth-service` |
| Mot de passe haché | `auth-service` |
| Rôles et sessions | `auth-service` |
| Profil bancaire fictif | `customer-service` |
| Statut du client | `customer-service` |
| Compte bancaire | `account-service` |
| Solde et statut du compte | `account-service` |
| Demande et statut du virement | `transaction-service` |
| Clé d’idempotence externe | `transaction-service` |
| Évaluation de risque | `risk-service` |
| Journal d’audit | `audit-service` |
| Notification et état d’envoi | `notification-service` |
| Agrégats statistiques | `reporting-service` |

---

## 2.3 Une référence interservice n’est pas une clé étrangère SQL

Exemple :

```text
customer-service.Customer.auth_user_id
```

référence logiquement :

```text
auth-service.User.id
```

Mais aucune contrainte SQL `FOREIGN KEY` ne relie les deux bases.

La validité de cette relation est assurée par :

- un appel API contrôlé ;
- un événement métier ;
- une règle de création ;
- une procédure de réconciliation.

---

## 2.4 Aucune jointure SQL entre services

Requête interdite :

```sql
SELECT *
FROM transaction_db.transactions t
JOIN account_db.bank_accounts a
  ON a.id = t.source_account_id;
```

Un service ne doit pas recevoir de droits SQL sur la base d’un autre service.

Les données combinées sont obtenues par :

- composition d’API ;
- projection dans `reporting-service` ;
- événements ;
- agrégation côté gateway ou backend dédié lorsque justifiée.

---

## 2.5 Cohérence locale forte

À l’intérieur d’un service et d’une base de données, les invariants critiques doivent être garantis par :

- transactions SQL ;
- contraintes ;
- index uniques ;
- verrouillage optimiste ou pessimiste ;
- validations métier.

Exemple dans `account-service` :

```text
débit source + crédit destination + enregistrement operation_id
```

doivent être atomiques.

---

## 2.6 Cohérence interservices contrôlée

Une transaction SQL ne peut pas couvrir plusieurs bases indépendantes.

La cohérence entre services repose sur :

- états métier explicites ;
- appels idempotents ;
- événements ;
- retries contrôlés ;
- compensation lorsque nécessaire ;
- réconciliation ;
- audit.

---

## 2.7 Minimisation des données

Chaque service reçoit uniquement les données dont il a besoin.

Exemple :

`notification-service` peut recevoir :

```text
notificationType
recipientEmail
templateVariables
correlationId
```

Il ne doit pas recevoir :

```text
passwordHash
refreshToken
solde complet si inutile
profil client complet
```

---

# 3. Déploiement PostgreSQL recommandé

## 3.1 Environnement local

Il est acceptable d’utiliser un seul moteur PostgreSQL local, à condition de créer des bases et des utilisateurs séparés :

```text
PostgreSQL instance
├── auth_db
├── customer_db
├── account_db
├── transaction_db
└── audit_db
```

Chaque service possède ses propres identifiants :

```text
auth_service_user
customer_service_user
account_service_user
transaction_service_user
audit_service_user
```

Un utilisateur ne doit pas avoir accès aux autres bases.

---

## 3.2 Environnements staging et production simulée

La séparation logique reste obligatoire.

Selon les ressources disponibles, on peut utiliser :

- une instance PostgreSQL avec plusieurs bases et comptes isolés ;
- plusieurs instances PostgreSQL ;
- un opérateur PostgreSQL Kubernetes, en évolution avancée.

Le code ne doit pas dépendre du fait que les bases soient hébergées sur le même serveur.

---

## 3.3 Comptes de base de données

Deux catégories de comptes peuvent être utilisées :

### Compte de migration

Autorisé à :

- créer et modifier les tables ;
- créer les index ;
- appliquer les migrations Flyway.

### Compte d’exécution

Autorisé seulement à :

- lire ;
- insérer ;
- mettre à jour les tables nécessaires ;
- supprimer uniquement lorsque le domaine l’autorise.

Cette séparation est recommandée pour staging et production simulée.

---

# 4. Conventions de modélisation

## 4.1 Nommage

| Élément | Convention |
|---|---|
| Tables | `snake_case`, pluriel |
| Colonnes | `snake_case` |
| Clés primaires | `id` |
| Références externes | `<resource>_id` |
| Dates de création | `created_at` |
| Dates de modification | `updated_at` |
| Versions optimistes | `version` |
| Statuts | chaîne contrôlée ou enum applicatif |

---

## 4.2 Identifiants

Les ressources principales utilisent des UUID.

Type PostgreSQL :

```sql
UUID
```

Type Java :

```java
UUID
```

Les UUID sont générés par l’application ou par PostgreSQL selon une convention unique.

---

## 4.3 Montants

Les montants utilisent :

```text
Java       : BigDecimal
PostgreSQL : NUMERIC(19,2)
JSON       : nombre décimal
```

L’utilisation de `float`, `real`, `double` ou `double precision` pour les montants bancaires est interdite.

---

## 4.4 Dates

Les dates techniques utilisent :

```text
Java       : Instant
PostgreSQL : TIMESTAMP WITH TIME ZONE
JSON       : ISO 8601 UTC
```

Exemple :

```text
2026-07-21T10:30:00Z
```

---

## 4.5 Statuts

Les statuts sont représentés par des valeurs explicites.

Exemple :

```text
ACTIVE
BLOCKED
PENDING
COMPLETED
REJECTED
FAILED
```

Une valeur inconnue doit être refusée.

Pour faciliter l’évolution, les enums PostgreSQL natifs ne sont pas obligatoires. Une colonne `VARCHAR` avec validation applicative et éventuellement une contrainte `CHECK` est suffisante.

---

## 4.6 Suppression logique

Pour les ressources historiques, la suppression physique est évitée.

On utilise plutôt :

- `status` ;
- `disabled_at` ;
- `revoked_at` ;
- `closed_at`.

Les transactions et audits ne sont jamais supprimés par une API standard.

---

# 5. Classification des données

## 5.1 Niveaux

| Niveau | Description | Exemples |
|---|---|---|
| `SECRET` | Donnée d’authentification ou clé | clé privée JWT, mot de passe de DB |
| `SENSITIVE` | Donnée personnelle ou financière | solde, historique, téléphone |
| `INTERNAL` | Donnée technique interne | IDs, correlation ID, statuts |
| `PUBLIC` | Donnée publiable | documentation OpenAPI publique |

---

## 5.2 Données secrètes

Ne doivent jamais être enregistrées dans Git, les logs ou les événements :

- mot de passe en clair ;
- secret JWT ;
- clé privée ;
- mot de passe PostgreSQL ;
- access token ;
- refresh token brut ;
- secret de fournisseur email.

---

## 5.3 Données sensibles

Doivent être limitées selon le besoin :

- email ;
- téléphone ;
- adresse fictive ;
- solde ;
- montant ;
- comptes participant à une transaction ;
- adresse IP ;
- historique de connexion.

Même si les données sont fictives, le projet doit appliquer des pratiques réalistes.

---

# 6. Auth Service — modèle de données

## 6.1 Base

```text
auth_db
```

## 6.2 Tables initiales

```text
users
refresh_tokens
login_attempts ou champs intégrés à users
mfa_challenges, après le premier incrément
```

---

## 6.3 Table `users`

### Proposition

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_users_role
        CHECK (role IN (
            'CLIENT',
            'ADMIN',
            'AUDITOR',
            'SECURITY_VIEWER'
        )),

    CONSTRAINT chk_failed_login_attempts
        CHECK (failed_login_attempts >= 0)
);
```

### Contraintes et index

```sql
CREATE UNIQUE INDEX ux_users_email_normalized
ON users (LOWER(email));
```

Le service doit appliquer la même normalisation avant insertion et recherche.

---

## 6.4 Table `refresh_tokens`

```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    replaced_by_token_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT ux_refresh_token_hash
        UNIQUE (token_hash),

    CONSTRAINT chk_refresh_expiration
        CHECK (expires_at > issued_at)
);
```

`replaced_by_token_id` peut référencer un autre token localement.

---

## 6.5 Table `mfa_challenges`

Ajout ultérieur :

```sql
CREATE TABLE mfa_challenges (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
);
```

Le code MFA brut ne doit jamais être conservé.

---

## 6.6 Données exposables

Par JWT ou API contrôlée :

```text
userId
role
enabled, si nécessaire
token expiration
```

Par API administrative :

```text
id
email masqué ou complet selon autorisation
role
enabled
accountLocked
createdAt
```

---

## 6.7 Données interdites hors service

Ne doivent jamais être exposées :

```text
passwordHash
tokenHash
codeHash
secret JWT
historique technique complet de sécurité sans autorisation
```

---

# 7. Customer Service — modèle de données

## 7.1 Base

```text
customer_db
```

## 7.2 Table `customers`

```sql
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    auth_user_id UUID NOT NULL,
    customer_number VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    contact_email VARCHAR(254),
    phone VARCHAR(30),
    address_line VARCHAR(255),
    city VARCHAR(100),
    country_code VARCHAR(2),
    status VARCHAR(30) NOT NULL,
    disabled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ux_customers_auth_user_id
        UNIQUE (auth_user_id),

    CONSTRAINT ux_customers_customer_number
        UNIQUE (customer_number),

    CONSTRAINT chk_customer_status
        CHECK (status IN ('ACTIVE', 'DISABLED'))
);
```

---

## 7.3 Référence `auth_user_id`

`auth_user_id` est une référence logique.

Il n’existe aucune contrainte SQL vers `auth_db.users`.

La création doit garantir que :

- l’identité authentifiée existe ;
- elle possède le rôle approprié ;
- aucun profil n’existe déjà.

---

## 7.4 Données exposables

Au client propriétaire :

```text
id
customerNumber
firstName
lastName
contactEmail
phone
address
status
createdAt
updatedAt
```

À un autre service, uniquement le minimum nécessaire :

```text
customerId
authUserId
status
```

---

## 7.5 Données interdites

Le service ne doit jamais posséder ou exposer :

```text
password
passwordHash
refreshToken
rôle de sécurité comme source de vérité
solde bancaire
historique complet des transactions
```

---

# 8. Account Service — modèle de données

## 8.1 Base

```text
account_db
```

## 8.2 Tables initiales

```text
bank_accounts
account_transfer_operations
balance_movements
```

`balance_movements` apporte une traçabilité locale complémentaire au journal d’audit central.

---

## 8.3 Table `bank_accounts`

```sql
CREATE TABLE bank_accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    balance NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ux_bank_accounts_number
        UNIQUE (account_number),

    CONSTRAINT chk_account_balance_non_negative
        CHECK (balance >= 0),

    CONSTRAINT chk_account_status
        CHECK (status IN ('ACTIVE', 'BLOCKED')),

    CONSTRAINT chk_account_currency
        CHECK (currency IN ('MAD'))
);
```

Le `customer_id` est une référence logique vers `customer-service`.

---

## 8.4 Index

```sql
CREATE INDEX ix_bank_accounts_customer_id
ON bank_accounts(customer_id);

CREATE INDEX ix_bank_accounts_status
ON bank_accounts(status);
```

---

## 8.5 Table `account_transfer_operations`

Cette table protège l’idempotence interne entre `transaction-service` et `account-service`.

```sql
CREATE TABLE account_transfer_operations (
    id UUID PRIMARY KEY,
    operation_id UUID NOT NULL,
    source_account_id UUID NOT NULL,
    destination_account_id UUID NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    initiating_customer_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    rejection_code VARCHAR(80),
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT ux_account_transfer_operation_id
        UNIQUE (operation_id),

    CONSTRAINT fk_transfer_source_account
        FOREIGN KEY (source_account_id)
        REFERENCES bank_accounts(id),

    CONSTRAINT fk_transfer_destination_account
        FOREIGN KEY (destination_account_id)
        REFERENCES bank_accounts(id),

    CONSTRAINT chk_transfer_accounts_different
        CHECK (source_account_id <> destination_account_id),

    CONSTRAINT chk_transfer_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_account_transfer_status
        CHECK (status IN (
            'PROCESSING',
            'APPLIED',
            'REJECTED'
        ))
);
```

---

## 8.6 Table `balance_movements`

```sql
CREATE TABLE balance_movements (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    operation_id UUID NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    balance_before NUMERIC(19,2) NOT NULL,
    balance_after NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    FOREIGN KEY (account_id)
        REFERENCES bank_accounts(id),

    CONSTRAINT chk_movement_type
        CHECK (movement_type IN ('DEBIT', 'CREDIT')),

    CONSTRAINT chk_movement_amount_positive
        CHECK (amount > 0),

    CONSTRAINT ux_balance_movement_operation_account_type
        UNIQUE (operation_id, account_id, movement_type)
);
```

Cette table n’est pas la source de vérité de la transaction globale. Elle trace les mouvements appliqués localement.

---

## 8.7 Transaction SQL locale

Lors d’un virement, `account-service` exécute dans une seule transaction :

```text
1. Réserver operation_id
2. Charger et protéger les deux comptes
3. Vérifier propriétaire, statut, devise et solde
4. Débiter le compte source
5. Créditer le compte destination
6. Insérer deux balance_movements
7. Marquer l’opération APPLIED
8. Commit
```

En cas d’échec :

```text
Rollback complet
```

---

## 8.8 Données exposables

Au propriétaire :

```text
accountId
accountNumber masqué ou complet selon besoin
balance
currency
status
createdAt
```

À `transaction-service` :

```text
operationId
result
rejectionCode
account status minimal
```

Le numéro de compte et le solde ne doivent pas être inclus dans tous les événements par défaut.

---

## 8.9 Données interdites

`account-service` ne doit pas stocker :

```text
password
JWT
refreshToken
profil personnel complet
clé d’idempotence externe du navigateur
décision finale du cycle de transaction
```

---

# 9. Transaction Service — modèle de données

## 9.1 Base

```text
transaction_db
```

## 9.2 Tables initiales

```text
transactions
idempotency_records
transaction_status_history, recommandé
```

---

## 9.3 Table `transactions`

```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    initiating_user_id UUID NOT NULL,
    initiating_customer_id UUID NOT NULL,
    source_account_id UUID NOT NULL,
    destination_account_id UUID NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    risk_decision VARCHAR(30),
    failure_code VARCHAR(80),
    correlation_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_transaction_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_transaction_accounts_different
        CHECK (source_account_id <> destination_account_id),

    CONSTRAINT chk_transaction_status
        CHECK (status IN (
            'PENDING',
            'COMPLETED',
            'REJECTED',
            'FAILED',
            'REVIEW_REQUIRED'
        ))
);
```

Les identifiants utilisateur, client et compte sont des références interservices sans clé étrangère SQL.

---

## 9.4 Index

```sql
CREATE INDEX ix_transactions_customer_created
ON transactions(initiating_customer_id, created_at DESC);

CREATE INDEX ix_transactions_source_account
ON transactions(source_account_id);

CREATE INDEX ix_transactions_destination_account
ON transactions(destination_account_id);

CREATE INDEX ix_transactions_status
ON transactions(status);

CREATE INDEX ix_transactions_correlation_id
ON transactions(correlation_id);
```

---

## 9.5 Table `idempotency_records`

```sql
CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    initiating_user_id UUID NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    request_fingerprint VARCHAR(128) NOT NULL,
    transaction_id UUID,
    processing_status VARCHAR(30) NOT NULL,
    response_status INTEGER,
    response_body JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT ux_idempotency_scope
        UNIQUE (
            initiating_user_id,
            operation_type,
            idempotency_key
        ),

    CONSTRAINT fk_idempotency_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id),

    CONSTRAINT chk_idempotency_processing_status
        CHECK (processing_status IN (
            'PROCESSING',
            'COMPLETED',
            'FAILED'
        ))
);
```

`response_body` ne doit contenir ni secret ni donnée inutile.

---

## 9.6 Table `transaction_status_history`

```sql
CREATE TABLE transaction_status_history (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    reason_code VARCHAR(80),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,

    FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
);
```

Cette table facilite l’audit fonctionnel et le diagnostic.

---

## 9.7 Données exposables

Au client autorisé :

```text
transactionId
sourceAccountId
destinationAccountId
amount
currency
status
createdAt
completedAt
failureCode public et générique
```

À l’audit et au reporting :

```text
transactionId
customerId si nécessaire
montant ou tranche de montant selon besoin
status
riskDecision
occurredAt
correlationId
```

---

## 9.8 Données interdites

Le service ne doit pas posséder :

```text
mot de passe
refresh token
solde comme source de vérité
profil client complet
clé privée JWT
```

---

# 10. Audit Service — modèle de données

## 10.1 Base

```text
audit_db
```

## 10.2 Table `audit_events`

```sql
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    external_event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    actor_id UUID,
    actor_role VARCHAR(40),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(80),
    resource_id VARCHAR(100),
    result VARCHAR(30) NOT NULL,
    severity VARCHAR(30) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    source_service VARCHAR(80) NOT NULL,
    ip_address VARCHAR(64),
    metadata JSONB,

    CONSTRAINT ux_audit_external_event_id
        UNIQUE (external_event_id),

    CONSTRAINT chk_audit_result
        CHECK (result IN (
            'SUCCESS',
            'FAILURE',
            'DENIED'
        )),

    CONSTRAINT chk_audit_severity
        CHECK (severity IN (
            'INFO',
            'LOW',
            'MEDIUM',
            'HIGH',
            'CRITICAL'
        ))
);
```

---

## 10.3 Index

```sql
CREATE INDEX ix_audit_actor
ON audit_events(actor_id);

CREATE INDEX ix_audit_occurred_at
ON audit_events(occurred_at DESC);

CREATE INDEX ix_audit_action
ON audit_events(action);

CREATE INDEX ix_audit_severity
ON audit_events(severity);

CREATE INDEX ix_audit_correlation
ON audit_events(correlation_id);

CREATE INDEX ix_audit_resource
ON audit_events(resource_type, resource_id);
```

---

## 10.4 Immutabilité

Le compte d’exécution de `audit-service` peut avoir :

```text
INSERT
SELECT
```

et, si possible, ne pas posséder :

```text
UPDATE
DELETE
```

Les corrections administratives exceptionnelles doivent être réalisées par une procédure séparée et auditée.

---

## 10.5 Métadonnées JSONB

`metadata` doit être limitée à une liste de champs autorisés.

Données interdites :

```text
password
passwordHash
token
secret
clé privée
payload complet non filtré
```

---

# 11. Risk Service — modèle futur

## 11.1 Base

```text
risk_db
```

Tables possibles :

```text
risk_evaluations
risk_alerts
risk_rule_configurations
```

Le service stocke :

- l’identifiant de la transaction ;
- les règles déclenchées ;
- la décision ;
- les raisons ;
- la date.

Il ne stocke pas une copie complète permanente du compte ou du profil.

---

# 12. Notification Service — modèle futur

## 12.1 Base

```text
notification_db
```

Tables possibles :

```text
notifications
notification_attempts
notification_templates
```

Le service conserve :

- type de notification ;
- destinataire minimal ;
- modèle ;
- statut ;
- nombre de tentatives ;
- erreur technique nettoyée ;
- dates.

Le contenu sensible doit être réduit.

---

# 13. Reporting Service — modèle futur

## 13.1 Base

```text
reporting_db
```

Tables possibles :

```text
daily_transaction_summaries
account_status_summaries
login_failure_summaries
security_alert_summaries
```

Ces données sont des projections dérivées.

Elles peuvent être reconstruites à partir des événements sources.

`reporting-service` ne devient jamais la source de vérité des transactions ou des soldes.

---

# 14. Références interservices

## 14.1 Carte des références

| Service | Champ | Référence logique |
|---|---|---|
| `customer-service` | `auth_user_id` | `auth-service.users.id` |
| `account-service` | `customer_id` | `customer-service.customers.id` |
| `transaction-service` | `initiating_user_id` | `auth-service.users.id` |
| `transaction-service` | `initiating_customer_id` | `customer-service.customers.id` |
| `transaction-service` | `source_account_id` | `account-service.bank_accounts.id` |
| `transaction-service` | `destination_account_id` | `account-service.bank_accounts.id` |
| `risk-service` | `transaction_id` | `transaction-service.transactions.id` |
| `audit-service` | `actor_id` | Identifiant externe selon événement |
| `audit-service` | `resource_id` | Identifiant externe générique |

---

## 14.2 Suppression d’une ressource référencée

Puisqu’il n’existe pas de clé étrangère entre services, une suppression pourrait créer des références orphelines.

Le projet évite cela par les règles suivantes :

- utilisateurs : désactivation ou anonymisation contrôlée ;
- clients : désactivation ;
- comptes : blocage puis futur statut `CLOSED` ;
- transactions : conservation ;
- audits : conservation.

La suppression physique n’est pas utilisée pour les ressources métier historiques.

---

# 15. Contrats de données REST

## 15.1 DTO distinct de l’entité

Les entités JPA ne doivent pas être exposées directement.

```text
Entity
  ↓ mapping
Response DTO
  ↓ Jackson
JSON
```

Cela évite :

- exposition accidentelle de champs ;
- couplage API/base de données ;
- problèmes de relations JPA ;
- modification involontaire de champs protégés.

---

## 15.2 Exemple `AccountResponse`

```json
{
  "id": "9de2...",
  "accountNumber": "ACC-2026-0001",
  "balance": 1250.00,
  "currency": "MAD",
  "status": "ACTIVE",
  "createdAt": "2026-07-21T10:30:00Z"
}
```

Champs exclus :

```text
version
customerId dans certaines vues publiques
détails internes de verrouillage
```

---

## 15.3 Exemple `TransferRequest`

```json
{
  "sourceAccountId": "9de2...",
  "destinationAccountId": "81ac...",
  "amount": 500.00,
  "currency": "MAD"
}
```

L’identité utilisateur et le `customerId` ne sont pas acceptés comme vérité depuis le body. Ils sont dérivés du contexte d’authentification.

---

# 16. Contrats événementiels

## 16.1 Enveloppe commune

```json
{
  "eventId": "uuid",
  "eventType": "TransactionCompleted",
  "eventVersion": 1,
  "occurredAt": "2026-07-21T10:30:00Z",
  "sourceService": "transaction-service",
  "correlationId": "req-uuid",
  "payload": {}
}
```

---

## 16.2 Règles

Chaque événement doit :

- posséder un `eventId` unique ;
- être versionné ;
- être idempotent côté consommateur ;
- contenir uniquement les données utiles ;
- ne contenir aucun secret ;
- rester compréhensible sans accès à la base du producteur ;
- éviter d’exposer une entité complète.

---

## 16.3 Exemple `TransactionCompleted`

```json
{
  "eventId": "2e4b...",
  "eventType": "TransactionCompleted",
  "eventVersion": 1,
  "occurredAt": "2026-07-21T10:30:00Z",
  "sourceService": "transaction-service",
  "correlationId": "req-47aa...",
  "payload": {
    "transactionId": "a10e...",
    "initiatingCustomerId": "fa91...",
    "amount": 500.00,
    "currency": "MAD",
    "status": "COMPLETED"
  }
}
```

Les numéros de compte complets ne sont pas inclus sans nécessité.

---

## 16.4 Exemple `LoginFailed`

```json
{
  "eventId": "uuid",
  "eventType": "LoginFailed",
  "eventVersion": 1,
  "occurredAt": "2026-07-21T10:30:00Z",
  "sourceService": "auth-service",
  "correlationId": "req-uuid",
  "payload": {
    "userId": "uuid-ou-null",
    "result": "FAILURE",
    "reasonCode": "INVALID_CREDENTIALS"
  }
}
```

L’email en clair peut être omis ou masqué.

---

# 17. Cohérence et scénarios de panne

## 17.1 Création utilisateur puis profil

### Premier incrément synchrone

```text
1. auth-service crée l’utilisateur
2. le frontend demande la création du profil
3. customer-service utilise l’identité du JWT
```

État possible :

```text
Utilisateur créé, profil pas encore créé
```

Cet état est autorisé temporairement et l’interface demande de compléter le profil.

---

## 17.2 Création d’un compte

```text
1. account-service vérifie le client actif
2. account-service crée le compte localement
3. événement AccountCreated publié
```

Si la publication échoue après création, un mécanisme transactionnel outbox sera ajouté pour garantir la reprise.

---

## 17.3 Virement

```text
1. transaction-service réserve la clé d’idempotence
2. crée une transaction PENDING
3. appelle account-service avec transactionId comme operationId
4. account-service applique débit/crédit atomiquement
5. transaction-service enregistre COMPLETED
```

### Cas incertain

Si l’appel expire après que `account-service` a validé :

- `transaction-service` ne génère pas un nouvel `operationId` ;
- il interroge `account-service` avec le même identifiant ;
- `account-service` retourne le résultat existant ;
- la transaction est réconciliée.

---

## 17.4 Audit indisponible

Une indisponibilité de `audit-service` ne doit pas annuler un virement déjà validé.

L’événement reste dans le broker ou dans une outbox, puis est retraité.

---

# 18. Transactional Outbox

## 18.1 Problème

Ce scénario est dangereux :

```text
1. Modifier la base
2. Commit
3. Publier un événement
4. Publication échoue
```

La donnée existe, mais l’événement est perdu.

---

## 18.2 Solution cible

Chaque service critique peut utiliser une table `outbox_events`.

```sql
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER NOT NULL,
    payload JSONB NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    publication_attempts INTEGER NOT NULL DEFAULT 0
);
```

La modification métier et l’insertion dans l’outbox sont effectuées dans la même transaction SQL.

Un processus séparé publie ensuite l’événement.

---

## 18.3 Priorité

L’outbox est fortement recommandée pour :

- `auth-service` ;
- `customer-service` ;
- `account-service` ;
- `transaction-service`.

Elle peut être introduite progressivement après le premier flux fonctionnel, mais avant de considérer la communication événementielle comme fiable.

---

# 19. Migrations Flyway

## 19.1 Un historique par service

Exemple :

```text
services/auth-service/src/main/resources/db/migration/
├── V1__create_users.sql
├── V2__create_refresh_tokens.sql
└── V3__add_login_indexes.sql
```

Chaque service possède ses propres migrations.

---

## 19.2 Règles

- une migration appliquée n’est pas modifiée ;
- une nouvelle modification crée une nouvelle version ;
- les migrations sont testées en CI ;
- aucune migration d’un service ne modifie la base d’un autre ;
- les scripts ne contiennent aucun secret ;
- les migrations destructrices sont évitées ou réalisées en plusieurs étapes.

---

# 20. Indexation et performance

Les index doivent être créés selon les requêtes réelles.

Index prioritaires :

- email normalisé ;
- `auth_user_id` ;
- numéro de client ;
- `customer_id` des comptes ;
- numéro de compte ;
- `operation_id` ;
- clé d’idempotence ;
- transactions par client et date ;
- audits par date, acteur, sévérité et correlation ID.

Les index ne remplacent pas la pagination.

---

# 21. Rétention des données

## 21.1 Données conservées

| Donnée | Politique MVP |
|---|---|
| Utilisateurs | Conservés, désactivables |
| Refresh tokens expirés | Nettoyage planifié possible |
| Clients | Conservés, désactivables |
| Comptes | Conservés, blocage/fermeture logique |
| Transactions | Conservées |
| Idempotency records | Conservés pendant la démonstration |
| Audit events | Conservés |
| Notifications | Conservées selon besoin de démonstration |
| Reporting | Reconstructible |

---

## 21.2 Purge contrôlée

Une purge est autorisée uniquement pour :

- environnement local ;
- données fictives ;
- procédure documentée ;
- réinitialisation de démonstration.

Elle ne doit pas être exposée comme endpoint public.

---

# 22. Sauvegarde et restauration

Pour le MVP local :

- volumes Docker nommés ;
- possibilité d’export `pg_dump` ;
- procédure de réinitialisation documentée.

Pour staging :

- sauvegarde planifiée recommandée ;
- test de restauration au moins une fois ;
- secrets de sauvegarde externalisés.

---

# 23. Sécurité des connexions PostgreSQL

Chaque service doit utiliser :

- une URL propre ;
- un utilisateur propre ;
- un mot de passe propre ;
- des variables d’environnement ou secrets ;
- TLS lorsque l’environnement le permet ;
- un pool de connexions limité ;
- des timeouts ;
- aucun compte superutilisateur pour l’application.

Exemple de configuration :

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

---

# 24. Tests de propriété et d’intégrité

## 24.1 Tests de base

```text
DATA-TEST-01 : un email dupliqué est refusé par PostgreSQL.
DATA-TEST-02 : deux profils pour le même authUserId sont refusés.
DATA-TEST-03 : deux comptes avec le même numéro sont refusés.
DATA-TEST-04 : un solde négatif est refusé.
DATA-TEST-05 : un operationId appliqué deux fois ne crée pas deux mouvements.
DATA-TEST-06 : une clé d’idempotence dupliquée dans le même périmètre est refusée.
DATA-TEST-07 : un eventId d’audit dupliqué ne crée pas deux audits.
```

---

## 24.2 Tests d’isolation

```text
ISOLATION-TEST-01 :
auth-service ne peut pas se connecter à account_db.

ISOLATION-TEST-02 :
transaction-service ne possède aucun droit SQL sur account_db.

ISOLATION-TEST-03 :
audit-service ne peut pas modifier les données métier des autres services.
```

---

## 24.3 Tests de confidentialité

```text
PRIVACY-TEST-01 :
passwordHash n’apparaît dans aucun DTO.

PRIVACY-TEST-02 :
refreshToken brut n’apparaît dans aucun log.

PRIVACY-TEST-03 :
les événements ne contiennent pas de secret.

PRIVACY-TEST-04 :
un client ne reçoit aucune donnée appartenant à un autre client.
```

---

# 25. Matrice finale de propriété

| Service | Base | Tables principales | Source de vérité |
|---|---|---|---|
| `auth-service` | `auth_db` | `users`, `refresh_tokens`, `mfa_challenges` | Identité, rôles, sessions |
| `customer-service` | `customer_db` | `customers` | Profil et statut client |
| `account-service` | `account_db` | `bank_accounts`, `account_transfer_operations`, `balance_movements` | Comptes et soldes |
| `transaction-service` | `transaction_db` | `transactions`, `idempotency_records`, `transaction_status_history` | Cycle de vie du virement |
| `audit-service` | `audit_db` | `audit_events` | Journal d’audit |
| `risk-service` | `risk_db` | `risk_evaluations`, `risk_alerts` | Décisions de risque |
| `notification-service` | `notification_db` | `notifications`, `notification_attempts` | État des notifications |
| `reporting-service` | `reporting_db` | tables d’agrégats | Projections statistiques |

---

# 26. Décisions à valider

Les décisions proposées sont :

1. une base logique et un utilisateur PostgreSQL par service ;
2. aucune clé étrangère entre bases ;
3. UUID pour les identifiants ;
4. `BigDecimal` et `NUMERIC(19,2)` pour les montants ;
5. `account-service` comme seule source de vérité du solde ;
6. mouvement débit/crédit atomique dans `account-service` ;
7. `transaction-service` propriétaire de l’idempotence externe ;
8. `transactionId` utilisé comme `operationId` interne ;
9. tables historiques non supprimables par API ;
10. audit fonctionnellement immuable ;
11. événements à payload minimal ;
12. introduction progressive du pattern Transactional Outbox ;
13. Flyway séparé pour chaque service ;
14. un seul rôle principal par utilisateur dans le premier MVP ;
15. devise initiale limitée à `MAD`.

---

# 27. Critères de validation du document

Ce document est validé lorsque :

- chaque donnée possède un unique service propriétaire ;
- les tables principales sont définies ;
- les contraintes critiques sont identifiées ;
- les références interservices sont distinguées des clés étrangères ;
- aucune jointure interbase n’est nécessaire ;
- les données exposables et interdites sont connues ;
- le flux de virement peut être récupéré après un timeout ;
- les règles de migrations sont acceptées ;
- les tests d’intégrité et d’isolation sont prévus ;
- les décisions de la section précédente sont confirmées ou corrigées.

---

# 28. Prochaine étape

Le document suivant sera :

```text
docs/security/initial-threat-model.md
```

Il identifiera les actifs à protéger, les frontières de confiance, les menaces STRIDE, les scénarios d’attaque prioritaires et les premières mesures de réduction des risques.
