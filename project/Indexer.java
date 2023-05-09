package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import jdbm.helper.FastIterator;
import org.junit.Test;
import project.utils.Porter;
import jdbm.htree.HTree;

public class Indexer {

    private Hashtable<String, Term> invertedFileTitle = new Hashtable();
    private Hashtable<String, Term> invertedFileBody = new Hashtable();
    private Hashtable<String, String> termToID = new Hashtable(); // word to word_id
    private static Porter porter = new Porter();
    private DatabaseManager recmanInvTitle;
    private DatabaseManager recmanInvBody;
    private DatabaseManager recmanTermToId;
    private DatabaseManager recmanPages;

    // Constructor
    public Indexer(DatabaseManager recmanInvTitle, DatabaseManager recmanInvBody, DatabaseManager recmanTermToId) throws IOException {
        this.recmanInvTitle = recmanInvTitle;
        this.recmanInvBody = recmanInvBody;
        this.recmanTermToId = recmanTermToId;

        // Load webpages from database
        recmanPages = new DatabaseManager(TableName.PAGES);
        recmanPages.linkDatabase();

        System.out.println("Indexer created.");
    }

    // Combine all remove stopwords, stemming, indexing, update database
    public void process() {
        System.out.println("Indexing...");

        try {
            HTree pages = recmanPages.getDb();

            FastIterator iter = pages.keys();
            String pageId;
            Page page;

            // Processing webpages: stopwords, stem, indexing
            while ((pageId = (String) iter.next()) != null) {
                page = (Page) pages.get(pageId);

                // Body
                ArrayList<String> body = page.getBody();
                body = removeStopWords(body);
                body = stem(body);
                indexing(body, pageId, true);

                // Title
                // Tokenizing
                ArrayList<String> title = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(page.getTitle().replaceAll("[^a-zA-Z0-9]", " "));
                while (st.hasMoreTokens()) {
                    title.add(st.nextToken());
                }
                title = removeStopWords(title);
                title = stem(title);
                indexing(title, pageId, false);
            }

            // Update database - invertedFileBody & invertedFileTitle
            updateInvertedFiles();

            // Update database - termToId
            updateTermToId();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Indexing completed");
    }

    public static ArrayList<String> removeStopWords(ArrayList<String> body) {
        HashSet<String> stopWords = new HashSet<>();
        try {
            File file = new File("project/utils/stopwords.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null) {
                stopWords.add(st);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        body.removeAll(stopWords);
        return body;
    }

    public static ArrayList<String> stem(ArrayList<String> body) {
        ArrayList<String> result = new ArrayList<>();
        for (String token : body) {
            result.add(porter.stripAffixes(token));
        }
        return result;
    }

    private void indexing(ArrayList<String> stringArrayList, String pageId, boolean isBody) throws IOException {
        // Body
        if (isBody) {
            // Temporary hashtable without word_id.
            // Processes and contains only terms info of this body batch.
            Hashtable<String, ArrayList<Integer>> forwardIndex = new Hashtable<>(); // word -> positions of word in page
            int position = 0;
            for (String str : stringArrayList) {
//                ArrayList<Integer> positions;
                // Term already exists in forwardIndex
                if (forwardIndex.get(str) != null) {
                    ArrayList<Integer> positions = forwardIndex.get(str);
                    positions.add(position);
                    forwardIndex.put(str, positions);
                } else {
                    ArrayList<Integer> positions = new ArrayList<>();
                    positions.add(position);
                    forwardIndex.put(str, positions);
                }
                position++;
            }

            // Update forwardIndex of page (for body only)
            Page page = (Page) recmanPages.getDb().get(pageId);
            page.setForwardIndex(forwardIndex);
            recmanPages.update(pageId, page);

            // Update invertedFileBody and termToID
            for (String word : forwardIndex.keySet()) {
                String wordId = termToID.get(word);
                if (wordId == null) {
                    // Create new term in invertedFileBody and termToID
                    Term term = new Term(word);
                    term.incrementDf();
                    term.addPostingList(pageId, forwardIndex.get(word));
                    wordId = term.getWordId();
                    invertedFileBody.put(wordId, term);
                    termToID.put(word, wordId);
                } else if (invertedFileBody.get(wordId) == null) {
                    // Create new term in invertedFileBody
                    Term term = new Term(word);
                    term.setWordId(wordId);
                    term.incrementDf();
                    term.addPostingList(pageId, forwardIndex.get(word));
                    invertedFileBody.put(wordId, term);
                } else {
                    // Update invertedFileBody
                    Term term = invertedFileBody.get(wordId);
                    term.incrementDf();
                    term.addPostingList(pageId, forwardIndex.get(word));
                    invertedFileBody.put(wordId, term);
                }
            }
        } else {
            // Title

            // Temporary hashtable without word_id.
            // Processes and contains only terms info of this body batch.
            Hashtable<String, ArrayList<Integer>> forwardIndex = new Hashtable<>(); // word -> positions of word in page
            int position = 0;
            for (String str : stringArrayList) {
                ArrayList<Integer> positions;
                // Term already exists in forwardIndex
                if ((positions = forwardIndex.get(str)) != null) {
                    positions.add(position);
                    forwardIndex.put(str, positions);
                } else {
                    positions = new ArrayList<>();
                    positions.add(position);
                    forwardIndex.put(str, positions);
                }
                position++;
            }

            // Update forwardIndex of page (for body only)
            Page page = (Page) recmanPages.getDb().get(pageId);
            page.setForwardIndexTitle(forwardIndex);
            recmanPages.update(pageId, page);

            // Update invertedFileTitle and termToID
            for (String word : forwardIndex.keySet()) {
                String wordId = termToID.get(word);
                if (wordId == null) {
                    // Create new term in invertedFileTitle and termToID
                    Term term = new Term(word);
                    term.incrementDf();
                    term.addPostingList(pageId, forwardIndex.get(word));
                    wordId = term.getWordId();
                    invertedFileTitle.put(wordId, term);
                    termToID.put(word, wordId);
                } else if (invertedFileTitle.get(wordId) == null) {
                    // Create new term in invertedFileTitle
                    Term term = new Term(word);
                    term.setWordId(wordId);
                    term.incrementDf();
                    term.addPostingList(pageId, forwardIndex.get(word));
                    invertedFileTitle.put(wordId, term);
                } else {
                    // Update invertedFileTitle
                    Term term = invertedFileTitle.get(wordId);
                    term.incrementDf();
                    term.addPostingList(pageId, forwardIndex.get(word));
                    invertedFileTitle.put(wordId, term);
                }
            }
        }

    }

    // updateInvertedFiles()
    private void updateInvertedFiles() throws IOException {
        for (String wordId : invertedFileBody.keySet()) {
            recmanInvBody.update(wordId, invertedFileBody.get(wordId));
        }
        for (String wordId : invertedFileTitle.keySet()) {
            recmanInvTitle.update(wordId, invertedFileTitle.get(wordId));
        }
    }

    // updateTermToId()
    private void updateTermToId() throws IOException {
        for (String term : termToID.keySet()) {
            recmanTermToId.update(term, termToID.get(term));
        }
    }

    // TODO: fetchInvertedIndexDatabase()

}