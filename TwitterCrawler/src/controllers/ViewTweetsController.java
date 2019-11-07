/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * FXML Controller class
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class ViewTweetsController implements Initializable {

    @FXML
    private Button choosebtn;
    @FXML
    private Label loader;
    @FXML
    private Label screen;
    @FXML
    private Label description;
    @FXML
    private Label name;
    @FXML
    private ImageView imageview;
    // @FXML
    //  private ImageView back;
    @FXML
    private VBox vbox;
    @FXML
    private ScrollPane scroll;
    @FXML
    private Button viewtweetshelp;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        scroll.setStyle("-fx-background-color:transparent;");
        scroll.setVisible(false);
    }
    private HostServices hostServices;

    public HostServices getHostServices() {
        return hostServices;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void chooseFile() throws java.text.ParseException, IOException, FileNotFoundException, ParseException {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a file to open");

        chooser.setInitialDirectory(new java.io.File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Json File", "*.json"));

        File file = chooser.showOpenDialog(null);
        //writepath.setText(file.getAbsolutePath());
        if (file != null) {

            // Dialog<Boolean> dialog = new Dialog<>();
            //  dialog.initModality(Modality.APPLICATION_MODAL);
            //   dialog.initModality(Modality.WINDOW_MODAL);
            //Label loader = new Label("");
            // loader.setContentDisplay(ContentDisplay.BOTTOM);
            loader.setGraphic(new ProgressIndicator());
            // dialog.getDialogPane().setGraphic(new ProgressIndicator());
            //dialog.setTitle("Getting Tweets");
            // DropShadow ds = new DropShadow();
            // ds.setOffsetX(1.3);
            //ds.setOffsetY(1.3);
            //ds.setColor(Color.DARKGRAY);
            // dialog.getDialogPane().setEffect(ds);
            //dialog.setResult(Boolean.TRUE);
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                public Boolean call() throws java.text.ParseException, java.text.ParseException, URISyntaxException {

                    Boolean b = getTweets(file.getAbsolutePath());

                    return b;
                }
            };

            task.setOnRunning((e) -> {

                choosebtn.setDisable(true);
                loader.setVisible(true);
                //dialog.show();
            });
            task.setOnSucceeded((e) -> {

                choosebtn.setDisable(false);

                loader.setVisible(false);

                //dialog.close();
                if (task.getValue()) {

                    System.out.println("done");

                } else {
                    System.out.println("faild");

                }
            });
            task.setOnFailed((e) -> {

                choosebtn.setDisable(false);

                loader.setVisible(false);

                //  dialog.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Information Alert");
                String s = "Inapropriate Json File Format!";
                alert.setContentText(s);
                alert.show();
            });
            new Thread(task).start();

        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Information Alert");
            String s = "You canceled!";
            alert.setContentText(s);
            alert.show();

        }
    }

    public boolean getTweets(String fileName) throws URISyntaxException, java.text.ParseException {

        boolean b = true;

        scroll.setVisible(true);
        JSONObject obj1;
        JSONObject obj;

        // This will reference one line at a time
        String line = null;

        try {

            FileReader fileReader1 = new FileReader(fileName);

            ///******* read firsst line only first to get the user information ******///
            BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
            // FileReader reads text files in the default encoding.
            // I should not parse reader more than once thats why 2 file readers
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            int i = 0;
            obj1 = (JSONObject) new JSONParser().parse(bufferedReader1.readLine());

            // get JSON object for the user to get the information about him
            JSONObject user = (JSONObject) obj1.get("user");
            // trying t0 change JavaFX elements in a thread different from the FX application thread.
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    vbox.getChildren().clear();

                    // take each value from the json object separately
                    name.setText((String) user.get("name") + "");
                    screen.setText("@" + (String) user.get("screen_name") + "");

                    description.setText((String) user.get("description") + "");
                    System.out.println("\n\n\n" + user.get("profile_image_url"));
                    if ((String) user.get("profile_image_url") != "") {
                        Image image = new Image((String) user.get("profile_image_url"));
                        imageview.setImage(image);
                        imageview.setFitWidth(100);
                    }

                }
// place the code here, that you want to execute
            });
            System.out.println("*-----------*-*");

            while ((line = bufferedReader.readLine()) != null) {
                obj = (JSONObject) new JSONParser().parse(line);

                //******//
                VBox panevbox = new VBox();
                panevbox.setStyle("-fx-background-color: white;");
                panevbox.setPrefWidth(557);
                //pane.getChildren().add(panevbox);
                System.out.println("----------------------*-*-*");

                Label tweet = new Label();
                tweet.setMinHeight(Region.USE_PREF_SIZE);
                tweet.setMinWidth(Region.USE_PREF_SIZE);
                System.out.println("----------------------*-*-*");

                //  tweet.setTextAlignment(TextAlignment.JUSTIFY);
                // Image im = new Image(" \\icons\\heart.png");
                ImageView like = new ImageView(getClass().getResource("/icons/heart.png").toExternalForm());
                //       ImageView like = new ImageView(im);

                like.setFitHeight(20);
                like.setFitWidth(20);
                ImageView retweet = new ImageView(getClass().getResource("/icons/retweet.png").toExternalForm());

                retweet.setFitWidth(20);
                retweet.setFitHeight(20);

                Label favorite_count = new Label();

                Label date = new Label();
                Label retweet_count = new Label();
                Hyperlink link = new Hyperlink();
                link.setStyle("-fx-text-fill: red;");
                HBox panehbox = new HBox();
                String t = (String) obj.get("full_text");
                String[] ar = t.split("https");

                tweet.setText(ar[0]);
                System.out.println("----------3------------*-*-*");
                final ContextMenu contextMenu = new ContextMenu();
                System.out.println("----------3-1------------*-*-*");

                MenuItem copy = new MenuItem("Copy");
                System.out.println("----------3-2------------*-*-*");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        System.out.println("----------3-3------------*-*-*");

                        final ClipboardContent content = new ClipboardContent();
                        System.out.println("----------3-4------------*-*-*");

                        contextMenu.getItems().addAll(copy);
                        System.out.println("----------3-5------------*-*-*");

                        copy.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {

                                content.putString(ar[0]);
                                clipboard.setContent(content);
                                System.out.println("----------3-6------------*-*-*");

                            }
                        });
                    }
                });

                System.out.println("----------3-7------------*-*-*");

                System.out.println("----------4------------*-*-*");
                tweet.setContextMenu(contextMenu);

                if (ar.length == 2) {
                    link.setText("https" + ar[1]);
                    link.setOnAction(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent t) {
                            System.out.println("/*/*/*/*/*/*/*/*");
//                            HostServices host=getHostServices();
//                            hostServices.showDocument("https" + ar[1]);
//                            System.out.println("/*/*/*/*/*/*/*/*");
                            try {
                                Desktop.getDesktop().browse(new URI("https" + ar[1]));
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            } catch (URISyntaxException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
                System.out.println("----------5-----------*-*-*");
                String createdat = (String) obj.get("created_at");
                String[] created = createdat.split(" ");
                DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss aa");

                Date d = new SimpleDateFormat("hh:mm:ss").parse(created[3]);

                String dateString = dateFormat.format(d);

                String at = dateString + " - " + created[2] + " " + created[1] + " " + created[5];
                date.setText(at);
                System.out.println("----------6------------*-*-*");
                // date.setAlignment(Pos.TOP_RIGHT);
                // tweet.setAlignment(Pos.CENTER);
                retweet_count.setText((Long) obj.get("retweet_count") + "");
                favorite_count.setText((Long) obj.get("favorite_count") + "");
                panehbox.getChildren().addAll(like, favorite_count, new Label("     "), retweet, retweet_count);
                Separator sepa = new Separator(Orientation.HORIZONTAL);
                if (ar.length == 2) {
                    panevbox.getChildren().addAll(date, tweet, link, panehbox, sepa);
                } else {
                    panevbox.getChildren().addAll(date, tweet, panehbox, sepa);
                }
                System.out.println("------------7----------*-*-*");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("*-************-*");

                        vbox.getChildren().add(panevbox);
                    }
                });

            }
            // Always close files.

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            b = false;
            System.out.println("Unable to open file '" + fileName + "'");
        } catch (IOException ex) {
            b = false;
            System.out.println("Error reading file '" + fileName + "'");
            // Or we could just do this: 
            // ex.printStackTrace();
        } catch (ParseException e) {
            b = false;
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("*-*-*");

        } catch (java.text.ParseException ex) {
            b = false;
            System.out.println("*-in last catch*-*");
            Logger.getLogger(ViewTweetsController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return b;
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
        root = FXMLLoader.load(getClass().getResource("/views/ViewTweetsHelpPage.fxml"));
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(viewtweetshelp.getScene().getWindow());
        stage.show();
    }

}
