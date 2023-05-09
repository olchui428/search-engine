package project;

import project.utils.Porter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static Porter porter = new Porter();

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

    static public double CosSim(double weight, Page page, Hashtable<String, Integer> queryIndex) {
        int numWord = page.getPageSize();

        // Calculating document length
        double documentLength = 0;
        for (String word : page.getForwardIndex().keySet()) {
            documentLength += Math.pow(page.getForwardIndex().get(word).size(), 2);
        }
        documentLength = Math.sqrt(documentLength);
//        System.out.println("documentLength = " + documentLength);

        // Calculating query length
        double queryLength = 0;
        for (String word : queryIndex.keySet()) {
            queryLength += Math.pow(queryIndex.get(word), 2);
        }
        queryLength = Math.sqrt(queryLength);
//        System.out.println("queryLength = " + queryLength);

        double dotProduct = weight;
//        System.out.println("dotProduct = " + dotProduct);
        double score = 0;
        if (documentLength != 0 && queryLength != 0) {
            score = dotProduct / (documentLength * queryLength);
        } else {
            score = dotProduct;
        }
//        System.out.println("score = " + score);
        return score;
    }

    static public ArrayList<ArrayList<String>> splitQueryPhraseNormal(String inputQuery) {
        ArrayList<String> queryPhrase = new ArrayList<>();
        ArrayList<String> query = new ArrayList<>();
        ArrayList<ArrayList<String>> phraseNormal = new ArrayList<>();

        // Phrases
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(inputQuery);
        while (m.find()) {
            String phrase = m.group(1);
            queryPhrase.add(phrase);
            System.out.println(phrase);
            inputQuery = inputQuery.replace(phrase, "");
        }

        // Normal
        StringTokenizer st = new StringTokenizer(inputQuery);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (!s.equals("\"")) {
                query.add(s);
            }
        }
        query = Utils.removeStopWords(query);
        query = Utils.stem(query);
        query.removeAll(Arrays.asList("", null));

        phraseNormal.add(queryPhrase);
        phraseNormal.add(query);
        return phraseNormal;
    }


    static public <K, V extends Comparable<? super V>> Map<K, V> sortMapDesc(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
