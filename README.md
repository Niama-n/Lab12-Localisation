# 📍 LAB 12 : Localisation temps réel via GPS et Google Maps

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![PHP](https://img.shields.io/badge/php-%23777BB4.svg?style=for-the-badge&logo=php&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=for-the-badge&logo=google-maps&logoColor=white)




<img width="720" height="1600" alt="1" src="https://github.com/user-attachments/assets/29d7f448-34d3-49c4-bef4-04e6bc86654b" />

## 📖 À propos de ce projet

J'ai développé cette application Android dans le but de mettre en pratique la géolocalisation et la communication client-serveur. 
L'application fonctionne comme un "tracker" silencieux : elle récupère les coordonnées GPS du téléphone en temps réel, les transmet automatiquement à une base de données MySQL via une API PHP, puis permet de retracer l'historique complet des déplacements sur une carte interactive Google Maps.

---

## 🎯 Objectifs de l'application

1. **Acquisition de données capteur** : Être capable d'interroger le hardware du smartphone (Puce GPS) avec le bon niveau de précision et d'intervalle.
2. **Synchronisation asynchrone** : Assurer l'envoi de requêtes HTTP sans bloquer l'interface utilisateur (Thread principal), même en cas de mauvaise connexion réseau.
3. **Persistance des données** : Mettre en place une base de données relationnelle sécurisée pour archiver les déplacements.
4. **Restitution visuelle** : Savoir exploiter l'API Google Maps pour afficher des données métier de manière intuitive (marqueurs dynamiques, zoom auto-ajustable).
5. **Gestion de la vie privée** : Implémenter le système moderne de permissions Android (demande d'autorisation au moment de l'exécution).

---

## 📂 Arborescence du projet et Architecture

L'architecture a été pensée pour séparer les responsabilités (UI vs Réseau vs Utilitaires) :

```text
📦 Localisation GPS
 ┣ 📂 app/src/main
 ┃ ┣ 📂 java/com/example/localisation
 ┃ ┃ ┣ 📜 MainActivity.java     # Cœur de l'UI, gestion du LocationListener
 ┃ ┃ ┣ 📜 MapsActivity.java     # Affichage de la carte et des marqueurs
 ┃ ┃ ┣ 📜 ServerConnector.java  # Classe dédiée aux requêtes HTTP (Volley)
 ┃ ┃ ┗ 📜 DeviceUtils.java      # Classe utilitaire pour identifier l'appareil (IMEI/Android ID)
 ┃ ┃
 ┃ ┣ 📂 res
 ┃ ┃ ┣ 📂 layout
 ┃ ┃ ┃ ┣ 📜 activity_main.xml   # Interface d'accueil avec ConstraintLayout & CardView
 ┃ ┃ ┃ ┗ 📜 activity_maps.xml   # Layout intégrant le Fragment Google Maps
 ┃ ┃ ┣ 📂 values
 ┃ ┃ ┃ ┣ 📜 colors.xml          # Palette personnalisée Teal & Coral
 ┃ ┃ ┃ ┣ 📜 strings.xml         # Textes externalisés
 ┃ ┃ ┃ ┣ 📜 themes.xml          # Définition du Theme.GeoTracker
 ┃ ┃ ┃ ┗ 📜 google_maps_api.xml # Clé de sécurité API Google
 ┃ ┃
 ┃ ┗ 📜 AndroidManifest.xml     # Configuration des permissions & activités
 ┃
 ┗ 📜 build.gradle.kts          # Dépendances du projet (Volley, Play Services Maps, CardView)
```

---

## 🛠️ Stack Technique

| Composant | Technologie utilisée |
|---|---|
| **Langage principal** | Java 11 |
| **SDK Android** | Min SDK 24, Target SDK 36 |
| **Réseau / API** | Volley (Requêtes asynchrones) |
| **Cartographie** | Google Maps SDK for Android |
| **Backend** | PHP pur (scripts `createPosition.php`, `showPositions.php`) |
| **Base de données** | MySQL / MariaDB |
| **UI / Design** | Material Components, CardView, LinearLayout |

---

## 🚀 Guide d'installation pas à pas

Ce projet nécessite une infrastructure côté serveur et côté mobile. Voici les étapes détaillées pour tout configurer :

### 1️⃣ Configuration de l'environnement Serveur (Backend)
1. **Téléchargez un serveur local** comme [XAMPP](https://www.apachefriends.org/fr/index.html) ou WAMP et installez-le sur votre PC.
2. Démarrez les modules **Apache** et **MySQL**.
3. Allez sur **phpMyAdmin** (généralement `http://localhost/phpmyadmin`).
4. Créez une nouvelle base de données nommée `localisation`.
5. Exécutez ce script SQL pour générer la table :
   ```sql
   CREATE TABLE `position` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `latitude` double NOT NULL,
     `longitude` double NOT NULL,
     `date` datetime NOT NULL,
     `imei` varchar(255) NOT NULL,
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
   ```
6. Dans le dossier de votre serveur (`C:\xampp\htdocs\` pour XAMPP), créez un dossier `/localisation/`.
7. Placez-y vos deux scripts PHP (`createPosition.php` et `showPositions.php`). Assurez-vous que les identifiants de base de données dans ces fichiers (utilisateur, mot de passe) sont corrects.

### 2️⃣ Récupération de votre Adresse IP
Pour que votre téléphone puisse envoyer les données à votre PC, ils doivent être sur le même réseau Wi-Fi.
1. Ouvrez l'invite de commande Windows (`cmd`).
2. Tapez la commande `ipconfig`.
3. Notez l'adresse **IPv4** (qui ressemble souvent à `192.168.1.X` ou `192.168.43.X`).

### 3️⃣ Configuration de l'application Android
1. Ouvrez ce projet dans **Android Studio**.
2. **Mettez à jour l'IP** : Ouvrez les fichiers `MainActivity.java` et `MapsActivity.java`. Remplacez l'IP dans les variables `nma_saveEndpoint` et `nma_fetchEndpoint` par celle que vous venez de noter :
   ```java
   private final String nma_saveEndpoint = "http://192.168.1.X/localisation/createPosition.php";
   ```
3. **Clé Google Maps** : Rendez-vous sur la [Google Cloud Console](https://console.cloud.google.com/), activez l'API *Maps SDK for Android* et générez une clé.
4. Ouvrez le fichier `app/src/main/res/values/google_maps_api.xml` et insérez votre clé :
   ```xml
   <string name="google_maps_key">VOTRE_CLE_API_ICI</string>
   ```

### 4️⃣ Lancement de l'application
1. Cliquez sur **Sync Project with Gradle Files**.
2. Branchez votre téléphone Android en USB (avec le débogage USB activé) ou lancez un émulateur.
3. Cliquez sur **Run (Shift+F10)**.
4. Au lancement, acceptez la demande d'autorisation de localisation.
5. Déplacez-vous ! (Ou simulez un déplacement dans l'émulateur). Vos coordonnées s'afficheront et seront envoyées au serveur. Cliquez ensuite sur "View Map" pour voir le résultat.

---

## 🔒 Permissions requises

L'application demande de manière transparente les permissions suivantes lors du premier lancement :
- `ACCESS_FINE_LOCATION` : Indispensable pour interroger la puce GPS avec une haute précision.
- `ACCESS_COARSE_LOCATION` : Pour la géolocalisation de secours basée sur le réseau mobile/Wi-Fi.
- `INTERNET` : Requis pour autoriser la communication avec l'API PHP et l'affichage de Google Maps.
- `READ_PHONE_STATE` : Utilisé uniquement en plan B pour récupérer l'identifiant matériel unique du téléphone.

---

## 💡 Pistes d'améliorations futures
- [ ] Ajouter une ligne (Polyline) sur la carte pour relier les points chronologiquement et dessiner le trajet complet.
- [ ] Mettre en place un système de "Foreground Service" avec une notification persistante pour continuer à traquer la position même quand l'application est en arrière-plan.
- [ ] Permettre à l'utilisateur de purger l'historique directement depuis l'application via un bouton "Supprimer mes traces".

## Realise par 
NAFTAOUI NIAMA
