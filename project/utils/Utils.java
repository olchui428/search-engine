package project;

import java.util.*;

public class Utils {
    static public double CosSim(double weight, Page page, ArrayList<String> query) {
        int numWord = page.getPageSize();
        double documentLength = Math.sqrt(numWord);
        double queryLength = Math.sqrt(query.size());
        double dotProduct = weight;
        double score = dotProduct / (documentLength * queryLength);
        return score;
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