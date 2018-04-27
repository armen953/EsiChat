# EsiChat 

> Application Chat avec un client et un serveur

## Client 

> ### Interface de connexion
![client interface connexion](https://github.com/armen953/EsiChat/blob/master/ChatClient/src/assets/gitEsichatClient.PNG)

> ### Interface du chat
![client interface chat](https://raw.githubusercontent.com/armen953/EsiChat/master/ChatClient/src/assets/gitPresentationInterface.PNG)

* **(1) :** File de discussion
* **(2) :** Zone d’entré utilisateur
* **(3) :** Les smiley disponible que l’utilisateur peut utiliser (connexion internet requise)
* **(4) :** Le bouton qui permet d’ouvrir l’interface des smileys
* **(5) :** Bouton qui permet d’envoyer le message (raccourci clavier : touche entrée)
* **(6) :** Permet de se déconnecter du chat
* **(7) :** Les personnes actuellement connectés au chat

> ### Commandes disponibles dans le chat :

En effectuant un clic droit sur le nom d'un utilisateur connecté dans la zone **(7)**, l'utilisateur a accès à un menu qui permet d’effectuer des actions. En cliquant sur l'une de ces actions la commande correspondant et directement entré dans le champ de saisie. Par exemple pour l'option 1 « *Envoyer un message privé* » va entrer dans le chat la commande **@Arnaud**. 

![](https://raw.githubusercontent.com/armen953/EsiChat/master/ChatClient/src/assets/gitPopUp.PNG)

Pour les autres commandes on peut les faire de manière graphique en cliquant sur l'interface ou on peut directement les entrer dans le champ de saisie. La commande !help affiche les différents commandes disponibles.

* **!ban [pseudo]:** permet de bannir l'utilisateur pendant 60 sec
* **!bandef [pseudo]:** permet de bannir l'utilisateur définitivement
* **!unban [pseudo]:** permet de retirer l'utilisateur de la banlist s'il est présent
* **!bannedList:** permet d'avoir la liste des utilisateurs que vous avez bannie
* **!help:** avoir des informations sur les commandes disponibles

Historique des 10 derniers messages : remonter les messages avec la flèche du haut

Possibilité d'envoyer des Smiley : en cliquant sur l'interface graphique ou en entrant directement les symboles :) ;) :(

## Serveur

> ### Interface serveur
![client interface chat](https://github.com/armen953/EsiChat/blob/master/ChatServeur/src/assets/gitServeur.PNG)
