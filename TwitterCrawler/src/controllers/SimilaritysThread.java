/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

/**
 * Thread used to crawl the similar tweets
 * 
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class SimilaritysThread extends Thread implements Serializable {

    public static boolean LOGGING = true;
    public static boolean stopbool = false;
    public static String authFile = "./auth_account.config";

    SimilarTweetsPageController similartweetsPageController;

    int pos;
    ArrayList<Twitter> AllAccount = new ArrayList<>();
    Twitter twitter;  //currentAccount
    int nbrAccount;
    int posOfCurrentAccount;

    ArrayList<Long> nodes;
    String saveFile;
    ObservableList data;

    Queue<Long> queue = new LinkedList<>();
    boolean ContinueWorking;
    int recentPosition;
    boolean resume = false;

    String fromDateTime;
    String toDateTime;

    int nbrSimilarTweetsForEachTweet = 0;
    Long tweetid;
    String format;
    String username;
    Long lastid;

    public SimilaritysThread(ArrayList<Twitter> twitterAccounts, Long tweetid, String format, String saveFile, ObservableList data, SimilarTweetsPageController similartweetsPageController, String fromDateTime, String toDateTime) {

        this.format = format;
        this.tweetid = tweetid;
        this.similartweetsPageController = similartweetsPageController;
        this.saveFile = saveFile;
        this.data = data;

        ContinueWorking = true;
        recentPosition = -1;

        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;

        AllAccount = twitterAccounts;
        nbrAccount = AllAccount.size();
        posOfCurrentAccount = 0;

        twitter = AllAccount.get(posOfCurrentAccount);

        queue.add(tweetid);

    }

    public SimilaritysThread(ArrayList<Twitter> twitterAccounts, ArrayList<Long> queuetweets, String format, String saveFile, ObservableList data, SimilarTweetsPageController similartweetsPageController, String fromDateTime, String toDateTime) {

        this.format = format;
        this.tweetid = tweetid;
        this.similartweetsPageController = similartweetsPageController;
        this.saveFile = saveFile;
        this.data = data;

        ContinueWorking = true;
        recentPosition = -1;

        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;

        AllAccount = twitterAccounts;
        nbrAccount = AllAccount.size();
        posOfCurrentAccount = 0;

        twitter = AllAccount.get(posOfCurrentAccount);

        for (int i = 0; i < queuetweets.size(); i++) {
            queue.add(queuetweets.get(i));
        }

    }

    public SimilaritysThread(ArrayList<Twitter> twitterAccounts, Queue queuetweets, String format, String saveFile, ObservableList data, SimilarTweetsPageController similartweetsPageController, String fromDateTime, String toDateTime) {

        this.format = format;
        this.tweetid = tweetid;
        this.similartweetsPageController = similartweetsPageController;
        this.saveFile = saveFile;
        this.data = data;

        ContinueWorking = true;
        recentPosition = -1;

        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;

        AllAccount = twitterAccounts;
        nbrAccount = AllAccount.size();
        posOfCurrentAccount = 0;

        twitter = AllAccount.get(posOfCurrentAccount);

        this.queue = queuetweets;

    }

    void changeCurrentAccount() {

        posOfCurrentAccount = (posOfCurrentAccount + 1) % (AllAccount.size());
        twitter = AllAccount.get(posOfCurrentAccount);

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

//    public void saveCurrentState() throws FileNotFoundException, IOException {
//        String fileName = System.getProperty("user.dir") + "/ImportantData";
//        Path path = Paths.get(fileName);
//        if (!Files.exists(path)) {
//
//            Files.createDirectory(path);
//            System.out.println("Directory created");
//        }
//
//        PrintWriter p = new PrintWriter(System.getProperty("user.dir") + "/ImportantData/t1.tmp");
//
//        p.println(format);
//        p.println(saveFile);
//
//        p.println(minsimilarityDegree);
//        p.println(fromDateTime);
//        p.println(toDateTime);
//
//        p.println(queue.size());
//        for (int i = 0; i < queue.size(); i++) {
//            p.println(queue.remove());
//        }
//        p.close();
//
//    }
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

    /**
     * sentence after removing all stopWords from it
     *
     * @param s The sentence
     * @param ar ArrayList contains all the english stop words
     * @return sentence after removing all stopWords from it
     */
    public ArrayList<String> SentenceWithNoKeyWords(String s, ArrayList<String> ar) {
        ArrayList<String> result = new ArrayList<>();
        String[] temp;
        temp = s.split("\\W+");
        for (int i = 0; i < temp.length; i++) {
            result.add(temp[i].toLowerCase());
        }
        result.remove(" ");
        for (int i = 0; i < result.size(); i++) {
            if (ar.contains(result.get(i)) || result.get(i).equals(" ")) {
                result.remove(i);
                i--;
            }
        }

        return result;
    }

    /**
     * degree of similarity between two sentences
     *
     * @param s The query entered by the user
     * @param s1 The crawled query
     * @return degree of similarity between zero and one
     */
