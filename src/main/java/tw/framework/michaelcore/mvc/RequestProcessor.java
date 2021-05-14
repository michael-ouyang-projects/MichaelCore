package tw.framework.michaelcore.mvc;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.ioc.annotation.components.Component;
import tw.framework.michaelcore.ioc.annotation.components.Controller;
import tw.framework.michaelcore.mvc.annotation.RequestBody;
import tw.framework.michaelcore.mvc.annotation.RequestParam;

@Component
public class RequestProcessor {

    @Autowired
    private Gson gson;

    @Value("web.loggingPage")
    private String loggingPage;

    @Autowired
    private CoreContext coreContext;

    public Request getRequest(InputStream inputStream) throws IOException {
        Request request = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        if (containsData(line = reader.readLine())) {
            request = initRequest(line);
            while (containsData(line = reader.readLine())) {
                request.appendToRequestHeader(line + "\n");
            }
            while (reader.ready()) {
                request.appendToRequestBody((char) reader.read());
            }
        }
        return request;
    }

    private boolean containsData(String line) {
        return line != null && line.length() > 0;
    }

    private Request initRequest(String line) {
        Request request = new Request();
        String[] requestMethodAndPath = line.split(" ");
        request.setRequestMethod(requestMethodAndPath[0].toUpperCase());
        setRequestPathAndPathParametersString(requestMethodAndPath[1], request);
        request.setRequestProtocol(requestMethodAndPath[2]);
        return request;
    }

    private void setRequestPathAndPathParametersString(String requestPathAndPathParametersString, Request request) {
        if (requestPathAndPathParametersString.contains("?")) {
            String[] dataString = requestPathAndPathParametersString.split("\\?");
            request.setRequestPath(dataString[0]);
            request.setPathParametersString(dataString[1]);
        } else {
            request.setRequestPath(requestPathAndPathParametersString);
        }
    }

