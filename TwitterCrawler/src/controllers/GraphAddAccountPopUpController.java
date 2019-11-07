/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static controllers.GraphPageController.allAccounts;
import static controllers.GraphPageController.filePath;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * FXML Controller class
 * Page used to add twitter developer account at any time
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class GraphAddAccountPopUpController implements Initializable {

    @FXML
    private TextField text1;
    @FXML
    private TextField text2;
    @FXML
    private TextField text3;
    @FXML
    private TextField text4;

    @FXML
    private Button cancel;

    @FXML
    private Button addAccount;
    TwitterThread twitterthread;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 
        System.out.println("*+++++******++++-***********************************************************++*******+++++****");
    }

    @FXML
    private void cancelPopup() {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void addAccount() throws SAXException, IOException {
        System.out.println("*+++++******++++++*******+++++****");
        String consumerKey = text1.getText();
        String consumerSecret = text2.getText();
        String accessToken = text3.getText();
        String accessTokenSecret = text4.getText();
        if (!consumerKey.isEmpty() && !consumerSecret.isEmpty() && !accessToken.isEmpty() && !accessTokenSecret.isEmpty()) {
            if (checkAuth(consumerKey, consumerSecret, accessToken, accessTokenSecret)) {
                if (!filePath.isEmpty()) {

                    File file = new File(filePath);
                    if (file.exists()) {
                        writeToOldfile(consumerKey, consumerSecret, accessToken, accessTokenSecret);
                    } else {//file doesnt exist so we should create new one
                        writeToNewfile(consumerKey, consumerSecret, accessToken, accessTokenSecret);
                    }

                } else {

                }
                //after saving
                Stage stage = (Stage) cancel.getScene().getWindow();
                stage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Invalid Account");
                String s = "Please enter a valid account";
                alert.setContentText(s);
                alert.show();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setHeaderText("Error Alert");
            String s = "Please Fill All The TextFields!";

            alert.setContentText(s);
            alert.show();

        }

    }

    private void writeToOldfile(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) throws SAXException, IOException {
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            dom = db.parse(filePath);
            Element doc = dom.getDocumentElement();
            // create the root element
            Element rootEle = dom.createElement("App");

            // create data elements and place them under root
            e = dom.createElement("ConsumerKey");
            e.appendChild(dom.createTextNode(consumerKey));
            rootEle.appendChild(e);

            e = dom.createElement("ConsumerSecret");
            e.appendChild(dom.createTextNode(consumerSecret));
            rootEle.appendChild(e);

            e = dom.createElement("AccessToken");
            e.appendChild(dom.createTextNode(accessToken));
            rootEle.appendChild(e);

            e = dom.createElement("AccessTokenSecret");
            e.appendChild(dom.createTextNode(accessTokenSecret));
            rootEle.appendChild(e);
            doc.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(filePath)));

            } catch (TransformerException | IOException te) {
                System.out.println(te.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    private void writeToNewfile(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootElet = dom.createElement("Apps");

            Element rootEle = dom.createElement("App");

            // create data elements and place them under root
            e = dom.createElement("ConsumerKey");
            e.appendChild(dom.createTextNode(consumerKey));
            rootEle.appendChild(e);

            e = dom.createElement("ConsumerSecret");
            e.appendChild(dom.createTextNode(consumerSecret));
            rootEle.appendChild(e);

            e = dom.createElement("AccessToken");
            e.appendChild(dom.createTextNode(accessToken));
            rootEle.appendChild(e);

            e = dom.createElement("AccessTokenSecret");
            e.appendChild(dom.createTextNode(accessTokenSecret));
            rootEle.appendChild(e);
            rootElet.appendChild(rootEle);
            dom.appendChild(rootElet);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(filePath)));

            } catch (TransformerException | IOException te) {
                System.out.println(te.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    private boolean checkAuth(String conskey, String consSek, String AccesTok, String AccesSec) {
        ConfigurationBuilder cb;
        Twitter twitterr;
        TwitterFactory tf;
        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(conskey)
                .setOAuthConsumerSecret(consSek)
                .setOAuthAccessToken(AccesTok)
                .setOAuthAccessTokenSecret(AccesSec)
                .setTweetModeExtended(true);
        cb.setJSONStoreEnabled(true);


        tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        Boolean b = true;
        try {
            User user = twitter.verifyCredentials();
            allAccounts.add(twitter);
            GraphPageController.getInstance().setNumberAcc();
            System.out.println("-=-=-=-=-=-=-" + allAccounts.size());
            PrintWriter printWriter;
            try {
                printWriter = new PrintWriter("nbrOfAccount.txt");
                printWriter.println(allAccounts.size());
                printWriter.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoginPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (TwitterException e) {

            b = false;
        }

        return b;

    }
}
