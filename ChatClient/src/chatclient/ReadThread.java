/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import IHM.ClientInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;


/**
 * Sert a lire les messages envoyé par le serveur et faire des actions en fonction
 * @author armen
 */
public class ReadThread extends Thread {
    
    private BufferedReader reader;
    private ClientInterface view;

    public ReadThread(BufferedReader reader, ClientInterface cliIn) {
        this.reader = reader;
        this.view = cliIn;
    }
    
    
    @Override
    public void run(){
        String mgs;
        while (!isInterrupted()) {            
            try {
                mgs = this.reader.readLine();
                if (mgs.charAt(0) == '[') {
                    mgs = mgs.substring(1, mgs.length()-1); // retirer les [] du tableau
                    ArrayList<String> connectedUsers = new ArrayList<String>(Arrays.asList(mgs.split(", "))); // stocket les pseudos dans un tableau

                    this.view.cleanConnectedUsers();
                    for(String user : connectedUsers){
                        this.view.addToConnectedUsers(user);
                    }
                }else{
                    this.view.showMessageOnScreen(mgs);
                    // System.out.println(mgs);
                }
                // traitement du message en fnc des codes retours du serveur   

            } catch (IOException ex) {
                System.out.println("Impossible de lire le message entrant");
                JOptionPane.showMessageDialog(null, "Probleme de communication avec le serveur !!! " );
                this.view.closeWhenError(); // a revoir
            }
            
        }
    }
    
}
