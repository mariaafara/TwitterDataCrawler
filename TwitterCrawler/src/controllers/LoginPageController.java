package controllers;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Login Page
 *
 * @author Ali FAKIH, Maria Afara fakih.k.ali@gmail.com maria-afara5@hotmail.com
 *
 */
public class LoginPageController implements Initializable {

    GraphPageController graphPageController = null;
    @FXML
    private Label label;

    @FXML
    private Button choose;
    @FXML
    private CheckBox save;
    @FXML
    private TextField text1;
    @FXML
    private TextField text2;
    @FXML
    private TextField text3;
    @FXML
    private TextField text4;

    @FXML
    private Button auth;

    @FXML
    private Button bro;

    private ArrayList<Twitter> allAccount = new ArrayList<>();
    private String filePath = "";

    @FXML
    private void authenticate() throws SAXException, IOException {

        String consumerKey = text1.getText();
        String consumerSecret = text2.getText();
        String accessToken = text3.getText();
        String accessTokenSecret = text4.getText();
        if (!consumerKey.isEmpty() && !consumerSecret.isEmpty() && !accessToken.isEmpty() && !accessTokenSecret.isEmpty()) {
            if (save.isSelected()) {
                filePath = label.getText();
                File file = new File(label.getText());
                if (file.exists()) {
                    Document dom;
                    Element e = null;

                    // instance of a DocumentBuilderFactory
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    try {
                        // use factory to get an instance of document builder
                        DocumentBuilder db = dbf.newDocumentBuilder();

                        dom = db.parse(label.getText());
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
                                    new StreamResult(new FileOutputStream(label.getText())));

                        } catch (TransformerException | IOException te) {
                            System.out.println(te.getMessage());
                        }
                    } catch (ParserConfigurationException pce) {
                        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
                    }
                } else {//file doesnt exist so we should create new one
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
                                    new StreamResult(new FileOutputStream(label.getText())));

                        } catch (TransformerException | IOException te) {
                            System.out.println(te.getMessage());
                        }
                    } catch (ParserConfigurationException pce) {
                        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
                    }
                }

                //After adding the informations now we should traverse the file and collect the active accounts!
                forwad();

            } else { //if user won't to save

                Dialog<Boolean> dialog = new Dialog<>();

                dialog.initModality(Modality.WINDOW_MODAL);
                Label loader = new Label("LOADING");
                loader.setContentDisplay(ContentDisplay.BOTTOM);
                loader.setGraphic(new ProgressIndicator());
                dialog.getDialogPane().setGraphic(loader);
                DropShadow ds = new DropShadow();
                ds.setOffsetX(1.3);
                ds.setOffsetY(1.3);
                ds.setColor(Color.DARKGRAY);
                dialog.getDialogPane().setEffect(ds);
                dialog.setResult(Boolean.TRUE);

                Task<Boolean> task = new Task<Boolean>() {
                    @Override
                    public Boolean call() {

                        auth.setDisable(true);
                        bro.setDisable(true);
                        choose.setDisable(true);
                        save.setDisable(true);

                        ConfigurationBuilder cb = new ConfigurationBuilder();
                        cb.setDebugEnabled(true)
                                .setOAuthConsumerKey(consumerKey)
                                .setOAuthConsumerSecret(consumerSecret)
                                .setOAuthAccessToken(accessToken)
                                .setOAuthAccessTokenSecret(accessTokenSecret)
                                .setTweetModeExtended(true);
                        cb.setJSONStoreEnabled(true);

                        TwitterFactory tf = new TwitterFactory(cb.build());
                        Twitter twitter = tf.getInstance();
                        Boolean b = true;
                        try {
                            User user = twitter.verifyCredentials();
                            allAccount.clear();
                            allAccount.add(twitter);
                        } catch (TwitterException e) {
                            b = false;
                        }
                        return b;
                    }
                };

                task.setOnRunning((e) -> dialog.show());
                task.setOnSucceeded((e) -> {
                    dialog.close();
                    if (task.getValue()) {
                        PrintWriter printWriter;
                        try {
                            printWriter = new PrintWriter("nbrOfAccount.txt");
                            printWriter.println(allAccount.size());
                            printWriter.close();
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(LoginPageController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        Parent root = null;
                        FXMLLoader Loader = new FXMLLoader();

                        try {

                            Stage stage = (Stage) bro.getScene().getWindow();
                            stage.close();

                            Loader.setLocation(getClass().getResource("/views/Home.fxml"));
                            Loader.load();
                            root = Loader.getRoot();

                            HomeController homeController = Loader.getController();

                            //send the array of twitter account to the HomePage
                            homeController.setArrayOfAccounts(allAccount);
                            homeController.setFilePath(filePath);

                            //
                            FXMLLoader Loader1 = new FXMLLoader();
                            Loader1.setLocation(getClass().getResource("/views/GraphPage.fxml"));
                            Loader1.load();
                            graphPageController = Loader1.getController();
                            //send the array of twitter account to the graph page
                            graphPageController.setFilePath(filePath);

                        } catch (IOException ex) {
                            Logger.getLogger(LoginPageController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root, 1100, 650));
                        stage.setResizable(false);
                        graphPageController.stage = stage;
                        stage.show();
                        graphPageController.setArrayOfAccounts(allAccount);
                        graphPageController.stage = stage;
                        System.out.println("done");

                    } else {
                        System.out.println("faild");
                        //////////////check h0nnn later//////
                        auth.setDisable(false);
                        bro.setDisable(false);
                        choose.setDisable(false);
                        save.setDisable(false);
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setHeaderText("Information Alert");
                        String s = "Not Active Account!";
                        alert.setContentText(s);
                        alert.show();

                    }
                });
                task.setOnFailed((e) -> {
                    dialog.close();
                });
                new Thread(task).start();

            }
        } else {
            Alert alert = new Alert(AlertType.ERROR);

            alert.setHeaderText("Error Alert");
            String s = "Please Fill All The TextFields!";

            alert.setContentText(s);
            alert.show();

        }

    }

    @FXML
    private void ClickBtn() throws IOException {
        if (save.isSelected()) {
            label.setVisible(true);
            choose.setVisible(true);
        } else {
            label.setVisible(false);
            choose.setVisible(false);
        }

    }

    @FXML
    private void ChangePath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Specify a file to save");

        chooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("XML", "*.xml"));

        File file = chooser.showSaveDialog(null);
        String filename = file.getAbsolutePath();
        //this filePath is sent to home page
        filePath = filename;
        label.setText(filename);
    }

    @FXML
    private void Browse() throws FileNotFoundException, IOException, InterruptedException {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a file to open");

        chooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("XML", "*.xml"));

        File file = chooser.showOpenDialog(null);
        //this filePath is sent to home page home controller
        filePath = file.getAbsolutePath();
        label.setText(file.getAbsolutePath());
        if (file != null) {
            forwad();

        } else {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Information Alert");
            String s = "You canceled!";
            alert.setContentText(s);
            alert.show();

        }

    }

    private boolean traverseFile() {
        String conskey = null;
        String consSek = null;
        String AccesTok = null;
        String AccesSec = null;
        Document dom;

        allAccount.clear();
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file
            dom = db.parse(label.getText());
            Element doc = dom.getDocumentElement();
            NodeList nl = doc.getElementsByTagName("App");
            for (int i = 0; i < nl.getLength(); i++) {
                NodeList app = nl.item(i).getChildNodes();
                if (app.getLength() == 9) { //we have the 4 variables in The app

                    conskey = app.item(1).getFirstChild().getNodeValue();
                    consSek = app.item(3).getFirstChild().getNodeValue();
                    AccesTok = app.item(5).getFirstChild().getNodeValue();
                    AccesSec = app.item(7).getFirstChild().getNodeValue();
                    System.out.println(conskey + " " + conskey + " " + AccesTok + " " + AccesSec);
                    //        System.out.println(checkAuth(conskey, consSek, AccesTok, AccesSec));
                    if (checkAuth(conskey, consSek, AccesTok, AccesSec)) {
                        System.out.println("Valid Account!");
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException pce) {
            System.out.println(pce.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return allAccount.size() > 0;
    }

    private boolean checkAuth(String conskey, String consSek, String AccesTok, String AccesSec) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(conskey)
                .setOAuthConsumerSecret(consSek)
                .setOAuthAccessToken(AccesTok)
                .setOAuthAccessTokenSecret(AccesSec)
                .setTweetModeExtended(true);
        cb.setJSONStoreEnabled(true);

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        Boolean b = true;
        try {
            User user = twitter.verifyCredentials();
            allAccount.add(twitter);
        } catch (TwitterException e) {
            System.out.println(e);
            b = false;
        }
        return b;

    }

    public void forwad() {

        auth.setDisable(true);
        bro.setDisable(true);
        choose.setDisable(true);
        save.setDisable(true);
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        Label loader = new Label("Loging in...");
        loader.setContentDisplay(ContentDisplay.BOTTOM);
        loader.setGraphic(new ProgressIndicator());
        dialog.getDialogPane().setGraphic(loader);
        //dialog.showAndWait();
        DropShadow ds = new DropShadow();
        ds.setOffsetX(1.3);
        ds.setOffsetY(1.3);
        ds.setColor(Color.DARKGRAY);
        dialog.getDialogPane().setEffect(ds);
        dialog.setResult(Boolean.TRUE);

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            public Boolean call() {
                return traverseFile();

            }
        };

        task.setOnRunning((e) -> dialog.show());
        task.setOnSucceeded((e) -> {
            dialog.close();
            if (task.getValue()) {
                PrintWriter printWriter;
                try {
                    printWriter = new PrintWriter("nbrOfAccount.txt");
                    printWriter.println(allAccount.size());
                    printWriter.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(LoginPageController.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("success");
                Parent root = null;
                FXMLLoader Loader = new FXMLLoader();

                try {

                    Stage stage = (Stage) bro.getScene().getWindow();
                    stage.close();

                    Loader.setLocation(getClass().getResource("/views/Home.fxml"));
                    Loader.load();
                    root = Loader.getRoot();

                    HomeController homeController = Loader.getController();

                    //send the array of twitter account to the HomePage
                    homeController.setArrayOfAccounts(allAccount);
                    homeController.setFilePath(filePath);

                    //
                    FXMLLoader Loader1 = new FXMLLoader();
                    Loader1.setLocation(getClass().getResource("/views/GraphPage.fxml"));
                    Loader1.load();
                    graphPageController = Loader1.getController();
                    //send the array of twitter account to the graph page
                    graphPageController.setFilePath(filePath);

                } catch (IOException ex) {
                    Logger.getLogger(LoginPageController.class.getName()).log(Level.SEVERE, null, ex);
                }
                Stage stage = new Stage();
                stage.setScene(new Scene(root, 1100, 650));
                stage.setResizable(false);
                graphPageController.stage = stage;
                stage.show();
                graphPageController.setArrayOfAccounts(allAccount);
                graphPageController.stage = stage;
                System.out.println("done");

            } else {
                System.out.println("faild");
                auth.setDisable(false);
                bro.setDisable(false);
                choose.setDisable(false);
                save.setDisable(false);
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("Information Alert");
                String s = "You Dont Have Active Accounts!";
                alert.setContentText(s);
                alert.show();

            }
        });
        task.setOnFailed((e) -> {
            dialog.close();
        });
        new Thread(task).start();

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        label.setText(System.getProperty("user.dir") + "/src/Keys.xml");

    }

}
