package GUI_Appic_Infos;

import Request.RequestControlID;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Properties;

public class GUI_Applic_Infos extends JFrame
{
    private JPanel rootPanel;
    private JButton buttonInfoCoursMonetaire;
    private JList listResult;

    private String _host;
    private int _port;
    private String _separator;
    private String _endOfLine;
    private Socket _connexion = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;
    private DefaultListModel<String> dlm;
    private String codeProvider = "BC"; //CryptixCrypto";
    private MessageDigest md;
    private ArrayList<String> _arrayOfArg = new ArrayList<>();



    public GUI_Applic_Infos() throws IOException {
        super("Application Informations");

        // Binder JList à un model
        dlm = new DefaultListModel<String>();
        listResult.setModel(dlm);

        // look de gui
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentPane(rootPanel);
        setExtendedState(JFrame.MAXIMIZED_HORIZ);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);    // centrer la fenetre
        pack();
        setVisible(true);

        // lire config.properties
        ReadPropertyFile();

        // se connecter au serveur
        try {
            _connexion = new Socket(_host, _port);
            oos = new ObjectOutputStream(_connexion.getOutputStream());
            ois = new ObjectInputStream(_connexion.getInputStream());
            dlm.addElement("Connexion Serveur Informations OK");
            System.out.println("Connexion Serveur Informations OK");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // listeners des boutons
        buttonInfoCoursMonetaire.addActionListener(e -> {
            try
            {
                btnInfoCoursMonetaires_handler();
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        });
    }

    private void btnInfoCoursMonetaires_handler() throws IOException {

        RequestControlID request = null;
        RequestControlID response = null;

        request = new RequestControlID("INFOP", RequestControlID.REQUEST_INFO_COURS);

        try {
            response = SendRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dlm.addElement("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        dlm.addElement("String reponse serveur : *" + response.toString() + "*");

        AnalyseReponse(response);

    }





    private void AnalyseReponse(RequestControlID response) {
        switch(response.getType()) {
            case RequestControlID.REQUEST_INFO_COURS:



                /////
                if(response.getResult().equals("ACK")) {

                    dlm.clear();
                    dlm.addElement("Les cours des principales unités monétaires (Euro, Dollar US, Yen, Franc suisse, Livre sterling) :");

                    // Lire reponse du serveur
                    String data = response.getData();
                    String tokfull = data.replaceAll(_endOfLine, "");
                    String[] tok = tokfull.split(_separator);

                    // recup nombre des monaies different
                    String nbreMonDiff = tok[0];

                    for(int i=0, j=1; i < Integer.parseInt(nbreMonDiff); i++, j++)
                    {

                        String[] data_monnaie = tok[j].split("\\|");
                        String nom_monnaie = data_monnaie[0];
                        String cours_monnaie = data_monnaie[1];
                        dlm.addElement("Monnaie : " + nom_monnaie + ", Cours : " + cours_monnaie);

                    }

                } else {
                    dlm.addElement("Erreur lors d'obtention des cours monetaires");
                }

                break;
            default :
                break;
        }

    }

    private RequestControlID SendRequest(RequestControlID request) throws IOException {

        oos.writeObject(request);
        oos.flush();

        System.out.println("Commande [" + request + "] envoyée au serveur");

        //On attend la réponse
        RequestControlID response = read();
        System.out.println("\t * " + response + " : Réponse reçue " + response);

        return response;
    }

    private String MakeRequest(String cmd, ArrayList<String> arrayOfArg) {
        String request = cmd;

        for(String str : arrayOfArg) {
            request += _separator + str;
        }

        request += _endOfLine;


        return request;
    }

    //Méthode pour lire les réponses du serveur
    private RequestControlID read() throws IOException{
        RequestControlID response = null;
        try {
            response = (RequestControlID)ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void ReadPropertyFile(){

        //Lecture PROPERTY FILE
        Properties _propFile = new Properties();
        InputStream _InStream = null;
        try
        {
            _InStream = new FileInputStream("config.properties");
            _propFile.load(_InStream);

            _port = Integer.parseInt(_propFile.getProperty("PORT"));
            _host = _propFile.getProperty("HOST");
            _separator = _propFile.getProperty("SEPARATOR");
            _endOfLine = _propFile.getProperty("ENDOFLINE");

            _InStream.close();

        } catch (IOException e) {
            System.err.println("Error Reading Properties Files [" + e + "]");
        }

    }

    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}
