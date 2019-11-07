/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import controllers.TweetsPageController;
import controllers.ViewTweetsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Administrator
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/views/ViewTweets.fxml"));
        Parent root1=loader2.load();
        ViewTweetsController controller = loader2.getController();
        controller.setHostServices(getHostServices());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginPage.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
