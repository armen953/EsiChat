/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IHM;

import chatclient.ReadThread;
import com.sun.glass.events.KeyEvent;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author armen
 */
public class ClientInterface extends javax.swing.JFrame {

    //<editor-fold defaultstate="collapsed" desc="Pour la gestion de déplacement de la fenetre">
    private int xSouris;
    private int ySouris;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Pour la gestion jList et popup">
    private DefaultListModel<String> dm;
    private final JPopupMenu pop;
    private String row="";
    private int index=0;
    //</editor-fold>
   
    //<editor-fold defaultstate="collapsed" desc="Les informations entré lors de la connexion">
    private String ServName;
    private int port;
    private String pseudo;
    //</editor-fold>
    
    private boolean smileyClicked = false;
    
    private Socket server;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread readThread;
    private ArrayList<String> previousMessages;
    private int previousMessagesIndex = 0;

    
    /**
     * Creates new form ClientInterface
     */
    public ClientInterface() {
        initComponents();
        this.chatPanel.setVisible(false);
        this.ServName = "localhost";
        this.port = 8080;
        
        this.txtAdressIp.setText(this.ServName);
        this.txtPort.setText(""+this.port);
        this.discusionPane.setContentType("text/html");
        //this.connectedUsers.setContentType("text/html");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/assets/esichat.png"))); // ajouter un icon 
        this.SmileySrollPanel.setVisible(smileyClicked);
        this.previousMessages = new ArrayList<>();
        
        // pour fonctionnalité du pop up
        this.dm = new DefaultListModel<>();
        this.pop = new JPopupMenu();
        addPopupToConnectedusers();  // ajouter le popup
        connectedUsers.setFixedCellHeight(30);
    }

    
    /**
     * Ajoute le texte donné à la fin du JTextPane
     * @param jTP le TextPane dans lequel il faut ajouter le text
     * @param text le text a ajouter dans le TextPane
     * @ref:  https://stackoverflow.com/questions/4059198/jtextpane-appending-a-new-string?answertab=active#tab-top
     */
    public void appendJTextPane(JTextPane jTP, String text){
        HTMLDocument document = (HTMLDocument)jTP.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit)jTP.getEditorKit();
        try {
          editorKit.insertHTML(document, document.getLength(), text, 0, 0, null);
          jTP.setCaretPosition(document.getLength());
        } catch(Exception e){
            System.out.println("Erreur lors de l'ajout du text dans le TextPane: " + e);
        }
    }
    
    /**
     * Afficher le message passé en parametre dans la file de dicussion
     * @param mgs le message a afficher
     */
    public void showMessageOnScreen(String mgs){
        this.appendJTextPane(discusionPane, mgs);
    }
    
    /**
     * Envoyer le message qui ce trouve dans le champs de saisie de l'utilisateur et sauvgarde le message envoyé
     */
    public void sendMessage(){
        String mgs = this.txtUserInput.getText().trim();
        if(!mgs.isEmpty()){
            this.previousMessages.add(mgs);
            this.previousMessagesIndex = this.previousMessages.size();
            this.writer.println(mgs);
            this.txtUserInput.setText(null);
            this.txtUserInput.requestFocus();
            if(this.previousMessages.size() > 10){  // faire en sorte que la taille de l'historique des messages ne soit pas trop élevé 
                this.previousMessages.remove(0);
                System.out.println(this.previousMessagesIndex);
                this.previousMessagesIndex--;
            } 
        }
//this.appendJTextPane(discusionPane, "Hey salut <img src='http://img1.starwars-holonet.com/_v8/images/smileys/3.gif'>");
   }
    
    
    /**
     * Désactive les champs du formulaire de connexion
     */
    private void disableLoginFields(){
        this.btnConexion.setEnabled(false);
        this.txtPseudo.setEnabled(false);
        this.txtPort.setEnabled(false);
        this.txtAdressIp.setEnabled(false);
    }
    
    /**
     * Active les champs du formulaire de connexion
     */
    private void enableLoginFields(){
        this.btnConexion.setEnabled(true);
        this.txtPseudo.setEnabled(true);
        this.txtPort.setEnabled(true);
        this.txtAdressIp.setEnabled(true);
    }
    
    
    
