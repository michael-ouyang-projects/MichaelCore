package tw.framework.michaelcore.mvc;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.mvc.annotation.RequestParam;

@Component
public class RequestProcessor {

    @Value
    public String loggingPage;

    @Autowired
    public MvcCore mvcCore;

    private String webRoot = "resources";

    public RequestInfo getClientRequest(BufferedReader reader) throws IOException {
        StringBuilder requestHeader = new StringBuilder();
        StringBuilder requestBody = new StringBuilder();
        String line = reader.readLine();
        if (line != null) {
            String method = line.split(" ")[0];
            while (havingData(line)) {
                requestHeader.append(line + "\n");
                line = reader.readLine();
            }
            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
                while (reader.ready()) {
                    requestBody.append((char) reader.read());
                }
            }
        }
        return requestHeader.toString().length() > 0 ? new RequestInfo(requestHeader.toString(), requestBody.toString()) : null;
    }

    private boolean havingData(String line) {
        return line != null && line.length() > 0;
    }

    public void logRequest(RequestInfo request) {
        try (FileWriter writer = new FileWriter(loggingPage, true)) {
            writer.write(request.getRequestHeader() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void responseToClient(RequestInfo requestInfo, OutputStream outputStream) throws IOException {
        byte[] response = createResponse(requestInfo);
        if (response != null) {
            outputStream.write(response);
        }
    }

    private byte[] createResponse(RequestInfo requestInfo) {
        byte[] resource = getResource(requestInfo);
        if (resource != null) {
            byte[] info = getInfo(requestInfo, resource);
            return concatenateInfoAndResource(info, resource);
        }
        return null;
    }

    private byte[] getResource(RequestInfo request) {
        byte[] resource = null;
        try {
            String requestPath = request.getRequestPath();
            Map<String, String> requestParameters = null;
            if ("GET".equals(request.getRequestMethod()) && requestPath.contains("?")) {
                String[] pathAndParameters = requestPath.split("\\?");
                requestPath = pathAndParameters[0];
                requestParameters = getRequestParameters(pathAndParameters[1]);
            } else if ("POST".equals(request.getRequestMethod()) && request.getRequestHeader().contains("Content-Type: application/x-www-form-urlencoded")) {
                requestParameters = getRequestParameters(request.getRequestBody());
            }

            Map<String, Method> mapping = mvcCore.getRequestMapping().get(request.getRequestMethod());
            Method mappingMethod = mapping.get(requestPath);
            if (mappingMethod != null) {
                Object[] parameters = new Object[mappingMethod.getParameterCount()];
                for (int i = 0; i < mappingMethod.getParameterCount(); i++) {
                    if (mappingMethod.getParameters()[i].isAnnotationPresent(RequestParam.class)) {
                        String key = mappingMethod.getParameters()[i].getAnnotation(RequestParam.class).value();
                        String typeName = mappingMethod.getParameters()[i].getParameterizedType().getTypeName();
                        parameters[i] = requestParameters.get(key);
                        if ("int".equals(typeName)) {
                            parameters[i] = Integer.parseInt((String) parameters[i]);
                        }
                    }
                }
                Object controller = CoreContext.getBean(mappingMethod.getDeclaringClass().getName());
                Model model = (Model) mappingMethod.invoke(controller, parameters);
                resource = readAndProcessTemplate(model);
            } else {
                resource = Files.readAllBytes(Paths.get(webRoot, request.getRequestPath()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            resource = new String("Error while getting resource!").getBytes();
        }
        return resource;
    }

    private Map<String, String> getRequestParameters(String parametersString) {
        Map<String, String> requestParameters = null;
        if (parametersString.length() > 0) {
            requestParameters = new HashMap<>();
            for (String parameter : parametersString.split("&")) {
                String[] keyValue = parameter.split("=");
                requestParameters.put(keyValue[0], keyValue[1]);
            }
        }
        return requestParameters;
    }

    private byte[] readAndProcessTemplate(Model model) throws IOException {
        StringBuilder template = new StringBuilder();
        Files.readAllLines(Paths.get(webRoot, "templates", model.getTemplate())).forEach(line -> {
            if (line.contains("${")) {
                String[] variables = line.split("\\$\\{");
                for (int i = 1; i < variables.length; i++) {
                    String variable = variables[i].split("\\}")[0];
                    line = line.replaceAll(String.format("\\$\\{%s\\}", variable), String.valueOf(model.get(variable)));
                }
            }
            template.append(line);
        });
        return template.toString().getBytes();
    }

    private byte[] getInfo(RequestInfo request, byte[] resource) {
        String infoFormat = "HTTP/1.1 200 OK"
                + "Content-Length: %d"
                + "Content-Type: %s\r\n\r\n";
        // return String.format(infoFormat, resource.length, request.getContentType()).getBytes();
        return String.format(infoFormat, resource.length, "fe").getBytes();
    }

    private byte[] concatenateInfoAndResource(byte[] info, byte[] resource) {
        int responseSize = info.length + resource.length;
        return ByteBuffer.allocate(responseSize).put(info).put(resource).array();
    }

}
