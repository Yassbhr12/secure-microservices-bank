# SecureMicroservicesBank — Project Scope

**Document :** Définition du périmètre du projet  
**Projet :** SecureMicroservicesBank  
**Version :** 1.0  
**Statut :** Brouillon initial de cadrage  
**Référence :** Cahier des charges SecureMicroservicesBank, version 1.0 du 19 juin 2026

---

## 1. Contexte

SecureMicroservicesBank est un projet pédagogique avancé visant à simuler une application bancaire moderne conçue selon une architecture microservices et accompagnée d’une chaîne DevSecOps complète.

Le projet ne consiste pas uniquement à développer des fonctionnalités bancaires. Il doit également démontrer la capacité à :

- sécuriser les APIs et les communications entre services ;
- automatiser les tests et les contrôles de sécurité ;
- construire et analyser des images Docker ;
- déployer l’application sur Kubernetes ;
- superviser l’état technique et sécuritaire du système ;
- assurer la traçabilité des artefacts logiciels ;
- documenter les décisions d’architecture et de sécurité.

Le système utilise uniquement des données fictives et ne réalise aucune opération bancaire réelle.

---

## 2. Problématique

Comment concevoir, développer, sécuriser, tester, déployer et superviser une application bancaire fondée sur plusieurs microservices, tout en intégrant la sécurité dans chaque étape du cycle de développement et de déploiement ?

Le projet doit répondre à plusieurs problèmes techniques :

- séparation correcte des responsabilités entre microservices ;
- authentification et autorisation sécurisées ;
- protection des données bancaires fictives ;
- prévention des accès non autorisés aux comptes d’autres clients ;
- garantie qu’un virement ne soit pas exécuté plusieurs fois ;
- traçabilité des opérations sensibles ;
- détection automatique des vulnérabilités et des secrets exposés ;
- sécurisation du déploiement Kubernetes ;
- supervision des erreurs, anomalies et comportements suspects.

---

## 3. Vision du projet

La vision cible est de produire un projet présentable dans un portfolio professionnel, capable de démontrer un niveau avancé en développement backend, architecture microservices, DevSecOps, sécurité applicative, conteneurisation, Kubernetes et observabilité.

Le système final doit comporter :

1. une application bancaire fictive fonctionnelle ;
2. plusieurs microservices indépendants ;
3. une API Gateway comme point d’entrée unique ;
4. une authentification sécurisée avec JWT, rôles et MFA simple ;
5. des tests automatisés ;
6. une pipeline CI/CD avec contrôles de qualité et de sécurité ;
7. des images Docker versionnées, scannées et signées ;
8. un déploiement Kubernetes sécurisé ;
9. une stack d’observabilité ;
10. une détection des événements de sécurité runtime.

---

## 4. Objectif général

Construire une application bancaire microservices sécurisée, testée automatiquement, conteneurisée, déployée sur Kubernetes et supervisée par une stack d’observabilité et de sécurité runtime.

La sécurité doit être intégrée dès la conception et non ajoutée uniquement à la fin du projet.

---

## 5. Objectifs spécifiques

### 5.1 Développement applicatif

- développer plusieurs microservices Spring Boot indépendants ;
- exposer des APIs REST documentées avec OpenAPI ;
- développer une interface utilisateur avec React ;
- utiliser PostgreSQL pour la persistance ;
- appliquer le principe de séparation des responsabilités.

### 5.2 Sécurité applicative

- hacher les mots de passe avec BCrypt ou Argon2 ;
- utiliser des access tokens et refresh tokens ;
- appliquer un contrôle d’accès basé sur les rôles ;
- empêcher un client d’accéder aux ressources d’un autre client ;
- valider toutes les données reçues par les APIs ;
- ajouter un MFA simple pour les opérations sensibles ;
- appliquer du rate limiting sur les endpoints sensibles ;
- journaliser les opérations critiques.

### 5.3 DevSecOps

- automatiser les builds et les tests ;
- intégrer du SAST, SCA et secret scanning ;
- analyser les images Docker ;
- analyser les manifests Kubernetes et les fichiers IaC ;
- exécuter un scan DAST sur l’environnement de staging ;
- générer un SBOM pour chaque release ;
- signer les images publiées ;
- bloquer automatiquement les changements ne respectant pas les quality gates.

