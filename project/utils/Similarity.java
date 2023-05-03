//package project.utils;
//
//import org.junit.Test;
//
//import java.lang.reflect.Array;
//import java.util.*;
//
//public class Similarity {
//
//    public static double cosSim(double[] ds, double[] qs) {
//        int numTerms = ds.length;
////        double[] ds = {2, 3, 5};
////        double[] ds = {3, 7, 1};
////        double[] qs = {0, 0, 2};
//
//        double cosSim = 0;
//        double numerator = 0;
//        double denominatorDoc = 0;
//        double denominatorQuery = 0;
//        for (int i = 0; i < numTerms; i++) {
//            numerator += ds[i] * qs[i];
//            denominatorDoc += Math.pow(ds[i], 2);
//            denominatorQuery += Math.pow(qs[i], 2);
//        }
//        double denominator = Math.pow(denominatorDoc, 0.5) * Math.pow(denominatorQuery, 0.5);
//        cosSim = numerator / denominator;
//        return cosSim;
//    }
//
//    public static ArrayList<HashMap<String, Double>> docTermWeights(String[] documents) {
//        ArrayList<HashMap<String, Double>> wordToWeights = new ArrayList<>();
//
//        ArrayList<String> tokens = new ArrayList<>();
//        StringTokenizer st = new StringTokenizer(String.join(" ", documents));
//        while (st.hasMoreTokens()) {
//            tokens.add(st.nextToken());
//        }
//
//        int i = 0;
//        for (String doc : documents) {
//            HashMap<String, Double> wordToWeight = new HashMap<>();
//            doc = doc.toLowerCase();
//            for (String word : tokens) {
//                if (wordToWeight.get(word) == null) {
//                    double tf = Collections.frequency(Arrays.asList(doc.split(" ")), word); // term freq in document
//                    double df = Arrays.stream(documents).filter(document -> document.contains(word)).count(); // number of docs containing term
//                    double n = documents.length;
//                    double idf = Math.log(n / df) / Math.log(2);
//                    double weight = tf * idf;
//                    wordToWeight.put(word, weight);
//                }
//            }
//
//            // Normalize
//            double tfmax = Collections.max(wordToWeight.values());
//            for (String word : wordToWeight.keySet()) {
//                wordToWeight.put(word, wordToWeight.get(word) / tfmax);
//            }
//
//            wordToWeights.add(wordToWeight);
//        }
//        return wordToWeights;
//    }
//
//
//    @Test
//    public void testCosSim() {
////        double[] ds = {2, 3, 5};
//        double[] ds = {3, 7, 1};
//        double[] qs = {0, 0, 2};
//        System.out.println(cosSim(ds, qs));
//    }
//
//    @Test
//    public void testTermWeight() {
//        String[] documents = {
//                "natural language processing is a subfield of linguistics, computer science, and artificial intelligence concerned with the interactions between computers and human language",
//                "the goal of natural language processing is to understand the contents of documents",
//                "the tools can accurately extract information and insights from natural language as well as categorize and organize the documents."};
//        String[] query = {"information"};
//
//        ArrayList<HashMap<String, Double>> weights = docTermWeights(documents);
//        ArrayList<HashMap<String, Double>> weightsQuery = docTermWeights(query);
//
//        for (int i = 0; i < weights.size(); i++) {
//            System.out.println("Document " + i + ">>> ");
//            for (String word : weights.get(i).keySet()) {
//                System.out.println(word + ": " + weights.get(i).get(word));
//            }
//            System.out.println();
//        }
//
////        cosSim(weights.get(0), weightsQuery.get(0));
//
//
//    }
//}
