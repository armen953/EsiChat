/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserveur;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author armen
 */
public class User {
    
    // Infotmation de l'utilisateur
    private int userId;
    private String userName;
    // Pour la communication
    private Socket userSocket;
    private PrintStream write;
    private InputStream read;


    public User(String name, Socket sock){
        this.userName = name;
        this.userSocket = sock;
        try {
            this.write = new PrintStream(this.userSocket.getOutputStream());
            this.read = this.userSocket.getInputStream();
        } catch (IOException ex) {
            System.out.println("IOException: Problème lors de la récuperation des flux: " + ex);
        }
    }

    @Override
    public String toString() {
        return "" + this.userName +"";
    }
    
    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Socket getUserSocket() {
        return userSocket;
    }

    public PrintStream getWrite() {
        return write;
    }

    public InputStream getRead() {
        return read;
    }

}
