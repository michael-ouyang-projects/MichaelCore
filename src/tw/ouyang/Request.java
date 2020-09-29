package tw.ouyang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Request {

    private String requestInfo;
    private String resourcePath;
    private String contentType;

    public Request(String requestInfo) throws IOException {
        if (requestInfo.trim().length() > 0) {
            this.requestInfo = requestInfo;
            this.resourcePath = fetchResourcePath(requestInfo);
            this.contentType = fetchContentType(resourcePath);
            System.out.println(resourcePath + ", " + contentType);
        }
    }

    private String fetchResourcePath(String requestInfo) {
        String resourcePath = requestInfo.split(" ")[1];
        return "/".equals(resourcePath) ? "/index.html" : resourcePath;
    }

    private String fetchContentType(String resourcePath) throws IOException {
        String contentType = Files.probeContentType(Paths.get("D:/var/www/" + resourcePath));
        if (contentType == null) {
            if (resourcePath.endsWith(".js")) {
                contentType = "application/javascript";
            }
        }
        return contentType;
    }

    public String getRequestInfo() {
        return requestInfo;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getContentType() {
        return contentType;
    }

}
