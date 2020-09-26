package tw.ouyang;

public class Request {

    private String requestInfo;
    private String requestResourcePath;

    public Request(String requestInfo) {
        if(requestInfo.trim().length() > 0) {
            this.requestInfo = requestInfo;
            this.requestResourcePath = requestInfo.split(" ")[1];
            if ("/".equals(requestResourcePath)) {
                requestResourcePath = "/index.html";
            }
            System.out.println(requestResourcePath);
        }
    }

    public String getRequestInfo() {
        return requestInfo;
    }

    public String getRequestResourcePath() {
        return requestResourcePath;
    }

}
