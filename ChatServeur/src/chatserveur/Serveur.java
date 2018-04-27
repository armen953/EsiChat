/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserveur;

import View.ServeurInterface;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;


/**
 *
 * @author armen
 */
public class Serveur {
    
    private int port;
    private ArrayList<User> users;
    private ServerSocket servSocket;
    private ServeurInterface view;
    /**
     * Permer de gerer les bans par utilisateur. Chaque tilisateur a une liste de ban et peut ban d'autres utilisateur
     */
    private Map<User, HashSet<User>> bannedUserByUser = new HashMap<User, HashSet<User>>();
    private User bannedUser = null;
    
    public Serveur(int port, ServeurInterface view) {
        this.port = port;
        this.users = new ArrayList<User>();
        this.view = view;
        
    }
    
    public void run() throws IOException{
        this.servSocket = new ServerSocket(this.port){
            @Override
            protected void finalize(){ // appeler quand l'objet va etre détruit par le garbage colector
                try {
                    this.close();
                } catch (IOException ex) {
                    System.out.println("Le socket n'as pas put etre fermé " + ex);
                }
            }
        };
        this.view.logMessage("<h1 style=\"text-align:center\">Le serveur écoute sur le port " + this.port + "</h1>");
               
        while(true){
            Socket newClient = this.servSocket.accept();  // accepter la co avec le client
            boolean isValid = this.users.isEmpty();
            String pseudo = ( new Scanner(newClient.getInputStream()) ).nextLine();  // récuperer le pseudo du client qui vient de se connecter
            
            pseudo = pseudo.replace(",", ""); // voir si besoin
            pseudo = pseudo.replace(" ", "_");
            pseudo = pseudo.replace("@", "_");  // @ utilisé pour envoyer un mgs privé
            pseudo = pseudo.replace("!", "_");  // ! utilisé pour entrer des commandes
            pseudo = pseudo.replace(":", "_");  // : utilisé pour les smileys
            pseudo = pseudo.replace(";", "_");  // : utilisé pour les smileys
            pseudo = pseudo.replace("8)", "_");  // : utilisé pour les smileys

            
            PrintStream write = new PrintStream(newClient.getOutputStream());  // recuperer le descripteur pour envoyer un message au client
            for(User user : this.users){
                if(user.getUserName().equals(pseudo)){
                    // le pseudo existe deja
                    write.println("Refuse|Le pseudo existe déjà");  // envoyer le message au client
                    newClient.close();  // fermer la communication
                    isValid = false;
                }else{
                    isValid = true;
                }
            }

            if (isValid){
                write.println("Accept|");  // envoyer un message au client
                User newUser = new User(pseudo, newClient); // créer un nouveau utilisateur
                this.users.add(newUser); // ajouter a la liste des utilisateurs
                bannedUserByUser.putIfAbsent(newUser, new HashSet<>()); // ajouter le user a la liste pour gerer ses bans
                
                System.out.println(newClient);
                
                sendMessageUserCoAndDeco("<b>" + pseudo + "</b> s'est connecté" , newUser);
               
                
                this.view.logMessage("[" + getHourWithMinutes() + "] " + "<b> > Connexion: </b> " + pseudo + " vient de se connecter :"); // logger la connexion
                this.view.logMessage("<div style='margin-left:10px'>"
                        + "<div> - adresse ip:" + newClient.getInetAddress().getHostAddress() + "</div>" 
                        + "<div> - le port:   " + newClient.getPort()  +  "</div>"
                        + "</div>"); 
                this.view.getNbUser().setText("" + this.users.size() + "");
                
                new Thread(new UserThread(this, newUser)).start();   // lancer un nouveau thread pour gérer les nouveau message de l'utilisateur
            }            
        }
        
    }
    