//<editor-fold defaultstate="collapsed" desc="Gestion du jList pour afficher les utilisateurs connectés avec un menu popup">
    /**
     * Ajoute un nouveau element dans le jList qui contiens les utilisateurs connectés
     * @param name le nom a ajouter
     */
    public void addToConnectedUsers(String name){
        dm.addElement(name);
        this.connectedUsers.setModel(dm);
    }
    
    /**
     * Efface tous le contenu du jList connectedUsers 
     */
    public void cleanConnectedUsers(){
        dm.clear();
        this.connectedUsers.setModel(dm);
    }
    
    /**
     * Ajoute la fonctionalité du poup
     */
    private void addPopupToConnectedusers(){
        // a changer
        //JMenuItem delete = new JMenuItem("Delete");        
        JMenuItem privateMessage = new JMenuItem("Envoyer un message privé");
        JMenuItem banUser = new JMenuItem("Bannir l'utilisateur (1 min)");
        JMenuItem bannedList = new JMenuItem("Liste des utilisateurs bannies");
        JMenuItem banDef = new JMenuItem("Ban définitif");
        JMenuItem unBan = new JMenuItem("Débannir");        
        JMenuItem help = new JMenuItem("Aide");        


        //add
        this.pop.add(privateMessage);
        this.pop.add(banUser);
        this.pop.add(bannedList);
        this.pop.add(banDef);        
        this.pop.add(unBan);       
        this.pop.add(help);


        //this.pop.add(delete);
        
        //events
        banDef.addActionListener((ActionEvent e) -> {
            // faire ce qui doir etre fait
            //JOptionPane.showMessageDialog(null, "Click sur: " + row );
            String user = dm.getElementAt(index).toString();
            this.txtUserInput.setText("!bandef "+ user);
        });
        
        banUser.addActionListener((ActionEvent e) -> {
            String user = dm.getElementAt(index).toString();
            this.txtUserInput.setText("!ban "+ user);
        });
        
        privateMessage.addActionListener((ActionEvent e) -> {
            String user = dm.getElementAt(index).toString();
            this.txtUserInput.setText("@"+user + " ");
        });
        
        unBan.addActionListener((ActionEvent e) -> {
            String user = dm.getElementAt(index).toString();
            this.txtUserInput.setText("!unban "+ user + " ");
        });
        
        bannedList.addActionListener((ActionEvent e) -> {
            this.txtUserInput.setText("!bannedList");
        });
        
        help.addActionListener((ActionEvent e) -> {
            this.txtUserInput.setText("!help");
        });
        
//        delete.addActionListener((ActionEvent e) -> {
//            dm.removeElementAt(index);
//        });
    }