### 5.4 Kubernetes et exploitation

- déployer les services dans des namespaces séparés ;
- utiliser ConfigMap et Secret ;
- appliquer RBAC, NetworkPolicy et SecurityContext ;
- exécuter les conteneurs applicatifs avec un utilisateur non-root ;
- configurer readiness probes et liveness probes ;
- définir des limites de ressources ;
- permettre le rollback d’un déploiement.

### 5.5 Observabilité et sécurité runtime

- exposer les métriques avec Spring Boot Actuator ;
- collecter les métriques avec Prometheus ;
- créer des dashboards Grafana ;
- centraliser les logs avec Loki ou ELK ;
- propager un correlation ID entre les services ;
- détecter des comportements suspects avec Falco ;
- documenter des scénarios d’incident et leurs runbooks.

---

## 6. Acteurs du système

### 6.1 Visiteur

Peut :

- créer un compte utilisateur ;
- se connecter.

### 6.2 Client bancaire

Peut :

- consulter et modifier son profil ;
- consulter ses propres comptes ;
- consulter ses soldes ;
- effectuer des virements internes ;
- consulter son historique de transactions ;
- recevoir des notifications lors d’opérations sensibles.

Ne peut pas :

- consulter les comptes d’un autre client ;
- bloquer ou débloquer un compte ;
- modifier directement un solde ;
- consulter les journaux globaux d’audit.

### 6.3 Administrateur

Peut :

- consulter les clients ;
- activer ou désactiver un client ;
- bloquer ou débloquer un compte ;
- consulter les comptes bloqués ;
- consulter les transactions suspectes ;
- consulter les statistiques et événements de sécurité.

Ne peut pas :

- modifier directement le solde d’un compte ;
- supprimer les transactions ou les journaux d’audit.

### 6.4 Auditeur

Peut :

- consulter les journaux d’audit ;
- filtrer les événements par acteur, action, date ou sévérité ;
- consulter les opérations sensibles.

Ne peut pas :

- modifier les données bancaires ;
- supprimer ou modifier les journaux d’audit.

### 6.5 Security Viewer / Security Analyst

Peut :

- consulter les alertes de sécurité ;
- analyser les vulnérabilités et événements suspects ;
- consulter les rapports de sécurité ;
- examiner les incidents runtime.

### 6.6 DevSecOps Engineer

Responsable de :

- la pipeline CI/CD ;
- la configuration des scans ;
- la construction et publication des images ;
- le déploiement Kubernetes ;
- l’observabilité ;
- la configuration des alertes ;
- la sécurité de la supply chain.

---

## 7. Périmètre fonctionnel global

Le projet complet comprend les domaines suivants :

- authentification et identité ;
- gestion des profils clients ;
- gestion des comptes bancaires fictifs ;
- transactions internes ;
- contrôle de risque ;
- notifications ;
- audit ;
- administration ;
- reporting ;
- observabilité ;
- sécurité applicative et DevSecOps.

---

## 8. Périmètre du MVP

Le MVP doit se concentrer sur le cœur fonctionnel et sécuritaire du système.

### 8.1 Microservices du MVP

- `api-gateway` ;
- `auth-service` ;
- `customer-service` ;
- `account-service` ;
- `transaction-service` ;
- `audit-service`.

### 8.2 Fonctionnalités du MVP

#### Authentification

- inscription avec email unique ;
- mot de passe fort et haché ;
- connexion ;
- génération d’un access token ;
- génération et rotation d’un refresh token ;
- gestion initiale des rôles `CLIENT`, `ADMIN` et `AUDITOR` ;
- blocage temporaire après plusieurs échecs de connexion.

#### Gestion client

- création d’un profil client fictif ;
- consultation de son propre profil ;
- modification des informations autorisées ;
- activation ou désactivation par un administrateur.

#### Gestion des comptes

- création d’un compte bancaire fictif ;
- consultation de ses propres comptes ;
- consultation du solde et du statut ;
- blocage et déblocage par un administrateur ;
- interdiction des opérations sur un compte bloqué.