    /**
     * Déconnecte l'utilisateur passé en prarametre et ferme le socket qui lui est associé 
     * @param user L'utilisateur a déconnecter
     */
    public void disconnectUser(User user){
        try {
            user.getUserSocket().close();   // fermer le socket de l'utilisateur
            this.view.logMessage("[" + getHourWithMinutes() + "] " + "<b> > Déconnexion: </b> " + user.getUserName() + " vient de se déconnecter :");
           
            this.users.remove(user);        // retirer l'utilisateur de la liste pour liberer le pseudo
            this.bannedUserByUser.remove(user); // retirer l'utilisateur de la liste pour génrer ses bans
            
            sendMessageUserCoAndDeco("<b>" + user.getUserName() + "</b> s'est déconnecté", user);
            broadcastAllConnectedUsers();
            refreshConnectedUsers();
            this.view.getNbUser().setText("" + this.users.size() + "");
            showUsers(); // pour débug
        } catch (IOException ex) {
            System.out.println("Une erreur est survenu lors de la fermeture du userSocket: " + ex);
        }
    }
    
    /**
     * Affiche tous les utlisateur connectés
     */
    private void showUsers(){
        this.users.forEach((u) -> {
            System.out.println(u.getUserName());
        });
    }
    
    /**
     * Rafrechie la liste des utilisateurs
     */
    private void refreshConnectedUsers(){
        this.view.cleanConnectedUsers();
        this.users.forEach((u) -> {
            this.view.addToConnectedUsers(u.getUserName());
        });
    }    
    
    /**
     * Diffuse le message a tous les utilisateurs connectés
     * @param message le message a diffuser
     * @param sender la personne qui a envoyé le messages
     */
    public void broadcastMgs(String message, User sender){
        this.users.forEach((user) -> {
            user.getWrite().println(sender.toString() + "<span>: " + message + "</span>");
        });
    }
    
    /**
     * Envoyer un message a tous les utilsiateur sauf un (permet de dire la connexion et la déconnexion)
     * @param message le message a envoyer
     * @param user l'utilsiateur qui ne doit pas recevoir le message
     */
    public void sendMessageUserCoAndDeco(String message, User user){
        for(User u : this.users){
            if(!u.equals(user)){
                u.getWrite().println("<b>(Serveur) > </b>" + message);
            }
        }
    }
    
    /**
     * Envoie la lsite des utilisateurs connectés
     */
    public void broadcastAllConnectedUsers(){
        users.forEach((user) -> {
            user.getWrite().println(this.users);
        });
    }

   /**
    * Envoie un message privé (le message est envoyé a la personne qui envoie et la personne qui doit le rececoir). Si la aucun utilisateur possedant le pseudo est connecté la personne qui envoie le message est informé
    * @param message le message a envoyer
    * @param from la personne qui envoie le message
    * @param to le pseudo de la personne a qui on veux envoyer le message privé
    */
    public void privateMessageToUser(String message, User from, String to){
        boolean userFind = false;
        for(User user : this.users){
            if(user.getUserName().equals(to) && user != from){
                userFind = true;
                if(this.bannedUserByUser.get(user).contains(from)){  // l'utilisateur qui doit recevoir le message a bani l'utilisateur qui envoie
                    serverSendInfoToUser(from, user.getUserName() +  " vous a ban. Soit un ban temporaire de 60 sec soit un ban définitif. Réessayer dans 1 minutes", "red");
                }else{  // pas de ban
                    from.getWrite().println(from.toString() + " -> " + user.toString() + ": " + message); // envoyer le message a celui qui envoie
                    user.getWrite().println("<b>Message Privé de " + from.toString() + ": </b> <span>" + message + "</span>" ); // envoyer le message a la personne
                }
            }else if(user.getUserName().equals(to)){
                serverSendInfoToUser(from, "Impossible d'envoyer un message privé a soi-même", "red");
                userFind = true;
            }
        }
        if (!userFind) {
            serverSendInfoToUser(from, to + " n'est pas connecté", "red");
        }
        
    }
    