    public void writeLog(Request request) {
        try (FileWriter writer = new FileWriter(loggingPage, true)) {
            writer.write(String.format("%s %s %s\n", request.getRequestMethod(), request.getRequestPath(), request.getRequestProtocol()));
            writer.write(String.format("%s\n", request.getRequestHeader()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void responseToClient(Request request, OutputStream outputStream) throws IOException {
        byte[] resource = getResource(request);
        if (resource != null) {
            outputStream.write(createResponse(request, resource));
        }
    }

    private byte[] getResource(Request request) {
        try {
            Method mappingMethod = getMappingMethod(request);
            if (mappingMethod != null) {
                return getResourceThroughMappingMethod(request, mappingMethod);
            }
            return getStaticResource(request);
        } catch (Exception e) {
            e.printStackTrace();
            return new String("Error while getting resource!").getBytes();
        }
    }

    private Method getMappingMethod(Request request) {
        Map<String, Method> requestMapping = MvcCore.getRequestMapping().get(request.getRequestMethod());
        return requestMapping.get(request.getRequestPath());
    }

    private byte[] getResourceThroughMappingMethod(Request request, Method method) throws Exception {
        if (method.getDeclaringClass().isAnnotationPresent(Controller.class)) {
            return processControllerMethod(request, method);
        }
        return processRestControllerMethod(request, method);
    }

    private byte[] processControllerMethod(Request request, Method method) throws Exception {
        Object[] parameters = null;
        if ("GET".equals(request.getRequestMethod()) && request.getPathParametersString() != null) {
            parameters = getParametersObjectByParametersString(request.getPathParametersString(), method);
        } else {
            parameters = getParametersObjectByParametersString(request.getRequestBody(), method);
        }
        Model model = (Model) method.invoke(coreContext.getBean(method.getDeclaringClass().getName()), parameters);
        return readAndProcessTemplate(model);
    }

    private Object[] getParametersObjectByParametersString(String parameterString, Method method) {
        Object[] parameters = new Object[method.getParameterCount()];
        Map<String, String> parameterPairs = getParameterPairs(parameterString);
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (method.getParameters()[i].isAnnotationPresent(RequestParam.class)) {
                parameters[i] = parameterPairs.get(method.getParameters()[i].getAnnotation(RequestParam.class).value());
                dealWithParameterType(parameters, i, method.getParameters()[i].getParameterizedType().getTypeName());
            }
        }
        return parameters;
    }

    private Map<String, String> getParameterPairs(String parameterString) {
        Map<String, String> requestParameters = null;
        if (parameterString.trim().length() > 0) {
            requestParameters = new HashMap<>();
            for (String parameterEntry : parameterString.split("&")) {
                String[] keyValuePair = parameterEntry.split("=");
                requestParameters.put(keyValuePair[0], keyValuePair[1]);
            }
        }
        return requestParameters;
    }

    private void dealWithParameterType(Object[] parameters, int i, String type) {
        switch (type) {
        case "java.lang.String":
            break;
        case "int":
            parameters[i] = Integer.parseInt((String) parameters[i]);
            break;
        case "long":
            parameters[i] = Long.parseLong((String) parameters[i]);
            break;
        case "short":
            parameters[i] = Short.parseShort((String) parameters[i]);
            break;
        case "float":
            parameters[i] = Float.parseFloat((String) parameters[i]);
            break;
        case "double":
            parameters[i] = Double.parseDouble((String) parameters[i]);
            break;
        case "boolean":
            parameters[i] = Boolean.parseBoolean((String) parameters[i]);
            break;
        case "char":
            parameters[i] = ((String) parameters[i]).charAt(0);
            break;
        }
    }

    private byte[] readAndProcessTemplate(Model model) throws IOException {
        StringBuilder pageContent = new StringBuilder();
        Files.readAllLines(Paths.get("src/main/resources/templates/", model.getTemplate())).forEach(line -> {
            if (line.contains("${")) {
                String[] variables = line.split("\\$\\{");
                for (int i = 1; i < variables.length; i++) {
                    String variable = variables[i].split("\\}")[0];
                    line = line.replaceFirst(String.format("\\$\\{%s\\}", variable), String.valueOf(model.get(variable)));
                }
            }
            pageContent.append(line);
        });
        return pageContent.toString().getBytes();
    }

    private byte[] processRestControllerMethod(Request request, Method method) throws Exception {
        request.setResponseContentType("application/json");
        Object[] parameters = getParametersObjectByJson(request.getRequestBody(), method);
        Object resultObject = method.invoke(coreContext.getBean(method.getDeclaringClass().getName()), parameters);
        if (resultObject != null) {
            return gson.toJson(resultObject).getBytes();
        }
        return new String("Completed without error!").getBytes();
    }

    private Object[] getParametersObjectByJson(String json, Method method) {
        Object[] parameters = new Object[method.getParameterCount()];
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (method.getParameters()[i].isAnnotationPresent(RequestBody.class)) {
                Type type = method.getParameters()[i].getParameterizedType();
                parameters[i] = gson.fromJson(json, type);
            }
        }
        return parameters;
    }

    private byte[] getStaticResource(Request request) throws IOException {
        setContentType(request);
        return Files.readAllBytes(Paths.get("src/main/resources/static/", request.getRequestPath()));
    }

    private void setContentType(Request request) throws IOException {
        String contentType = Files.probeContentType(Paths.get("resources", request.getRequestPath()));
        if (contentType == null && request.getRequestPath().endsWith(".js")) {
            contentType = "application/javascript";
        }
        request.setResponseContentType(contentType);
    }

    private byte[] createResponse(Request request, byte[] resource) {
        byte[] responseHeader = createResponseHeader(resource.length, request.getResponseContentType());
        int dataSize = responseHeader.length + resource.length;
        return ByteBuffer.allocate(dataSize).put(responseHeader).put(resource).array();
    }

    private byte[] createResponseHeader(int resourceLength, String contentType) {
        String headerFormat = "HTTP/1.1 200 OK"
                + "Content-Length: %d"
                + "Content-Type: %s\r\n\r\n";
        return String.format(headerFormat, resourceLength, contentType).getBytes();
    }

}