#### Transactions

- virement interne entre deux comptes ;
- validation du montant ;
- vérification du solde ;
- vérification de l’état des comptes ;
- interdiction d’un virement vers le même compte ;
- gestion d’une clé d’idempotence ;
- historique des transactions.

#### Audit

- génération d’un événement pour toute opération sensible ;
- stockage de l’acteur, de l’action, du résultat, de la ressource, de la date et du correlation ID ;
- consultation des événements par un administrateur ou un auditeur ;
- interdiction de modification et suppression par API standard.

### 8.3 Parcours principal du MVP

1. Un visiteur crée un compte utilisateur.
2. Il se connecte et obtient un JWT.
3. Un profil client fictif est créé.
4. Un compte bancaire fictif est associé au client.
5. Le client consulte son compte.
6. Le client effectue un virement.
7. Le système vérifie les comptes et le solde.
8. Le système traite la requête une seule fois grâce à la clé d’idempotence.
9. La transaction est enregistrée.
10. Un événement d’audit est créé.

---

## 9. Fonctionnalités reportées après le MVP

Les éléments suivants seront ajoutés après la validation du flux bancaire principal :

- `risk-service` avec décisions `APPROVED`, `REVIEW_REQUIRED` et `REJECTED` ;
- `notification-service` avec email ou simulation console ;
- `reporting-service` pour les statistiques ;
- MFA pour les transactions sensibles ;
- dashboard administrateur avancé ;
- événements asynchrones avec RabbitMQ ;
- observabilité complète ;
- déploiement Kubernetes multi-environnements ;
- scans DevSecOps complets ;
- SBOM, signature et provenance ;
- sécurité runtime avec Falco.

---

## 10. Hors périmètre

Les éléments suivants ne font pas partie du projet :

- paiements bancaires réels ;
- intégration avec une vraie banque ;
- cartes bancaires réelles ;
- transfert d’argent réel ;
- données personnelles réelles ;
- conformité bancaire officielle ;
- infrastructure cloud hautement disponible obligatoire ;
- système de crédit réel ;
- gestion de chèques ;
- change de devises réel ;
- connexion à des réseaux de paiement réels ;
- utilisation de secrets ou identités de production réels.

---

## 11. Contraintes techniques

- backend développé avec Java 21 et Spring Boot 4.1.x ;
- frontend développé avec React et TypeScript ;
- APIs exposées au format REST/JSON ;
- base de données PostgreSQL ;
- une base ou un schéma séparé par microservice ;
- API Gateway avec Spring Cloud Gateway ;
- authentification avec Spring Security et JWT ;
- tests avec JUnit, Mockito, MockMvc et Testcontainers ;
- exécution locale avec Docker Compose ;
- déploiement avec Kubernetes ;
- CI/CD avec GitHub Actions ;
- registre d’images avec GitHub Container Registry ou Docker Hub ;
- documentation OpenAPI pour les APIs principales.

---

## 12. Contraintes de sécurité

- aucun mot de passe stocké en clair ;
- aucun secret commité dans Git ;
- aucun token ou mot de passe écrit dans les logs ;
- contrôle d’autorisation au niveau des rôles et de la propriété des ressources ;
- messages d’erreur génériques côté client ;
- détails techniques réservés aux logs internes ;
- validation systématique des entrées ;
- protection contre les accès aux objets d’un autre utilisateur ;
- journalisation des opérations critiques ;
- utilisation de correlation IDs ;
- images Docker analysées avant publication ;
- conteneurs exécutés sans privilèges inutiles ;
- réseau Kubernetes limité aux communications nécessaires.

---

## 13. Hypothèses

- toutes les données utilisées sont fictives ;
- les notifications email peuvent être simulées localement ;
- le cluster Kubernetes peut être local avec Kind, Minikube ou Docker Desktop ;
- la production est un environnement simulé de démonstration ;
- les outils DevSecOps seront intégrés progressivement ;
- le projet peut être développé par une seule personne ;
- les microservices seront regroupés dans un monorepo ;
- RabbitMQ sera préféré au début pour simplifier la communication événementielle.

---

## 14. Risques principaux

