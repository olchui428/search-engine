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
    public int NUM_PAGES = 300;
    public double TITLE_BONUS = 1.05;

    private DatabaseManager recmanPages;
    private DatabaseManager recmanInvTitle;
    private DatabaseManager recmanInvBody;
    private ArrayList<String> query;
    private ArrayList<String> queryPhrase;
    private int numResult;


    protected SearchEngine(String inputQuery, int numResult, DatabaseManager recmanPages, DatabaseManager recmanInvTitle, DatabaseManager recmanInvBody) {
        this.recmanPages = recmanPages;
        this.recmanInvTitle = recmanInvTitle;
        this.recmanInvBody = recmanInvBody;

        // ===== Process inputQuery =====
        ArrayList<ArrayList<String>> phraseNormal = Utils.splitQueryPhraseNormal(inputQuery);
        this.queryPhrase = phraseNormal.get(0);
        System.out.println("queryPhrase = " + queryPhrase);
        this.query = phraseNormal.get(1);
        System.out.println("query = " + query);

        this.numResult = numResult;
    }

    public Map<String, Double> start() throws IOException {
        DatabaseManager recmanTermToId = new DatabaseManager(TableName.TERM_TO_ID);
        recmanTermToId.linkDatabase();

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

        // ===== Ranking =====

        Map<String, Double> docNumeratorMap = new HashMap<>(); // PageId -> weight
        Map<String, Double> cosSimMap = new HashMap<>(); // PageId -> cosine similarity

        // ----- Phrase Search -----

        // Search for phrases

        for (String phraseStr : queryPhrase) {
            String[] phrase = phraseStr.split("\\s+");
            if (phrase.length < 2) {
                // Not an actual phrase
                continue;
            }

            Hashtable<String, ArrayList<Integer>> docToPrevWordPos = new Hashtable<>(); // pageID -> previous word's [positions]
            Hashtable<String, ArrayList<Integer>> docToPrevWordPosTitle = new Hashtable<>(); // pageID -> previous word's [positions]

            for (String word : phrase) {
                System.out.println("For Word " + word);
                if (word.equals("")) {
                    continue;
                }
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

            // Compute Title weight (weight of doc += pf * idfP * TITLE_BONUS / maxPf)
            double dfTitle = docToPrevWordPosTitle.keySet().size();
//            System.out.println("docToPrevWordPosTitle = " + docToPrevWordPosTitle);
            double maxPfTitle = 0;
            for (String pageID : docToPrevWordPosTitle.keySet()) {
                maxPfTitle = Math.max(maxPfTitle, docToPrevWordPosTitle.get(pageID).size());
            }
//            System.out.println("maxPfTitle = " + maxPfTitle);
            for (String pageID : docToPrevWordPosTitle.keySet()) {
//                System.out.println("Title = " + ((Page) recmanPages.get(pageID)).getTitle());
                double pf = docToPrevWordPosTitle.get(pageID).size();
//                System.out.println("pf = " + pf);
                double idfP = Math.log(NUM_PAGES / dfTitle) / Math.log(2);
//                System.out.println("idfP = " + idfP);
                if (maxPfTitle == 0) {
                    continue;
                }
                double weight = pf * idfP * TITLE_BONUS / maxPfTitle;
//                System.out.println("weight = " + weight);

                // Update numerator (Sum of d_ik)
                Double numeratorOld = docNumeratorMap.get(pageID);
//                System.out.println("numeratorOld = " + numeratorOld);
                if (numeratorOld == null) {
                    docNumeratorMap.put(pageID, weight);
                } else {
                    docNumeratorMap.put(pageID, numeratorOld + weight);
                }
//                System.out.println("numeratorNew = " + docNumeratorMap.get(pageID));
            }

            // Compute Body weight (weight of doc += pf * idfP / maxPf)
            double df = docToPrevWordPos.keySet().size();
            double maxPf = 0;
            for (String pageID : docToPrevWordPos.keySet()) {
                maxPf = Math.max(maxPf, docToPrevWordPos.get(pageID).size());
            }
//            System.out.println("Compute Body weight>>>");
//            System.out.println("maxPf = " + maxPf);
            for (String pageID : docToPrevWordPos.keySet()) {
//                System.out.println("Title = " + ((Page) recmanPages.get(pageID)).getTitle());
                double pf = docToPrevWordPos.get(pageID).size();
//                System.out.println("pf = " + pf);
                double idfP = Math.log(NUM_PAGES / df) / Math.log(2);
//                System.out.println("idfP = " + idfP);
                if (maxPf == 0) {
                    continue;
                }
                double weight = pf * idfP / maxPf;
//                System.out.println("weight = " + weight);

                // Update numerator (Sum of d_ik)
                Double numeratorOld = docNumeratorMap.get(pageID);
//                System.out.println("numeratorOld = " + numeratorOld);
                if (numeratorOld == null) {
                    docNumeratorMap.put(pageID, weight);
                } else {
                    docNumeratorMap.put(pageID, numeratorOld + weight);
                }
//                System.out.println("numeratorNew = " + docNumeratorMap.get(pageID));
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

            // ----- Title (weight of doc += tf * idf * TITLE_BONUS / maxTf) -----

            if (recmanInvTitle.get(wordID) != null) {
                Term term = (Term) recmanInvTitle.get(wordID);
                for (Posting posting : term.getPostingList()) {
                    String pageID = posting.getPageId();
                    Page page = (Page) recmanPages.get(pageID);

                    // Compute term weight
                    double tf = posting.getTf();
                    double maxTf = 0;
                    for (ArrayList<Integer> integers : page.getForwardIndexTitle().values()) {
                        maxTf = Math.max(maxTf, Collections.max(integers));
                    }
                    double df = ((Term) recmanInvTitle.get(wordID)).getDf();
                    double idf = Math.log(NUM_PAGES / df) / Math.log(2);
                    if (maxTf == 0) {
                        continue;
                    }
                    double weight = tf * idf * TITLE_BONUS / maxTf;

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
                    double idf = Math.log(NUM_PAGES / df) / Math.log(2);
                    if (maxTf == 0) {
                        continue;
                    }
                    double weightD = tf * idf / maxTf;

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
            double score = Utils.CosSim(docWeight, page, queryIndex);
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


