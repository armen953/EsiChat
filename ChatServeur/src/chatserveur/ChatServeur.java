/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserveur;

import View.ServeurInterface;
import java.io.IOException;


/**
 *
 * @author armen
 */
public class ChatServeur {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServeurInterface ServeurGui = new ServeurInterface();
        ServeurGui.setVisible(true);
        ServeurGui.setLocationRelativeTo(null);
      
        Serveur serv = new Serveur(8080, ServeurGui);
        
        try {
            serv.run();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    
    }
    
}
