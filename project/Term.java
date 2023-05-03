package project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

class Posting implements Serializable {
    static final long serialVersionUID = -1768459135542239720L;

    private String pageId;
    private int tf;
    private ArrayList<Integer> positions;

    Posting(String pageId, int tf) {
        this.pageId = pageId;
        this.tf = tf;
        this.positions = new ArrayList<>();
    }

    Posting(String pageId, int tf, ArrayList<Integer> positions) {
        this.pageId = pageId;
        this.tf = tf;
        this.positions = positions;
    }

    @Override
    public String toString() {
        return "Posting{" +
                "pageId='" + pageId + '\'' +
                ", tf=" + tf +
                ", positions=" + positions +
                '}';
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }

    public void setPositions(ArrayList<Integer> positions) {
        this.positions = positions;
    }

    public void addPosition(Integer position) {
        this.positions.add(position);
    }

}

public class Term implements Serializable {

    static final long serialVersionUID = 3318125043629749747L;

    private String wordId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private final String word;
    private int df;
    private ArrayList<Posting> postingList = new ArrayList<>();

    // Constructors
    public Term(String word) {
        this.word = word;
    }

    // Getters

    public String getWordId() {
        return wordId;
    }

    public String getWord() {
        return word;
    }

    public int getDf() {
        return df;
    }

    public ArrayList<Posting> getPostingList() {
        return postingList;
    }

    // Setters

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public void setDf(int df) {
        this.df = df;
    }

    public void incrementDf() {
        df++;
    }

    public void setPostingList(ArrayList<Posting> postingList) {
        this.postingList = postingList;
    }

    public void addPostingList(String pageId, ArrayList<Integer> positions) {
        System.out.println("IN TERM: positions.size() = " + positions.size());
        postingList.add(new Posting(pageId, positions.size(), positions));
    }

    @Override
    public String toString() {
        String result = "Word id: " + wordId + "\n";
        result += "Word: " + word + "\n";
        result += "Df: " + df + "\n";
        result += "Posting list: ";
        for (Posting posting : postingList) {
            result += posting.toString() + ", ";
        }
        result += "\n";
        return result;
    }
}
