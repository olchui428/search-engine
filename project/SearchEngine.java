package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import project.utils.Porter;
import project.Utils;

public class SearchEngine {
    public int numPages = 30;
    public double TITLE_BONUS = 1.2;

    private DatabaseManager recmanPages;
    private DatabaseManager recmanInvTitle;
    private DatabaseManager recmanInvBody;
    private ArrayList<String> query;
    private ArrayList<String> queryPhrase;

    protected SearchEngine(String query, int numPages, DatabaseManager recmanPages, DatabaseManager recmanInvTitle, DatabaseManager recmanInvBody) {
        this.recmanPages = recmanPages;
        this.recmanInvTitle = recmanInvTitle;
        this.recmanInvBody = recmanInvBody;
        this.query = new ArrayList<String>(Arrays.asList(query.split(" ")));
        this.queryPhrase = new ArrayList<>();
        this.numPages = numPages;
    }

    public Map<String, Double> start(String inputQuery) throws IOException {
        DatabaseManager recmanTermToId = new DatabaseManager(TableName.TERM_TO_ID);
        recmanTermToId.linkDatabase();

        // ----- Process inputQuery -----

        // Phrase query
        System.out.println("Phrases...");
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(inputQuery);
        while (m.find()) {
            String phrase = m.group(1);
            queryPhrase.add(phrase);
            System.out.println(phrase);
        }

        // Normal query (ignore phrase search)
        System.out.println("Normal queries...");
        StringTokenizer st = new StringTokenizer(inputQuery);
        ArrayList<String> query = new ArrayList<>();
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

        // Compute numerator (for cosine similarity) for each document

        Map<String, Double> docNumeratorMap = new HashMap<>(); // PageId -> weight
        Map<String, Double> cosSimMap = new HashMap<>(); // PageId -> cosine similarity

        // ----- Phrase Search -----

        // Search for phrases

        for (String phraseStr : queryPhrase) {
            System.out.println("Calculating " + phraseStr);
            String[] phrase = phraseStr.split(" ");
            if (phrase.length < 2) {
                // Not an actual phrase
                continue;
            }

            Hashtable<String, ArrayList<Integer>> docToPrevWordPos = new Hashtable<>(); // pageID -> previous word's [positions]
            Hashtable<String, ArrayList<Integer>> docToPrevWordPosTitle = new Hashtable<>(); // pageID -> previous word's [positions]

            for (String word : phrase) {
                System.out.println("For Word " + word);
                String wordID = (String) recmanTermToId.get(word);
                if (wordID == null) {
                    continue;
                }

                // ----- Title -----

                Hashtable<String, ArrayList<Integer>> docToThisWordPosTitle = new Hashtable<>();

                if (recmanInvTitle.get(wordID) != null) {
                    Term term = (Term) recmanInvTitle.get(wordID);
                    for (Posting posting : term.getPostingList()) {
                        String pageID = posting.getPageId();

                        if (docToPrevWordPos.isEmpty()) {
                            // For the first word in phrase
                            docToThisWordPosTitle.put(pageID, posting.getPositions());
                        } else {
                            if (docToPrevWordPos.get(pageID) == null) {
                                // The previous word in the phrase doesnt exist in this page
                                continue;
                            }
                            ArrayList<Integer> prevWordPositions = docToPrevWordPos.get(pageID);
                            ArrayList<Integer> thisWordPositions = posting.getPositions();
                            for (Integer prevPos : prevWordPositions) {
                                // For each position of the previous word
                                // Check in the same page, if there is a (position + 1) for this word
                                if (thisWordPositions.contains(prevPos + 1)) {
                                    docToThisWordPosTitle.put(pageID, thisWordPositions);
                                }
                            }
                        }
                    }
                }

                // ----- Body -----

                Hashtable<String, ArrayList<Integer>> docToThisWordPos = new Hashtable<>();

                if (recmanInvBody.get(wordID) != null) {
                    Term term = (Term) recmanInvBody.get(wordID);
                    for (Posting posting : term.getPostingList()) {
                        String pageID = posting.getPageId();

                        if (docToPrevWordPos.isEmpty()) {
                            // For the first word in phrase
                            docToThisWordPos.put(pageID, posting.getPositions());
                        } else {
                            if (docToPrevWordPos.get(pageID) == null) {
                                // The previous word in the phrase doesnt exist in this page
                                continue;
                            }
                            ArrayList<Integer> prevWordPositions = docToPrevWordPos.get(pageID);
                            ArrayList<Integer> thisWordPositions = posting.getPositions();
                            for (Integer prevPos : prevWordPositions) {
                                // For each position of the previous word
                                // Check in the same page, if there is a (position + 1) for this word
                                if (thisWordPositions.contains(prevPos + 1)) {
                                    docToThisWordPos.put(pageID, thisWordPositions);
                                }
                            }
                        }
                    }
                }

                // Update maps
                docToPrevWordPos.clear(); // reset
                docToPrevWordPos = docToThisWordPos;
                docToPrevWordPosTitle.clear(); // reset
                docToPrevWordPosTitle = docToThisWordPosTitle;
            }
//            System.out.println("docToPrevWordPos = " + docToPrevWordPos);

            // Compute Title weight (weight of doc += pf * TITLE_BONUS)
            double dfTitle = docToPrevWordPos.keySet().size();
//            double maxPfTitle = 0;
//            for (String pageID : docToPrevWordPos.keySet()) {
//                maxPfTitle = Math.max(maxPfTitle, docToPrevWordPos.get(pageID).size());
//            }
            for (String pageID : docToPrevWordPos.keySet()) {
                double pf = docToPrevWordPos.get(pageID).size();
                double idfP = Math.log(numPages / dfTitle) / Math.log(2);
                double weight = pf * idfP * TITLE_BONUS;

                // Update numerator (Sum of d_ik)
                Double numeratorOld = docNumeratorMap.get(pageID);
                if (numeratorOld == null) {
                    docNumeratorMap.put(pageID, weight);
                } else {
                    docNumeratorMap.put(pageID, numeratorOld + weight);
                }
            }

            // Compute Body weight (weight of doc += pf * idfP / maxPf)
            double df = docToPrevWordPos.keySet().size();
            double maxPf = 0;
            for (String pageID : docToPrevWordPos.keySet()) {
                maxPf = Math.max(maxPf, docToPrevWordPos.get(pageID).size());
            }
            for (String pageID : docToPrevWordPos.keySet()) {
                double pf = docToPrevWordPos.get(pageID).size();
                double idfP = Math.log(numPages / df) / Math.log(2);
                double weight = pf * idfP / maxPf;

                // Update numerator (Sum of d_ik)
                Double numeratorOld = docNumeratorMap.get(pageID);
                if (numeratorOld == null) {
                    docNumeratorMap.put(pageID, weight);
                } else {
                    docNumeratorMap.put(pageID, numeratorOld + weight);
                }
            }

//            System.out.println("docNumeratorMap = " + docNumeratorMap);
        }

        // ----- Normal Search -----

        for (String q : queryIndex.keySet()) {
            System.out.println("Calculating " + q);
            String wordID = (String) recmanTermToId.get(q);
            if (wordID == null) {
                continue;
            }

            // Compute query weight
            double weightQ = queryIndex.get(q);

            // ----- Title (weight of doc += tf * idf / maxTf * TITLE_BONUS) -----

            if (recmanInvTitle.get(wordID) != null) {
                Term term = (Term) recmanInvTitle.get(wordID);
                for (Posting posting : term.getPostingList()) {
                    String pageID = posting.getPageId();
                    Page page = (Page) recmanPages.get(pageID);

                    // Compute term weight
                    double tf = posting.getTf();
                    double df = ((Term) recmanInvTitle.get(wordID)).getDf();
                    double idf = Math.log(numPages / df) / Math.log(2);
                    double weight = tf * idf * TITLE_BONUS;

                    // Compute numerator
                    double numerator = weight * weightQ;

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
                    double tf = posting.getTf();
                    double maxTf = 0;
                    for (ArrayList<Integer> integers : page.getForwardIndex().values()) {
                        maxTf = Math.max(maxTf, Collections.max(integers));
                    }
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
        for (
                Map.Entry<String, Double> entry : docNumeratorMap.entrySet()) {
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


