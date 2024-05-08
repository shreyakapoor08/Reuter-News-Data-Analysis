package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BagOfWordsAnalyzer {

    public static void main(String[] args) {
        try {
            Map<String, Boolean> positiveWords = readWordsFromFile("./src/main/java/org/example/positive-words.txt", true);
            Map<String, Boolean> negativeWords = readWordsFromFile("./src/main/java/org/example/negative-words.txt", false);
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/word_analyser" , "root", "8898");

            System.out.println("Connection Established Successfully\n");
            createTable(connection);

                String fileName = "titles.txt";
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String line;
                while ((line = reader.readLine()) != null) {
                    Map<String, Integer> bagOfWords = createBagOfWords(line);

                    Map<String, Integer> matchedPositiveWords = countMatches(bagOfWords, positiveWords);
                    Map<String, Integer> matchedNegativeWords = countMatches(bagOfWords, negativeWords);

                    int positiveCount = matchedPositiveWords.values().stream().mapToInt(Integer::intValue).sum();
                    int negativeCount = matchedNegativeWords.values().stream().mapToInt(Integer::intValue).sum();

                    int score = positiveCount - negativeCount;
                    String tag = determineTag(score);

                    insertIntoDatabase(connection, line, tag, matchedPositiveWords, matchedNegativeWords, score);
                }
            } catch (Exception e){
                System.out.println("Error in connection with the Database");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("File not Found");
            e.printStackTrace();
        }
    }

    private static void createTable(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS news_titles (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(255)," +
                "tag VARCHAR(255)," +
                "matched_words TEXT," +
                "score INT" +
                ")";
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
            preparedStatement.executeUpdate();
        }
    }


    private static Map<String, Boolean> readWordsFromFile(String fileName, boolean isPositive) throws IOException {
        Map<String, Boolean> words = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            words.put(line.trim(), isPositive);
        }
        return words;
    }

    private static Map<String, Integer> createBagOfWords(String text) {
        Map<String, Integer> bagOfWords = new HashMap<>();
        Pattern pattern = Pattern.compile("\\b\\w+\\b");
        Matcher matcher = pattern.matcher(text.toLowerCase());
        while (matcher.find()) {
            String word = matcher.group();
            bagOfWords.put(word, bagOfWords.getOrDefault(word, 0) + 1);
        }
        return bagOfWords;
    }

    private static Map<String, Integer> countMatches(Map<String, Integer> bagOfWords, Map<String, Boolean> wordsToMatch) {
        Map<String, Integer> matchedWords = new HashMap<>();
        for (String word : bagOfWords.keySet()) {
            if (wordsToMatch.containsKey(word)) {
                matchedWords.put(word, matchedWords.getOrDefault(word, 0) + 1);
            }
        }
        return matchedWords;
    }

    private static String determineTag(int score) {
        if (score > 0) {
            return "positive";
        } else if (score < 0) {
            return "negative";
        } else {
            return "neutral";
        }
    }

    private static void insertIntoDatabase(Connection connection, String title, String tag, Map<String, Integer> matchedPositiveWords, Map<String, Integer> matchedNegativeWords, int score) throws SQLException {
        String insertDataSQL = "INSERT INTO news_titles (title, tag, matched_words, score) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, tag);
            preparedStatement.setString(3, buildMatchedWordsString(matchedPositiveWords, matchedNegativeWords));
            preparedStatement.setInt(4, score);
            preparedStatement.executeUpdate();
        }
    }

    private static String buildMatchedWordsString(Map<String, Integer> matchedPositiveWords, Map<String, Integer> matchedNegativeWords) {
        StringBuilder matchedWordsBuilder = new StringBuilder();

        matchedWordsBuilder.append(String.join(", ", matchedPositiveWords.keySet()));

        if (!matchedPositiveWords.isEmpty() && !matchedNegativeWords.isEmpty()) {
            matchedWordsBuilder.append("; ");
        }

        matchedWordsBuilder.append(String.join(", ", matchedNegativeWords.keySet()));

        return matchedWordsBuilder.toString();
    }


}
