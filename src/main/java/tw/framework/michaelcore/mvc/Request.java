package tw.framework.michaelcore.mvc;

public class Request {

    private String requestMethod;
    private String requestPath;
    private String pathParametersString;
    private String requestProtocol;
    private StringBuilder requestHeader = new StringBuilder();
    private StringBuilder requestBody = new StringBuilder();
    private String responseContentType;

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getPathParametersString() {
        return pathParametersString;
    }

    public void setPathParametersString(String pathParametersString) {
        this.pathParametersString = pathParametersString;
    }

    public String getRequestProtocol() {
        return requestProtocol;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
    }

    public String getRequestHeader() {
        return requestHeader.toString();
    }

    public void appendToRequestHeader(String headerData) {
        this.requestHeader.append(headerData);
    }

    public String getRequestBody() {
        return requestBody.toString();
    }

    public void appendToRequestBody(char bodyData) {
        this.requestBody.append(bodyData);
    }

    public String getResponseContentType() {
        if (responseContentType == null) {
            return "text/plain";
        }
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

}
