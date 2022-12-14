package utils.VeryBadHTTP;

import Model.Session;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import utils.VeryBadFramework.Middleware;
import utils.VeryBadFramework.Routing.Router;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class Request {
    Headers headers;
    Body body;
    private METHOD method;


    public void setUrl(String url) {
        this.url = url;
    }

    private String url;
    private Session session;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public StringBuilder getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(StringBuilder originalUrl) {
        this.originalUrl = originalUrl;
    }

    private StringBuilder originalUrl;

    private StringBuilder baseUrl;

    public Middleware getRoute() {
        return route;
    }

    public void setRoute(Middleware route) {
        this.route = route;
    }

    private Middleware route;
    private List<String> path;
    private int pathDept;
    private String contentType;
    private int contentLength;
    Map<String, String> query;
    Map<String, String> params;

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    HashMap<String, String> cookies;

    BufferedReader bs;

    public StringBuilder getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(StringBuilder baseUrl) {
        this.baseUrl = baseUrl;
    }

    public METHOD getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public Request() {
    }

    public int getPathDept() {
        return pathDept;
    }

    public void setPathDept(int pathDept) {
        this.pathDept = pathDept;
    }

    public List<String> getPath() {
        return path;
    }

    public Request(InputStream inputStream) throws BadRequestException {

        try {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bs = bufferedReader;
            /// Getting Request Type
            String startLine;
            startLine = bufferedReader.readLine();
            String[] request = startLine.split(" ");
            if ((request.length != 3 && request.length != 2) || request[1].charAt(0) != '/')
                throw new BadRequestException("Error Parsing Request");
            switch (request[0]) {
                case "POST" -> method = METHOD.POST;
                case "GET" -> method = METHOD.GET;
                case "PUT" -> method = METHOD.PUT;
                case "DELETE" -> method = METHOD.DELETE;
                default -> throw new BadRequestException("Invalid Method");
            }


            this.url = request[1];

            /// Reading Headers
            while (startLine != null && !startLine.equals("")) {
                startLine = bufferedReader.readLine();
                if (startLine != null) {
                    String[] header = startLine.split(":");
                    switch (header[0].trim()) {
                        case "Cookie" -> {
                            cookies = new HashMap<>();

                            for (String cookie : header[1].split(";")) {
                                String[] keyVal = cookie.split("=");
                                if (keyVal.length == 2) {
                                    cookies.put(keyVal[0], keyVal[1]);
                                }
                            }
                        }
                        case "Content-Type" -> this.contentType = header[1];
                        case "Content-Length" -> {
                            try {
                                this.contentLength = parseInt(header[1].trim());
                            } catch (NumberFormatException ex) {
                                this.contentLength = -1;
                            }
                        }
                    }
                }
            }

            /// Reading body
            int charsIn = -1;
            if (method != METHOD.GET && contentLength != -1) {
                StringBuilder sb = new StringBuilder(128);
                String line = "p";
                char[] buffer = new char[contentLength];
                if (bs.read(buffer) != contentLength)
                    throw new BadRequestException("Unexpected body length");
                else {
                    System.out.println("All good, chief");
                }
                sb.append(buffer);
                System.out.println(sb);
            }
            inputStream.close();
        } catch (IOException ex) {
            System.out.println("Error sending response");
            ex.printStackTrace();
        }
    }

    public Request(HttpExchange exchange) throws IOException {
        this.headers = exchange.getRequestHeaders();
        this.contentLength = parseInt(headers.get("Content-Length").get(0));
        this.url = exchange.getRequestURI().toString();
        this.method = METHOD.valueOf(exchange.getRequestMethod());
        InputStream is = exchange.getRequestBody();

        if (method != METHOD.GET || contentLength != 0)
            body = new Body(is, contentLength);
        else
            body = null;
    }

    public Map<String, String> cookies() {
        List<String> cookieStrings = headers.get("Cookie");
        Map<String, String> cookies = new HashMap<>();
        if (cookieStrings != null)
            for (String cookie : cookieStrings) {
                String[] keyVal = cookie.split("=");
                cookies.put(keyVal[0], keyVal[1]);
            }
        return cookies;
    }

    public Body getBody() {
        return body;
    }

    public Headers getHeaders() {
        return this.headers;
    }
}
