package tw.ouyang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Request {

    private String requestInfo;
    private String requestMethod;
    private String requestPath;
    private String contentType;

    public Request(String requestInfo) throws IOException {
        this.requestInfo = requestInfo;
        String[] requestInfoBlock = requestInfo.split(" ");
        this.requestMethod = fetchRequestMethod(requestInfoBlock);
        this.requestPath = fetchRequestPath(requestInfoBlock);
        this.contentType = fetchContentType(requestPath);
        System.out.println(String.format("%s, %s, %s", requestMethod, requestPath, contentType));
    }

    private String fetchRequestMethod(String[] requestInfoBlock) {
        return requestInfoBlock[0];
    }

    private String fetchRequestPath(String[] requestInfoBlock) {
        String resourcePath = requestInfoBlock[1];
        return "/".equals(resourcePath) ? "/index.html" : resourcePath;
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

    public String getRequestInfo() {
        return requestInfo;
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

}
