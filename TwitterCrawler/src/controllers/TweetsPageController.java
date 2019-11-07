/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import org.controlsfx.control.CheckComboBox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import javafx.scene.control.TextField;
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
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;


/**
 * FXML Controller class
 
 * The main idea of this page is to crawl the tweets for a specific twitter user Account or a file that contains multiple twitter user id. 
 * It crawl at most the latest 3200 tweets (Limited chose by Twitter).
 * 
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */

public class TweetsPageController implements Initializable {

    @FXML
    private Button tweethelp;
    @FXML
    private TableView<Content> tableView;
    @FXML
    private TableColumn<Content, String> Informations;
    @FXML
    private TableColumn<Content, String> Date;
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

    static TweetsThread tweetsThread;

    @FXML
    private AnchorPane anchor;

    @FXML
    private Pane pane;
    @FXML
    private Button openFileOfIdsButton;
    @FXML
    private Label fileofIDS;
    @FXML
    private TextField userId;
    @FXML
    private Button Xbutton;
    @FXML
    private Button chooseFile, addbtn;
    @FXML
    private Label saveResults, numberAccounts;
    @FXML
    private Tooltip tooltip;
    @FXML
    private Tooltip tooltip1;
    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;
    @FXML
    private CheckComboBox checkComboTweet;
    @FXML
    private CheckBox ProfileCheckAll, TweetsCheckAll;
    @FXML
    private Label labelOrChoose, labelOrChoose1;

//    TimeSpinner fromTime;
//    TimeSpinner toTime;
//    ArrayList<String> ProfileLang = new ArrayList<>();
    ArrayList<String> TweetsLang = new ArrayList<>();
    ArrayList<String> langs = new ArrayList<>();
    ArrayList<String> langsTweets = new ArrayList<>();

    String fromDateTime;
    String toDateTime;

    static ArrayList<Twitter> allAccounts = new ArrayList<>();
    private final ObservableList<Content> data
            = FXCollections.observableArrayList();
    ObservableList<String> strings = FXCollections.observableArrayList();
    ObservableList<String> stringsTweets = FXCollections.observableArrayList();

    public void initialize(URL url, ResourceBundle rb) {
        getObservableLanguage();

        //  checkCombo.getItems().addAll(strings);
        //   checkCombo.getCheckModel().check(0);
        checkComboTweet.getItems().addAll(stringsTweets);
        //    checkComboTweet.getCheckModel().check(0);

//        checkCombo.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
//            @Override
//            public void onChanged(ListChangeListener.Change cc) {
//                if (checkCombo.getCheckModel().getCheckedIndices().isEmpty()) {
//                    labelOrChoose.setVisible(true);
//                    ProfileCheckAll.setVisible(true);
//                } else {
//                    labelOrChoose.setVisible(false);
//                    ProfileCheckAll.setVisible(false);
//                }
//            }
//        });
        checkComboTweet.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change cc) {
                if (checkComboTweet.getCheckModel().getCheckedIndices().isEmpty()) {
                    labelOrChoose1.setVisible(true);
                    TweetsCheckAll.setVisible(true);
                } else {
                    labelOrChoose1.setVisible(false);
                    TweetsCheckAll.setVisible(false);
                }
            }
        });

        Informations.setCellValueFactory(new PropertyValueFactory<>("Informations"));
        Date.setCellValueFactory(new PropertyValueFactory<>("Date"));
        tableView.setItems(data);
        Xbutton.setVisible(false);
        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);
