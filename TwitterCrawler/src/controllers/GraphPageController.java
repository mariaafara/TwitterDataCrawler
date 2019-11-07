/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * FXML Controller class
 
 * The main idea of this page is to crawl the graph of friends, friends of friends…. 
 * for a specific twitter user Account or a file that contains multiple twitter user id.
 * 
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class GraphPageController implements Initializable {

    @FXML
    private TableView<Content> tableView;
    @FXML
    private TableColumn<Content, String> Informations;
    @FXML
    private TableColumn<Content, String> Date;
    @FXML
    private Label saveResults;
    @FXML
    private Label fileofIDS;
    @FXML
    private Label nbrNodeCrawledLabel;
    @FXML
    Label numberAccounts;
    @FXML
    private Button Xbutton;
    @FXML
    private TextField userId;
    @FXML
    private TextField Depth;

    @FXML
    private Label valueOfSlider;

    @FXML
    private Button addbtn;
    @FXML
    Button startCrawlingButton;
    @FXML
    private Button stopCrawlingButton;
    @FXML
    private Button clearHistoryButton;
    @FXML
    private Button resumeCrawlingButton;
    @FXML
    private Button chooseFileButton;
    @FXML
    private Button openFileOfIdsButton;

    @FXML
    private Slider slider;
    @FXML
    private Tooltip tooltip;
    @FXML
    private Button graphhelp;

    static TwitterThread twitterThread;

    static ArrayList<Twitter> allAccounts = new ArrayList<>();
    static String s;
    static String filePath;
    static Stage stage;
    Twitter twitter;  //currentAccount

    private final ObservableList<Content> data
            = FXCollections.observableArrayList();

    private static GraphPageController instance;

    public GraphPageController() {
        instance = this;
    }

    public static GraphPageController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Informations.setCellValueFactory(new PropertyValueFactory<>("Informations"));
        Date.setCellValueFactory(new PropertyValueFactory<>("Date"));
        tableView.setItems(data);
        Xbutton.setVisible(false);

        Scanner in;
        try { // Get the number of accounts from a file then delete it...
            File file = new File("nbrOfAccount.txt");
            in = new Scanner(file);
            numberAccounts.setText(in.nextLine());
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
        }

        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);
        openFileOfIdsButton.setDisable(false);
        resumeCrawlingButton.setDisable(true);
        clearHistoryButton.setDisable(true);

        slider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            valueOfSlider.setText("" + newValue);
        });
        slider.valueProperty().addListener((obs, oldval, newVal)
                -> slider.setValue(Math.round(newVal.doubleValue())));

        //if the user stop the crawling and the informations is saved in the file t.tmp then the user can resume 
