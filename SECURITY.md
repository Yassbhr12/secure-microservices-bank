# Politique de sécurité

La sécurité fait partie du périmètre principal de SecureMicroservicesBank. Cette politique explique quelles versions sont prises en charge, comment signaler une vulnérabilité et quelles règles doivent être respectées dans le dépôt.

## Statut et périmètre

SecureMicroservicesBank est actuellement un projet pédagogique en développement. Il simule des fonctions bancaires avec des données fictives et ne doit pas être utilisé en production ni traiter des données personnelles ou financières réelles.

Les documents d’architecture décrivent également des contrôles cibles qui ne sont pas encore tous implémentés. Leur présence dans la documentation ne constitue pas une garantie de sécurité opérationnelle.

## Versions prises en charge

| Version | Prise en charge | Remarque |
|---|---|---|
| Branche `main` | Oui | Prise en charge au meilleur effort pendant le développement |
| Branches de fonctionnalité | Limitée | Uniquement pendant la revue de la pull request |
| Releases publiées | Aucune | Aucune version de production n’existe actuellement |

## Signaler une vulnérabilité

Ne publiez pas de vulnérabilité, d’exploit, de secret ou de donnée sensible dans une issue publique, une discussion ou une pull request.

Utilisez, par ordre de préférence :

1. la fonctionnalité privée GitHub **Security > Advisories** lorsqu’elle est disponible ;
2. un canal privé déjà établi avec le propriétaire du dépôt ;
3. la méthode de contact indiquée sur le profil GitHub du propriétaire du dépôt.

Si le dépôt devient public, vérifiez toujours que votre signalement reste privé avant d’ajouter une preuve de concept.

## Informations attendues

Un rapport utile doit contenir autant que possible :

- un résumé clair de la vulnérabilité ;
- le composant, le fichier, l’endpoint ou la version concernés ;
- les préconditions nécessaires ;
- des étapes de reproduction minimales ;
- l’impact technique et métier potentiel ;
- une preuve de concept non destructive ;
- une proposition de correction, si elle est connue ;
- toute indication montrant qu’un secret ou une donnée aurait pu être exposé.

N’incluez jamais de donnée bancaire réelle, de donnée personnelle réelle ou d’identifiant appartenant à un tiers.

## Délais indicatifs de traitement

Les délais suivants sont des objectifs de meilleur effort et non un engagement contractuel :

| Étape | Objectif |
|---|---|
| Accusé de réception | Sous 3 jours ouvrés |
| Première qualification | Sous 7 jours ouvrés |
| Point d’avancement | Sous 14 jours ouvrés |
| Correction | Selon la gravité et la complexité |

La priorité est donnée aux vulnérabilités permettant notamment :

- une exécution de code ou une compromission de l’environnement ;
- un contournement de l’authentification ou de l’autorisation ;
- une élévation vers un rôle administrateur ;
- l’accès aux ressources d’un autre client ;
- la modification frauduleuse d’un solde ou d’une transaction ;
- la double exécution d’un virement ;
- l’exposition d’un secret, d’un token ou d’un mot de passe ;
- la suppression ou la falsification des événements d’audit.

## Divulgation coordonnée

Après réception du rapport :

1. la vulnérabilité est reproduite et qualifiée ;
2. une correction et des tests de non-régression sont préparés ;
3. les secrets éventuellement concernés sont révoqués ou renouvelés ;
4. l’impact et les risques résiduels sont documentés ;
5. une divulgation éventuelle est coordonnée avec le rapporteur.

Ne rendez pas la vulnérabilité publique avant qu’une correction soit disponible ou qu’une date de divulgation ait été convenue.

## Règles de sécurité pour les contributions

Toute contribution doit respecter les règles suivantes :

- utiliser exclusivement des données fictives ;
- ne jamais commiter `.env`, un secret, un token, une clé privée ou un mot de passe réel ;
- utiliser la configuration externe ou un gestionnaire de secrets adapté à l’environnement ;
- hacher les mots de passe avec BCrypt ou Argon2 ;
- ne jamais écrire de mot de passe, token ou secret dans les logs ;
- utiliser des DTO et valider toutes les entrées non fiables ;
- retourner des erreurs publiques génériques sans détail sensible ;
- appliquer RBAC et vérifier la propriété des ressources côté service ;
- appliquer le moindre privilège aux comptes PostgreSQL et aux identités techniques ;
- versionner les changements de schéma avec Flyway ;
- ajouter des tests d’autorisation et de non-régression pour tout correctif sensible ;
- passer par une branche et une pull request avant toute fusion dans `main` ;
- documenter toute exception temporaire dans [`security/risk-acceptance.md`](security/risk-acceptance.md).

## Procédure en cas de secret exposé

La suppression du secret dans un nouveau commit ne suffit pas, car il peut rester accessible dans l’historique Git, les logs ou les caches.

En cas d’exposition réelle ou suspectée :

1. révoquer ou renouveler immédiatement le secret ;
2. identifier les services, données et environnements accessibles avec ce secret ;
3. vérifier les journaux pour rechercher une utilisation non autorisée ;
4. retirer le secret du code et le remplacer par une configuration externe ;
5. nettoyer l’historique Git uniquement selon une procédure coordonnée ;
6. documenter l’incident, la correction et le risque résiduel ;
7. ajouter un contrôle empêchant la réintroduction du même problème.

Un secret ayant été commité ou publié doit être considéré comme compromis, même si le dépôt était privé.

## Références internes

- [Modèle de menace STRIDE](docs/security/initial-threat-model.md)
- [Règles métier et de sécurité](docs/business-rules.md)
- [Propriété et gouvernance des données](docs/architecture/data-ownership.md)
- [Registre d’acceptation des risques](security/risk-acceptance.md)

## Programme de récompense

Ce projet ne dispose actuellement d’aucun programme de bug bounty et ne promet aucune récompense financière. Les recherches doivent rester légales, non destructives et limitées aux environnements explicitement autorisés.
