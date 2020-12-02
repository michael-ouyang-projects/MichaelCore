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
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Value;
import tw.framework.michaelcore.mvc.annotation.Controller;
import tw.framework.michaelcore.mvc.annotation.RequestBody;
import tw.framework.michaelcore.mvc.annotation.RequestParam;
import tw.framework.michaelcore.mvc.annotation.RestController;

@Component
public class RequestProcessor {

    @Autowired
    private Gson gson;

    @Value
    private String loggingPage;

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
        request.setRequestPath(requestMethodAndPath[1]);
        request.setRequestProtocol(requestMethodAndPath[2]);
        return request;
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
        byte[] response = createResponse(request);
        if (response != null) {
            outputStream.write(response);
        }
    }

    private byte[] createResponse(Request requestInfo) {
        byte[] resource = getResource(requestInfo);
        if (resource != null) {
            byte[] info = getInfo(requestInfo, resource);
            return concatenateInfoAndResource(info, resource);
        }
        return null;
    }

    private byte[] getResource(Request requestInfo) {
        byte[] resource = null;
        try {
            String requestMethod = requestInfo.getRequestMethod();
            String requestPath = requestInfo.getRequestPath();
            Map<String, Method> requestMapping = MvcCore.getRequestMapping().get(requestMethod);
            Method mappingMethod = requestMapping.get(requestPath.split("\\?")[0]);

            if (mappingMethod != null) {
                Object[] invokeParameters = new Object[mappingMethod.getParameterCount()];
                if ("GET".equals(requestMethod)) {
                    if (requestPath.contains("?")) {
                        parameterStringToInvokeParameters(requestPath.split("\\?")[1], mappingMethod, invokeParameters);
                    }
                    Class<?> mappingClass = mappingMethod.getDeclaringClass();
                    if (mappingClass.isAnnotationPresent(Controller.class)) {
                        Object controller = CoreContext.getBean(mappingMethod.getDeclaringClass().getName());
                        Model model = (Model) mappingMethod.invoke(controller, invokeParameters);
                        resource = readAndProcessTemplate(model);
                    } else if (mappingClass.isAnnotationPresent(RestController.class)) {
                        jsonToInvokeParameters(requestInfo.getRequestBody(), mappingMethod, invokeParameters);
                        Object controller = CoreContext.getBean(mappingMethod.getDeclaringClass().getName());
                        Object resultObject = mappingMethod.invoke(controller, invokeParameters);
                        if (resultObject != null) {
                            resource = gson.toJson(resultObject).getBytes();
                        } else {
                            resource = new String("Completed without error!").getBytes();
                        }
                    }
                } else {
                    Class<?> mappingClass = mappingMethod.getDeclaringClass();
                    if (mappingClass.isAnnotationPresent(Controller.class)) {
                        parameterStringToInvokeParameters(requestInfo.getRequestBody(), mappingMethod, invokeParameters);
                        Object controller = CoreContext.getBean(mappingMethod.getDeclaringClass().getName());
                        Model model = (Model) mappingMethod.invoke(controller, invokeParameters);
                        resource = readAndProcessTemplate(model);
                    } else if (mappingClass.isAnnotationPresent(RestController.class)) {
                        jsonToInvokeParameters(requestInfo.getRequestBody(), mappingMethod, invokeParameters);
                        Object controller = CoreContext.getBean(mappingMethod.getDeclaringClass().getName());
                        Object resultObject = mappingMethod.invoke(controller, invokeParameters);
                        if (resultObject != null) {
                            resource = gson.toJson(resultObject).getBytes();
                        } else {
                            resource = new String("Completed without error!").getBytes();
                        }
                    }
                }
            } else {
                resource = Files.readAllBytes(Paths.get("resources", requestPath));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            resource = new String("Error while getting resource!").getBytes();
        }
        return resource;
    }

    private void parameterStringToInvokeParameters(String parameterString, Method mappingMethod, Object[] invokeParameters) {
        Map<String, String> requestParameters = getRequestParameters(parameterString);
        for (int i = 0; i < mappingMethod.getParameterCount(); i++) {
            if (mappingMethod.getParameters()[i].isAnnotationPresent(RequestParam.class)) {
                String name = mappingMethod.getParameters()[i].getAnnotation(RequestParam.class).value();
                invokeParameters[i] = requestParameters.get(name);
                String type = mappingMethod.getParameters()[i].getParameterizedType().getTypeName();
                if ("int".equals(type)) {
                    invokeParameters[i] = Integer.parseInt((String) invokeParameters[i]);
                }
            }
        }
    }

    private void jsonToInvokeParameters(String json, Method mappingMethod, Object[] invokeParameters) {
        for (int i = 0; i < mappingMethod.getParameterCount(); i++) {
            if (mappingMethod.getParameters()[i].isAnnotationPresent(RequestBody.class)) {
                Type type = mappingMethod.getParameters()[i].getParameterizedType();
                invokeParameters[i] = gson.fromJson(json, type);
            }
        }
    }

    private Map<String, String> getRequestParameters(String parameterString) {
        Map<String, String> requestParameters = null;
        if (parameterString.trim().length() > 0) {
            requestParameters = new HashMap<>();
            for (String parameter : parameterString.split("&")) {
                String[] keyValue = parameter.split("=");
                requestParameters.put(keyValue[0], keyValue[1]);
            }
        }
        return requestParameters;
    }

    private byte[] readAndProcessTemplate(Model model) throws IOException {
        StringBuilder template = new StringBuilder();
        Files.readAllLines(Paths.get("resources", "templates", model.getTemplate())).forEach(line -> {
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

    private byte[] getInfo(Request request, byte[] resource) {
        String infoFormat = "HTTP/1.1 200 OK"
                + "Content-Length: %d"
                + "Content-Type: %s\r\n\r\n";
        return String.format(infoFormat, resource.length, "fe").getBytes();
    }

    private byte[] concatenateInfoAndResource(byte[] info, byte[] resource) {
        int responseSize = info.length + resource.length;
        return ByteBuffer.allocate(responseSize).put(info).put(resource).array();
    }

}