//</editor-fold>
    
    
        
    private void onArrowClick(){
        String mgs = this.previousMessages.get(this.previousMessagesIndex);
        this.txtUserInput.setText(mgs);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelReduce = new javax.swing.JLabel();
        labelClose = new javax.swing.JLabel();
        deplacement = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        chatPanel = new javax.swing.JPanel();
        SmileySrollPanel = new javax.swing.JScrollPane();
        smileyPanel = new javax.swing.JPanel();
        smiley1 = new javax.swing.JLabel();
        smiley2 = new javax.swing.JLabel();
        smiley3 = new javax.swing.JLabel();
        smiley4 = new javax.swing.JLabel();
        smiley5 = new javax.swing.JLabel();
        smiley6 = new javax.swing.JLabel();
        smiley7 = new javax.swing.JLabel();
        smiley8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        discusionPane = new javax.swing.JTextPane();
        txtUserInput = new javax.swing.JTextField();
        btnDeco = new java.awt.Button();
        btnSend = new java.awt.Button();
        jScrollPane2 = new javax.swing.JScrollPane();
        connectedUsers = new javax.swing.JList<>();
        smileyBtn = new javax.swing.JLabel();
        loginPanel = new javax.swing.JPanel();
        Image = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        loginFrom = new javax.swing.JPanel();
        labelPseudo = new javax.swing.JLabel();
        txtPseudo = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        txtAdressIp = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        btnConexion = new java.awt.Button();
        labelAdresse = new javax.swing.JLabel();
        labelPort = new javax.swing.JLabel();
        txtPort = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        labelInfoMessage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelReduce.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelReduce.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/minimize.png"))); // NOI18N
        labelReduce.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelReduce.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                labelReduceMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                labelReduceMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                labelReduceMousePressed(evt);
            }
        });
        getContentPane().add(labelReduce, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 5, 30, 30));

        labelClose.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/close.png"))); // NOI18N
        labelClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                labelCloseMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                labelCloseMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                labelCloseMousePressed(evt);
            }
        });
        getContentPane().add(labelClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 5, 30, 30));

        deplacement.setBackground(new java.awt.Color(0, 0, 0));
        deplacement.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        deplacement.setForeground(new java.awt.Color(255, 255, 255));
        deplacement.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        deplacement.setText("EsiChat v1.0");
        deplacement.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                deplacementMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                deplacementMouseMoved(evt);
            }
        });
        getContentPane().add(deplacement, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1010, 40));

        jPanel2.setBackground(new java.awt.Color(37, 35, 67));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1010, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1010, 40));

        chatPanel.setBackground(new java.awt.Color(94, 92, 142));
        chatPanel.setPreferredSize(new java.awt.Dimension(1022, 650));
        chatPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        SmileySrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        SmileySrollPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        smiley1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        smiley1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smileys/3.gif"))); // NOI18N
        smiley1.setToolTipText(":)");
        smiley1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smiley1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smiley1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smiley1MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                smiley1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                smiley1MouseReleased(evt);
            }
        });

        smiley2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        smiley2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smileys/4.gif"))); // NOI18N
        smiley2.setToolTipText(":(");
        smiley2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smiley2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smiley2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smiley2MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                smiley2MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                smiley2MouseReleased(evt);
            }
        });

        smiley3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        smiley3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smileys/5.gif"))); // NOI18N
        smiley3.setToolTipText(":P");
        smiley3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smiley3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smiley3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smiley3MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                smiley3MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                smiley3MouseReleased(evt);
            }
        });

        smiley4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        smiley4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smileys/6.gif"))); // NOI18N
        smiley4.setToolTipText(":I");
        smiley4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smiley4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smiley4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smiley4MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                smiley4MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                smiley4MouseReleased(evt);
            }
        });

        smiley5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        smiley5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smileys/7.gif"))); // NOI18N
        smiley5.setToolTipText(":S");
        smiley5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smiley5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smiley5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smiley5MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                smiley5MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                smiley5MouseReleased(evt);
            }
        });

        smiley6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        smiley6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smileys/8.gif"))); // NOI18N
        smiley6.setToolTipText("8)");
        smiley6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smiley6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smiley6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smiley6MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                smiley6MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                smiley6MouseReleased(evt);
            }
        });

        smiley7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        smiley7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smileys/9.gif"))); // NOI18N
        smiley7.setToolTipText(";)");
        smiley7.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smiley7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smiley7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smiley7MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                smiley7MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                smiley7MouseReleased(evt);
            }
        });

        smiley8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        smiley8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smileys/1.gif"))); // NOI18N
        smiley8.setToolTipText(":D");
        smiley8.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smiley8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smiley8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smiley8MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                smiley8MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                smiley8MouseReleased(evt);
            }
        });

        javax.swing.GroupLayout smileyPanelLayout = new javax.swing.GroupLayout(smileyPanel);
        smileyPanel.setLayout(smileyPanelLayout);
        smileyPanelLayout.setHorizontalGroup(
            smileyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(smileyPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(smileyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(smileyPanelLayout.createSequentialGroup()
                        .addComponent(smiley1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(smiley2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(smiley3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(smileyPanelLayout.createSequentialGroup()
                        .addComponent(smiley5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(smiley6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(smiley7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(smileyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(smiley8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smiley4, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        smileyPanelLayout.setVerticalGroup(
            smileyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(smileyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(smileyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(smiley3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smiley4, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smiley2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smiley1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(smileyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(smiley5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smiley6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smiley7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smiley8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(77, Short.MAX_VALUE))
        );

        SmileySrollPanel.setViewportView(smileyPanel);

        chatPanel.add(SmileySrollPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 460, 180, 100));

        discusionPane.setEditable(false);
        discusionPane.setBackground(new java.awt.Color(221, 222, 247));
        jScrollPane1.setViewportView(discusionPane);

        chatPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 750, 490));

        txtUserInput.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        txtUserInput.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        txtUserInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtUserInputKeyPressed(evt);
            }
        });
        chatPanel.add(txtUserInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 560, 690, 60));

        btnDeco.setBackground(new java.awt.Color(51, 51, 255));
        btnDeco.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        btnDeco.setForeground(new java.awt.Color(255, 255, 255));
        btnDeco.setLabel("Déconnecter");
        btnDeco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDecoActionPerformed(evt);
            }
        });
        chatPanel.add(btnDeco, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 560, 120, 60));

        btnSend.setBackground(new java.awt.Color(51, 51, 255));
        btnSend.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        btnSend.setForeground(new java.awt.Color(255, 255, 255));
        btnSend.setLabel("Envoyer");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });
        chatPanel.add(btnSend, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 560, 100, 60));

        connectedUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                connectedUsersMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(connectedUsers);

        chatPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 60, 220, 490));

        smileyBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/smiley.png"))); // NOI18N
        smileyBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        smileyBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        smileyBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smileyBtnMouseClicked(evt);
            }
        });
        chatPanel.add(smileyBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 560, 60, 60));

        getContentPane().add(chatPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1010, 640));

        loginPanel.setMinimumSize(new java.awt.Dimension(1020, 640));
        loginPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Image.setBackground(new java.awt.Color(0, 0, 204));
        Image.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/esichat.png"))); // NOI18N
        Image.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 150, 380, 350));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/81RTZEw.png"))); // NOI18N
        Image.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(-910, -90, 1440, 750));

        loginPanel.add(Image, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 500, 650));

        loginFrom.setBackground(new java.awt.Color(32, 33, 35));

        labelPseudo.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        labelPseudo.setForeground(new java.awt.Color(0, 255, 255));
        labelPseudo.setText("Pseudo");

        txtPseudo.setBackground(new java.awt.Color(32, 33, 35));
        txtPseudo.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        txtPseudo.setForeground(new java.awt.Color(255, 255, 255));
        txtPseudo.setBorder(null);
        txtPseudo.setCaretColor(new java.awt.Color(255, 255, 255));
        txtPseudo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPseudoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPseudoFocusLost(evt);
            }
        });

        txtAdressIp.setBackground(new java.awt.Color(32, 33, 35));
        txtAdressIp.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        txtAdressIp.setForeground(new java.awt.Color(255, 255, 255));
        txtAdressIp.setText("localhost");
        txtAdressIp.setBorder(null);
        txtAdressIp.setCaretColor(new java.awt.Color(255, 255, 255));
        txtAdressIp.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtAdressIpFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAdressIpFocusLost(evt);
            }
        });

        btnConexion.setBackground(new java.awt.Color(126, 87, 194));
        btnConexion.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        btnConexion.setForeground(new java.awt.Color(255, 255, 255));
        btnConexion.setLabel("Connexion");
        btnConexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConexionActionPerformed(evt);
            }
        });

        labelAdresse.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        labelAdresse.setForeground(new java.awt.Color(51, 52, 54));
        labelAdresse.setText("Adresse du serveur");

        labelPort.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        labelPort.setForeground(new java.awt.Color(51, 52, 54));
        labelPort.setText("Port");

        txtPort.setBackground(new java.awt.Color(32, 33, 35));
        txtPort.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        txtPort.setForeground(new java.awt.Color(255, 255, 255));
        txtPort.setText("8080");
        txtPort.setBorder(null);
        txtPort.setCaretColor(new java.awt.Color(255, 255, 255));
        txtPort.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPortFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPortFocusLost(evt);
            }
        });

        labelInfoMessage.setFont(new java.awt.Font("Arial Black", 1, 12)); // NOI18N
        labelInfoMessage.setForeground(new java.awt.Color(255, 0, 0));
        labelInfoMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout loginFromLayout = new javax.swing.GroupLayout(loginFrom);
        loginFrom.setLayout(loginFromLayout);
        loginFromLayout.setHorizontalGroup(
            loginFromLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginFromLayout.createSequentialGroup()
                .addGap(599, 599, 599)
                .addGroup(loginFromLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnConexion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtPseudo)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2)
                    .addComponent(labelAdresse, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                    .addComponent(labelPseudo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelPort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtAdressIp)
                    .addComponent(txtPort)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(127, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loginFromLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelInfoMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43))
        );
        loginFromLayout.setVerticalGroup(
            loginFromLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginFromLayout.createSequentialGroup()
                .addGap(145, 145, 145)
                .addComponent(labelPseudo, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPseudo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelAdresse, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAdressIp, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelPort, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43)
                .addComponent(btnConexion, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(labelInfoMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );

        loginPanel.add(loginFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 650));

        getContentPane().add(loginPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1010, 640));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Quand le champ de saisie pour l'adresse ip gagne le focus
     * @param evt 
     */
    private void txtAdressIpFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAdressIpFocusGained
       this.labelAdresse.setForeground(new Color(0,255,255));
    }//GEN-LAST:event_txtAdressIpFocusGained

    private void txtPseudoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPseudoFocusLost
        this.labelPseudo.setForeground(new Color(51,52,54));
    }//GEN-LAST:event_txtPseudoFocusLost

    private void txtPseudoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPseudoFocusGained
        this.labelPseudo.setForeground(new Color(0,255,255));
    }//GEN-LAST:event_txtPseudoFocusGained

    private void txtAdressIpFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAdressIpFocusLost
        this.labelAdresse.setForeground(new Color(51,52,54));
    }//GEN-LAST:event_txtAdressIpFocusLost

    private void txtPortFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPortFocusGained
        this.labelPort.setForeground(new Color(0,255,255));        
    }//GEN-LAST:event_txtPortFocusGained

    private void txtPortFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPortFocusLost
        this.labelPort.setForeground(new Color(51,52,54));
    }//GEN-LAST:event_txtPortFocusLost

    private void labelCloseMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelCloseMousePressed
        System.exit(0);
    }//GEN-LAST:event_labelCloseMousePressed

    private void labelReduceMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelReduceMousePressed
        this.setState(ClientInterface.ICONIFIED);
    }//GEN-LAST:event_labelReduceMousePressed

    private void deplacementMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deplacementMouseDragged
        // bouger la fenetre quand on clic en haut et qu'on bouge la souris
        //position de la souris a l'ecran
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        // position de la souris sur l'ecran - la position ou on clic
        this.setLocation(x - xSouris, y - ySouris);
    }//GEN-LAST:event_deplacementMouseDragged

    private void deplacementMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deplacementMouseMoved
        xSouris = evt.getX();
        ySouris = evt.getY();
    }//GEN-LAST:event_deplacementMouseMoved

    /**
     * Bouton pour etablir la connexion
     * @param evt 
     */
    private void btnConexionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConexionActionPerformed
        disableLoginFields();
        this.pseudo = this.txtPseudo.getText();
        this.port = Integer.parseInt(this.txtPort.getText());
        this.ServName = this.txtAdressIp.getText();
        if(!pseudo.isEmpty()){
            this.labelInfoMessage.setForeground(new Color(255,0,0));
            this.labelInfoMessage.setText("Connexion au serveur en cours ...");

            try {
                this.server = new Socket(this.ServName, this.port);
                this.reader = new BufferedReader(new InputStreamReader(this.server.getInputStream()));
                this.writer = new PrintWriter(this.server.getOutputStream(),true);

                // envoyer le pseudo au serveur
                this.writer.println(this.pseudo); 

                // Lire la reponse envoyé par le serveur concerant le Pseudo: si le serveur dit que le pseudo est existe déjà
                String servMgs = this.reader.readLine();
                // [0] -> header  [1] -> message    Message type:  header | message
                String[] mgs = servMgs.split("\\|");

                if(mgs[0].equals("Accept")){
                    this.loginPanel.setVisible(false);
                    this.chatPanel.setVisible(true);
                    appendJTextPane(discusionPane, "<h2>Connecté au serveur " + this.server.getInetAddress().getHostAddress() + " sur le port " + this.server.getPort() + "</h2>");
                    deplacement.setText("EsiChat v1.0 : " + pseudo);

                    // Creer un nouveau ReadThread
                    this.readThread = new ReadThread(reader, this);
                    this.readThread.start();

                    /**
                     * *********************************************** TO DO
                     */
                }else if (mgs[0].equals("Refuse")){
                    this.labelInfoMessage.setForeground(new Color(255,0,0));
                    this.labelInfoMessage.setText("Connexion refusé: " + mgs[1]);
                    this.server.close();
                    enableLoginFields();
                }else{
                    enableLoginFields();
                    System.out.println("message inconnu");
                }
            } catch (IOException ex) {
                System.out.println("Erreur de connexion");
                this.labelInfoMessage.setForeground(new Color(255,0,0));
                this.labelInfoMessage.setText("Erreur de connexion");            
                //this.labelInfoMessage.setIcon(new ImageIcon(getClass().getResource("../assets/load.gif")));
                //this.labelInfoMessage.setIcon(null);

                enableLoginFields();
            }        
        }else{  // le pseudo esr vide
            this.labelInfoMessage.setText("Le pseudo est obligatoire");
        }
    }//GEN-LAST:event_btnConexionActionPerformed
    
   
    /**
     * Se déconnecter du serveur
     * @param evt 
     */
    private void btnDecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDecoActionPerformed
//        try {
//           readThread.interrupt();
//            //this.connectedUsers.setText(null);
//            this.discusionPane.setText(null);
//            this.deplacement.setText("EsiChat v1.0");
//            //writer.close();
//            reader.close();
//            //this.server.close();
//
//            this.labelInfoMessage.setForeground(new Color(0,255,0));
//            this.labelInfoMessage.setText("Déconnecté du serveur");
//            this.chatPanel.setVisible(false);
//            this.loginPanel.setVisible(true);
//            enableLoginFields();
//        } catch (IOException ex) {
//            System.out.println("Probele lors de la déconnexion " + ex);
//        }
            System.exit(0); // si pas de meilleur solution
    }//GEN-LAST:event_btnDecoActionPerformed

    /**
     * Clic sur le bouton d'envoie
     * @param evt 
     */
    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        sendMessage();
    }//GEN-LAST:event_btnSendActionPerformed

    private void labelReduceMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelReduceMouseEntered
        this.labelReduce.setIcon(new ImageIcon(getClass().getResource("/assets/minimizeHover.png")));
    }//GEN-LAST:event_labelReduceMouseEntered

    private void labelReduceMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelReduceMouseExited
        if(this.isFocused()){
            this.labelReduce.setIcon(new ImageIcon(getClass().getResource("/assets/minimize.png")));
        }else{
            this.labelReduce.setIcon(new ImageIcon(getClass().getResource("/assets/unFocus.png")));
        }
    }//GEN-LAST:event_labelReduceMouseExited

    
    private void labelCloseMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelCloseMouseEntered
        this.labelClose.setIcon(new ImageIcon(getClass().getResource("/assets/closeHover.png")));
    }//GEN-LAST:event_labelCloseMouseEntered

    /**
     * Quand la souris quitte le champs du bouton pour fermer la fenetre
     * @param evt 
     */
    private void labelCloseMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelCloseMouseExited
        if(this.isFocused()){
            this.labelClose.setIcon(new ImageIcon(getClass().getResource("/assets/close.png")));
        }else{
            this.labelClose.setIcon(new ImageIcon(getClass().getResource("/assets/unFocus.png")));
    }
    }//GEN-LAST:event_labelCloseMouseExited

    /**
     * Quand la fenetre perd le focus
     * @param evt l'evenement
     */
    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        this.labelReduce.setIcon(new ImageIcon(getClass().getResource("/assets/unFocus.png")));
        this.labelClose.setIcon(new ImageIcon(getClass().getResource("/assets/unFocus.png")));
    }//GEN-LAST:event_formWindowLostFocus

    /**
     * Quand la fenetre gagne le focus
     * @param evt l'evenement
     */
    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        this.labelReduce.setIcon(new ImageIcon(getClass().getResource("/assets/minimize.png")));
        this.labelClose.setIcon(new ImageIcon(getClass().getResource("/assets/close.png")));
    }//GEN-LAST:event_formWindowGainedFocus

    /**
     * Quand l'utilisateur clic sur le Jlist (connectedUsers)
     * @param evt l'evenement
     */
    private void connectedUsersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_connectedUsersMouseClicked
         // si la selection n'est pas vide
        if(!this.connectedUsers.isSelectionEmpty()){
            this.connectedUsers.setSelectedIndex(this.connectedUsers.locationToIndex(evt.getPoint()));

            row = this.connectedUsers.getSelectedValue().toString();
            index = this.connectedUsers.getSelectedIndex();

            // si click droit et l'element selectioné est l'index
            if(SwingUtilities.isRightMouseButton(evt) && this.connectedUsers.locationToIndex(evt.getPoint()) == index){
                    pop.show(this.connectedUsers, evt.getX(), evt.getY());
            }
        }

    }//GEN-LAST:event_connectedUsersMouseClicked

    // Gstion smiley
    private void smileyBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smileyBtnMouseClicked
        this.SmileySrollPanel.setVisible(!smileyClicked);
        if(!smileyClicked){
            this.smileyBtn.setIcon(new ImageIcon(getClass().getResource("/assets/smileySelected.png")));
            smileyBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
            this.smileyClicked = true;
        }else{
            this.smileyBtn.setIcon(new ImageIcon(getClass().getResource("/assets/smiley.png")));
            smileyBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            this.smileyClicked = false;
        }
    }//GEN-LAST:event_smileyBtnMouseClicked

    private void smiley1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley1MouseClicked
       appendSmiley(this.smiley1.getToolTipText());  // ToolTipText réglé a :) pour smiley1
    }//GEN-LAST:event_smiley1MouseClicked

    private void smiley1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley1MousePressed
        this.smiley1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    }//GEN-LAST:event_smiley1MousePressed

    private void smiley1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley1MouseReleased
        this.smiley1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    }//GEN-LAST:event_smiley1MouseReleased

    private void smiley2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley2MouseClicked
        appendSmiley(this.smiley2.getToolTipText());
    }//GEN-LAST:event_smiley2MouseClicked

    private void smiley2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley2MousePressed
        this.smiley2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    }//GEN-LAST:event_smiley2MousePressed

    private void smiley2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley2MouseReleased
        this.smiley2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    }//GEN-LAST:event_smiley2MouseReleased

    private void smiley3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley3MouseClicked
        appendSmiley(this.smiley3.getToolTipText());
    }//GEN-LAST:event_smiley3MouseClicked

    private void smiley3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley3MousePressed
        this.smiley3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    }//GEN-LAST:event_smiley3MousePressed

    private void smiley3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley3MouseReleased
        this.smiley3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    }//GEN-LAST:event_smiley3MouseReleased

    private void smiley4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley4MouseClicked
        appendSmiley(this.smiley4.getToolTipText());
    }//GEN-LAST:event_smiley4MouseClicked

    private void smiley4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley4MousePressed
        this.smiley4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    }//GEN-LAST:event_smiley4MousePressed

    private void smiley4MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley4MouseReleased
        this.smiley4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    }//GEN-LAST:event_smiley4MouseReleased

    private void smiley5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley5MouseClicked
        appendSmiley(this.smiley5.getToolTipText());
    }//GEN-LAST:event_smiley5MouseClicked

    private void smiley5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley5MousePressed
        this.smiley5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    }//GEN-LAST:event_smiley5MousePressed

    private void smiley5MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley5MouseReleased
        this.smiley5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    }//GEN-LAST:event_smiley5MouseReleased

    private void smiley6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley6MouseClicked
        appendSmiley(this.smiley6.getToolTipText());
    }//GEN-LAST:event_smiley6MouseClicked

    private void smiley6MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley6MousePressed
        this.smiley6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    }//GEN-LAST:event_smiley6MousePressed

    private void smiley6MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley6MouseReleased
        this.smiley6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    }//GEN-LAST:event_smiley6MouseReleased

    private void smiley7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley7MouseClicked
        appendSmiley(this.smiley7.getToolTipText());
    }//GEN-LAST:event_smiley7MouseClicked

    private void smiley7MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley7MousePressed
        this.smiley7.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    }//GEN-LAST:event_smiley7MousePressed

    private void smiley7MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley7MouseReleased
        this.smiley7.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    }//GEN-LAST:event_smiley7MouseReleased

    private void smiley8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley8MouseClicked
        appendSmiley(this.smiley8.getToolTipText());
    }//GEN-LAST:event_smiley8MouseClicked

    private void smiley8MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley8MousePressed
        this.smiley8.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    }//GEN-LAST:event_smiley8MousePressed

    private void smiley8MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smiley8MouseReleased
        this.smiley8.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
    }//GEN-LAST:event_smiley8MouseReleased

    /**
     * Quand l'utilisateur appuis sur un bouton dans le champ de saisie
     * @param evt 
     */
    private void txtUserInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtUserInputKeyPressed
        // implementer autre action avec les boutons
        if (evt.getKeyCode() == KeyEvent.VK_ENTER){
            sendMessage();
        }else if(evt.getKeyCode() == KeyEvent.VK_UP){
            if (previousMessagesIndex > 0) {
                previousMessagesIndex--;
                onArrowClick();
            }
        }else if(evt.getKeyCode() == KeyEvent.VK_DOWN){
            if (this.previousMessages.size() > previousMessagesIndex) {
                onArrowClick();
                previousMessagesIndex++;
            }else if(this.previousMessagesIndex == this.previousMessages.size()){
                this.txtUserInput.setText("");
            }
        }
    }//GEN-LAST:event_txtUserInputKeyPressed

    
    private void appendSmiley(String smiley){
        this.txtUserInput.setText(this.txtUserInput.getText() + smiley);
    }
    
    public void closeWhenError(){
        System.exit(0);
    }

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ClientInterface clientLog = new ClientInterface();
                clientLog.setVisible(true);
                clientLog.setLocationRelativeTo(null);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Image;
    private javax.swing.JScrollPane SmileySrollPanel;
    private java.awt.Button btnConexion;
    private java.awt.Button btnDeco;
    private java.awt.Button btnSend;
    private javax.swing.JPanel chatPanel;
    private javax.swing.JList<String> connectedUsers;
    private javax.swing.JLabel deplacement;
    private javax.swing.JTextPane discusionPane;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelAdresse;
    private javax.swing.JLabel labelClose;
    private javax.swing.JLabel labelInfoMessage;
    private javax.swing.JLabel labelPort;
    private javax.swing.JLabel labelPseudo;
    private javax.swing.JLabel labelReduce;
    private javax.swing.JPanel loginFrom;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JLabel smiley1;
    private javax.swing.JLabel smiley2;
    private javax.swing.JLabel smiley3;
    private javax.swing.JLabel smiley4;
    private javax.swing.JLabel smiley5;
    private javax.swing.JLabel smiley6;
    private javax.swing.JLabel smiley7;
    private javax.swing.JLabel smiley8;
    private javax.swing.JLabel smileyBtn;
    private javax.swing.JPanel smileyPanel;
    private javax.swing.JTextField txtAdressIp;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtPseudo;
    private javax.swing.JTextField txtUserInput;
    // End of variables declaration//GEN-END:variables
}