//    public double degreeOfSimilarity(ArrayList<String> s, ArrayList<String> s1) {
//        double result = 0;
//        double length = s.size();
//        double count = 0;
//        for (int i = 0; i < s.size(); i++) {
//            if (s1.contains(s.get(i))) {
//                count++;
//            }
//        }
//        result = count / length;
//        return result;
//    }
    public void exec() throws TwitterException, InterruptedException, FileNotFoundException, UnsupportedEncodingException, IOException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                similartweetsPageController.nbrtweetCrawledLabel.setText(nbrSimilarTweetsForEachTweet + "");

            }
        });
        // we start from 1 node
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                data.add(new Content("Start Crawling...", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                data.add(new Content(" ", " "));

            }
        });

        //List<Long> similartweetsIds = null;
        //List<Status> statuses = null;
        boolean check = true;
        boolean check1 = true;
        boolean check2 = true;
        // boolean firstTime = true;

        while (!queue.isEmpty()) {

            //tl3 twet id
            long tweet_id = queue.peek();
            System.out.println("\n parsing the ids : " + tweet_id);
            //start
            int cont = 0;
            nbrSimilarTweetsForEachTweet = 0;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    data.add(new Content("Crawling Tweet " + tweet_id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                }
            });

            try {
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        data.add(new Content("abl query_status ", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
//
//                    }
//                });s
                Status query_status = twitter.showStatus(tweet_id);
                username = query_status.getUser().getScreenName();
                String query = query_status.getText();
                String[] usedQuery = query.split("https");
                query = usedQuery[0];
                ///hon lsplitbdo ysir 

                //   statuses = twitter.getUserTimeline(a, new Paging(ii, 200));  //application/rate_limit_status
                ///****************************************//
                File subfolder;
                PrintWriter writer;

                if (format.equals("jsnFormat")) {

                    // String saveFile = "C:\\Users\\lenovo\\Desktop\\Test";
                    subfolder = new File(saveFile + "\\" + tweet_id);
                    System.out.println("\n!!!\n");

                    if (!subfolder.getParentFile().exists()) {
                        subfolder.getParentFile().mkdirs();
                    }
                } else if (format.equals("txtFormat")) {
                    subfolder = new File(saveFile + "\\" + tweet_id + ".txt");

                    subfolder.delete();
                    subfolder = new File(saveFile + "\\" + tweet_id + ".txt");
                }

                ArrayList<Long> result = new ArrayList<>();
                ArrayList<Long> temp = new ArrayList<>();

                //  HashMap<Long, Integer> hashmap = new HashMap<>();
                try {
                    WebClient client = new WebClient(BrowserVersion.EDGE);

                    Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

                    client.getOptions().setJavaScriptEnabled(true);
                    client.getOptions().setRedirectEnabled(true);
                    client.getOptions().setThrowExceptionOnScriptError(true);
                    client.getOptions().setCssEnabled(false);
                    client.getOptions().setUseInsecureSSL(true);
                    client.getOptions().setThrowExceptionOnFailingStatusCode(false);
                    client.setAjaxController(new NicelyResynchronizingAjaxController());
                    client.getCookieManager().setCookiesEnabled(true);

                    String minPosition = "";
                    String maxPosition = "";
                    String condition = "";
                    if (LOGGING) {
                        System.out.println("getting data...");
                    }

                    int countAdded = 1;
                    //   int j = 0;
//boolean la yw2ef
                    while (countAdded > 0 && ContinueWorking) {
                        countAdded = 0;
                        Page newPage;

                        newPage = client.getPage("https://twitter.com/i/search/timeline?f=tweets&q=" + query + condition);

                        if (LOGGING) {

                            System.out.println("***" + " https://twitter.com/i/search/timeline?f=tweets&q=" + query + condition);
                            System.out.println("\n\n");
                        }

                        String content = newPage.getWebResponse().getContentAsString();
                        String[] parts = content.split(" ");//shu he

                        for (String part : parts) {

                            String[] parts2 = part.split("=");

                            for (int i = 0; i < parts2.length; i++) {
                                if (parts2[i].equals("data-item-id")) {

                                    String data = parts2[i + 1];
                                    StringBuilder itemId = new StringBuilder();
                                    for (char c : data.toCharArray()) {
                                        if (Character.isDigit(c)) {
                                            itemId.append(c);
                                        }
                                    }
                                    //   result.add(itemId.toString());

                                    Long id = Long.parseLong(itemId.toString());
                                    if (!temp.contains(id)) {
                                        temp.add(id);
                                        Status status = twitter.showStatus(id);
                                        if (status == null) { // 
                                            // don't know if needed - T4J docs are very bad
                                            System.out.println("status=null");
                                        } else {

                                            //!username.equals(status.getUser().getScreenName()) &&                                     iza date between hol 2 dates ...
                                            if (isBetween(fromDateTime, toDateTime, getStringFromDate(status.getCreatedAt().toString()))) {
                                                System.out.println("-----");

                                                System.out.println("-----");
                                                result.add(id);
                                                nbrSimilarTweetsForEachTweet++;

                                                Platform.runLater(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        similartweetsPageController.nbrtweetCrawledLabel.setText(nbrSimilarTweetsForEachTweet + "");

                                                    }
                                                });
                                                if (format.equals("jsnFormat")) {

                                                    File similartweetFile1 = new File(saveFile + "\\" + tweet_id + "\\" + id + ".json");
                                                    similartweetFile1.delete();
                                                    File similartweetFile = new File(saveFile + "\\" + tweet_id + "\\" + id + ".json");

                                                    if (!similartweetFile.getParentFile().exists()) {
                                                        similartweetFile.getParentFile().mkdirs();
                                                    }
                                                    writer = new PrintWriter(new BufferedWriter(new FileWriter(saveFile + "\\" + tweet_id + "\\" + id + ".json", true)));

                                                    String json;
                                                    // Status status = twitter.showStatus(id);
                                                    json = DataObjectFactory.getRawJSON(status);
                                                    writer.println(json);
                                                    writer.close();

                                                } else if (format.equals("txtFormat")) {

                                                    File similartweetFile1 = new File(saveFile + "\\" + tweet_id + "\\" + id + ".txt");
                                                    similartweetFile1.delete();
                                                    File similartweetFile = new File(saveFile + "\\" + tweet_id + "\\" + id + ".txt");

                                                    if (!similartweetFile.getParentFile().exists()) {
                                                        similartweetFile.getParentFile().mkdirs();
                                                    }
                                                    writer = new PrintWriter(new BufferedWriter(new FileWriter(saveFile + "\\" + tweet_id + "\\" + id + ".txt", true)));
//                        writer.print("tweetId   tweetText");
                                                    writer.print("tweetId   tweetUrl    tweetText");
                                                    writer.println();
                                                    //Status status = twitter.showStatus(id);
                                                    writer.print(id);
                                                    writer.print("  ");
                                                    writer.print("https://twitter.com/" + status.getUser().getScreenName()
                                                            + "/status/" + status.getId());
                                                    writer.print("  ");
                                                    writer.print(status.getText());
                                                    writer.println();
                                                    writer.close();

                                                }
                                            }
                                        }
                                    }

                                    countAdded++;
                                    if (LOGGING) {
                                        //     System.out.println(itemId.toString());
                                    }
                                }
                            }

                        }
                        if (result.size() == 0) {
                            break;
                        }

                        System.out.println(result);
                        System.out.println("\n");

                        minPosition = result.get(0) + "";
                        maxPosition = result.get(result.size() - 1) + "";
                        condition = "&max_position=TWEET-" + maxPosition + "-" + minPosition;
                    }
                    client.close();
                } catch (TwitterException e) {
                    System.out.println(e.getMessage());
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
                    switch (e.getErrorCode()) {
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
                                    data.add(new Content("Tweet " + l + " Doesnt Exsist!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
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
                                    data.add(new Content("Tweet  " + ll + " has been suspended!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                }
                            });
                            break;
                        case 179:
                            //Thrown when a Tweet cannot be viewed by the authenticating user, usually due to the Tweet’s author having protected their Tweets.
                            long lll = queue.remove();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    data.add(new Content(" ", " "));
                                    data.add(new Content("Tweet  " + lll + "is not authorized to be seen", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                                }
                            });
                            break;
                        case 186:
                            // The status text is too long.
                            long llll = queue.remove();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    data.add(new Content(" ", " "));
                                    data.add(new Content("Tweet  " + llll + "needs to be a bit shorter.", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
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
                    System.out.println(e.getErrorCode());
                }
                //********************************************//

//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        data.add(new Content("Saving results of tweet " + tweet_id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
//
//                    }
//                });
            } catch (TwitterException ex) {
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
                                data.add(new Content("Tweet " + l + " Doesnt Exsist!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
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
                                data.add(new Content("Tweet  " + ll + " has been suspended!", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                            }
                        });
                        break;
                    case 179:
                        //Thrown when a Tweet cannot be viewed by the authenticating user, usually due to the Tweet’s author having protected their Tweets.
                        long lll = queue.remove();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                data.add(new Content(" ", " "));
                                data.add(new Content("Tweet  " + lll + "is not authorized to be seen", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                            }
                        });
                        break;
                    case 186:
                        // The status text is too long.
                        long llll = queue.remove();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                data.add(new Content(" ", " "));
                                data.add(new Content("Tweet  " + llll + "needs to be a bit shorter.", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
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
            //***//

            if (ContinueWorking) {
                queue.remove();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        data.add(new Content("Finish Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

                        data.add(new Content("*Found " + nbrSimilarTweetsForEachTweet + " similar Tweets For tweet" + tweet_id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                        similartweetsPageController.restoreInitialeState();
                    }
                });
                System.out.println("Working");
            } else {
                System.out.println("no Working");

//                saveCurrentState();
                //    similartweetsPageController.getTableData();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        data.add(new Content("Stop Crawling", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                        data.add(new Content("Found " + nbrSimilarTweetsForEachTweet + " similar Tweets For tweet" + tweet_id, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                        similartweetsPageController.restoreInitialeState();
                    }
                });
                break;
            }

        } //end crawling or stop crawling

        System.out.println(
                "\nfinishedddd");
    }

}
