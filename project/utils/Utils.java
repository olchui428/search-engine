package project;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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
        double score = dotProduct / (documentLength * queryLength);
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
        query = Indexer.removeStopWords(query);
        query = Indexer.stem(query);
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
