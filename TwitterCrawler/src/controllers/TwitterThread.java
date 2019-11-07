/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
/**
 * Thread used to crawl the graph of the user or group of users
 * 
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class TwitterThread extends Thread implements Serializable {

    GraphPageController graphPageController;
    int nodesInNextLevel;
    int currentLevel;
    int pos;

    ArrayList<Twitter> AllAccount = new ArrayList<>();

    Twitter twitter;  //currentAccount
    int nbrAccount;
    int posOfCurrentAccount;
    long node;
    ArrayList<Long> nodes;
    ArrayList<String> nodesStrings;
    int depth;
    String saveFile;
    ObservableList data;
    Label nbrOfNodeCrawledLabel;
    int nbNodeCrawled;
    int nodesInCurrentLevel;
    Queue<Long> queue = new LinkedList<>();
    Queue<String> queueString = new LinkedList<>();
    boolean ContinueWorking;
    int recentPosition;
    boolean resume = false;
    String typeOfFile;

    public TwitterThread(ArrayList<Twitter> twitterAccounts, long node, int depth, String saveFile, ObservableList data, Label nbrOfNodeCrawledLabel, GraphPageController graphPageController) {
        //CRAWLING node from ID or link or screen name
        typeOfFile = "IDS";
        this.node = node;
        this.graphPageController = graphPageController;
        this.saveFile = saveFile;
        this.data = data;
        this.depth = depth;
        this.nbrOfNodeCrawledLabel = nbrOfNodeCrawledLabel;
        ContinueWorking = true;
        nodesInCurrentLevel = 1;
        recentPosition = -1;
        nbNodeCrawled = 0;
        nodesInNextLevel = 0;
        currentLevel = 0;
        AllAccount = twitterAccounts;
        nbrAccount = AllAccount.size();
        posOfCurrentAccount = 0;
        twitter = AllAccount.get(posOfCurrentAccount);
        queue.add(node);

    }

    public TwitterThread(ArrayList<Twitter> twitterAccounts, ArrayList<Long> nodes, int depth, String saveFile, ObservableList data, Label nbrOfNodeCrawledLabel, GraphPageController graphPageController) {
        //CRAWLING node from file of LONGS
        typeOfFile = "IDS";
        this.nodes = nodes;
        this.graphPageController = graphPageController;
        this.saveFile = saveFile;
        this.data = data;
        this.depth = depth;
        ContinueWorking = true;
        nbNodeCrawled = 0;
        recentPosition = -1;
        nodesInNextLevel = 0;
        currentLevel = 0;
        this.nbrOfNodeCrawledLabel = nbrOfNodeCrawledLabel;
        nodesInCurrentLevel = nodes.size();
        for (int i = 0; i < nodes.size(); i++) {
            queue.add(nodes.get(i));
        }
        AllAccount = twitterAccounts;
        nbrAccount = AllAccount.size();
        posOfCurrentAccount = 0;
        twitter = AllAccount.get(posOfCurrentAccount);
    }

    public TwitterThread(ArrayList<Twitter> twitterAccounts, Queue<Long> q, int depth, String saveFile, ObservableList data, Label nbrOfNodeCrawledLabel, int nodesInCurrentLevel, int nodesInNextLevel, int nbNodeCrawled, int currentLevel, GraphPageController graphPageController) {
        //Constructor for resume the thread work and the type of queue is lONG
        typeOfFile = "IDS";
        this.graphPageController = graphPageController;
        resume = true;
        this.saveFile = saveFile;
        this.data = data;
        this.depth = depth;
        ContinueWorking = true;

        this.nbNodeCrawled = nbNodeCrawled;
        this.nodesInNextLevel = nodesInNextLevel;
        this.currentLevel = currentLevel;
        this.nbrOfNodeCrawledLabel = nbrOfNodeCrawledLabel;
        this.nodesInCurrentLevel = nodesInCurrentLevel;
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

    }

    public void exec() throws TwitterException, InterruptedException, FileNotFoundException, UnsupportedEncodingException, IOException {

        User u1 = null;
        long cursor = -1;
        IDs ids;

        // we start from 1 node
        int i = 0;

        System.out.println("Listing followers's ids.");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                if (!resume) {
                    data.add(new Content("Start Crawling...", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    nbrOfNodeCrawledLabel.setText("0");
                } else {
                    data.add(new Content("Resume Crawling...", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    nbrOfNodeCrawledLabel.setText(nbNodeCrawled + "");
                }
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                data.add(new Content(" ", " "));

            }
        });
        boolean check = true;
        boolean check1 = true;
        boolean check2 = true;

        while (!queue.isEmpty() && ContinueWorking) {
            if (nodesInCurrentLevel != 0) { //we did the work level by level!
                long a = queue.peek();
                ids = null;
                cursor = -1;

                try {
                    ids = twitter.getFriendsIDs(a, cursor);   ///friends/ids 
                    check = true;
                    check1 = true;
                    check2 = true;
                    if (recentPosition != posOfCurrentAccount) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                data.add(new Content("Account " + (posOfCurrentAccount + 1), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                nbrOfNodeCrawledLabel.setText("0");

                            }
                        });
                        recentPosition = posOfCurrentAccount;
                    }

                    //PrintWriter writer = new PrintWriter(saveFile, "UTF-8");
                    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveFile, true)));
                    int ind = saveFile.lastIndexOf('.');
                    String ext = saveFile.substring(ind + 1);
                    nodesInCurrentLevel--;
                    i = 0;
                    queue.remove();
                    for (long id : ids.getIDs()) {
                        queue.add(id);

                        if (ext.equals("csv")) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(a + ",");
                            sb.append(id + "");
                            sb.append('\n');
                            writer.write(sb.toString());
                        } else {
                            writer.println(a + "\t" + id);
                        }

                        nbNodeCrawled++;

                    }
                    nodesInNextLevel += ids.getIDs().length;
                    writer.close();
                    int total = ids.getIDs().length;
                    System.out.println("DONE!" + " " + total);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            data.add(new Content("Found " + total + " Friends For " + a, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                            nbrOfNodeCrawledLabel.setText(nbNodeCrawled + "");
                        }
                    });

                } catch (TwitterException ex) {
                    if (ex.getErrorCode() == -1) {  //THERE'S NO INTERNET CONNECTION
                        if (ex.getStatusCode() == 401) {
                            long l = queue.remove();
                            nodesInCurrentLevel--;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    data.add(new Content(" ", " "));
                                    data.add(new Content("Missing or incorrect authentication credentials for node" + l, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                                }
                            });
                        } else if (check) {
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
                    } else if (ex.getErrorCode() == 88) { //LIMIT EXCEEDED

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
                    } else if (ex.getErrorCode() == 34) {//Sorry, that page does not exist.
                        long l = queue.remove();
                        nodesInCurrentLevel--;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                data.add(new Content(" ", " "));
                                data.add(new Content("Node " + l + " Doesnt Exsist!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                            }
                        });
                    } else if (ex.getErrorCode() == 326) {
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
                    } else {
                        changeCurrentAccount();
                    }
                    System.out.println(ex.getErrorCode());
                    //   Logger.getLogger(TwitterThread.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else { //nodes in current level equal zero
                currentLevel++;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        data.add(new Content("Depth Equal " + currentLevel + " Has Done...", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                        data.add(new Content(" ", " "));

                    }
                });
                if (depth == currentLevel) {
                    break;
                }
                System.out.println("currentLevel-------------->>>>>>>" + currentLevel);
                nodesInCurrentLevel = nodesInNextLevel;
                nodesInNextLevel = 0;
            }

        }
        if (ContinueWorking) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    data.add(new Content("Finish Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    graphPageController.restoreInitialeState();

                }
            });

        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    data.add(new Content("Stop Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                }
            });
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
        //variable i need! queue depth nodesInCurrentLevel nbNodeCrawled saveFile currentLevel nodesInNextLevel
        //PrintWriter p = new PrintWriter("t.tmp");
        String fileName = System.getProperty("user.dir") + "/ImportantData";

        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {

            Files.createDirectory(path);
            System.out.println("Directory created");
        }

        PrintWriter p = new PrintWriter(System.getProperty("user.dir") + "/ImportantData/t.tmp");

        p.println(depth);
        p.println(nodesInCurrentLevel);
        p.println(nodesInNextLevel);
        p.println(nbNodeCrawled);
        p.println(saveFile);
        p.println(currentLevel);
        p.println(queue.size());
        for (int i = 0; i < queue.size(); i++) {
            p.println(queue.remove());
        }

        p.close();

    }
}
