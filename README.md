# AtelierAndroid_TP04_Alternatif

**Application Android de gestion des inscriptions avec capacité limitée par groupe, liste d'attente FIFO et gestion dynamique des places**, développée en Kotlin dans le cadre du module Développement Mobile.

## Badges

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

---

## Table des matières

- [1. Fonctionnalités](#1-fonctionnalités)
- [2. Règles de gestion](#2-règles-de-gestion)
- [3. Technologies utilisées](#3-technologies-utilisées)
- [4. Structure du projet](#4-structure-du-projet)
- [5. Prérequis](#5-prérequis)
- [6. Configuration et installation](#6-configuration-et-installation)
- [7. Compilation et exécution](#7-compilation-et-exécution)
- [8. Scénarios de test](#8-scénarios-de-test)
- [9. Réponses aux questions du TP](#9-réponses-aux-questions-du-tp)
- [10. Dépannage](#10-dépannage)
- [11. Auteur](#11-auteur)

---

## 1. Fonctionnalités

### Formulaire d'inscription

- Saisie du nom, du prénom et de l'âge du participant.
- Sélection du groupe parmi `G1`, `G2` et `G3` à l'aide d'un `Spinner`.
- Choix du soutien aux tests avec des `RadioButton`.
- Confirmation de l'inscription avec une `CheckBox`.
- Validation des champs avant toute opération.
- **Afficher** : prévisualiser la fiche du participant sans modifier les inscriptions.
- **Enregistrer** : inscrire un participant en tenant compte de la capacité du groupe et de la liste d'attente.
- **Effacer** : réinitialiser les champs du formulaire sans supprimer les inscriptions existantes.

### Capacité et liste d'attente

- Capacité maximale de **2 participants confirmés par groupe**.
- Gestion indépendante des places disponibles pour `G1`, `G2` et `G3`.
- Attribution automatique d'un ticket d'attente aux participants lorsque leur groupe est complet.
- Numérotation croissante des tickets : `1`, `2`, `3`, etc.
- Promotion automatique du participant ayant le plus petit ticket d'attente lorsqu'une place se libère.
- Respect de l'ordre FIFO (*First In, First Out*) au sein de chaque groupe.
- Affichage des places disponibles par groupe.
- Affichage de la liste d'attente triée par groupe, puis par numéro de ticket.

### Gestion des participants

- **Liste** : afficher tous les participants, qu'ils soient confirmés ou en attente.
- **Rechercher** : rechercher un participant par son nom, sans distinction entre majuscules et minuscules.
- **Supprimer** : supprimer un participant après confirmation dans une boîte de dialogue `AlertDialog`.
- **Transférer** : déplacer un participant vers un autre groupe à l'aide d'une boîte de dialogue.
- Vérification de la disponibilité du groupe de destination avant le transfert.
- Demande de confirmation lorsque le groupe de destination est complet et que le transfert implique une mise en attente.
- Mise à jour des inscriptions, des capacités et de la liste d'attente après les opérations.
- Détection des doublons à partir du nom et du prénom, y compris pour les participants en attente.

Un compteur affiche les informations globales suivantes :

- Le nombre total d'inscriptions.
- Les places libres dans chaque groupe.
- Les participants présents dans la liste d'attente.

---

## 2. Règles de gestion

La logique métier est centralisée dans la classe `GestionPlaces`, afin de séparer les règles d'inscription de l'interface Android.

### 2.1. Capacité des groupes

Chaque groupe possède une capacité maximale de deux participants confirmés.

| Groupe | Capacité maximale | Condition de confirmation |
|---|---:|---|
| G1 | 2 participants | Une place confirmée est disponible dans G1. |
| G2 | 2 participants | Une place confirmée est disponible dans G2. |
| G3 | 2 participants | Une place confirmée est disponible dans G3. |

Un participant est confirmé uniquement si une place est disponible dans son groupe au moment de l'inscription.

Si le groupe est complet, le participant est inscrit sur la liste d'attente avec un ticket unique.

### 2.2. Gestion FIFO des tickets

La liste d'attente fonctionne selon le principe FIFO (*First In, First Out*).

Chaque nouveau participant placé en attente reçoit un ticket strictement croissant. Lorsqu'une place se libère dans un groupe, le participant en attente de ce groupe possédant le plus petit ticket est promu.

**Exemple :**

1. Deux participants sont confirmés dans G1.
2. Un troisième participant reçoit le ticket d'attente `1`.
3. Un quatrième participant reçoit le ticket d'attente `2`.
4. Un participant confirmé de G1 est supprimé.
5. Le participant portant le ticket `1` est automatiquement confirmé.
6. Le ticket `2` reste en attente.

La promotion concerne uniquement le groupe dans lequel une place s'est libérée.

### 2.3. Invariants à respecter

L'application doit maintenir les quatre invariants suivants.

**Invariant 1 — Capacité des groupes**

Pour chaque groupe, le nombre de participants confirmés doit rester compris entre zéro et deux :

```kotlin
0 <= confirmesParGroupe <= 2
```

Un participant en attente ne consomme pas de place confirmée.

**Invariant 2 — Unicité des participants**

Deux inscriptions ne peuvent pas correspondre au même couple nom-prénom. La comparaison est insensible à la casse et concerne aussi bien les participants confirmés que ceux en attente.

**Invariant 3 — Unicité des tickets d'attente**

Chaque participant en attente possède un ticket non nul et unique. Les tickets sont attribués dans l'ordre croissant et ne sont pas réutilisés simplement parce qu'un participant a été supprimé ou promu.

**Invariant 4 — Cohérence des inscriptions**

Le nombre total d'inscriptions correspond à la somme des participants confirmés et des participants en attente :

```kotlin
totalInscriptions = totalConfirmes + totalEnAttente
```

Les statistiques doivent être calculées à partir des participants réellement enregistrés dans la logique métier, et non à partir d'une liste temporaire utilisée pour l'affichage.

### 2.4. Suppression et transfert

**Suppression d'un participant confirmé :**
- Le participant est supprimé.
- Une place se libère dans son groupe.
- Le participant en attente ayant le plus petit ticket de ce groupe est promu, s'il existe.

**Suppression d'un participant en attente :**
- Le participant est supprimé de la liste d'attente.
- Aucune place confirmée ne se libère.
- Les autres participants conservent leurs tickets.

**Transfert vers un autre groupe :**
- La disponibilité de la destination est calculée avant toute modification.
- Si une place est disponible, le transfert peut confirmer le participant dans le nouveau groupe.
- Si la destination est complète, le transfert nécessite une confirmation et peut placer le participant en attente.
- Si le participant transféré était confirmé dans son ancien groupe, une place s'y libère et le premier participant en attente de ce groupe peut être promu.

---

## 3. Technologies utilisées

| Technologie | Version / Configuration | Utilisation |
|---|---|---|
| Kotlin | Langage principal | Implémentation des modèles et de la logique métier |
| Android SDK | Target SDK 34 | Cible de compilation et de fonctionnement |
| Android SDK | Minimum SDK 24 | Compatibilité à partir d'Android 7.0 |
| Java Development Kit | JDK 21 | Environnement Java pour Gradle et la compilation |
| Gradle | 8.5 | Automatisation du build |
| Android Gradle Plugin | 8.2.2 | Configuration et compilation du module Android |
| Visual Studio Code | Éditeur utilisé | Développement et édition des fichiers du projet |
| Android XML | Ressources Android | Définition des layouts et des composants de l'interface |
| Android Debug Bridge | `adb` | Installation et lancement de l'application |

Le projet utilise Gradle pour compiler l'application et peut être développé avec Visual Studio Code sans Android Studio. L'installation de l'Android SDK reste nécessaire pour la compilation et l'exécution.

---

## 4. Structure du projet

La structure suivante représente l'organisation attendue du projet. Les noms de packages et certains fichiers de ressources peuvent varier selon l'implémentation réelle.

```text
AtelierAndroid_TP04_Alternatif/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/
│           │   └── <package>/
│           │       ├── MainActivity.kt
│           │       ├── Personne.kt
│           │       ├── Participant.kt
│           │       └── GestionPlaces.kt
│           └── res/
│               ├── layout/
│               │   └── activity_main.xml
│               ├── values/
│               │   ├── strings.xml
│               │   ├── colors.xml
│               │   └── themes.xml
│               └── drawable/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties
└── README.md
```

### Rôle des principaux fichiers

| Fichier | Responsabilité |
|---|---|
| `Personne.kt` | Définit les propriétés communes `nom`, `prenom` et `age`. |
| `Participant.kt` | Étend `Personne` et ajoute `groupe`, `support` et `ticketAttente`. |
| `GestionPlaces.kt` | Centralise les inscriptions, les capacités, les suppressions, les transferts et les promotions. |
| `MainActivity.kt` | Gère les interactions utilisateur, la validation des champs et l'actualisation de l'interface. |
| `activity_main.xml` | Définit le formulaire, les boutons et les composants d'affichage. |
| `AndroidManifest.xml` | Déclare les composants de l'application Android. |
| `app/build.gradle.kts` | Configure le module Android et les paramètres de compilation. |
| `build.gradle.kts` | Configure les paramètres Gradle au niveau du projet. |
| `settings.gradle.kts` | Définit les modules inclus dans le projet. |
| `gradle.properties` | Définit les propriétés globales de Gradle. |
| `local.properties` | Indique le chemin local vers l'Android SDK. |

### Modèle de données

La classe `Personne` représente les informations communes :

```kotlin
open class Personne(
    val nom: String,
    val prenom: String,
    var age: Int
)
```

La classe `Participant` hérite de `Personne` et ajoute les informations propres à l'inscription :

```kotlin
class Participant(
    nom: String,
    prenom: String,
    age: Int,
    var groupe: String,
    val support: Boolean,
    var ticketAttente: Int? = null
) : Personne(nom, prenom, age)
```

Le type `Int?` permet à `ticketAttente` de contenir un numéro de ticket ou `null` lorsqu'aucun ticket d'attente n'est associé au participant.

### Principales méthodes de `GestionPlaces`

| Méthode | Rôle |
|---|---|
| `placesLibres(group): Int` | Calcule le nombre de places confirmées disponibles dans un groupe. |
| `statut(p): String` | Retourne le statut du participant : `Confirmé` ou `Attente #N`. |
| `inscrire(candidate): Boolean` | Vérifie les doublons et inscrit le participant en confirmation ou en attente. |
| `supprimer(p): Boolean` | Supprime un participant et déclenche une promotion si une place confirmée est libérée. |
| `changerGroupe(p, newGroup): Boolean` | Vérifie les conditions de transfert et déplace le participant. |
| `promouvoirPremier(group): Participant?` | Sélectionne le participant ayant le plus petit ticket dans le groupe. |
| `capacitesGlobales(): String` | Retourne les places disponibles dans G1, G2 et G3. |
| `fileAttenteTexte(): String` | Retourne la liste d'attente triée par groupe et par ticket. |

La méthode `promouvoirPremier(group)` est privée : elle est utilisée par la logique métier pour préserver les règles de promotion.

---

## 5. Prérequis

Avant de compiler le projet, installer les outils suivants :

- **JDK 21**.
- **Gradle Wrapper** fourni avec le dépôt, configuré pour Gradle 8.5.
- **Android SDK Platform 34**.
- **Android SDK Build-Tools** compatibles avec la configuration du projet.
- **Android SDK Platform-Tools**, incluant `adb`.
- Un appareil Android ou un émulateur compatible avec Android API 24 ou supérieur.
- Visual Studio Code ou un éditeur de code équivalent.

### Vérifier Java

```bash
java -version
```

Vérifier également le compilateur Java :

```bash
javac -version
```

Les deux commandes doivent indiquer Java 21.

### Vérifier Gradle

Depuis la racine du projet :

```bash
./gradlew --version
```

### Vérifier ADB

```bash
adb version
```

### Vérifier les appareils disponibles

```bash
adb devices
```

---

## 6. Configuration et installation

### 6.1. Cloner le dépôt

```bash
git clone https://github.com/fakherneila/AtelierAndroid_TP04_Alternatif.git
```

Accéder au répertoire du projet :

```bash
cd AtelierAndroid_TP04_Alternatif
```

### 6.2. Configurer Java 21

Sous Linux, définir `JAVA_HOME` avec le chemin réel du JDK 21 installé sur la machine.

Exemple :

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
```

Vérifier la configuration :

```bash
echo "$JAVA_HOME"
java -version
./gradlew --version
```

Le chemin `/usr/lib/jvm/java-21-openjdk` est donné à titre d'exemple. Il doit être adapté à l'installation locale.

### 6.3. Configurer `local.properties`

Le fichier `local.properties`, situé à la racine du projet, doit indiquer le chemin local vers l'Android SDK.

Exemple :

```properties
sdk.dir=/home/username/Android/Sdk
```

Remplacer `username` par le nom de l'utilisateur et adapter le chemin si le SDK est installé ailleurs.

Les variables suivantes peuvent aider à identifier l'installation :

```bash
echo "$ANDROID_HOME"
echo "$ANDROID_SDK_ROOT"
```

**Important :** `local.properties` contient généralement un chemin spécifique à la machine et ne doit pas être versionné dans Git.

### 6.4. Vérifier `gradle.properties`

Le fichier `gradle.properties` contient les paramètres globaux de Gradle.

Exemple de configuration courante :

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
```

Conserver les autres propriétés nécessaires au projet et éviter de remplacer une configuration existante sans raison.

### 6.5. Vérifier la configuration Gradle

Le fichier `gradle/wrapper/gradle-wrapper.properties` doit référencer Gradle 8.5.

Exemple :

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

La version de l'Android Gradle Plugin doit être `8.2.2`, conformément à la configuration du projet.

### 6.6. Vérifier la configuration Android

Le module Android doit utiliser les paramètres suivants :

- `compileSdk = 34`
- `targetSdk = 34`
- `minSdk = 24`

Ces paramètres définissent respectivement la plateforme utilisée pour compiler, la cible Android et le niveau minimal d'API pris en charge.

---

## 7. Compilation et exécution

### 7.1. Compiler l'application

Depuis la racine du projet :

```bash
./gradlew assembleDebug
```

L'APK de débogage est normalement généré ici :

```text
app/build/outputs/apk/debug/app-debug.apk
```

Pour nettoyer les fichiers de compilation :

```bash
./gradlew clean
```

Puis reconstruire l'application :

```bash
./gradlew assembleDebug
```

### 7.2. Démarrer un appareil ou un émulateur

Vérifier que l'appareil est disponible :

```bash
adb devices
```

Si aucun appareil n'apparaît, connecter un téléphone avec le débogage USB activé ou démarrer un émulateur Android.

### 7.3. Installer l'APK

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

L'option `-r` permet de réinstaller une application compatible sans la désinstaller au préalable.

### 7.4. Lancer l'application

La commande suivante utilise un package fictif à titre d'exemple. Il faut remplacer le package et le nom de l'activité par les valeurs réelles du projet.

```bash
adb shell am start -n com.example.atelierandroid_tp04_alternatif/.MainActivity
```

Le package réel peut être vérifié dans la configuration Gradle du module et dans les déclarations Android.

### 7.5. Consulter les logs

Afficher les logs Android :

```bash
adb logcat
```

Filtrer les messages contenant `MainActivity` :

```bash
adb logcat | grep MainActivity
```

Ces commandes sont utiles pour diagnostiquer les erreurs de lancement, les exceptions Kotlin et les problèmes de logique métier.

---

## 8. Scénarios de test

Les scénarios ci-dessous couvrent les fonctionnalités principales de gestion des places et de la liste d'attente. Ils constituent une base de validation fonctionnelle à adapter aux identifiants et aux formulations exactes du PDF du TP.

| ID | Action | Résultat attendu |
|---|---|---|
| T01 | Lancer l'application et afficher le formulaire. | Le formulaire s'affiche avec les champs, les groupes G1/G2/G3 et les boutons de gestion. |
| T02 | Enregistrer deux participants distincts dans G1. | Les deux participants sont confirmés et G1 ne dispose plus de places libres. |
| T03 | Enregistrer un troisième participant dans G1. | Le participant est inscrit en attente et reçoit un ticket unique. |
| T04 | Enregistrer plusieurs participants supplémentaires dans G1. | Chaque nouveau participant en attente reçoit un ticket croissant, sans dépasser la capacité de confirmation. |
| T05 | Enregistrer deux participants dans G2 et deux dans G3. | Chaque groupe dispose de deux participants confirmés et les capacités globales sont mises à jour. |
| T06 | Enregistrer un participant dont le nom et le prénom correspondent à une inscription existante, en modifiant la casse. | Le doublon est détecté, qu'il soit confirmé ou en attente, et aucune inscription supplémentaire n'est créée. |
| T07 | Supprimer un participant confirmé dans un groupe ayant une liste d'attente. | Une place se libère et le participant en attente ayant le plus petit ticket de ce groupe est promu. |
| T08 | Supprimer un participant en attente. | Le participant disparaît de la liste d'attente, aucune place confirmée ne se libère et les autres tickets restent inchangés. |
| T09 | Afficher les capacités globales après plusieurs inscriptions, suppressions et promotions. | Le compteur et les capacités reflètent les participants réellement enregistrés, avec au maximum deux participants confirmés par groupe. |
| T10 | Transférer un participant confirmé vers un groupe disposant d'une place libre. | Le transfert est effectué selon les règles de gestion, les capacités sont actualisées et le premier participant en attente de l'ancien groupe peut être promu. |
| T11 | Transférer un participant vers un groupe complet. | Une confirmation est demandée. Si le transfert est accepté, le participant rejoint la liste d'attente de destination avec un ticket valide et les règles de capacité restent respectées. |
| T12 | Rechercher un participant, afficher la liste complète, puis utiliser `Effacer`. | La recherche et la liste affichent les participants enregistrés. L'effacement réinitialise le formulaire sans supprimer les inscriptions. |

**Remarque :** les scénarios T01 à T12 décrivent les comportements fonctionnels attendus à partir des exigences du projet. Le PDF original du TP n'étant pas inclus ici, la correspondance exacte entre ces identifiants et ceux du document de cours n'a pas pu être vérifiée.

---

## 9. Réponses aux questions du TP

### Q1. Pourquoi utiliser l'héritage entre `Personne` et `Participant` ?

L'héritage permet de factoriser les propriétés communes `nom`, `prenom` et `age` dans la classe `Personne`. La classe `Participant` hérite de ces propriétés et ajoute les informations propres à l'inscription, comme le groupe, le soutien et le ticket d'attente.

### Q2. Pourquoi déclarer `ticketAttente` avec le type `Int?` ?

Le type nullable `Int?` permet de représenter deux situations : un participant en attente possède un numéro de ticket, tandis qu'un participant confirmé peut avoir `null`. Cette distinction facilite la gestion du statut et la promotion des participants.

### Q3. Pourquoi détecter les doublons parmi les participants confirmés et ceux en attente ?

Un participant ne doit pas pouvoir s'inscrire plusieurs fois simplement parce que sa première inscription est en attente. La vérification doit donc parcourir toutes les inscriptions et comparer le nom et le prénom sans tenir compte des majuscules et des minuscules.

### Q4. Pourquoi supprimer un participant en attente ne libère-t-il pas de place ?

Un participant en attente ne consomme aucune place confirmée. Sa suppression réduit uniquement le nombre de personnes en attente et ne déclenche donc aucune promotion liée à la libération d'une place confirmée.

### Q5. Pourquoi sélectionner le plus petit ticket plutôt que le premier élément d'une `ListView` ?

L'ordre d'affichage de la `ListView` ne garantit pas que le premier élément soit le participant ayant attendu le plus longtemps. La promotion doit sélectionner explicitement le participant du groupe concerné dont le ticket est le plus petit, afin de respecter la règle FIFO.

### Q6. Pourquoi calculer `destinationLibre` avant de modifier le groupe ?

La disponibilité de la destination doit être vérifiée avant de modifier les propriétés du participant ou les collections associées. Cette approche évite de laisser les données dans un état incohérent si le transfert est impossible ou refusé.

### Q7. Comment interpréter les capacités affichées dans T09 ?

Les capacités doivent représenter les places confirmées encore disponibles dans chaque groupe, et non le nombre de participants en attente. Elles sont recalculées à partir des inscriptions réelles après chaque opération susceptible de modifier les confirmations.

### Q8. Pourquoi recalculer les statistiques depuis les participants enregistrés plutôt que depuis `participantsAffiches` ?

`participantsAffiches` peut être une collection temporaire filtrée ou utilisée uniquement pour présenter des résultats à l'écran. Elle ne représente donc pas nécessairement l'ensemble des inscriptions et ne doit pas servir de source pour les statistiques globales.

### Q9. Pourquoi les inscriptions ne persistent-elles pas après la fermeture de l'application ?

Les participants sont stockés en mémoire dans les objets et collections du processus Android. Lorsque le processus est arrêté, ces données peuvent être perdues ; une persistance durable nécessiterait un mécanisme supplémentaire, comme Room, une base SQLite ou un autre système de stockage.

---

## 10. Dépannage

| Symptôme | Cause possible | Solution |
|---|---|---|
| `Val cannot be reassigned` lors du transfert d'un participant. | La propriété `groupe` est déclarée avec `val`, ce qui interdit sa réaffectation. | Déclarer `groupe` avec `var` dans `Participant`, puisque le transfert nécessite de modifier le groupe après validation. |
| `Unsupported class file major version` pendant le build. | La version de Java utilisée par Gradle n'est pas compatible avec l'environnement de compilation. | Vérifier `java -version`, `JAVA_HOME` et `./gradlew --version`. Utiliser JDK 21 conformément à la configuration du projet. |
| Erreur indiquant que Java 11 est insuffisant. | Gradle utilise JDK 11, alors que la configuration du projet requiert un environnement Java plus récent. | Installer JDK 21, mettre à jour `JAVA_HOME` et vérifier la JVM réellement utilisée par Gradle. L'AGP 8.2.2 nécessite au minimum Java 17. |
| `Cannot mutate dependencies of configuration ... after it has been resolved`. | Une dépendance ou une configuration Gradle est modifiée après la résolution de la configuration concernée. Un plugin peut également être incompatible avec la version de Gradle. | Examiner les scripts Gradle et les plugins, déclarer les dépendances dans les blocs appropriés et éviter les modifications tardives. Vérifier la compatibilité des versions de Gradle et de l'AGP. |
| `SDK location not found`. | Le chemin de l'Android SDK est absent ou incorrect. | Vérifier la propriété `sdk.dir` dans `local.properties` et s'assurer que le répertoire existe. |
| `Unresolved reference: R`. | Une ressource XML contient une erreur ou la génération des ressources Android a échoué. | Corriger les erreurs XML, vérifier les noms de ressources et le namespace, puis exécuter `./gradlew clean assembleDebug`. |
| `SDK platform 34 not found`. | La plateforme Android SDK 34 n'est pas installée. | Installer Android SDK Platform 34 et relancer la compilation. |
| `Permission denied` lors de l'exécution de `./gradlew`. | Le script Gradle Wrapper ne possède pas les droits d'exécution. | Exécuter `chmod +x gradlew`, puis relancer la commande. |
| `adb: no devices/emulators found`. | Aucun appareil autorisé ni émulateur démarré n'est détecté. | Connecter un appareil avec le débogage USB activé ou démarrer un émulateur, puis exécuter `adb devices`. |
| `Activity class does not exist`. | Le package ou le nom de l'activité utilisé dans la commande ADB est incorrect. | Vérifier l'`applicationId`, le namespace et l'activité de lancement dans la configuration du projet. |
| Le troisième participant est confirmé alors que le groupe est complet. | Le calcul de capacité ou la vérification des places disponibles est incorrect. | Vérifier que `placesLibres(group)` compte uniquement les participants confirmés et que `inscrire(candidate)` attribue un ticket lorsque la capacité est atteinte. |
| Un participant en attente est promu dans le mauvais groupe. | La promotion ne filtre pas correctement les participants par groupe. | Vérifier que `promouvoirPremier(group)` sélectionne uniquement les participants en attente du groupe concerné. |
| Le participant ayant le plus petit ticket n'est pas promu. | La sélection dépend de l'ordre d'affichage ou de l'ordre d'insertion dans une liste. | Sélectionner explicitement le participant possédant le ticket minimal parmi les participants en attente du groupe. |
| Un transfert provoque une capacité négative ou dépasse la limite. | La disponibilité de destination n'est pas vérifiée avant la mutation ou le transfert n'est pas traité de manière cohérente. | Calculer la disponibilité avant toute modification et appliquer les règles de transfert et de promotion dans la logique métier. |
| Le compteur global est incorrect après une recherche. | Les statistiques sont calculées à partir d'une liste temporaire de résultats filtrés. | Recalculer les statistiques à partir de la collection complète des participants gérée par `GestionPlaces`. |
| Les inscriptions disparaissent après la fermeture de l'application. | Les données sont conservées uniquement en mémoire. | Ce comportement est normal pour cette version du TP. Une persistance nécessite un mécanisme de stockage supplémentaire, hors du périmètre actuel. |

### Commandes utiles pour diagnostiquer les problèmes

Vérifier Java :

```bash
java -version
```

Vérifier la JVM utilisée par Gradle :

```bash
./gradlew --version
```

Nettoyer et reconstruire le projet :

```bash
./gradlew clean assembleDebug
```

Afficher les détails d'une erreur de compilation :

```bash
./gradlew assembleDebug --stacktrace
```

Vérifier les appareils disponibles :

```bash
adb devices
```

---

## 11. Auteur

**Fakher Neila**  
Étudiant en ingénierie logicielle  
**Classe :** ING-A3-GL-AL-04  
**Module :** Développement Mobile

**Dépôt GitHub :** [AtelierAndroid_TP04_Alternatif](https://github.com/fakherneila/AtelierAndroid_TP04_Alternatif)

Ce projet universitaire met en pratique la programmation orientée objet en Kotlin, la gestion des capacités, les collections, la validation des inscriptions et les structures de données nécessaires à une liste d'attente FIFO.

---
