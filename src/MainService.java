import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainService {
    private static final int PORT = 8080;
    public static final List<String> quotesList = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Main service is running on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClientRequest(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error handling client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    private static void handleClientRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestLine = in.readLine();
        if (requestLine == null) return;

        String[] requestParts = requestLine.split("\\s+");
        if (requestParts.length < 2) return;

        String method = requestParts[0];
        String path = requestParts[1];

        if ("GET".equals(method) && "/quotes".equals(path)) {
            handleQuotesRequest(out);
        } else if ("POST".equals(method) && "/save-quote".equals(path)) {
            StringBuilder requestBody = new StringBuilder();
            while (in.ready()) {
                requestBody.append((char) in.read());
            }
            handleSaveQuoteRequest(out, requestBody.toString());
        } else {
            sendNotFound(out);
        }
    }

    private static void handleQuotesRequest(PrintWriter out) {
        String quoteOfTheDayJson = fetchQuoteOfTheDay();
        String quoteOfTheDay = "N/A";
        try {
            quoteOfTheDay = quoteOfTheDayJson.split("\"quote\":\"")[1].split("\"}")[0].replace("\\\"", "\"");
        } catch (Exception e) {
            System.err.println("Error parsing quote of the day: " + e.getMessage());
        }

        StringBuilder quotesHtml = new StringBuilder();
        for (String quote : quotesList) {
            quotesHtml.append("<li>").append(quote).append("</li>\n");
        }

        String htmlResponse = "<html>\n" +
                "<head>\n" +
                "<title>Quotes</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Enter a new quote</h1>\n" +
                "<form action=\"/save-quote\" method=\"post\">\n" +
                "<label for=\"quote\">Quote:</label><br>\n" +
                "<input type=\"text\" id=\"quote\" name=\"quote\"><br><br>\n" +
                "<label for=\"author\">Author:</label><br>\n" +
                "<input type=\"text\" id=\"author\" name=\"author\"><br><br>\n" +
                "<input type=\"submit\" value=\"Save Quote\">\n" +
                "</form>\n" +
                "<hr>\n" +
                "<h2>Quote of the Day: " +
                "<br>\n" +
                quoteOfTheDayJson + "</h2>" +
                "<br>\n" +
                "<h2>Saved Quotes</h2>\n" +
                "<ul>\n" +
                quotesHtml +
                "</ul>\n" +
                "<hr>\n" +

                "</body>\n" +
                "</html>";

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println("Content-Length: " + htmlResponse.length());
        out.println();
        out.println(htmlResponse);
    }

    private static void handleSaveQuoteRequest(PrintWriter out, String requestBody) {
        Map<String, String> params = parseFormData(requestBody);
        String quote = params.getOrDefault("quote", "");
        String author = params.getOrDefault("author", "");
        quotesList.add("\"" + quote + "\" - " + author);

        out.println("HTTP/1.1 303 See Other");
        out.println("Location: /quotes");
        out.println();
    }

    static Map<String, String> parseFormData(String data) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = data.split("&");
        try {
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    params.put(key, value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("Error decoding form data: " + e.getMessage());
        }
        return params;
    }

    private static void sendNotFound(PrintWriter out) {
        String response = "<html><body><h1>404 Not Found</h1></body></html>";
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/html");
        out.println("Content-Length: " + response.length());
        out.println();
        out.println(response);
    }

    private static String fetchQuoteOfTheDay() {
        try (Socket socket = new Socket("localhost", 9090);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String responseLine = in.readLine();
            if (responseLine != null && responseLine.startsWith("{\"quote\":")) {
                int startIndex = responseLine.indexOf('"', responseLine.indexOf(':')) + 1;
                int endIndex = responseLine.lastIndexOf('"');
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    // Extract the quote text
                    return responseLine.substring(startIndex, endIndex);
                }
            }
            return "No quote available"; // Default text if parsing fails or response is unexpected
        } catch (IOException e) {
            System.err.println("Error fetching quote of the day: " + e.getMessage());
            return "Error fetching quote of the day";
        }
    }


}