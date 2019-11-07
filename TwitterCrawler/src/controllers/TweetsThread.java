/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

/**
 * Thread used to crawl the tweets of user or group of users
 * 
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class TweetsThread extends Thread implements Serializable {

    TweetsPageController tweetsPageController;
    int pos;
    ArrayList<Twitter> AllAccount = new ArrayList<>();
    Twitter twitter;  //currentAccount
    int nbrAccount;
    int posOfCurrentAccount;
    long node;
    ArrayList<Long> nodes;
    String saveFile;
    ObservableList data;

    Queue<Long> queue = new LinkedList<>();
    boolean ContinueWorking;
    int recentPosition;
    boolean resume = false;
    
    ArrayList<String> TweetsLang;
    String fromDateTime;
    String toDateTime;

    //constructor for single node
    public TweetsThread(ArrayList<Twitter> twitterAccounts, long node, String saveFile, ObservableList data, TweetsPageController tweetsPageController, ArrayList<String> TweetsLang, String fromDateTime, String toDateTime) {

        this.node = node;
        this.tweetsPageController = tweetsPageController;
        this.saveFile = saveFile;
        this.data = data;

        ContinueWorking = true;
        recentPosition = -1;
        this.TweetsLang = TweetsLang;
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
        AllAccount = twitterAccounts;
        nbrAccount = AllAccount.size();
        posOfCurrentAccount = 0;
        twitter = AllAccount.get(posOfCurrentAccount);
        queue.add(node);

    }
//constructor for file of nodes

    public TweetsThread(ArrayList<Twitter> twitterAccounts, ArrayList<Long> nodes, String saveFile, ObservableList data, TweetsPageController tweetsPageController, ArrayList<String> TweetsLang, String fromDateTime, String toDateTime) {
        this.nodes = nodes;
        this.tweetsPageController = tweetsPageController;
        this.saveFile = saveFile;
        this.data = data;
        ContinueWorking = true;
        
        this.TweetsLang = TweetsLang;
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;

        recentPosition = -1;

        for (int i = 0; i < nodes.size(); i++) {
            queue.add(nodes.get(i));
        }
        AllAccount = twitterAccounts;
        nbrAccount = AllAccount.size();
        posOfCurrentAccount = 0;
        twitter = AllAccount.get(posOfCurrentAccount);
    }

    public TweetsThread(ArrayList<Twitter> twitterAccounts, Queue<Long> q, String saveFile, ObservableList data, TweetsPageController tweetsPageController,  ArrayList<String> TweetsLang, String fromDateTime, String toDateTime) {
        //Constructor for resume the thread work
        this.tweetsPageController = tweetsPageController;
        resume = true;
        this.saveFile = saveFile;
        this.data = data;
      
        this.TweetsLang = TweetsLang;
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
        ContinueWorking = true;

        this.queue = q;
        AllAccount = twitterAccounts;
        recentPosition = -1;
        nbrAccount = AllAccount.size();
        posOfCurrentAccount = 0;
        twitter = AllAccount.get(posOfCurrentAccount);
    }

    void changeCurrentAccount() {
        posOfCurrentAccount = (posOfCurrentAccount + 1) % (AllAccount.size());
        twitter = AllAccount.get(posOfCurrentAccount);
//        System.out.println(posOfCurrentAccount);

    }

    int nbrTweetsForEachAccount = 0;

    public void exec() throws TwitterException, InterruptedException, FileNotFoundException, UnsupportedEncodingException, IOException {

        // we start from 1 node
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                if (!resume) {
                    data.add(new Content("Start Crawling...", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                } else {
                    data.add(new Content("Resume Crawling...", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                }
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                data.add(new Content(" ", " "));

            }
        });

        List<Status> statuses = null;
        boolean check = true;
        boolean check1 = true;
        boolean check2 = true;

        while (!queue.isEmpty()) {
            long a = queue.peek();
            //start
            int cont = 0;
            nbrTweetsForEachAccount = 0;
            
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    data.add(new Content("Crawling Node " + a, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                }
            });

            for (int ii = 1; ii < 18 && ContinueWorking; ii++) {
                try {
                    statuses = twitter.getUserTimeline(a, new Paging(ii, 200));  //application/rate_limit_status
                    check = true;
                    check1 = true;
                    check2 = true;
                    if (recentPosition != posOfCurrentAccount) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                data.add(new Content("Account " + (posOfCurrentAccount + 1), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                            }
                        });
                        recentPosition = posOfCurrentAccount;
                    }
                    cont = statuses.size();
                    if (cont == 0) {
                        break;
                    }

                    PrintWriter writer;

                    if (ii == 1) {  // if the file already exist then clear it!
                        File f = new File(saveFile + "\\" + a + ".json");
                        f.delete();
                        writer = new PrintWriter(new BufferedWriter(new FileWriter(saveFile + "\\" + a + ".json", true)));
                    } else {
                        writer = new PrintWriter(new BufferedWriter(new FileWriter(saveFile + "\\" + a + ".json", true)));
                    }

                    for (Status status : statuses) {
                        String json;
                        if (isBetween(fromDateTime, toDateTime, getStringFromDate(status.getCreatedAt().toString())) && (TweetsLang.contains("all") || TweetsLang.contains(status.getLang()))) {
                            json = DataObjectFactory.getRawJSON(status);
                            writer.println(json);

                            nbrTweetsForEachAccount++;
                        }

                    }
                    writer.close();

                } catch (TwitterException ex) {
                    switch (ex.getErrorCode()) {
                        case -1:
                            //THERE'S NO INTERNET CONNECTION
                            if (check) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        data.add(new Content(" ", " "));
                                        data.add(new Content("There's No Internet Connection", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                        data.add(new Content("Waiting a  Connection...", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                                    }
                                });
                                check = false;
                            }
                            break;
                        case 88:
                            //LIMIT EXCEEDED
                            if (check1) {
                                int m = (posOfCurrentAccount + 1);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        data.add(new Content(" ", " "));
                                        data.add(new Content("Account " + m + " Exceed The Limit Of Queries!!!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                        data.add(new Content("Trying To Change To Another Account!!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                        data.add(new Content("Please Wait...", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                                    }
                                });
                                check1 = false;
                            }
                            changeCurrentAccount();
                            break;
                        case 34:
                            //Sorry, that page does not exist.
                            long l = queue.remove();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    data.add(new Content(" ", " "));
                                    data.add(new Content("Node " + l + " Doesnt Exsist!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                }
                            });
                            break;
                        case 63:
                            //Sorry, that page does not exist.
                            long ll = queue.remove();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    data.add(new Content(" ", " "));
                                    data.add(new Content("User  " + ll + " has been suspended!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                }
                            });
                            break;

                        case 326:
                            //   System.out.println("blockeeeed");
                            if (check2) {
                                int m = (posOfCurrentAccount + 1);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        data.add(new Content(" ", " "));
                                        data.add(new Content("Account " + m + " Is Temporarily Locked.!!!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                        data.add(new Content("Trying To Change To Another Account!!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                                    }
                                });
                                check2 = false;
                            }
                            changeCurrentAccount();
                            break;
                        default:
                            changeCurrentAccount();
                            break;
                    }
                    System.out.println(ex.getErrorCode());
                    //   Logger.getLogger(TwitterThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (ContinueWorking) {
                queue.remove();

                int z = nbrTweetsForEachAccount;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        data.add(new Content("Found " + z + " Tweets For " + a, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                    }
                });
                System.out.println("Working");
            } else {
                System.out.println("no Working");
                File ff = new File(saveFile + "/" + a + ".json");
                ff.delete();
                saveCurrentState();
                tweetsPageController.getTableData();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        data.add(new Content("Stop Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    }
                });
                break;
            }

        } //end crawling or stop crawling

        if (ContinueWorking) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    data.add(new Content("Finish Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    tweetsPageController.restoreInitialeState();
                }
            });

        } else {

        }

    }

    public void run() {
        try {
            exec();
        } catch (TwitterException | InterruptedException | FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(TwitterThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TwitterThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveCurrentState() throws FileNotFoundException, IOException {
        String fileName = System.getProperty("user.dir") + "/ImportantData";
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {

            Files.createDirectory(path);
            System.out.println("Directory created");
        }

        PrintWriter p = new PrintWriter(System.getProperty("user.dir") + "/ImportantData/t1.tmp");

        p.println(saveFile);
//        p.println(ProfileLang.size());
//        for (int i = 0; i < ProfileLang.size(); i++) {
//            p.println(ProfileLang.get(i));
//        }

        p.println(TweetsLang.size());
        for (int i = 0; i < TweetsLang.size(); i++) {
            p.println(TweetsLang.get(i));
        }
        p.println(fromDateTime);
        p.println(toDateTime);

        p.println(queue.size());
        for (int i = 0; i < queue.size(); i++) {
            p.println(queue.remove());
        }
        p.close();

    }

    public String getStringFromDate(String dateInString) {
        //dateInString format: "Mon Jul 07 19:18:26 CEST 2014"
        SimpleDateFormat s = new SimpleDateFormat("dd/MMM/yyyy");
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date datee = null;
        try {
            datee = s.parse(dateInString.split(" ")[2] + "/" + dateInString.split(" ")[1] + "/" + dateInString.split(" ")[5]);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new SimpleDateFormat("YYYY-MM-dd").format(datee) + " " + dateInString.split(" ")[3];
    }

    public boolean isBetween(String lower, String upper, String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTimeLower = LocalDateTime.parse(lower, formatter);
        LocalDateTime dateTimeUpper = LocalDateTime.parse(upper, formatter);
        LocalDateTime dateTime = LocalDateTime.parse(value, formatter);

        if (dateTime.compareTo(dateTimeLower) >= 0 && dateTime.compareTo(dateTimeUpper) <= 0) {
            return true;
        } else {
            return false;
        }

    }

}
