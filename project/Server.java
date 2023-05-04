package project;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import project.Utils;

import static java.lang.Integer.parseInt;

public class Server {
    static public int NUM_PAGES = 30;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/crawl", new Crawl());
        server.createContext("/query", new Query());

        server.setExecutor(null);
        server.start();
    }

    static class Crawl implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // GET (numPages, startingURL)
            Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
            String startingURL = params.get("startingURL");
            int numPages = parseInt(params.get("numPages"));
            NUM_PAGES = numPages;

            // Crawling
            DatabaseManager recmanPages, recmanUrlToId, recmanInvTitle, recmanInvBody, recmanTermToId;
            try {
                // Fetching
                recmanPages = new DatabaseManager(TableName.PAGES);
                recmanUrlToId = new DatabaseManager(TableName.URL_to_ID);
                recmanPages.createDatabase();
                recmanUrlToId.createDatabase();
                Spider spider = new Spider(recmanPages, recmanUrlToId, startingURL, numPages);
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
                TestClass.testOutputToTxt(TableName.PAGES, "project/output/spider_result.txt");
                TestClass.testOutputToTxt(TableName.URL_to_ID, "project/output/url_to_id_result.txt");
                TestClass.testOutputToTxt(TableName.INVERTED_INDEX_TITLE, "project/output/indexer_title_result.txt");
                TestClass.testOutputToTxt(TableName.INVERTED_INDEX_BODY, "project/output/indexer_body_result.txt");
                TestClass.testOutputToTxt(TableName.TERM_TO_ID, "project/output/term_to_id_result.txt");

                // Printing
//                System.out.println("\n----- Printing database -----\n");
//                TestClass.printDb(TableName.INVERTED_INDEX_BODY);
//                TestClass.printDb(TableName.PAGES);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Response
            String response = "Crawling and Indexing completed";
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().set("Access-Control-Allow-Methods", "*");
            t.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
            t.getResponseHeaders().set("Access-Control-Allow-Credentials", "false");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class Query implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // GET (numPages, query)
            Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
            System.out.println("params.get(\"numPages\")" + params.get("numPages"));
            int numPages = params.get("numPages") != null ? parseInt(params.get("numPages")) : 20;
            String query = params.get("query");

            // Searching
            DatabaseManager recmanPages = new DatabaseManager(TableName.PAGES);
            DatabaseManager recmanUrlToId = new DatabaseManager(TableName.URL_to_ID);
            DatabaseManager recmanInvTitle = new DatabaseManager(TableName.INVERTED_INDEX_TITLE);
            DatabaseManager recmanInvBody = new DatabaseManager(TableName.INVERTED_INDEX_BODY);
            recmanPages.linkDatabase();
            recmanUrlToId.linkDatabase();
            recmanInvTitle.linkDatabase();
            recmanInvBody.linkDatabase();

            SearchEngine searchEngine = new SearchEngine(query, NUM_PAGES, recmanPages, recmanInvTitle, recmanInvBody);
            Map<String, Double> results = searchEngine.start(query);
//             DEBUG
//            for (String s : results.keySet()) {
//                System.out.println(s + ": " + results.get(s));
//            }

            // JSON
            JSONArray list = new JSONArray();
            int i = 0;
            for (String pageId : results.keySet()) {
                if (i >= numPages) {
                    break;
                }

                JSONObject data = new JSONObject();
                Page page = (Page) recmanPages.get(pageId);

                data.put("score", results.get(pageId).toString());
                data.put("pageId", page.getId());
                data.put("url", page.getUrl().toString());
                data.put("modifiedAt", page.getLastModificationDate().atZone(ZoneId.systemDefault()).toEpochSecond());
                data.put("size", String.valueOf(page.getPageSize()));
                data.put("title", page.getTitle());
                data.put("body", new JSONArray(page.getBody()));
                data.put("childLinks", new JSONArray(page.getChildLinks()));
                data.put("parentLinks", new JSONArray(page.getParentLinks()));

                // forwardIndex (array)
                Hashtable<String, ArrayList<Integer>> forwardPositions = page.getForwardIndex();
                Hashtable<String, Integer> forwardIndex = new Hashtable<>();
                for (String word : forwardPositions.keySet()) {
                    forwardIndex.put(word, forwardPositions.get(word).size());
                }
//                Map<String, Integer> forwardIndex = Utils.sortMapDesc(forwardIndex);
//                Map<String, Integer> forwardIndex = project.Utils.sortMapDesc(page.getForwardIndex());
                JSONArray forwardIndexJson = new JSONArray();
                for (String word : Utils.sortMapDesc(forwardIndex).keySet()) {
//                    System.out.println("word = " + word + ", tf = " + forwardIndex.get(word));
                    JSONObject wordToTf = new JSONObject();
                    wordToTf.put("word", word);
                    wordToTf.put("tf", forwardIndex.get(word));
                    forwardIndexJson.put(wordToTf);
                }
                data.put("forwardIndex", forwardIndexJson);

                list.put(data);
                i++;
            }
            var responseJson = list.toString();

            // Response
            System.out.println("Page ranking completed");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().set("Access-Control-Allow-Methods", "*");
            t.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
            t.getResponseHeaders().set("Access-Control-Allow-Credentials", "false");
            t.sendResponseHeaders(200, responseJson.length());
            OutputStream os = t.getResponseBody();
            os.write(responseJson.getBytes());
            os.close();
        }
    }

    static public Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

}