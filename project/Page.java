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
    private HashSet<URL> parentLinks = new HashSet();
    private Hashtable<String, Integer> forwardIndex = new Hashtable(); // word -> tf
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

    public Hashtable<String, Integer> getForwardIndex() {
        return forwardIndex;
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

    public void setForwardIndex(Hashtable<String, Integer> forwardIndex) {
        this.forwardIndex = forwardIndex;
    }

    @Override
    public String toString() {
        String result = "";

//        result += "Page ID: " + pageId + "\n";
        result += "Title: " + title + "\n";
        result += "URL: " + url + "\n";
        result += "Last modification date: " + lastModificationDate.toLocalDate().toString() + "\n";
        result += "Size of page: " + pageSize + "\n";
        result += "Body: " + body.toString() + "\n";

//        result += "Forward Index length: " + forwardIndex.size() + "\n";
        result += "Forward Index: [";
        int j = 0;
        if (!forwardIndex.isEmpty()) {
            for (String word : forwardIndex.keySet()) {
                if (j >= 10) {
                    break;
                }
                result += "\"" + word + "\"" + ": " + forwardIndex.get(word).toString() + ", ";
                j++;
            }
        }
        result += "]\n";

        result += "Parent links length: " + parentLinks.size() + "\n";
        result += "Parent links: [";
        if (!parentLinks.isEmpty()) {
            for (URL parentLink : parentLinks) {
                result += "\"" + parentLink.toString() + "\", ";
            }
        }
        result += "]\n";

        result += "Child links length: " + childLinks.size() + "\n";
        result += "Child links: [";
//        int i = 0;
        if (!childLinks.isEmpty()) {
            for (URL childLink : childLinks) {
//                if (i >= 10) {
//                    break;
//                }
                result += "\"" + childLink.toString() + "\", ";
//                i++;
            }
        }
        result += "]\n";

        return result;
    }
}