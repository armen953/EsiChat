/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserveur;

import java.util.HashSet;
import java.util.Scanner;

/**
 *
 * @author armen
 */
public class UserThread implements Runnable{

    private Serveur serveur;
    private User user;

    public UserThread(Serveur serveur, User user) {
        this.serveur = serveur;
        this.user = user;
        this.serveur.broadcastAllConnectedUsers();
        this.serveur.getView().addToConnectedUsers(this.user.getUserName());
        this.serveur.getBannedUserByUser().put(user, new HashSet<>());
    }


    @Override
    public void run() {
        String message;
        
        Scanner reader = new Scanner(this.user.getRead());
        while (reader.hasNextLine()) {            
            message = reader.nextLine();
            
            // traitement des smilyes
            message = message.replace(":)", "<img src='http://img1.starwars-holonet.com/_v8/images/smileys/3.gif'>");
            message = message.replace(":(", "<img src='http://img1.starwars-holonet.com/_v8/images/smileys/4.gif'>");
            message = message.replace(":P", "<img src='http://img1.starwars-holonet.com/_v8/images/smileys/5.gif'>");
            message = message.replace(":I", "<img src='http://img1.starwars-holonet.com/_v8/images/smileys/6.gif'>");
            message = message.replace(":S", "<img src='http://img1.starwars-holonet.com/_v8/images/smileys/7.gif'>");
            message = message.replace("8)", "<img src='http://img1.starwars-holonet.com/_v8/images/smileys/8.gif'>");
            message = message.replace(";)", "<img src='http://img1.starwars-holonet.com/_v8/images/smileys/9.gif'>");
            message = message.replace(":D", "<img src='http://img1.starwars-holonet.com/_v8/images/smileys/1.gif'>");

            // traitement des message privés ICI 
            if(message.charAt(0) == '@'){               // dans le cas d'un message privé
                if (message.contains(" ")) {
                    // System.out.println("Message privé " + message);
                    int firstSpaceIndex = message.indexOf(" ");   // recuperer l'index du 1er espace
                    String to = message.substring(1, firstSpaceIndex);  // recuperer le pseudo de la personne en retirant le @
                    String mgs = message.substring(firstSpaceIndex+1, message.length());
                    this.serveur.privateMessageToUser(mgs, user, to);
                }
            }else if (message.charAt(0) == '!'){        // dans le cas d'une commande
                if(message.contains(" ")) {
                    int firstSpaceIndex = message.indexOf(" ");   // recuperer l'index du 1er espace
                    String commande = message.substring(1, firstSpaceIndex);  // recuperer la commande entré
                    String option= message.substring(firstSpaceIndex+1, message.length()); // option de la commande

                    if(commande.equals("ban")){
                        this.serveur.addUserToBannedUserByUser(user, option, false);
                    }else if(commande.equals("bandef")){
                        this.serveur.addUserToBannedUserByUser(user, option, true);
                    }else if(commande.equals("unban")){
                        this.serveur.unBanUser(user, option);
                    }else{
                       this.serveur.serverSendInfoToUser(user, "la commande n'est pas valide", "red");
                    }
                }else if(message.equals("!bannedList")){
                    this.serveur.sendBannedUser(user);
                }else if(message.equals("!help")){
                    this.serveur.serverSendInfoToUser(user, "Liste des commandes <ul>"
                            + "<li> <b>!ban [pseudo]</b>: permet de bannir l'utilisateur pendant 60 sec</li> "
                            + "<li> <b>!bandef [pseudo]</b>: permet de bannir l'utilisateur définitivement</li> "
                            + "<li> <b>!unban [pseudo]</b>: permet de retirer l'utilisateur de la banlist s'il est présent</li> "
                            + "<li> <b>!bannedList </b>: permet d'avoir la liste des utilisateurs que vous avez bannie</li> "
                            + "<li> <b>!help </b>: avoir des informations sur les commandes disponibles</li> "
                            + "</ul>", "blue");
                }else{
                    this.serveur.serverSendInfoToUser(user, "la commande n'est pas valide", "red");
                }
            }else{                                      //
                // envoyer le mesage a tout le monde 
                this.serveur.broadcastMgs(message, user);
            }
            // autre traiement ----
            
        }
            // when the thread end
        //this.serveur.broadcastAllConnectedUsers();
        this.serveur.disconnectUser(user);
        
        reader.close();
    }
    
}