//        File f = new File("t.tmp");
        File f = new File(System.getProperty("user.dir") + "/ImportantData/t.tmp");
        if (f.exists()) {
            //disable buttons unlease cleared history
            HomeController.getInstance().disableButtons();
            resumeCrawlingButton.setDisable(false);
            startCrawlingButton.setDisable(true);
            clearHistoryButton.setDisable(false);
            stopCrawlingButton.setDisable(true);
            chooseFileButton.setDisable(true);
            openFileOfIdsButton.setDisable(true);
            userId.setDisable(true);

        }
        System.out.println("**********************Graph");

    }

    public void getTableData(TableView table) {
        try {

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/src/TableContent.txt", true)));
            Content content = new Content();

            for (int i = 0; i < table.getItems().size(); i++) {
                content = (Content) table.getItems().get(i);
                System.out.println(content.getInformations());
                writer.println(content.getInformations() + "__" + content.getDate());
            }
            content = new Content("Stop Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println(content.getInformations() + "__" + content.getDate());
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void importTableData(TableView table, ObservableList<Content> data) throws FileNotFoundException, IOException {
        String line;
        String[] contents;
        String infos, date;

        //by creating the contnts the observablelist of contents will t=dieclty listen to them and add them
        FileInputStream f = new FileInputStream(System.getProperty("user.dir") + "/src/TableContent.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(f, Charset.forName("UTF-8")));
        while ((line = br.readLine()) != null) {

            contents = line.split("__");
            System.out.println("!!!!!!!!!!!!!!!!!!" + contents[0] + "********");

            infos = contents[0];
            date = contents[1];
            Content content = new Content();
            content.setInformations(infos);
            content.setDate(date);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    data.add(content);
                }
            });
        }
        br.close();
        //inorder to empty the tablecontent file
        PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/src/TableContent.txt");
        pw.close();
        String s = System.getProperty("user.dir") + "/src/TableContent.txt";
        File file = new File(s);
        file.delete();

    }

    public void setNumberAccounts() {

        // this.numberAccounts.setText(allAccounts.size() + "e");
    }

    public void setNumberAcc() {

        this.numberAccounts.setText(allAccounts.size() + "");
    }

    @FXML
    private void openHelp() throws IOException {
        System.out.println("*-*-*-*-*-*-*-*-*-");
        Stage stage;
        Parent root;
        stage = new Stage();
        stage.getIcons().add(new Image("/icons/icons8_Info_32px.png"));
        System.out.println("*-*-*-*-*-*-*-*-*-");
        // stage.setTitle("Help Page");
        root = FXMLLoader.load(getClass().getResource("/views/GraphHelpPage.fxml"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(graphhelp.getScene().getWindow());
        stage.show();
    }

    @FXML
    private void StartCrawling() throws TwitterException, FileNotFoundException, IOException {

        // System.out.println(numberAccounts.getText());
        nbrNodeCrawledLabel.setText("0");
        tableView.getItems().clear();
        numberAccounts.setText(allAccounts.size() + "");
        if (!userId.getText().isEmpty()) {
            int depth;

            depth = (int) slider.getValue();
            if (saveResults.getText().isEmpty()) {
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Information Dialog");
                alert1.setHeaderText("Look, an Information Dialog");
                alert1.setContentText("You Should Choose a file to save the result");
                alert1.showAndWait();
                return;
            }
            if (!isNumeric(userId.getText())) { //if the input is a name
                try {
                    String name = userId.getText();
                    String[] s = userId.getText().split("/");
                    if (s.length > 1) {
                        name = s[s.length - 1];
                    }
                    if (name.charAt(0) == '@') {
                        name = name.substring(1, name.length());
                    }
                    User user = allAccounts.get(0).showUser(name);
                    long id = user.getId();
                    twitterThread = new TwitterThread(allAccounts, id, depth, saveResults.getText(), data, nbrNodeCrawledLabel, this);
                } catch (TwitterException t) {
                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                    alert1.setTitle("Information Dialog");
                    alert1.setHeaderText("Look, an Information Dialog");
                    alert1.setContentText("Not Valid Name");
                    alert1.showAndWait();
                    return;
                }
            } else { //the input is an ID

                try {
                    long id = Long.parseLong(userId.getText());
                    User user = allAccounts.get(0).showUser(id);

                    twitterThread = new TwitterThread(allAccounts, id, depth, saveResults.getText(), data, nbrNodeCrawledLabel, this);
                } catch (NumberFormatException | TwitterException numberFormatException) {
                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                    alert1.setTitle("Information Dialog");
                    alert1.setHeaderText("Look, an Information Dialog");
                    alert1.setContentText("Not a Valid Id ");
                    alert1.showAndWait();
                    return;
                }
            }
            HomeController.getInstance().disableButtons();
            disableEnableButton();
            twitterThread.start();

        } else if (!fileofIDS.getText().isEmpty()) {  // we get the ids from a file

            int depth = (int) slider.getValue();

            if (saveResults.getText().isEmpty()) {
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Information Dialog");
                alert1.setHeaderText("Look, an Information Dialog");
                alert1.setContentText("You Should Choose a file to save the result");
                alert1.showAndWait();
                return;
            }
            File f = new File(fileofIDS.getText());
            Scanner in = new Scanner(f);
            String temp;
            ArrayList<Long> arrayIds = new ArrayList<>();
            while (in.hasNext()) {
                temp = in.nextLine();
                try {
                    arrayIds.add(Long.parseLong(temp));
                } catch (NumberFormatException n) {

                }
            }
            in.close();
            if (arrayIds.size() > 0) {
                twitterThread = new TwitterThread(allAccounts, arrayIds, depth, saveResults.getText(), data, nbrNodeCrawledLabel, this);
                HomeController.getInstance().disableButtons();
                disableEnableButton();
                twitterThread.start();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText("File Should Contains IDS!");
                alert.showAndWait();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Look, an Information Dialog");
            alert.setContentText("Please Enter an Id or Choose a File");

            alert.showAndWait();

        }

    }

    @FXML
    private void chooseFile() throws IOException {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Specify a file to save");

        chooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text File", "*.txt"), new FileChooser.ExtensionFilter("Csv File", " *.csv"));

        File file = chooser.showSaveDialog(null);
        if (file != null) {
            String filename = file.getAbsolutePath();
            saveResults.setText(filename);
            tooltip.setText(filename);
            file.delete();
            file = new File(filename);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            int i = file.getAbsolutePath().lastIndexOf('.');
            String ext = file.getAbsolutePath().substring(i + 1);
            if (ext.equals("csv")) {
                StringBuilder sb = new StringBuilder();

                sb.append("Source,");
                //sb.append(',');
                sb.append("Target");
                sb.append('\n');

                writer.write(sb.toString());
            } else {
                writer.println("Source\tTarget");
            }
            writer.close();

        }
    }

    public void setArrayOfAccounts(ArrayList<Twitter> allAccountss) {

        this.allAccounts = allAccountss;

        //handler for the onCloseStage
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you really want to close this application?", ButtonType.YES, ButtonType.NO);
                ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

                if (ButtonType.NO.equals(result)) {
                    // no choice or no clicked -> don't close
                    event.consume();
                } else {
                    if (twitterThread != null && twitterThread.isAlive()) {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Crawling has been stoped...");
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText("Look, an Information Dialaog..");
                        alert.showAndWait();

                        try {
                            twitterThread.ContinueWorking = false;
                            twitterThread.saveCurrentState();
                            twitterThread.join();

                            getTableData(tableView);

                            //Delete the files that contains the number of accounts
                            System.out.println("controllers.GraphPageController.stopCrawling()");
                            System.out.println("Application Closed by click to Close Button(X)");
                        } catch (IOException | InterruptedException ex) {
                            Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (TweetsPageController.tweetsThread != null && TweetsPageController.tweetsThread.isAlive()) {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Crawling has been stoped...");
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText("Look, an Information Dialaog..");
                        alert.showAndWait();

                        try {
                            TweetsPageController.tweetsThread.ContinueWorking = false;

                            TweetsPageController.tweetsThread.join();
                            //    twitterThread.saveCurrentState();
                            //    getTableData(tableView);

                            //Delete the files that contains the number of accounts
                            System.out.println("controllers.GraphPageController.stopCrawling()");
                            System.out.println("Application Closed by click to Close Button(X)");
                        } catch (Exception e) {

                        }
                    }
                    File file = new File("nbrOfAccount.txt");
                    file.delete();

                    Platform.exit();
                }
            }
        });

        System.out.println("numberOfAccounts: " + numberAccounts.getText());

    }

    public void setArrayOfAccounts(String s) {

        numberAccounts.setText(s + "eee");

    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        System.out.println("-------------" + filePath);
    }

    @FXML
    private void addAccount() throws IOException {
        Stage stage;
        Parent root;
        stage = new Stage();
        root = FXMLLoader.load(getClass().getResource("/views/GraphAddAccountPopUp.fxml"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(addbtn.getScene().getWindow());
        stage.showAndWait();

        System.out.println("excuted after closing the pop up ");

        Scanner in;
        try { // Get the number of accounts from a file and put it in the label that s the easiest way
            File file = new File("nbrOfAccount.txt");
            in = new Scanner(file);
            numberAccounts.setText(in.nextLine());
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    public void openFileOfIds() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a file to open");

        chooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text File", "*.txt"));

        File file = chooser.showOpenDialog(null);
        fileofIDS.setText(file.getAbsolutePath());
        if (file != null) {
            userId.setDisable(true);
            Xbutton.setVisible(true);

        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Information Alert");
            String s = "You canceled!";
            alert.setContentText(s);
            alert.show();

        }
    }

    @FXML
    public void removePathFile() {
        fileofIDS.setText("");
        userId.setDisable(false);
        Xbutton.setVisible(false);
    }

    @FXML
    public void stopCrawling() throws IOException, InterruptedException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to stop CRAWLING", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Stop Crawling");
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

        if (ButtonType.NO.equals(result)) {
            // no choice or no clicked -> don't close

        } else {
            twitterThread.ContinueWorking = false;
            twitterThread.saveCurrentState();
            twitterThread.join();

            getTableData(tableView);
            System.out.println("controllers.GraphPageController.stopCrawling()");
            startCrawlingButton.setDisable(true);
            stopCrawlingButton.setDisable(true);
            resumeCrawlingButton.setDisable(false);
            clearHistoryButton.setDisable(false);
            openFileOfIdsButton.setDisable(true);
            chooseFileButton.setDisable(true);
            userId.setDisable(true);

        }
    }

    public void disableEnableButton() {
        startCrawlingButton.setDisable(true);
        stopCrawlingButton.setDisable(false);
        chooseFileButton.setDisable(true);
        openFileOfIdsButton.setDisable(true);
        userId.setDisable(true);
    }

    public void disableEnableButton1() {
        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);
        openFileOfIdsButton.setDisable(false);
        userId.setDisable(false);
    }

    @FXML
    public void resumeCrawling() throws FileNotFoundException, IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to resume CRAWLING", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Resume Crawling");
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

        if (ButtonType.NO.equals(result)) {
            // no choice or no clicked -> don't close

        } else {
            int depth;
            int nodesInCurrentLevel;
            int nodesInNextLevel;
            int nbNodeCrawled;
            String saveFile;
            int currentLevel;
            Queue<Long> q;
            try (
                    //Scanner in = new Scanner(new File("t.tmp"))) {
                    Scanner in = new Scanner(new File(System.getProperty("user.dir") + "/ImportantData/t.tmp"))) {
                //System.out.println("1");

                depth = Integer.parseInt(in.nextLine().trim());

                //System.out.println("11");
                nodesInCurrentLevel = Integer.parseInt(in.nextLine());
                nodesInNextLevel = Integer.parseInt(in.nextLine());
                nbNodeCrawled = Integer.parseInt(in.nextLine());
                saveFile = in.nextLine();
                currentLevel = Integer.parseInt(in.nextLine());
                int queueSize = Integer.parseInt(in.nextLine());
                q = new LinkedList();
                for (int i = 0; i < queueSize && in.hasNext(); i++) {
                    q.add(Long.parseLong(in.nextLine()));
                }
                in.close();
            }
            //    new File("t.tmp").delete();
            new File(System.getProperty("user.dir") + "/ImportantData/t.tmp").delete();

            tableView.getItems().clear();
            try {
                importTableData(tableView, data);
            } catch (IOException ex) {
                Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
            }

            twitterThread = new TwitterThread(allAccounts, q, depth, saveFile, data, nbrNodeCrawledLabel, nodesInCurrentLevel, nodesInNextLevel, nbNodeCrawled, currentLevel, this);
            twitterThread.start();
            stopCrawlingButton.setDisable(false);
            chooseFileButton.setDisable(true);
            openFileOfIdsButton.setDisable(true);
            resumeCrawlingButton.setDisable(true);
            clearHistoryButton.setDisable(true);
            userId.setDisable(true);

        }
    }

    @FXML
    public void clearHistory() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel current CRAWLING", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Cancel Crawling");
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

        if (ButtonType.NO.equals(result)) {
            // no choice or no clicked -> don't close

        } else {
            //clear the feedback table
            tableView.getItems().clear();
            nbrNodeCrawledLabel.setText("");
            //enable buttons for a new start
            HomeController.getInstance().enableButtons();

            //File f = new File("t.tmp");
            File f = new File(System.getProperty("user.dir") + "/ImportantData/t.tmp");

            f.delete();

            String s = System.getProperty("user.dir") + "/src/TableContent.txt";
            File file = new File(s);
            file.delete();
            saveResults.setText("");

            tooltip.setText("");
            startCrawlingButton.setDisable(false);
            stopCrawlingButton.setDisable(true);
            chooseFileButton.setDisable(false);
            openFileOfIdsButton.setDisable(false);
            resumeCrawlingButton.setDisable(true);
            clearHistoryButton.setDisable(true);
            userId.setDisable(false);
        }
    }

    public void restoreInitialeState() {

        HomeController.getInstance().enableButtons();
        saveResults.setText("");
        userId.setText("");
        fileofIDS.setText("");
        slider.setValue(1.0);
        tooltip.setText("");
        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);
        openFileOfIdsButton.setDisable(false);
        resumeCrawlingButton.setDisable(true);
        clearHistoryButton.setDisable(true);
        userId.setDisable(false);

    }

    @FXML
    public void extractFeedback() throws FileNotFoundException, IOException {
        if (tableView.getItems().isEmpty()) {
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setTitle("Information Dialog");
            alert1.setHeaderText("Look, an Information Dialog");
            alert1.setContentText("Oups, There's Nothing To Save...");
            alert1.showAndWait();
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Specify a file to save the Feedback!");

        chooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text File", "*.txt"));

        File file = chooser.showSaveDialog(null);
        if (file != null) {
            String filename = file.getAbsolutePath();
            
                try {
                    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
                    Content content = new Content();

                    for (int i = 0; i < tableView.getItems().size(); i++) {
                        content = (Content) tableView.getItems().get(i);
                        System.out.println(content.getInformations());
                        writer.println(content.getInformations() + "__" + content.getDate());
                    }
                    content = new Content("Stop Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    writer.println(content.getInformations() + "    " + content.getDate());
                    writer.close();

                } catch (IOException ex) {
                    Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            
        }
    }

}