| Risque | Impact | Mesure de réduction |
|---|---|---|
| Périmètre trop large | Projet incomplet | Prioriser le MVP |
| Trop de microservices développés en parallèle | Services superficiels | Construire un flux complet progressivement |
| Complexité des transactions distribuées | Incohérences | Commencer par des transactions simples puis introduire Saga/Outbox |
| Complexité Kubernetes | Blocage du projet | Valider d’abord Docker Compose |
| Trop d’outils DevSecOps | Configuration superficielle | Ajouter les outils par étapes |
| Fuite de secrets | Compromission | `.gitignore`, Gitleaks, GitHub Secrets |
| Accès aux données d’un autre client | Vulnérabilité critique | RBAC, ownership et tests d’autorisation |
| Double exécution d’un virement | Perte de cohérence | Clé d’idempotence et contrainte d’unicité |
| Faux positifs de sécurité | Pipeline bloquée | Documenter les exceptions et risques acceptés |

---

## 15. Critères de succès du MVP

Le MVP sera considéré comme réussi lorsque :

- l’environnement démarre avec Docker Compose ;
- l’utilisateur peut s’inscrire et se connecter ;
- un access token et un refresh token sont générés ;
- un client peut consulter uniquement son propre profil ;
- un client peut consulter uniquement ses propres comptes ;
- un administrateur peut bloquer ou débloquer un compte ;
- un administrateur ne peut pas modifier directement le solde ;
- un virement valide est enregistré ;
- un virement invalide est refusé ;
- une même clé d’idempotence ne produit pas deux virements ;
- chaque opération critique produit un événement d’audit ;
- un auditeur peut consulter les événements sans les modifier ;
- les APIs principales sont documentées avec OpenAPI ;
- les tests essentiels sont automatisés ;
- aucun secret n’est présent dans le dépôt Git.

---

## 16. Critères de succès du projet final

Le projet final sera considéré comme terminé lorsque :

- au moins cinq microservices principaux fonctionnent ;
- les rôles `CLIENT`, `ADMIN` et `AUDITOR` sont respectés ;
- la pipeline bloque les tests échoués, les secrets et les vulnérabilités critiques ;
- les images sont construites, scannées et publiées ;
- le déploiement Kubernetes de staging fonctionne ;
- les manifests incluent probes, limites de ressources, SecurityContext et NetworkPolicy ;
- un scan DAST est exécuté sur staging ;
- un SBOM est généré pour chaque release ;
- les images de release sont signées ;
- Prometheus et Grafana affichent des dashboards utiles ;
- Falco détecte au moins un scénario runtime ;
- la documentation permet d’installer, tester, déployer et comprendre le système.

---

## 17. Livrables associés au périmètre

- code source des microservices et du frontend ;
- documentation d’architecture ;
- documentation des APIs ;
- modèle de menace STRIDE ;
- Dockerfiles sécurisés ;
- fichier `docker-compose.yml` ;
- manifests Kubernetes ;
- workflows GitHub Actions ;
- rapports de tests et de couverture ;
- rapports SAST, SCA, secret scanning, image scanning, IaC scanning et DAST ;
- SBOM ;
- signatures des images ;
- dashboards Grafana ;
- scénarios Falco ;
- runbooks de gestion d’incident ;
- README et guide de déploiement ;
- rapport final et démonstration vidéo.

---

## 18. Décision de cadrage initiale

La première version du projet se concentrera sur un flux bancaire vertical complet et sécurisé :

```text
Inscription
    ↓
Connexion JWT
    ↓
Profil client
    ↓
Compte bancaire
    ↓
Virement idempotent
    ↓
Audit
```

La priorité est de terminer un flux cohérent, testé et sécurisé avant d’ajouter les fonctionnalités avancées, Kubernetes et les outils DevSecOps complémentaires.

---

## 19. Prochaine étape

Après validation de ce document, le prochain livrable sera :

```text
docs/use-cases.md
```

Ce document détaillera chaque cas d’utilisation avec :

- l’acteur ;
- les préconditions ;
- le scénario nominal ;
- les scénarios d’erreur ;
- les règles métier ;
- les événements d’audit ;
- les critères d’acceptation.