//        checkCombo.setDisable(false);
        checkComboTweet.setDisable(false);
        openFileOfIdsButton.setDisable(false);
        resumeCrawlingButton.setDisable(true);
        clearHistoryButton.setDisable(true);

        toDate.setValue(LocalDate.now());
        fromDate.setValue(LocalDate.of(2006, Month.MARCH, 21));//twitter was created on 21 march 2006

        allAccounts = HomeController.allAccounts;
        System.out.println(allAccounts.size());

        Scanner in;
        try { // Get the number of accounts from a file then delete it...
            File file = new File("nbrOfAccount.txt");
            in = new Scanner(file);
            numberAccounts.setText(in.nextLine());
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
        }

        File f = new File(System.getProperty("user.dir") + "/ImportantData/t1.tmp");
        if (f.exists()) {
            //disable buttons unlease cleared history
            HomeController.getInstance().disableButtonsForTweets();
            resumeCrawlingButton.setDisable(false);
            startCrawlingButton.setDisable(true);
            clearHistoryButton.setDisable(false);
            stopCrawlingButton.setDisable(true);
            chooseFileButton.setDisable(true);
            //      checkCombo.setDisable(true);
            checkComboTweet.setDisable(true);
            openFileOfIdsButton.setDisable(true);
            userId.setDisable(true);

        }
        System.out.println(fromDate.getValue().toString());

    }

    @FXML
    public void tweetsLangClick() {
        if (TweetsCheckAll.isSelected()) {
            labelOrChoose1.setVisible(false);
            checkComboTweet.setVisible(false);
        } else {
            labelOrChoose1.setVisible(true);
            checkComboTweet.setVisible(true);
        }
    }

    public void getTableData() {
        try {

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/src/TableContentForTweets.txt", true)));
            Content content = new Content();

            for (int i = 0; i < tableView.getItems().size(); i++) {
                content = (Content) tableView.getItems().get(i);
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
        FileInputStream f = new FileInputStream(System.getProperty("user.dir") + "/src/TableContentForTweets.txt");
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
        PrintWriter pw = new PrintWriter(System.getProperty("user.dir") + "/src/TableContentForTweets.txt");
        pw.close();
        String s = System.getProperty("user.dir") + "/src/TableContentForTweets.txt";
        File file = new File(s);
        file.delete();

    }

//    @FXML
//    private void openHelp() throws IOException {
//        System.out.println("*-*-*-*-*-*-*-*-*-");
//        Stage stage;
//        Parent root;
//        stage = new Stage();
//        stage.getIcons().add(new Image("/icons/icons8_Info_32px.png"));
//        System.out.println("*-*-*-*-*-*-*-*-*-");
//        // stage.setTitle("Help Page");
//        root = FXMLLoader.load(getClass().getResource("/views/GraphHelpPage.fxml"));
//        stage.setScene(new Scene(root));
//        stage.initModality(Modality.WINDOW_MODAL);
//        stage.initOwner(graphhelp.getScene().getWindow());
//        stage.show();
//    }
    @FXML
    private void StartCrawling() throws TwitterException, FileNotFoundException, IOException {
        if (userId.getText().isEmpty() && fileofIDS.getText().isEmpty()) {
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setTitle("Information Dialog");
            alert1.setHeaderText("Look, an Information Dialog");
            alert1.setContentText("You Should insert an Id/Link or Choose a file of Ids");
            alert1.showAndWait();
            return;
        }
        if (saveResults.getText().isEmpty()) {
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setTitle("Information Dialog");
            alert1.setHeaderText("Look, an Information Dialog");
            alert1.setContentText("You Should Choose a file to save the result");
            alert1.showAndWait();
            return;
        }
//        if (checkCombo.getCheckModel().getCheckedIndices().size() == 0 && !ProfileCheckAll.isSelected()) {
//            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
//            alert1.setTitle("Information Dialog");
//            alert1.setHeaderText("Look, an Information Dialog");
//            alert1.setContentText("You Should Choose The Profiles Language");
//            alert1.showAndWait();
//            return;
//        }

        if (checkComboTweet.getCheckModel().getCheckedIndices().size() == 0 && !TweetsCheckAll.isSelected()) {
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setTitle("Information Dialog");
            alert1.setHeaderText("Look, an Information Dialog");
            alert1.setContentText("You Should Choose The Tweets Language");
            alert1.showAndWait();
            return;
        }

//        ProfileLang.clear();
        TweetsLang.clear();

//        if (ProfileCheckAll.isSelected()) {
//            ProfileLang.add("all");
//        } else {
//            ObservableList<Integer> ob = checkCombo.getCheckModel().getCheckedIndices();
//            for (int i = 0; i < ob.size(); i++) {
//                ProfileLang.add(langs.get(ob.get(i)));
//            }
//        }
//        System.out.println(ProfileLang);
        if (TweetsCheckAll.isSelected()) {
            TweetsLang.add("all");
        } else {
            ObservableList<Integer> ob1 = checkComboTweet.getCheckModel().getCheckedIndices();
            for (int i = 0; i < ob1.size(); i++) {
                TweetsLang.add(langsTweets.get(ob1.get(i)));
            }
        }

        //Here we are sure that all the inputs are right!
        tableView.getItems().clear();

//        String[] fromtimearray = fromTime.getValue().toString().split(":");
//        if (fromtimearray.length == 2) {
//            fromDateTime = fromDate.getValue().toString() + " " + fromTime.getValue().toString() + ":00";
//        } else {
//            fromDateTime = fromDate.getValue().toString() + " " + fromTime.getValue().toString();
//        }
        fromDateTime = fromDate.getValue().toString() + " " + "00:00:00";

        //     String[] totimearray = toTime.getValue().toString().split(":");
//        if (totimearray.length == 2) {
//            toDateTime = toDate.getValue().toString() + " " + toTime.getValue().toString() + ":00";
//        } else {
//            toDateTime = toDate.getValue().toString() + " " + toTime.getValue().toString();
//        }
        toDateTime = toDate.getValue().toString() + " " + "23:59:59";

        if (!userId.getText().isEmpty()) {
            long id;
            if (isNumeric(userId.getText())) {
                id = Long.parseLong(userId.getText());

            } else {
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
                    id = user.getId();
                } catch (TwitterException t) {
                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                    alert1.setTitle("Information Dialog");
                    alert1.setHeaderText("Look, an Information Dialog");
                    alert1.setContentText("Not Valid Name");
                    alert1.showAndWait();
                    return;
                }
            }
            tweetsThread = new TweetsThread(allAccounts, id, saveResults.getText(), data, this, TweetsLang, fromDateTime, toDateTime);
            HomeController.getInstance().disableButtonsForTweets();
            disableEnableButton();
            tweetsThread.start();

        } else { // Read the file of IDs
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
                tweetsThread = new TweetsThread(allAccounts, arrayIds, saveResults.getText(), data, this, TweetsLang, fromDateTime, toDateTime);
                HomeController.getInstance().disableButtonsForTweets();
                disableEnableButton();
                tweetsThread.start();

            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText("File Should Contains IDS!");
                alert.showAndWait();
            }

        }
//tweetsThread=new TweetsThread(allAccounts, nodes, toDateTime, data, this, ProfileLang, TweetsLang, fromDateTime, toDateTime)
    }

    @FXML
    public void stopCrawling() throws IOException, InterruptedException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to stop CRAWLING", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Stop Crawling");
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

        if (ButtonType.NO.equals(result)) {
            // no choice or no clicked -> don't close

        } else {
            tweetsThread.ContinueWorking = false;
            tweetsThread.join();
            System.out.println("controllers.tweetsPage.stopCrawling()");
            startCrawlingButton.setDisable(true);
            stopCrawlingButton.setDisable(true);
            resumeCrawlingButton.setDisable(false);
            clearHistoryButton.setDisable(false);
            openFileOfIdsButton.setDisable(true);
            chooseFileButton.setDisable(true);
            //     checkCombo.setDisable(true);
            checkComboTweet.setDisable(true);
            userId.setDisable(true);

        }
    }

    @FXML
    private void openHelp() throws IOException {
        System.out.println("*-*-*-*-*-*-*-*-*-");
        Stage stage;
        Parent root;
        stage = new Stage();
        stage.getIcons().add(new Image("/icons/icons8_Info_32px.png"));
        System.out.println("*-*-*-*-*-*-*-*-*-");
        stage.setTitle("Help Page");
        root = FXMLLoader.load(getClass().getResource("/views/TweetHelpPage.fxml"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(tweethelp.getScene().getWindow());
        stage.show();
    }

    @FXML
    public void resumeCrawling() throws FileNotFoundException, IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to resume CRAWLING", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Resume Crawling");
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

        if (ButtonType.NO.equals(result)) {
            // no choice or no clicked -> don't close

        } else {

            String saveFile, fromDateTime, toDateTime;

            //  ArrayList<String> ProfileLang = new ArrayList<>();
            ArrayList<String> TweetsLang = new ArrayList<>();
            Queue<Long> q;
            try (
                    //Scanner in = new Scanner(new File("t.tmp"))) {
                    Scanner in = new Scanner(new File(System.getProperty("user.dir") + "/ImportantData/t1.tmp"))) {

                saveFile = in.nextLine();
//                int profileLangSize = Integer.parseInt(in.nextLine());
//                for (int i = 0; i < profileLangSize; i++) {
//                    ProfileLang.add(in.nextLine());
//                }
                int tweetsLangSize = Integer.parseInt(in.nextLine());
                for (int i = 0; i < tweetsLangSize; i++) {
                    TweetsLang.add(in.nextLine());
                }
                fromDateTime = in.nextLine();
                toDateTime = in.nextLine();
                int queueSize = Integer.parseInt(in.nextLine());
                q = new LinkedList();
                for (int i = 0; i < queueSize && in.hasNext(); i++) {
                    q.add(Long.parseLong(in.nextLine()));
                }
                in.close();
            }
            //    new File("t.tmp").delete();
            new File(System.getProperty("user.dir") + "/ImportantData/t1.tmp").delete();

            tableView.getItems().clear();
            try {
                importTableData(tableView, data);
            } catch (IOException ex) {
                Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
            tweetsThread = new TweetsThread(allAccounts, q, saveFile, data, this, TweetsLang, fromDateTime, toDateTime);
            tweetsThread.start();
            stopCrawlingButton.setDisable(false);
            chooseFileButton.setDisable(true);
            //     checkCombo.setDisable(true);
            checkComboTweet.setDisable(true);
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

            //enable buttons for a new start
            HomeController.getInstance().enableButtons();

            //File f = new File("t.tmp");
            File f = new File(System.getProperty("user.dir") + "/ImportantData/t1.tmp");

            f.delete();

            String s = System.getProperty("user.dir") + "/src/TableContentForTweets.txt";
            File file = new File(s);
            file.delete();
            saveResults.setText("");

            tooltip.setText("");
            tooltip1.setText("");

            startCrawlingButton.setDisable(false);
            stopCrawlingButton.setDisable(true);
            chooseFileButton.setDisable(false);
            //     checkCombo.setDisable(false);
            checkComboTweet.setDisable(false);
            openFileOfIdsButton.setDisable(false);
            resumeCrawlingButton.setDisable(true);
            clearHistoryButton.setDisable(true);
            userId.setDisable(false);
        }
    }

    @FXML
    private void chooseFile() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        Stage stage = (Stage) anchor.getScene().getWindow();
        if (!fileofIDS.getText().isEmpty()) {

            File f = new File(fileofIDS.getText());
            String folder = f.getParent();
            directoryChooser.setInitialDirectory(new java.io.File(folder));
        } else {
            directoryChooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")));
        }

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory == null) {
            //saveResults.setText("No Directory selected");
        } else {
            String filename = selectedDirectory.getAbsolutePath();
            saveResults.setText(filename);
            tooltip.setText(filename);
        }

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
            tooltip1.setText(file.getAbsolutePath());
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
    public void removeFilePath() {
        fileofIDS.setText("");
        userId.setDisable(false);
        Xbutton.setVisible(false);
    }

    public void disableEnableButton() {
        startCrawlingButton.setDisable(true);
        stopCrawlingButton.setDisable(false);
        chooseFileButton.setDisable(true);
        openFileOfIdsButton.setDisable(true);
        userId.setDisable(true);
        //    checkCombo.setDisable(true);
        checkComboTweet.setDisable(true);
    }

    public void disableEnableButton1() {
        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);
        openFileOfIdsButton.setDisable(false);
        userId.setDisable(false);
        //   checkCombo.setDisable(false);
        checkComboTweet.setDisable(false);
    }

    public void restoreInitialeState() {

        HomeController.getInstance().enableButtonsForTweets();
        saveResults.setText("");
        userId.setText("");
        fileofIDS.setText("");
        tooltip.setText("");
        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);
        //   checkCombo.setDisable(false);
        checkComboTweet.setDisable(false);
        openFileOfIdsButton.setDisable(false);
        resumeCrawlingButton.setDisable(true);
        clearHistoryButton.setDisable(true);
        userId.setDisable(false);
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

    public void getObservableLanguage() {
        strings = FXCollections.observableArrayList();
        //     strings.add("All");
        strings.add("English");
        strings.add("French");
        strings.add("Afar");
        strings.add("Abkhazian");
        strings.add("Avestan");
        strings.add("Afrikaans");
        strings.add("Akan");
        strings.add("Amharic");
        strings.add("Aragonese");
        strings.add("Arabic");
        strings.add("Assamese");
        strings.add("Avaric");
        strings.add("Aymara");
        strings.add("Azerbaijani");
        strings.add("Bashkir");
        strings.add("Belarusian");
        strings.add("Bulgarian");
        strings.add("Bihari");
        strings.add("Bislama");
        strings.add("Bambara");
        strings.add("Bengali");
        strings.add("Tibetan");
        strings.add("Breton");
        strings.add("Bosnian");
        strings.add("Catalan");
        strings.add("Chechen");
        strings.add("Chamorro");
        strings.add("Corsican");
        strings.add("Cree");
        strings.add("Czech");
        strings.add("Church");
        strings.add("Chuvash");
        strings.add("Welsh");
        strings.add("Danish");
        strings.add("German");
        strings.add("Divehi");
        strings.add("Dzongkha");
        strings.add("Ewe");
        strings.add("Greek");
        strings.add("Esperanto");
        strings.add("Spanish");
        strings.add("Estonian");
        strings.add("Basque");
        strings.add("Persian");
        strings.add("Fulah");
        strings.add("Finnish");
        strings.add("Fijian");
        strings.add("Faroese");
        strings.add("Western");
        strings.add("Irish");
        strings.add("Gaelic");
        strings.add("Galician");
        strings.add("Guarani");
        strings.add("Gujarati");
        strings.add("Manx");
        strings.add("Hausa");
        strings.add("Hebrew");
        strings.add("Hindi");
        strings.add("Hiri");
        strings.add("Croatian");
        strings.add("Haitian");
        strings.add("Hungarian");
        strings.add("Armenian");
        strings.add("Herero");
        strings.add("Interlingua");
        strings.add("Indonesian");
        strings.add("Interlingue");
        strings.add("Igbo");
        strings.add("Sichuan");
        strings.add("Inupiaq");
        strings.add("Ido");
        strings.add("Icelandic");
        strings.add("Italian");
        strings.add("Inuktitut");
        strings.add("Japanese");
        strings.add("Javanese");
        strings.add("Georgian");
        strings.add("Kongo");
        strings.add("Kikuyu");
        strings.add("Kuanyama");
        strings.add("Kazakh");
        strings.add("Kalaallisut");
        strings.add("Central");
        strings.add("Kannada");
        strings.add("Korean");
        strings.add("Kanuri");
        strings.add("Kashmiri");
        strings.add("Kurdish");
        strings.add("Komi");
        strings.add("Cornish");
        strings.add("Kirghiz;");
        strings.add("Latin");
        strings.add("Luxembourgish");
        strings.add("Ganda");
        strings.add("Limburgan");
        strings.add("Lingala");
        strings.add("Lao");
        strings.add("Lithuanian");
        strings.add("Luba-Katanga");
        strings.add("Latvian");
        strings.add("Malagasy");
        strings.add("Marshallese");
        strings.add("Maori");
        strings.add("Macedonian");
        strings.add("Malayalam");
        strings.add("Mongolian");
        strings.add("Marathi");
        strings.add("Malay");
        strings.add("Maltese");
        strings.add("Burmese");
        strings.add("Nauru");
        strings.add("Norwegian");

        strings.add("Nepali");
        strings.add("Ndonga");
        strings.add("Dutch");
        strings.add("Norwegian");
        strings.add("Norwegian");
        strings.add("Ndebele");
        strings.add("Navajo");
        strings.add("Chichewa");
        strings.add("Occitan");
        strings.add("Ojibwa");
        strings.add("Oromo");
        strings.add("Oriya");
        strings.add("Ossetian");
        strings.add("Panjabi");
        strings.add("Pali");
        strings.add("Polish");
        strings.add("Pushto");
        strings.add("Portuguese");
        strings.add("Quechua");
        strings.add("Romansh");
        strings.add("Rundi");
        strings.add("Romanian");
        strings.add("Russian");
        strings.add("Kinyarwanda");
        strings.add("Sanskrit");
        strings.add("Sardinian");
        strings.add("Sindhi");
        strings.add("Northern");
        strings.add("Sango");
        strings.add("Sinhala");
        strings.add("Slovak");
        strings.add("Slovenian");
        strings.add("Samoan");
        strings.add("Shona");
        strings.add("Somali");
        strings.add("Albanian");
        strings.add("Serbian");
        strings.add("Swati");
        strings.add("Sotho");
        strings.add("Sundanese");
        strings.add("Swedish");
        strings.add("Swahili");
        strings.add("Tamil");
        strings.add("Telugu");
        strings.add("Tajik");
        strings.add("Thai");
        strings.add("Tigrinya");
        strings.add("Turkmen");
        strings.add("Tagalog");
        strings.add("Tswana");
        strings.add("Tonga");
        strings.add("Turkish");
        strings.add("Tsonga");
        strings.add("Tatar");
        strings.add("Twi");
        strings.add("Tahitian");
        strings.add("Uighur");
        strings.add("Ukrainian");
        strings.add("Urdu");
        strings.add("Uzbek");
        strings.add("Venda");
        strings.add("Vietnamese");
        strings.add("VolapÃ¼k");
        strings.add("Walloon");
        strings.add("Wolof");
        strings.add("Xhosa");
        strings.add("Yiddish");
        strings.add("Yoruba");
        strings.add("Zhuang");
        strings.add("Chinese");
        strings.add("Zulu");

        //
        //      stringsTweets.add("All");
        stringsTweets.add("English");
        stringsTweets.add("French");
        stringsTweets.add("Afar");
        stringsTweets.add("Abkhazian");
        stringsTweets.add("Avestan");
        stringsTweets.add("Afrikaans");
        stringsTweets.add("Akan");
        stringsTweets.add("Amharic");
        stringsTweets.add("Aragonese");
        stringsTweets.add("Arabic");
        stringsTweets.add("Assamese");
        stringsTweets.add("Avaric");
        stringsTweets.add("Aymara");
        stringsTweets.add("Azerbaijani");
        stringsTweets.add("Bashkir");
        stringsTweets.add("Belarusian");
        stringsTweets.add("Bulgarian");
        stringsTweets.add("Bihari");
        stringsTweets.add("Bislama");
        stringsTweets.add("Bambara");
        stringsTweets.add("Bengali");
        stringsTweets.add("Tibetan");
        stringsTweets.add("Breton");
        stringsTweets.add("Bosnian");
        stringsTweets.add("Catalan");
        stringsTweets.add("Chechen");
        stringsTweets.add("Chamorro");
        stringsTweets.add("Corsican");
        stringsTweets.add("Cree");
        stringsTweets.add("Czech");
        stringsTweets.add("Church");
        stringsTweets.add("Chuvash");
        stringsTweets.add("Welsh");
        stringsTweets.add("Danish");
        stringsTweets.add("German");
        stringsTweets.add("Divehi");
        stringsTweets.add("Dzongkha");
        stringsTweets.add("Ewe");
        stringsTweets.add("Greek");
        stringsTweets.add("Esperanto");
        stringsTweets.add("Spanish");
        stringsTweets.add("Estonian");
        stringsTweets.add("Basque");
        stringsTweets.add("Persian");
        stringsTweets.add("Fulah");
        stringsTweets.add("Finnish");
        stringsTweets.add("Fijian");
        stringsTweets.add("Faroese");
        stringsTweets.add("Western");
        stringsTweets.add("Irish");
        stringsTweets.add("Gaelic");
        stringsTweets.add("Galician");
        stringsTweets.add("Guarani");
        stringsTweets.add("Gujarati");
        stringsTweets.add("Manx");
        stringsTweets.add("Hausa");
        stringsTweets.add("Hebrew");
        stringsTweets.add("Hindi");
        stringsTweets.add("Hiri");
        stringsTweets.add("Croatian");
        stringsTweets.add("Haitian");
        stringsTweets.add("Hungarian");
        stringsTweets.add("Armenian");
        stringsTweets.add("Herero");
        stringsTweets.add("Interlingua");
        stringsTweets.add("Indonesian");
        stringsTweets.add("Interlingue");
        stringsTweets.add("Igbo");
        stringsTweets.add("Sichuan");
        stringsTweets.add("Inupiaq");
        stringsTweets.add("Ido");
        stringsTweets.add("Icelandic");
        stringsTweets.add("Italian");
        stringsTweets.add("Inuktitut");
        stringsTweets.add("Japanese");
        stringsTweets.add("Javanese");
        stringsTweets.add("Georgian");
        stringsTweets.add("Kongo");
        stringsTweets.add("Kikuyu");
        stringsTweets.add("Kuanyama");
        stringsTweets.add("Kazakh");
        stringsTweets.add("Kalaallisut");
        stringsTweets.add("Central");
        stringsTweets.add("Kannada");
        stringsTweets.add("Korean");
        stringsTweets.add("Kanuri");
        stringsTweets.add("Kashmiri");
        stringsTweets.add("Kurdish");
        stringsTweets.add("Komi");
        stringsTweets.add("Cornish");
        stringsTweets.add("Kirghiz;");
        stringsTweets.add("Latin");
        stringsTweets.add("Luxembourgish");
        stringsTweets.add("Ganda");
        stringsTweets.add("Limburgan");
        stringsTweets.add("Lingala");
        stringsTweets.add("Lao");
        stringsTweets.add("Lithuanian");
        stringsTweets.add("Luba-Katanga");
        stringsTweets.add("Latvian");
        stringsTweets.add("Malagasy");
        stringsTweets.add("Marshallese");
        stringsTweets.add("Maori");
        stringsTweets.add("Macedonian");
        stringsTweets.add("Malayalam");
        stringsTweets.add("Mongolian");
        stringsTweets.add("Marathi");
        stringsTweets.add("Malay");
        stringsTweets.add("Maltese");
        stringsTweets.add("Burmese");
        stringsTweets.add("Nauru");
        stringsTweets.add("Norwegian");
        stringsTweets.add("Nepali");
        stringsTweets.add("Ndonga");
        stringsTweets.add("Dutch");
        stringsTweets.add("Norwegian");
        stringsTweets.add("Norwegian");
        stringsTweets.add("Ndebele");
        stringsTweets.add("Navajo");
        stringsTweets.add("Chichewa");
        stringsTweets.add("Occitan");
        stringsTweets.add("Ojibwa");
        stringsTweets.add("Oromo");
        stringsTweets.add("Oriya");
        stringsTweets.add("Ossetian");
        stringsTweets.add("Panjabi");
        stringsTweets.add("Pali");
        stringsTweets.add("Polish");
        stringsTweets.add("Pushto");
        stringsTweets.add("Portuguese");
        stringsTweets.add("Quechua");
        stringsTweets.add("Romansh");
        stringsTweets.add("Rundi");
        stringsTweets.add("Romanian");
        stringsTweets.add("Russian");
        stringsTweets.add("Kinyarwanda");
        stringsTweets.add("Sanskrit");
        stringsTweets.add("Sardinian");
        stringsTweets.add("Sindhi");
        stringsTweets.add("Northern");
        stringsTweets.add("Sango");
        stringsTweets.add("Sinhala");
        stringsTweets.add("Slovak");
        stringsTweets.add("Slovenian");
        stringsTweets.add("Samoan");
        stringsTweets.add("Shona");
        stringsTweets.add("Somali");
        stringsTweets.add("Albanian");
        stringsTweets.add("Serbian");
        stringsTweets.add("Swati");
        stringsTweets.add("Sotho");
        stringsTweets.add("Sundanese");
        stringsTweets.add("Swedish");
        stringsTweets.add("Swahili");
        stringsTweets.add("Tamil");
        stringsTweets.add("Telugu");
        stringsTweets.add("Tajik");
        stringsTweets.add("Thai");
        stringsTweets.add("Tigrinya");
        stringsTweets.add("Turkmen");
        stringsTweets.add("Tagalog");
        stringsTweets.add("Tswana");
        stringsTweets.add("Tonga");
        stringsTweets.add("Turkish");
        stringsTweets.add("Tsonga");
        stringsTweets.add("Tatar");
        stringsTweets.add("Twi");
        stringsTweets.add("Tahitian");
        stringsTweets.add("Uighur");
        stringsTweets.add("Ukrainian");
        stringsTweets.add("Urdu");
        stringsTweets.add("Uzbek");
        stringsTweets.add("Venda");
        stringsTweets.add("Vietnamese");
        stringsTweets.add("VolapÃ¼k");
        stringsTweets.add("Walloon");
        stringsTweets.add("Wolof");
        stringsTweets.add("Xhosa");
        stringsTweets.add("Yiddish");
        stringsTweets.add("Yoruba");
        stringsTweets.add("Zhuang");
        stringsTweets.add("Chinese");
        stringsTweets.add("Zulu");

        //       langs.add("all");
        langs.add("en");
        langs.add("fr");
        langs.add("aa");
        langs.add("ab");
        langs.add("ae");
        langs.add("af");
        langs.add("ak");
        langs.add("am");
        langs.add("an");
        langs.add("ar");
        langs.add("as");
        langs.add("av");
        langs.add("ay");
        langs.add("az");
        langs.add("ba");
        langs.add("be");
        langs.add("bg");
        langs.add("bh");
        langs.add("bi");
        langs.add("bm");
        langs.add("bn");
        langs.add("bo");
        langs.add("br");
        langs.add("bs");
        langs.add("ca");
        langs.add("ce");
        langs.add("ch");
        langs.add("co");
        langs.add("cr");
        langs.add("cs");
        langs.add("cu");
        langs.add("cv");
        langs.add("cy");
        langs.add("da");
        langs.add("de");
        langs.add("dv");
        langs.add("dz");
        langs.add("ee");
        langs.add("el");
        langs.add("eo");
        langs.add("es");
        langs.add("et");
        langs.add("eu");
        langs.add("fa");
        langs.add("ff");
        langs.add("fi");
        langs.add("fj");
        langs.add("fo");
        langs.add("fy");
        langs.add("ga");
        langs.add("gd");
        langs.add("gl");
        langs.add("gn");
        langs.add("gu");
        langs.add("gv");
        langs.add("ha");
        langs.add("he");
        langs.add("hi");
        langs.add("ho");
        langs.add("hr");
        langs.add("ht");
        langs.add("hu");
        langs.add("hy");
        langs.add("hz");
        langs.add("ia");
        langs.add("id");
        langs.add("ie");
        langs.add("ig");
        langs.add("ii");
        langs.add("ik");
        langs.add("io");
        langs.add("is");
        langs.add("it");
        langs.add("iu");
        langs.add("ja");
        langs.add("jv");
        langs.add("ka");
        langs.add("kg");
        langs.add("ki");
        langs.add("kj");
        langs.add("kk");
        langs.add("kl");
        langs.add("km");
        langs.add("kn");
        langs.add("ko");
        langs.add("kr");
        langs.add("ks");
        langs.add("ku");
        langs.add("kv");
        langs.add("kw");
        langs.add("ky");
        langs.add("la");
        langs.add("lb");
        langs.add("lg");
        langs.add("li");
        langs.add("ln");
        langs.add("lo");
        langs.add("lt");
        langs.add("lu");
        langs.add("lv");
        langs.add("mg");
        langs.add("mh");
        langs.add("mi");
        langs.add("mk");
        langs.add("ml");
        langs.add("mn");
        langs.add("mr");
        langs.add("ms");
        langs.add("mt");
        langs.add("my");
        langs.add("na");
        langs.add("nb");
        langs.add("ne");
        langs.add("ng");
        langs.add("nl");
        langs.add("nn");
        langs.add("no");
        langs.add("nr");
        langs.add("nv");
        langs.add("ny");
        langs.add("oc");
        langs.add("oj");
        langs.add("om");
        langs.add("or");
        langs.add("os");
        langs.add("pa");
        langs.add("pi");
        langs.add("pl");
        langs.add("ps");
        langs.add("pt");
        langs.add("qu");
        langs.add("rm");
        langs.add("rn");
        langs.add("ro");
        langs.add("ru");
        langs.add("rw");
        langs.add("sa");
        langs.add("sc");
        langs.add("sd");
        langs.add("se");
        langs.add("sg");
        langs.add("si");
        langs.add("sk");
        langs.add("sl");
        langs.add("sm");
        langs.add("sn");
        langs.add("so");
        langs.add("sq");
        langs.add("sr");
        langs.add("ss");
        langs.add("st");
        langs.add("su");
        langs.add("sv");
        langs.add("sw");
        langs.add("ta");
        langs.add("te");
        langs.add("tg");
        langs.add("th");
        langs.add("ti");
        langs.add("tk");
        langs.add("tl");
        langs.add("tn");
        langs.add("to");
        langs.add("tr");
        langs.add("ts");
        langs.add("tt");
        langs.add("tw");
        langs.add("ty");
        langs.add("ug");
        langs.add("uk");
        langs.add("ur");
        langs.add("uz");
        langs.add("ve");
        langs.add("vi");
        langs.add("vo");
        langs.add("wa");
        langs.add("wo");
        langs.add("xh");
        langs.add("yi");
        langs.add("yo");
        langs.add("za");
        langs.add("zh");
        langs.add("zu");

        //
//        langsTweets.add("all");
        langsTweets.add("en");
        langsTweets.add("fr");
        langsTweets.add("aa");
        langsTweets.add("ab");
        langsTweets.add("ae");
        langsTweets.add("af");
        langsTweets.add("ak");
        langsTweets.add("am");
        langsTweets.add("an");
        langsTweets.add("ar");
        langsTweets.add("as");
        langsTweets.add("av");
        langsTweets.add("ay");
        langsTweets.add("az");
        langsTweets.add("ba");
        langsTweets.add("be");
        langsTweets.add("bg");
        langsTweets.add("bh");
        langsTweets.add("bi");
        langsTweets.add("bm");
        langsTweets.add("bn");
        langsTweets.add("bo");
        langsTweets.add("br");
        langsTweets.add("bs");
        langsTweets.add("ca");
        langsTweets.add("ce");
        langsTweets.add("ch");
        langsTweets.add("co");
        langsTweets.add("cr");
        langsTweets.add("cs");
        langsTweets.add("cu");
        langsTweets.add("cv");
        langsTweets.add("cy");
        langsTweets.add("da");
        langsTweets.add("de");
        langsTweets.add("dv");
        langsTweets.add("dz");
        langsTweets.add("ee");
        langsTweets.add("el");
        langsTweets.add("eo");
        langsTweets.add("es");
        langsTweets.add("et");
        langsTweets.add("eu");
        langsTweets.add("fa");
        langsTweets.add("ff");
        langsTweets.add("fi");
        langsTweets.add("fj");
        langsTweets.add("fo");
        langsTweets.add("fy");
        langsTweets.add("ga");
        langsTweets.add("gd");
        langsTweets.add("gl");
        langsTweets.add("gn");
        langsTweets.add("gu");
        langsTweets.add("gv");
        langsTweets.add("ha");
        langsTweets.add("he");
        langsTweets.add("hi");
        langsTweets.add("ho");
        langsTweets.add("hr");
        langsTweets.add("ht");
        langsTweets.add("hu");
        langsTweets.add("hy");
        langsTweets.add("hz");
        langsTweets.add("ia");
        langsTweets.add("id");
        langsTweets.add("ie");
        langsTweets.add("ig");
        langsTweets.add("ii");
        langsTweets.add("ik");
        langsTweets.add("io");
        langsTweets.add("is");
        langsTweets.add("it");
        langsTweets.add("iu");
        langsTweets.add("ja");
        langsTweets.add("jv");
        langsTweets.add("ka");
        langsTweets.add("kg");
        langsTweets.add("ki");
        langsTweets.add("kj");
        langsTweets.add("kk");
        langsTweets.add("kl");
        langsTweets.add("km");
        langsTweets.add("kn");
        langsTweets.add("ko");
        langsTweets.add("kr");
        langsTweets.add("ks");
        langsTweets.add("ku");
        langsTweets.add("kv");
        langsTweets.add("kw");
        langsTweets.add("ky");
        langsTweets.add("la");
        langsTweets.add("lb");
        langsTweets.add("lg");
        langsTweets.add("li");
        langsTweets.add("ln");
        langsTweets.add("lo");
        langsTweets.add("lt");
        langsTweets.add("lu");
        langsTweets.add("lv");
        langsTweets.add("mg");
        langsTweets.add("mh");
        langsTweets.add("mi");
        langsTweets.add("mk");
        langsTweets.add("ml");
        langsTweets.add("mn");
        langsTweets.add("mr");
        langsTweets.add("ms");
        langsTweets.add("mt");
        langsTweets.add("my");
        langsTweets.add("na");
        langsTweets.add("nb");

        langsTweets.add("ne");
        langsTweets.add("ng");
        langsTweets.add("nl");
        langsTweets.add("nn");
        langsTweets.add("no");
        langsTweets.add("nr");
        langsTweets.add("nv");
        langsTweets.add("ny");
        langsTweets.add("oc");
        langsTweets.add("oj");
        langsTweets.add("om");
        langsTweets.add("or");
        langsTweets.add("os");
        langsTweets.add("pa");
        langsTweets.add("pi");
        langsTweets.add("pl");
        langsTweets.add("ps");
        langsTweets.add("pt");
        langsTweets.add("qu");
        langsTweets.add("rm");
        langsTweets.add("rn");
        langsTweets.add("ro");
        langsTweets.add("ru");
        langsTweets.add("rw");
        langsTweets.add("sa");
        langsTweets.add("sc");
        langsTweets.add("sd");
        langsTweets.add("se");
        langsTweets.add("sg");
        langsTweets.add("si");
        langsTweets.add("sk");
        langsTweets.add("sl");
        langsTweets.add("sm");
        langsTweets.add("sn");
        langsTweets.add("so");
        langsTweets.add("sq");
        langsTweets.add("sr");
        langsTweets.add("ss");
        langsTweets.add("st");
        langsTweets.add("su");
        langsTweets.add("sv");
        langsTweets.add("sw");
        langsTweets.add("ta");
        langsTweets.add("te");
        langsTweets.add("tg");
        langsTweets.add("th");
        langsTweets.add("ti");
        langsTweets.add("tk");
        langsTweets.add("tl");
        langsTweets.add("tn");
        langsTweets.add("to");
        langsTweets.add("tr");
        langsTweets.add("ts");
        langsTweets.add("tt");
        langsTweets.add("tw");
        langsTweets.add("ty");
        langsTweets.add("ug");
        langsTweets.add("uk");
        langsTweets.add("ur");
        langsTweets.add("uz");
        langsTweets.add("ve");
        langsTweets.add("vi");
        langsTweets.add("vo");
        langsTweets.add("wa");
        langsTweets.add("wo");
        langsTweets.add("xh");
        langsTweets.add("yi");
        langsTweets.add("yo");
        langsTweets.add("za");
        langsTweets.add("zh");
        langsTweets.add("zu");

    }
}
