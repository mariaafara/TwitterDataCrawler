/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import controllers.TweetsPageController;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author lenovo
 */
public class DynamicViews {

    private DynamicViews() {

    }
   
    public static void loadBordaerCenter(BorderPane borderpane, String resource) throws IOException {
        Parent dashboard=FXMLLoader.load(new DynamicViews().getClass().getResource("/views/"+resource));
        System.out.println("-------------------------------------------------------------Dynamic");
        borderpane.setCenter(dashboard);
    }
       
}
