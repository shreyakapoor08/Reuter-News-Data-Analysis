// Problem 1A
// Reuters News Data Reading & Transformation and storing in MongoDB

package org.example;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLEncoder;


public class ReutRead {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String encodedPassword = URLEncoder.encode("Srishti@2201", "UTF-8");

        String databaseConnection = "mongodb+srv://shreyakapoorcanada98:" + encodedPassword + "@database5408.7fxwzu9.mongodb.net/?retryWrites=true&w=majority";
        ServerApi api = ServerApi.builder().version(ServerApiVersion.V1).build();
        MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(databaseConnection)).serverApi(api).build();
        try (MongoClient mongoClient = MongoClients.create(clientSettings)) {
            System.out.println("Connection Established");
            try {
                MongoDatabase db = mongoClient.getDatabase("ReutersDb");
                processSgmFileAndInsert("reut2-009.sgm", db);
                processSgmFileAndInsert("reut2-014.sgm", db);
                List<String> titles = processSgmFileAndGetTitles("reut2-009.sgm", db);
                titles.addAll(processSgmFileAndGetTitles("reut2-014.sgm", db));
                saveTitlesToFile(titles, "titles.txt");


            } catch (MongoException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> processSgmFileAndGetTitles(String fileName, MongoDatabase database) throws IOException {
        List<String> titles = new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = fileReader.readLine()) != null) {
                fileContent.append(line).append("");
            }
            String[] reutersData = fileContent.toString().split("</REUTERS>");
            for (String reuterData : reutersData) {
                String newsTitle = cleanText(extractTagContent(reuterData, "TITLE"));
                if (!newsTitle.isEmpty()) {
                    titles.add(newsTitle);
                }
            }
        }
        return titles;
    }

    private static void saveTitlesToFile(List<String> titles, String filePath) {
        try (Writer writer = new FileWriter(filePath)) {
            for (String title : titles) {
                writer.write(title + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processSgmFileAndInsert(String fileName, MongoDatabase database) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = fileReader.readLine()) != null) {
                fileContent.append(line).append("");
            }
            String[] reutersData = fileContent.toString().split("</REUTERS>");
            List<Document> mongoDocs = new ArrayList<>();
            for (String reuterData : reutersData) {
                String newsTitle = cleanText(extractTagContent(reuterData, "TITLE"));
                String newsBody = cleanText(extractTagContent(reuterData, "BODY"));
                Document newsDocument = new Document();
                if (!newsTitle.isEmpty()) {
                    newsDocument.append("title", newsTitle);
                }
                if (!newsBody.isEmpty()) {
                    newsDocument.append("body", newsBody);
                }

                if (!newsTitle.isEmpty() || !newsBody.isEmpty()) {
                    mongoDocs.add(newsDocument);
                }
            }

            if (!mongoDocs.isEmpty()) {
                MongoCollection<Document> newsCollection = database.getCollection("news");
                newsCollection.insertMany(mongoDocs);
                System.out.println("Data successfully inserted into MongoDB!");
            }
        }
    }

    private static String extractTagContent(String input, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return "";
        }
    }

    private static String cleanText(String text) {
        text = text.replaceAll("&lt;", "").replaceAll("&gt;", "");
        text = text.replaceAll("[^a-zA-Z0-9\\s,.]", "");
        text = text.replace(" Reuter3","");
        return text.trim();
    }
}