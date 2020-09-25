package tw.ouyang;

public class Request {

    private String requestHeader;
    private String requestPath;

    public Request(String requestInfo) {
        this.requestHeader = requestInfo;
        this.requestPath = requestInfo.split(" ")[1];
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public String getRequestPath() {
        return requestPath;
    }

}
