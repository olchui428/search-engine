package project;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class Page implements Serializable {

    static final long serialVersionUID = 3318125043629749747L;

    private final String pageId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private URL url;
    private LocalDateTime lastModificationDate;
    private int pageSize;
    private String title;
    private ArrayList<String> body = new ArrayList();
    private HashSet<URL> childLinks = new HashSet<>();
    private HashSet<URL> parentLinks = new HashSet<>();
    private Hashtable<String, ArrayList<Integer>> forwardIndex = new Hashtable(); // word -> positions
    private Hashtable<String, ArrayList<Integer>> forwardIndexTitle = new Hashtable(); // word -> positions
    // TODO: map wordId instead of word

    // Constructor
    public Page(URL url) {
        this.url = url;
    }

    // Getters

    public String getId() {
        return pageId;
    }

    public URL getUrl() {
        return url;
    }

    public LocalDateTime getLastModificationDate() {
        return lastModificationDate;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getBody() {
        return body;
    }

    public HashSet<URL> getChildLinks() {
        return childLinks;
    }

    public HashSet<URL> getParentLinks() {
        return parentLinks;
    }

    public Hashtable<String, ArrayList<Integer>> getForwardIndex() {
        return forwardIndex;
    }

    public Hashtable<String, ArrayList<Integer>> getForwardIndexTitle() {
        return forwardIndexTitle;
    }

    // Setters

    public void setLastModificationDate(LocalDateTime date) {
        this.lastModificationDate = date;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        // Converts String to ArrayList<String>
        body = body.replaceAll("[^a-zA-Z0-9]", " ");
        StringTokenizer st = new StringTokenizer(body);
        while (st.hasMoreTokens()) {
            this.body.add(st.nextToken());
        }
    }

    public void setParentLinks(HashSet<URL> parentLinks) {
        this.parentLinks = parentLinks;
    }

    public void setChildLinks(HashSet<URL> childLinks) {
        HashSet<URL> set = new HashSet<>();
        set.addAll(childLinks);
        childLinks.clear();
        childLinks.addAll(set);
        childLinks.remove(this.url);

        this.childLinks = childLinks;
    }

    public void setForwardIndex(Hashtable<String, ArrayList<Integer>> forwardIndex) {
        this.forwardIndex = forwardIndex;
    }

    public void setForwardIndexTitle(Hashtable<String, ArrayList<Integer>> forwardIndexTitle) {
        this.forwardIndexTitle = forwardIndexTitle;
    }

    @Override
    public String toString() {
        String result = "";

        result += "Page ID: " + pageId + "\n";
        result += "Title: " + title + "\n";
        result += "URL: " + url + "\n";
        result += "Last modification date: " + lastModificationDate.toLocalDate().toString() + "\n";
        result += "Size of page: " + pageSize + "\n";
        result += "Body: " + body.toString() + "\n";

        result += "Forward Index: [";
        if (!forwardIndex.isEmpty()) {
            for (String word : forwardIndex.keySet()) {
                result += "\"" + word + "\"" + ": " + forwardIndex.get(word).toString() + ", ";
            }
        }
        result += "]\n";

        result += "Forward Index Title: [";
        if (!forwardIndexTitle.isEmpty()) {
            for (String word : forwardIndexTitle.keySet()) {
                result += "\"" + word + "\"" + ": " + forwardIndexTitle.get(word).toString() + ", ";
            }
        }
        result += "]\n";

        if (parentLinks != null) {
            result += "Parent links length: " + parentLinks.size() + "\n";
            result += "Parent links: [";
            for (URL parentLink : parentLinks) {
                result += "\"" + parentLink.toString() + "\", ";
            }
            result += "]\n";
        }

        if (childLinks != null) {
            result += "Child links length: " + childLinks.size() + "\n";
            result += "Child links: [";
            for (URL childLink : childLinks) {
                result += "\"" + childLink.toString() + "\", ";
            }
            result += "]\n";
        }

        return result;
    }
}