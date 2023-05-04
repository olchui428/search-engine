package project;

import static org.junit.Assert.*;

import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.junit.Test;
import project.utils.Porter;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestClass {

    @Test
    public void testSpiderIndexer() {
        String testingURL = "https://cse.hkust.edu.hk/";
        int numPages = 300;

        DatabaseManager recmanPages, recmanUrlToId, recmanInvTitle, recmanInvBody, recmanTermToId;

        try {
            // Fetching
            recmanPages = new DatabaseManager(TableName.PAGES);
            recmanUrlToId = new DatabaseManager(TableName.URL_to_ID);
            recmanPages.createDatabase();
            recmanUrlToId.createDatabase();
            Spider spider = new Spider(recmanPages, recmanUrlToId, testingURL, numPages);
            spider.fetch();

            // Indexing
            recmanInvTitle = new DatabaseManager(TableName.INVERTED_INDEX_TITLE);
            recmanInvBody = new DatabaseManager(TableName.INVERTED_INDEX_BODY);
            recmanTermToId = new DatabaseManager(TableName.TERM_TO_ID);
            recmanInvTitle.createDatabase();
            recmanInvBody.createDatabase();
            recmanTermToId.createDatabase();
            Indexer indexer = new Indexer(recmanInvTitle, recmanInvBody, recmanTermToId);
            indexer.process();

            // Output to Text
            testOutputToTxt(TableName.PAGES, "project/output/spider_result.txt");
            testOutputToTxt(TableName.URL_to_ID, "project/output/url_to_id_result.txt");
            testOutputToTxt(TableName.INVERTED_INDEX_TITLE, "project/output/indexer_title_result.txt");
            testOutputToTxt(TableName.INVERTED_INDEX_BODY, "project/output/indexer_body_result.txt");
            testOutputToTxt(TableName.TERM_TO_ID, "project/output/term_to_id_result.txt");

            // Printing
            System.out.println("\n----- Printing database -----\n");
            printDb(TableName.INVERTED_INDEX_BODY);
            printDb(TableName.PAGES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchEngine() throws IOException {
//        String query = """
//                To make the Robo-lawyer more like a real person, sentiment analysis has been employed to help identify true feelings and moods of a person heading to a divorce through the dialogue. When a user writes "I am glad", for instance, it should be interpreted as an expression of relief instead of being upbeat. It even makes effort to persuade couples to think twice before finally deciding to divorce. """;
        String query = "hkust";
        int numPages = 30;

        DatabaseManager recmanPages = new DatabaseManager(TableName.PAGES);
        DatabaseManager recmanUrlToId = new DatabaseManager(TableName.URL_to_ID);
        DatabaseManager recmanInvTitle = new DatabaseManager(TableName.INVERTED_INDEX_TITLE);
        DatabaseManager recmanInvBody = new DatabaseManager(TableName.INVERTED_INDEX_BODY);
        recmanPages.linkDatabase();
        recmanUrlToId.linkDatabase();
        recmanInvTitle.linkDatabase();
        recmanInvBody.linkDatabase();

        SearchEngine retriever = new SearchEngine(query, numPages, recmanPages, recmanInvTitle, recmanInvBody);
        Map<String, Double> result = retriever.start(query);
        for (String s : result.keySet()) {
            System.out.println(((Page) recmanPages.get(s)).getTitle() + ": " + result.get(s));
        }
    }

    @Test
    public void testSearchEnginePhrase() throws IOException {
//        String query = """
//                To make the Robo-lawyer more like a real person, sentiment analysis has been employed to help identify true feelings and moods of a person heading to a divorce through the dialogue. When a user writes "I am glad", for instance, it should be interpreted as an expression of relief instead of being upbeat. It even makes effort to persuade couples to think twice before finally deciding to divorce. """;
//        String query = "bitch \"hong kong\" hello \"nei ho\" bye";
        String query = "\"mood modulat\"";
        int numPages = 30;

        DatabaseManager recmanPages = new DatabaseManager(TableName.PAGES);
        DatabaseManager recmanUrlToId = new DatabaseManager(TableName.URL_to_ID);
        DatabaseManager recmanInvTitle = new DatabaseManager(TableName.INVERTED_INDEX_TITLE);
        DatabaseManager recmanInvBody = new DatabaseManager(TableName.INVERTED_INDEX_BODY);
        recmanPages.linkDatabase();
        recmanUrlToId.linkDatabase();
        recmanInvTitle.linkDatabase();
        recmanInvBody.linkDatabase();

        SearchEngine retriever = new SearchEngine(query, numPages, recmanPages, recmanInvTitle, recmanInvBody);
        Map<String, Double> result = retriever.start(query);
        for (String s : result.keySet()) {
            System.out.println(((Page) recmanPages.get(s)).getTitle() + ": " + result.get(s));
        }
    }

    static public void testOutputToTxt(TableName tableName, String filepath) {
        try {
            PrintStream o = new PrintStream(filepath);
            PrintStream console = System.out;
            System.setOut(o);
            printDb(tableName);

            // Reset to output to console.
            System.setOut(console);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPrintDbAll() {
        printDbAll();
    }

    static public void printDb(TableName tableName) {
        try {
            DatabaseManager databaseManager = new DatabaseManager(tableName);
            databaseManager.linkDatabase();

            String filename = databaseManager.getTable().getFilename();
            HTree data = databaseManager.getDb();

            System.out.println("===== Database: " + filename + " =====\n");

            FastIterator iter = data.keys();
            String key;
            boolean isToId = tableName == TableName.TERM_TO_ID || tableName == TableName.URL_to_ID;
            while ((key = (String) iter.next()) != null) {
                var value = data.get(key);
                if (isToId) {
                    System.out.printf("%s: %s\n", key, value.toString());
                } else {
                    System.out.printf("%s------------\n", value.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printDbAll() {
        printDb(TableName.PAGES);
        printDb(TableName.INVERTED_INDEX_BODY);
    }

}
