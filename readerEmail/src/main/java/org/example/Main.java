package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class Main {
    public static void main(String[] args) {
        try {
            String emailContent = readEmailFromFile("INSERT FILE PATH HERE");
            //System.out.println(emailContent);
            String response = chatGPT("Please extract the following data points from the email order request:" +
                    "Product" +
                    "Quantity" +
                    "Location" +
                    "Tank (Plant)" +
                    "Delivery Date" +
                    "PO" +
                    "MM#" +
                    "Notes (if any)" +
                    emailContent +
                    "Format the extracted information as follows:" +
                    "1. Product: [Product Name]" +
                    "2. Quantity: [Quantity]" +
                    "3. Location: [Location]" +
                    "4. Tank (Plant): [Tank]" +
                    "5. Delivery Date: [Delivery Date]" +
                    "6. PO: [PO Number]" +
                    "7. MM#: [MM Number]" +
                    "8. Notes: [Additional Notes]");
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readEmailFromFile(String filePath) throws Exception {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        InputStream source = new FileInputStream(new File(filePath));
        MimeMessage message = new MimeMessage(session, source);

        // Extract the email content
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            StringBuilder emailContent = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                emailContent.append(part.getContent().toString());
            }
            return emailContent.toString();
        }
        return "";
    }

    public static String chatGPT(String message) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "INSERT OPENAI API KEY HERE"; // API key
        String model = "gpt-4";

        try {
            // HTTP POST request
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("Content-Type", "application/json");

            // Request Body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", model);
            jsonBody.put("messages", new JSONArray()
                    .put(new JSONObject().put("role", "user").put("content", message))
            );
            String body = jsonBody.toString();
            con.setDoOutput(true);
            try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(body);
                writer.flush();
            }

            // Get Response
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    return extractContentFromResponse(response.toString());
                }
            } else {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    System.out.println("Error Response: " + response.toString());
                }
                throw new RuntimeException("HTTP response code: " + responseCode);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // This method extracts the response expected from chatgpt and returns it.
    public static String extractContentFromResponse(String response) {
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}
