package project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.junit.Test;

public class Spider {

    private URL startingURL;
    private int numPages;
    public ArrayList<URL> hyperlinks = new ArrayList();
    private DatabaseManager recmanPages;
    private DatabaseManager recmanUrlToId;

    // Constructor
    public Spider(DatabaseManager recmanPages, DatabaseManager recmanUrlToId, String url, int numPages) throws MalformedURLException {
        this.recmanPages = recmanPages;
        this.recmanUrlToId = recmanUrlToId;
        this.startingURL = new URL(url);
        this.numPages = numPages;
        System.out.println("Spider created.");
    }

    public void fetch() throws IOException, ParserException {
        System.out.println("Start crawling...");

        hyperlinks.add(startingURL);
        int numFetchedPages = 0;
        HashMap<URL,HashSet<URL>> childParentMap = new HashMap<>();

        System.out.println("Fetching hyperlinks...");

        // Breadth-first searching hyperlinks using a temporary queue
        Queue<URL> queue = new LinkedList();
        queue.add(startingURL);
        while (queue.size() != 0 && numFetchedPages < numPages) {
            URL url = queue.poll();
            System.out.println(numFetchedPages + ". Fetching " + url + "...");

            if (!shouldFetch(url)) {
                continue;
            }

            HashSet<URL> childLinks = extractChildLinks(url);

            // Establishing child-parent relationships
            for (URL childLink: childLinks) {
                if (childParentMap.containsKey(childLink)) {
                    // Child link already exists in map, append parent link
                    HashSet<URL> parentLinksTemp = childParentMap.get(childLink);
                    parentLinksTemp.add(url);
                    childParentMap.put(childLink, parentLinksTemp);
                } else {
                    // New child link
                    HashSet<URL> parentLinksTemp = new HashSet<>();
                    parentLinksTemp.add(url);
                    childParentMap.put(childLink, parentLinksTemp);
                }
            }

            // Preparing new Webpage
            Page newPage = new Page(url);
            newPage.setLastModificationDate(extractLastModificationDate(url));
            newPage.setPageSize(extractPageSize(url));
            newPage.setTitle(extractTitle(url));
            newPage.setBody(extractBody(url));
            newPage.setChildLinks(childLinks);
            String newPageId = newPage.getId();

            // Storing Webpage
            recmanPages.update(newPageId, newPage);
            recmanUrlToId.update(url.toString(), newPageId);

            // Checking if the Page has been stored in db already
//            Page getPage = (Page) recmanPages.getPage(url);
////            System.out.println("Stored in db:");
//            System.out.println(getPage.toString());
//            System.out.println("---------");
//            if (getPage == null) {
//                new IOException(url + " not stored in database.");
//            }
            numFetchedPages++;

            // Adding childlinks for further fetching
            for (URL childLink : childLinks) {
                if (!hyperlinks.contains(childLink)) {
                    queue.add(childLink);
                    hyperlinks.add(childLink);
                }
            }
        }

        // Adding back parentLinks to each page
        HTree dbPages = recmanPages.getDb();
        FastIterator iter = dbPages.keys();
        String id;
        while ((id = (String) iter.next()) != null) {
            Page page = (Page) dbPages.get(id);
            page.setParentLinks(childParentMap.get(page.getUrl()));
            recmanPages.update(id, page);
        }

        System.out.println("Fetching completed");

        // DEBUG
//        System.out.println("Fetched urls: ");
//        for (URL hyperlink : hyperlinks) {
//            Page getPage = (Page) recmanPages.getPage(hyperlink);
//            System.out.println(getPage.toString());
//        }
    }

    // extractPageSize(URL)
    public int extractPageSize(URL url) {
        URL oracle = url;
        int pageSize = 0;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                pageSize += inputLine.length();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pageSize;
    }

    // extractPageDate(URL)
    public LocalDateTime extractLastModificationDate(URL url) throws IOException {
        return LocalDateTime.now();
    }

    // extractChildLinks(URL)
    public HashSet<URL> extractChildLinks(URL url) {
        LinkBean lb = new LinkBean();
        lb.setURL(url.toString());
        URL[] childLinks = lb.getLinks();
        return new HashSet<>(Arrays.asList(childLinks));
    }

    // extractTitle(URL)
    public String extractTitle(URL url) {
        String title;
        try {
            Parser parser = new Parser(url.toString());
            NodeList nodes = parser.extractAllNodesThatMatch(new AndFilter());
            SimpleNodeIterator nodeIterator = nodes.elements();
            while (nodeIterator.hasMoreNodes()) {
                Node node = nodeIterator.nextNode();
                if (node instanceof TitleTag) {
                    TitleTag tag = (TitleTag) node;
                    title = tag.getTitle();
                    return title;
                }
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }
        return "<No Title Found>";
    }

    // extractBody(URL)
    public String extractBody(URL url) {
        String body;
        StringBean sb = new StringBean();
        sb.setURL(url.toString());
        body = sb.getStrings().toLowerCase();
        return body;
    }

    // addParentLinks
    public void addParentLinks() {
        System.out.println("....Adding parent links....");
    }

    // shouldFetch(URL)
    public boolean shouldFetch(URL url) {
        try {
            Page webpage = (Page) recmanPages.getPage(url);
            // No if htmlparser fail due to certification issues
            Parser parser = new Parser(url.toString());

            // Yes if url does not exist in the inverted index
            if (webpage == null) {
                return true;
            }

            // Yes if last modification date of url > retrieve date in record
            LocalDateTime retrieveDate = webpage.getLastModificationDate();
            LocalDateTime lastModificationDate = extractLastModificationDate(url);
            return retrieveDate.isBefore(lastModificationDate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
