/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import helpers.DynamicViews;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import twitter4j.Twitter;
/**
 * FXML Controller class
 * HomeController
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class HomeController implements Initializable {

    private static HomeController instance;
    @FXML
    private BorderPane MainBorderPane;
    @FXML
    private Button graphButton;
    @FXML
    private Button tweetsButton;
    @FXML
    private Button similartweetsButton;
  
    @FXML
    private Button viewTweets;

    @FXML
    private Pane topPane;
    @FXML
    private Pane sidePane;

    static ArrayList<Twitter> allAccounts = new ArrayList<>();
    static String filePath = "";
    int t = -1;

    public HomeController() {
        instance = this;
    }

    public static HomeController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // TODO
         //   System.out.println("ew");
            DynamicViews.loadBordaerCenter(MainBorderPane, "GraphPage.fxml");
           
         //   System.out.println("ew");

        } catch (IOException ex) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disableButtons() {
        tweetsButton.setDisable(true);
        similartweetsButton.setDisable(true);
        graphButton.setDisable(true);
   
        viewTweets.setDisable(true);

    }

    public void disableButtonsForTweets() {
        tweetsButton.setDisable(true);
        graphButton.setDisable(true);
        similartweetsButton.setDisable(true);
       
        viewTweets.setDisable(true);
    }

    public void enableButtons() {
        tweetsButton.setDisable(false);
        similartweetsButton.setDisable(false);
       
        viewTweets.setDisable(false);
        graphButton.setDisable(false);

    }

    public void enableButtonsForTweets() {
        graphButton.setDisable(false);
        similartweetsButton.setDisable(false);
      
        viewTweets.setDisable(false);
        tweetsButton.setDisable(false);
    }

    public void setArrayOfAccounts(ArrayList<Twitter> allAcounts) {

        this.allAccounts = allAcounts;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        System.out.println("*************" + filePath);
    }

    @FXML
    private void show_graph(MouseEvent event) throws IOException {
        graphButton.setStyle("-fx-background-color: #adc1d6;");
        viewTweets.setStyle(" -fx-background-color:  #0f1e2d;");
        similartweetsButton.setStyle("-fx-background-color: #0f1e2d;");
        tweetsButton.setStyle("-fx-background-color: #0f1e2d;");
       

        DynamicViews.loadBordaerCenter(MainBorderPane, "GraphPage.fxml");

    }

    @FXML
    private void show_tweets(MouseEvent event) throws IOException {
        graphButton.setStyle("-fx-background-color: #0f1e2d;");
        tweetsButton.setStyle("-fx-background-color: #adc1d6;");
        viewTweets.setStyle("-fx-background-color: #0f1e2d;");
        similartweetsButton.setStyle("-fx-background-color: #0f1e2d;");
     
        System.out.println("-----------------------------");
        DynamicViews.loadBordaerCenter(MainBorderPane, "TweetsPage.fxml");
        System.out.println("-----------------------------");
    }

    @FXML
    private void show_similartweets(MouseEvent event) throws IOException {
        tweetsButton.setStyle("-fx-background-color: #0f1e2d;");
        graphButton.setStyle(" -fx-background-color:  #0f1e2d;");
        viewTweets.setStyle(" -fx-background-color:  #0f1e2d;");
        similartweetsButton.setStyle("-fx-background-color: #adc1d6;");
     
        DynamicViews.loadBordaerCenter(MainBorderPane, "SimilarTweetsPage.fxml");

    }

    @FXML
    private void show_viewTweets(MouseEvent event) throws IOException {
        graphButton.setStyle("-fx-background-color: #0f1e2d;");
        tweetsButton.setStyle("-fx-background-color: #0f1e2d;");
        viewTweets.setStyle("-fx-background-color: #adc1d6;");
        similartweetsButton.setStyle("-fx-background-color: #0f1e2d;");
        DynamicViews.loadBordaerCenter(MainBorderPane, "ViewTweets.fxml");

    }

 
}
