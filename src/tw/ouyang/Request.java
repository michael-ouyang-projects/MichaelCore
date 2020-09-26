package tw.ouyang;

public class Request {

    private String requestHeader;
    private String requestResourcePath;

    public Request(String requestInfo) {
        this.requestHeader = requestInfo;
        this.requestResourcePath = requestInfo.split(" ")[1];
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public String getRequestResourcePath() {
        return requestResourcePath;
    }

}
