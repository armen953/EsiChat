/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import IHM.ClientInterface;

/**
 *
 * @author user
 */
public class ChatClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ClientInterface clientLog = new ClientInterface();
        clientLog.setVisible(true);
        clientLog.setLocationRelativeTo(null);
    }
    
}
