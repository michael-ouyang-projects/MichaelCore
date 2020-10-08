package tw.framework.michaelcore.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private String requestHeader;
    private String requestMethod;
    private String requestPath;
    private String contentType;
    private Map<String, String> requestParameters;

    public Request(String requestHeader) throws IOException {
        this.requestHeader = requestHeader;
        String[] requestInfoBlock = requestHeader.split(" ");
        this.requestMethod = fetchRequestMethod(requestInfoBlock);
        this.requestPath = fetchRequestPath(requestInfoBlock);
        this.contentType = fetchContentType(requestPath);
//        System.out.println(String.format("%s, %s, %s, %s", requestMethod, requestPath, requestParameters, contentType));
    }

    private String fetchRequestMethod(String[] requestInfoBlock) {
        return requestInfoBlock[0].toUpperCase();
    }

    private String fetchRequestPath(String[] requestInfoBlock) {
        String resourcePath = requestInfoBlock[1];
        if (resourcePath.contains("?")) {
            String[] resourcePathBlock = resourcePath.split("\\?");
            resourcePath = resourcePathBlock[0];
            getRequestParameters(resourcePathBlock[1].trim());
        } else if ("POST".equalsIgnoreCase(requestMethod)) {
            getRequestParameters(requestHeader.substring(requestHeader.lastIndexOf("\n") + 1));
        }
        return resourcePath;
    }

    private void getRequestParameters(String parametersString) {
        if (parametersString.length() > 0) {
            requestParameters = new HashMap<>();
            for (String parameter : parametersString.split("&")) {
                String[] keyValue = parameter.split("=");
                requestParameters.put(keyValue[0], keyValue[1]);
            }
        }
    }

    private String fetchContentType(String resourcePath) throws IOException {
        String contentType = Files.probeContentType(Paths.get("D:/var/www/" + resourcePath));
        if (contentType == null) {
            if (resourcePath.endsWith(".js")) {
                contentType = "application/javascript";
            } else {
                contentType = "text/plain";
            }
        }
        return contentType;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String> getRequestParameters() {
        return requestParameters;
    }

}
