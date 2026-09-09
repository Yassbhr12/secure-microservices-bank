# ADR-0001 — Baseline Java et Spring Boot

**Statut :** Acceptée  
**Date :** 9 septembre 2026  
**Périmètre :** Tous les microservices backend

## Contexte

Le cahier des charges initial autorise Java 17 ou Java 21 et recommande Spring Boot 3.x. Le premier module `auth-service` a ensuite été créé avec Java 21 et Spring Boot 4.1.0, tandis que `docs/project-scope.md` mentionnait encore Spring Boot 3.x.

Cette incohérence pouvait conduire les futurs services à utiliser des versions différentes, compliquer la gestion des dépendances, rendre les exemples incompatibles et produire des comportements différents entre le poste local, la CI et les images Docker.

Au moment de cette décision, Spring Boot 4.1.1 est une version stable. Sa documentation officielle indique une compatibilité avec Java 17 à Java 26 et Maven 3.6.3 ou plus récent. Le Maven Wrapper du projet utilise Maven 3.9.16.

## Décision

Le socle backend officiel de SecureMicroservicesBank est :

| Composant | Baseline retenue |
|---|---|
| Java | Java 21 LTS |
| Spring Boot | Ligne 4.1.x |
| Version Spring Boot courante | 4.1.1 |
| Maven | Maven Wrapper 3.9.16 |

Les règles suivantes s’appliquent :

1. tous les microservices backend doivent cibler Java 21 ;
2. tous les microservices Spring Boot doivent rester sur la même ligne 4.1.x ;
3. la version patch Spring Boot est fixée explicitement dans le parent Maven ;
4. une mise à jour patch doit passer par une pull request avec compilation et tests ;
5. un changement de version mineure ou majeure nécessite une nouvelle décision d’architecture ;
6. les futures images Docker et les workflows CI doivent utiliser un JDK 21 ;
7. la compatibilité du train Spring Cloud devra être vérifiée avant l’ajout de l’API Gateway.

## Justification

### Java 21

- version LTS adaptée à un projet maintenu sur plusieurs mois ;
- déjà configurée dans `auth-service` ;
- compatible avec Spring Boot 4.1.x ;
- évite de mélanger plusieurs niveaux de langage entre les microservices.

### Spring Boot 4.1.x

- ligne stable utilisée par l’implémentation actuelle ;
- gestion centralisée et cohérente des versions des dépendances Spring ;
- fondation commune pour Web MVC, Validation, Data JPA, Actuator et Testcontainers ;
- évite de maintenir simultanément des services Spring Boot 3.x et 4.x.

### Version patch 4.1.1

- version stable courante lors de la décision ;
- reste dans la ligne 4.1.x choisie ;
- permet de bénéficier des corrections de maintenance publiées après 4.1.0.

## Conséquences

### Conséquences positives

- environnement identique pour tous les microservices ;
- documentation et code alignés ;
- builds locaux et CI plus reproductibles ;
- maintenance des dépendances simplifiée ;
- intégration future des contrôles SCA et des mises à jour automatisées facilitée.

### Contraintes

- Java 21 doit être installé sur chaque poste de développement et dans la CI ;
- les exemples écrits uniquement pour Spring Boot 3 doivent être adaptés avant utilisation ;
- aucune fonctionnalité propre à une version Java supérieure à 21 ne doit être utilisée ;
- toute extension Spring doit être vérifiée pour Spring Boot 4.1.x avant son ajout.

## Vérification locale

Sous Windows PowerShell :

```powershell
java --version
Set-Location services/auth-service
.\mvnw.cmd --version
.\mvnw.cmd help:evaluate -Dexpression=project.parent.version -q -DforceStdout
```

Résultats attendus :

```text
Java 21
Apache Maven 3.9.16
4.1.1
```

## Références

- [Spring Boot — System Requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Boot — Project Page](https://spring.io/projects/spring-boot/)
- [Oracle Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html)
