package tw.framework.michaelcore.mvc;

import java.io.IOException;

public class RequestInfo {

    private String requestHeader;
    private String requestBody;

    private String requestMethod;
    private String requestPath;

    public RequestInfo(String requestHeader, String requestBody) throws IOException {
        this.requestHeader = requestHeader;
        this.requestBody = requestBody;

        String[] firstHeaderData = requestHeader.split("\n")[0].split(" ");
        this.requestMethod = getRequestMethod(firstHeaderData);
        this.requestPath = getRequestPath(firstHeaderData);
    }

    private String getRequestMethod(String[] firstHeaderData) {
        return firstHeaderData[0].toUpperCase();
    }

    private String getRequestPath(String[] firstHeaderData) {
        return firstHeaderData[1];
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }



    // private String fetchContentType(String resourcePath) throws IOException {
    // String contentType = Files.probeContentType(Paths.get("D:/var/www/" + resourcePath));
    // if (contentType == null) {
    // if (resourcePath.endsWith(".js")) {
    // contentType = "application/javascript";
    // } else {
    // contentType = "text/plain";
    // }
    // }
    // return contentType;
    // }

}
