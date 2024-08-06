import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        System.out.println(chatGPT("You received the following email:" +
                "Hello," +
                "Can you please schedule a 4,000 kg delivery of sulfuric acid (MM#1000543305) to Plant 12 for 7/16/2024? PO#3600015883 has been issued." +
                "Thanks," +
                "Abukar Mudei, P.Eng" +
                "Contact Engineer" +
                "Plants 11/12 (Butamer/HF Alkylation)" +
                "Suncor Edmonton Refinery" +
                "Tel: (587) 985-6979" +
                "amudei@suncor.com" +
                " " +
                "Capture these data points: 1. Product 2. Quantity 3. Location 4. Tank(Plant) 5. Delivery Date 6. PO "));
    }
    public static String chatGPT(String message) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "Insert API key HERE"; // API key
        String model = "gpt-4o-mini"; // model of ChatGPT api

        try {
            // HTTP POST request
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("Content-Type", "application/json");

            // Request Body
            String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            // Get Response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // returns the extracted contents of the response.
            return extractContentFromResponse(response.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // This method extracts the response expected from chatgpt and returns it.
    public static String extractContentFromResponse(String response) {
        int startMarker = response.indexOf("content")+11; // Marker for where the content starts.
        int endMarker = response.indexOf("\"", startMarker); // Marker for where the content ends.
        return response.substring(startMarker, endMarker); // Returns the substring containing only the response.
    }
}