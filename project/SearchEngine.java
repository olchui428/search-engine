package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import project.utils.Porter;
import project.Utils;

public class SearchEngine {
    public int numPages = 30;
    public double TITLE_BONUS = 0.5;

    private DatabaseManager recmanPages;
    private DatabaseManager recmanInvTitle;
    private DatabaseManager recmanInvBody;
    private ArrayList<String> query;

    protected SearchEngine(String query, int numPages, DatabaseManager recmanPages, DatabaseManager recmanInvTitle, DatabaseManager recmanInvBody) {
        this.recmanPages = recmanPages;
        this.recmanInvTitle = recmanInvTitle;
        this.recmanInvBody = recmanInvBody;
        this.query = new ArrayList<String>(Arrays.asList(query.split(" ")));
        this.numPages = numPages;
    }

    public Map<String, Double> start(String inputQuery) throws IOException {
        DatabaseManager recmanTermToId = new DatabaseManager(TableName.TERM_TO_ID);
        recmanTermToId.linkDatabase();

        // Process the input Query
        ArrayList<String> query = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(inputQuery);
        while (st.hasMoreTokens()) {
            query.add(st.nextToken());
        }
        query = Indexer.removeStopWords(query);
        query = Indexer.stem(query);
        System.out.println("StopStemmed query: " + query);

        // ----- Ranking -----

        // Processing query
        Hashtable<String, Integer> queryIndex = new Hashtable<>(); // word -> tf in query
        for (String str : query) {
            Integer i;
            // Term already exists in queryIndex
            if ((i = queryIndex.get(str)) != null) {
                queryIndex.put(str, ++i);
            } else {
                queryIndex.put(str, 1);
            }
        }
        // DEBUG
//        System.out.println("queryIndex >>>>>>" + queryIndex);
//        for (String q : queryIndex.keySet()) {
//            System.out.println("q = " + q);
//            System.out.println("queryIndex.get(q) = " + queryIndex.get(q));
//        }

        // Compute numerator (for cosine similarity) for each document

        Map<String, Double> docNumeratorMap = new HashMap<>(); // PageId -> weight
        Map<String, Double> cosSimMap = new HashMap<>(); // PageId -> cosine similarity

        for (String q : queryIndex.keySet()) {
            System.out.println("Calculating " + q);
            String wordID = (String) recmanTermToId.get(q);
            if (wordID == null) {
                continue;
            }

            // Compute query weight
            double weightQ = queryIndex.get(q);

            // ----- Title (weight of doc += TITLE_BONUS_WEIGHT * titleTf) -----

            if (recmanInvTitle.get(wordID) != null) {
                Term term = (Term) recmanInvTitle.get(wordID);
                for (Posting posting : term.getPostingList()) {
                    String pageID = posting.getPageId();
                    Page page = (Page) recmanPages.get(pageID);

                    // Compute term weight
                    double titleWeight = posting.getTf() * TITLE_BONUS;

                    // Compute numerator
                    double numerator = titleWeight * weightQ;

                    // Update numerator (Sum of d_ik * q_k)
                    Double numeratorOld = docNumeratorMap.get(pageID);
                    if (numeratorOld == null) {
                        docNumeratorMap.put(pageID, numerator);
                    } else {
                        docNumeratorMap.put(pageID, numeratorOld + numerator);
                    }
//                    System.out.printf("Page: %s, Title bonus: %s sum_weight: %f\n", page.getTitle(), q, sumWeight);
                }
            }

            // ----- Body (weight of doc += tf * idf / maxTf) -----

            if (recmanInvBody.get(wordID) != null) {
                Term term = (Term) recmanInvBody.get(wordID);
                for (Posting posting : term.getPostingList()) {
                    String pageID = posting.getPageId();
                    Page page = (Page) recmanPages.get(pageID);

                    // Compute document weight
                    double tf = (double) posting.getTf();
                    double maxTf = Collections.max(page.getForwardIndex().values());
                    double df = ((Term) recmanInvBody.get(wordID)).getDf();
                    double idf = Math.log(numPages / df) / Math.log(2);
                    if (maxTf == 0) {
                        continue;
                    }
                    double weightD = (tf / maxTf) * idf;

                    // Compute numerator
                    double numerator = weightD * weightQ;

                    // Update numerator (Sum of d_ik * q_k)
                    Double numeratorOld = docNumeratorMap.get(pageID);
                    if (numeratorOld == null) {
                        docNumeratorMap.put(pageID, numerator);
                    } else {
                        docNumeratorMap.put(pageID, numeratorOld + numerator);
                    }
//                    System.out.println("sumWeight = " + sumWeight + "/// page.getBody() = " + page.getBody());
                }
            }
        }

        // Compute cosine similarity score
        System.out.println();
        System.out.println("COS similarity>>>>");
        for (Map.Entry<String, Double> entry : docNumeratorMap.entrySet()) {
            String pageID = entry.getKey();
            double docWeight = entry.getValue();
            Page page = (Page) recmanPages.get(pageID);
            double score = Utils.CosSim(docWeight, page, query);
            System.out.printf("score:%.2f, page:%s \n", score * 1000, page.getTitle());

            cosSimMap.put(pageID, score);    // put inside a score map
        }

        Map<String, Double> sortedScoreMap = Utils.sortMapDesc(cosSimMap); // PageID -> score

        // DEBUG
//        for (Map.Entry<String, Double> entry : sortedScoreMap.entrySet()) {
//            String pageID = entry.getKey();
//            Page page = (Page) recmanPages.get(pageID);
//            double score = entry.getValue();
//            System.out.printf("score:%.2f, page:%s\n", score * 1000, page.getBody());
//        }

        return sortedScoreMap;
    }
}


