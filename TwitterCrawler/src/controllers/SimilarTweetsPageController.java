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
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * FXML Controller class
 * Crawling Similar tweets
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class SimilarTweetsPageController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    public Label nbrtweetCrawledLabel;
    @FXML
    private Label fileofTweetLinks;
    @FXML
    private TextField tweetLink;

    @FXML
    private Button Xbutton;
    @FXML
    Tooltip tooltip1;

    @FXML
    private RadioButton txtFormat;
    @FXML
    private RadioButton jsnFormat;
    @FXML
    private Button similartweethelp;
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
    @FXML
    private Button openFileOfTweetLinks;

    static SimilaritysThread similaritysThread;
    @FXML
    private AnchorPane anchor;

    @FXML
    private Pane pane;

    @FXML
    private Button chooseFile, addbtn;
    @FXML
    private Label saveResults, numberAccounts;
    @FXML
    private Tooltip tooltip;

    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;

    String fromDateTime;
    String toDateTime;

    static ArrayList<Twitter> allAccounts = new ArrayList<>();
    private final ObservableList<Content> data
            = FXCollections.observableArrayList();
    ObservableList<String> strings = FXCollections.observableArrayList();
    ObservableList<String> stringsTweets = FXCollections.observableArrayList();

    public void initialize(URL url, ResourceBundle rb) {
        fileofTweetLinks.setVisible(false);
        openFileOfTweetLinks.setVisible(false);
        Xbutton.setVisible(false);
        ToggleGroup toggleGroup = new ToggleGroup();
        txtFormat.setToggleGroup(toggleGroup);
        jsnFormat.setToggleGroup(toggleGroup);
// listen to changes in selected toggle
//        toggleGroup.selectedToggleProperty().addListener((observable, oldVal, newVal) -> System.out.println(newVal + " was selected"));

        Informations.setCellValueFactory(new PropertyValueFactory<>("Informations"));
        Date.setCellValueFactory(new PropertyValueFactory<>("Date"));
        tableView.setItems(data);
        Xbutton.setVisible(false);
        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);

        openFileOfTweetLinks.setDisable(false);

        resumeCrawlingButton.setDisable(true);

        resumeCrawlingButton.setVisible(false);
        clearHistoryButton.setDisable(true);
        clearHistoryButton.setVisible(false);
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
            openFileOfTweetLinks.setDisable(true);

            tweetLink.setDisable(true);

        }
        System.out.println(fromDate.getValue().toString());

    }

    @FXML
    private void chooseFile() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        Stage stage = (Stage) anchor.getScene().getWindow();
        if (!fileofTweetLinks.getText().isEmpty()) {

            File f = new File(fileofTweetLinks.getText());
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
    private void StartCrawling() throws TwitterException, FileNotFoundException, IOException {
        if (tweetLink.getText().isEmpty() && fileofTweetLinks.getText().isEmpty()) {
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setTitle("Information Dialog");
            alert1.setHeaderText("Look, an Information Dialog");
            alert1.setContentText("You Should insert an Id/Link or Choose a file of Ids/Links for tweets");
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

        if (!txtFormat.isSelected() && !jsnFormat.isSelected()) {
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setTitle("Information Dialog");
            alert1.setHeaderText("Look, an Information Dialog");
            alert1.setContentText("You Should select the format of saving the results");
            alert1.showAndWait();
            return;
        }

        //Here we are sure that all the inputs are right!
        tableView.getItems().clear();

        fromDateTime = fromDate.getValue().toString() + " " + "00:00:00";

        toDateTime = toDate.getValue().toString() + " " + "23:59:59";
        String format = "";
        if (txtFormat.isSelected()) {
            format = "txtFormat";
        } else if (jsnFormat.isSelected()) {
            format = "jsnFormat";
        }
        if (!tweetLink.getText().isEmpty()) {
            long tweetid;
            // String query;
            Status query_status;
            try {
                //imported an id not a url
                if (isNumeric(tweetLink.getText())) {
                    tweetid = Long.parseLong(tweetLink.getText());
                    query_status = allAccounts.get(0).showStatus(tweetid);

                } else {
                    //url of a tweet 
                    //      an example      https://twitter.com/Cristiano/status/1114510044714946560
                    String[] url = tweetLink.getText().split("/");

                    tweetid = Long.parseLong(url[5]);
                    query_status = allAccounts.get(0).showStatus(Long.parseLong(url[5]));

                }

            } catch (Exception t) {

                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Information Dialog");
                alert1.setHeaderText("Look, an Information Dialog");
                alert1.setContentText("Not Valid Tweet id or link");
                alert1.showAndWait();
                return;
            }
            //query = query_status.getText();
            // System.out.println("*tweet text of url * " + query);

            similaritysThread = new SimilaritysThread(allAccounts, tweetid, format, saveResults.getText(), data, this, fromDateTime, toDateTime);
            HomeController.getInstance().disableButtonsForTweets();
            disableEnableButton();
            similaritysThread.start();

        } else { // Read the file of IDs

            System.out.println("reading file of ids");
            File f = new File(fileofTweetLinks.getText());
            Scanner in = new Scanner(f);
            String temp;
            ArrayList<Long> arraytweetIds = new ArrayList<>();
            while (in.hasNext()) {
                temp = in.nextLine();
                try {
                    System.out.println(Long.parseLong(temp));
                    arraytweetIds.add(Long.parseLong(temp));
                } catch (NumberFormatException n) {

                }
            }
            in.close();
            if (arraytweetIds.size() > 0) {
                similaritysThread = new SimilaritysThread(allAccounts, arraytweetIds, format, saveResults.getText(), data, this, fromDateTime, toDateTime);
                HomeController.getInstance().disableButtonsForTweets();
                disableEnableButton();
                similaritysThread.start();

            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText("File Should Contain Tweet IDS or Links!");
                alert.showAndWait();
            }

        }
    }

    @FXML
    public void resumeCrawling() throws FileNotFoundException, IOException {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to resume CRAWLING", ButtonType.YES, ButtonType.NO);
//        alert.setTitle("Resume Crawling");
//        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
//
//        if (ButtonType.NO.equals(result)) {
//            // no choice or no clicked -> don't close
//
//        } else {
//
//            String saveFile, fromDateTime, toDateTime, format;
//            String deg;
//
//            Queue<Long> q;
//            try (
//                    //Scanner in = new Scanner(new File("t.tmp"))) {
//                    Scanner in = new Scanner(new File(System.getProperty("user.dir") + "/ImportantData/t1.tmp"))) {
//
//                format = in.nextLine();
//                saveFile = in.nextLine();
//                deg = in.nextLine();
//                fromDateTime = in.nextLine();
//                toDateTime = in.nextLine();
//                int queueSize = Integer.parseInt(in.nextLine());
//                q = new LinkedList();
//                for (int i = 0; i < queueSize && in.hasNext(); i++) {
//                    q.add(Long.parseLong(in.nextLine()));
//                }
//                in.close();
//            }
//            //    new File("t.tmp").delete();
//            new File(System.getProperty("user.dir") + "/ImportantData/t1.tmp").delete();
//
//            tableView.getItems().clear();
//            try {
//                importTableData(tableView, data);
//            } catch (IOException ex) {
//                Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            similaritysThread = new SimilaritysThread(allAccounts, q, format, saveResults.getText(), data, this, Double.parseDouble(deg), fromDateTime, toDateTime);
//            similaritysThread.start();
//            stopCrawlingButton.setDisable(false);
//            chooseFileButton.setDisable(true);
//            txtFormat.setDisable(true);
//            jsnFormat.setDisable(true);
//
//            degree.setDisable(true);
//            openFileOfTweetLinks.setDisable(true);
//            resumeCrawlingButton.setDisable(true);
//            clearHistoryButton.setDisable(true);
//            tweetLink.setDisable(true);
//
//        }
    }

    @FXML
    public void clearHistory() {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel current CRAWLING", ButtonType.YES, ButtonType.NO);
//        alert.setTitle("Cancel Crawling");
//        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
//        
//        if (ButtonType.NO.equals(result)) {
//            // no choice or no clicked -> don't close
//
//        } else {
//            //clear the feedback table
//            tableView.getItems().clear();
//
//            //enable buttons for a new start
//            HomeController.getInstance().enableButtons();
//
//            //File f = new File("t.tmp");
//            File f = new File(System.getProperty("user.dir") + "/ImportantData/t1.tmp");
//            
//            f.delete();
//            
//            String s = System.getProperty("user.dir") + "/src/TableContentForTweets.txt";
//            File file = new File(s);
//            file.delete();
//            saveResults.setText("");
//            
//            tooltip.setText("");
//            tooltip1.setText("");
//            
//            startCrawlingButton.setDisable(false);
//            stopCrawlingButton.setDisable(true);
//            chooseFileButton.setDisable(false);
//            txtFormat.setDisable(false);
//            jsnFormat.setDisable(false);
//            
//            openFileOfTweetLinks.setDisable(false);
//            resumeCrawlingButton.setDisable(true);
//            clearHistoryButton.setDisable(true);
//            tweetLink.setDisable(false);
//        }
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

    @FXML
    public void stopCrawling() throws IOException, InterruptedException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to stop CRAWLING", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Stop Crawling");
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

        if (ButtonType.NO.equals(result)) {
            // no choice or no clicked -> don't close

        } else {
            int z = similaritysThread.nbrSimilarTweetsForEachTweet;

            similaritysThread.ContinueWorking = false;
//            similaritysThread.stop();
            similaritysThread.stopbool = true;
            HomeController.getInstance().enableButtons();
            saveResults.setText("");

            tooltip.setText("");
            tooltip1.setText("");
//            
            startCrawlingButton.setDisable(false);
            stopCrawlingButton.setDisable(true);
            chooseFileButton.setDisable(false);
            txtFormat.setDisable(false);
            jsnFormat.setDisable(false);

            openFileOfTweetLinks.setDisable(false);
            //  resumeCrawlingButton.setDisable(true);
            // clearHistoryButton.setDisable(true);
            tweetLink.setDisable(false);
//            similaritysThread.join();
//            System.out.println("controllers.tweetsPage.stopCrawling()");
//            startCrawlingButton.setDisable(true);
//            stopCrawlingButton.setDisable(true);
//            resumeCrawlingButton.setDisable(false);
//            clearHistoryButton.setDisable(false);
//            openFileOfTweetLinks.setDisable(true);
//            chooseFileButton.setDisable(true);
//            degree.setDisable(true);
//            txtFormat.setDisable(true);
//            jsnFormat.setDisable(true);
//
//            tweetLink.setDisable(true);
        }
    }

    public void getTableData() {
//        try {
//            
//            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/src/TableContentForTweets.txt", true)));
//            Content content = new Content();
//            
//            for (int i = 0; i < tableView.getItems().size(); i++) {
//                content = (Content) tableView.getItems().get(i);
//                System.out.println(content.getInformations());
//                writer.println(content.getInformations() + "__" + content.getDate());
//            }
//            content = new Content("Stop Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//            writer.println(content.getInformations() + "__" + content.getDate());
//            writer.close();
//            
//        } catch (IOException ex) {
//            Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
//        }

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

    public void restoreInitialeState() {
//        
        HomeController.getInstance().enableButtonsForTweets();
        saveResults.setText("");
        tweetLink.setText("");
        //fileofTweetLinks.setText("");
        tooltip.setText("");
        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);
        //  degree.setDisable(false);
        nbrtweetCrawledLabel.setText(0 + "");
        txtFormat.setDisable(false);
        // openFileOfTweetLinks.setDisable(false);
        resumeCrawlingButton.setDisable(true);
        clearHistoryButton.setDisable(true);
        jsnFormat.setDisable(false);
        tweetLink.setDisable(false);
    }

    @FXML
    public void removeFilePath() {
        fileofTweetLinks.setText("");
        tweetLink.setDisable(false);
        Xbutton.setVisible(false);
    }

    public void disableEnableButton() {
        startCrawlingButton.setDisable(true);
        stopCrawlingButton.setDisable(false);
        chooseFileButton.setDisable(true);
        openFileOfTweetLinks.setDisable(true);
        tweetLink.setDisable(true);
        txtFormat.setDisable(true);
        jsnFormat.setDisable(true);

    }

    public void disableEnableButton1() {
        startCrawlingButton.setDisable(false);
        stopCrawlingButton.setDisable(true);
        chooseFileButton.setDisable(false);
        openFileOfTweetLinks.setDisable(false);
        tweetLink.setDisable(false);
        txtFormat.setDisable(false);
        jsnFormat.setDisable(false);

    }

    @FXML
    private void openFileOfTweetLinks() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a file to open");

        chooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text File", "*.txt"));

        File file = chooser.showOpenDialog(null);
        fileofTweetLinks.setText(file.getAbsolutePath());
        if (file != null) {
            tweetLink.setDisable(true);
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
        try {
            // Get the number of accounts from a file and put it in the label that s the easiest way

            File file = new File("nbrOfAccount.txt");

            in = new Scanner(file);

            numberAccounts.setText(in.nextLine());

            in.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphPageController.class.getName()).log(Level.SEVERE, null, ex);
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
        root = FXMLLoader.load(getClass().getResource("/views/SimilarTweetHelpPage.fxml"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(similartweethelp.getScene().getWindow());
        stage.show();
    }
}