    /**
     * Ajouter un utilisateur a la liste des utilsiateur bannie par utilisateur
     * @param user l'utilsaiteur qui veux ban 
     * @param banned le pseudo de l'utilisateur qui doit etre bannie
     * @param permaBan si le ban est définitif ou non
     */
    public void addUserToBannedUserByUser(User user, String banned, boolean permaBan){
        bannedUser = null;
        for(User u : this.users){
            if (u.getUserName().equals(banned)) {
                bannedUser = u;
            }
        }
        if (bannedUser != null && bannedUser != user) {
            this.bannedUserByUser.get(user).add(bannedUser);
            if (!permaBan) {
                serverSendInfoToUser(user, "Vous avez ban <b>" + bannedUser.getUserName() + "</b> pendant 60 sec. Pour un ban définitif il faut utiliser la commande <b>!bandef</b>", "green");
            }else{
                serverSendInfoToUser(user, "Vous avez perma ban <b>" + bannedUser.getUserName() + "</b>", "green");
            }
        }else if(bannedUser == user){
            serverSendInfoToUser(user, "Impossible de se bannir soi-même", "red");
        }else{
            serverSendInfoToUser(user, banned + " n'est pas connecté", "red");
        }
        if (!permaBan) {
            User banUser = bannedUser;
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    if (bannedUserByUser.get(user).contains(banUser)) { // si la personne bannie est toujours dans la liste apres le timer alors le retirer sinon rien faire
                        bannedUserByUser.get(user).remove(banUser);
                        serverSendInfoToUser(user, "<b>"+ banUser + "</b> n'est plus bannie" , "green");
                        System.out.println(bannedUserByUser);
                    }
                }
            }, 60000);
        }
        System.out.println(this.bannedUserByUser);
    }
    
    /**
     * Retirer l'utilisateur qui est dans la banlist de user
     * @param user l'utilisateur qui possede la banlist
     * @param banned le pseudo de l'utilisateur qui doit etre unban
     */
    public void unBanUser(User user, String banned){
        User uBanned = null;
        for(User u : this.users){
            if (u.getUserName().equals(banned)) {
                uBanned = u;
            }
        }
        if(uBanned != null){
            if (this.bannedUserByUser.get(user).contains(uBanned)) {
                this.bannedUserByUser.get(user).remove(uBanned);
                serverSendInfoToUser(user, "<b>"+ uBanned.getUserName() + "</b> à été unban", "green");
            }else{
                serverSendInfoToUser(user, "<b>"+ uBanned.getUserName() + "</b> n'est pas dans votre banlist", "red");
            }
        }else{
            serverSendInfoToUser(user, "<b>"+ banned + "</b> n'est pas connecté", "red");
        }
       
    }
    
    /**
     * Envoie la liste des utilisateur bannie par l'utlisateur passé en parametre
     * @param user l'utilisateur pour lequel on veux reciperer le liste
     */
    public void sendBannedUser(User user){
        serverSendInfoToUser(user, this.bannedUserByUser.get(user).toString(), "black");
    }

    /**
     * Envoie un message a un utilisateur spécifique
     * @param user l'utilisateur a qui envoyer le message
     * @param message  le message a envoyer
     */
    public void sendMessageToUser(User user, String message){
        user.getWrite().println(message);
    }
    
    
    /**
     * Envoyer un message a l'utilisateur de la part du serveur
     * @param user l'utilisateur a qui envoyer le message
     * @param message le message a envoyer
     * @param color la couleur du message
     */
    public void serverSendInfoToUser(User user, String message, String color){
        user.getWrite().println("<b>(Serveur) > </b> <span style\"color="+ color + "\"> " + message + "</span>");
    }
    
    
    /**
     * Permet d'avoir l'heure et les minutes
     * @return une chaine de caractere de l'heure et de la minute sous la forme "HH:MM"
     */
    private String getHourWithMinutes(){
        return "" + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + "";
    }
    
    /**
     * Permet d'avoir la date, le mois et l'année
     * @return une chaine de caractere du jour, du mois et de l'année sous la forme "DD/MM//YY"
     */
    private String getDay(){
        return "" + LocalDateTime.now().getDayOfMonth() + "/" + LocalDateTime.now().getMonthValue() + "/" + LocalDateTime.now().getYear();
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ServeurInterface getView() {
        return view;
    }

    public Map<User, HashSet<User>> getBannedUserByUser() {
        return bannedUserByUser;
    }
     
}
