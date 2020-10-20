package tw.framework.michaelcore.mvc;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Value;

@Component
public class RequestProcessor {

    @Value
    public String loggingPage;

    @Autowired
    public MvcCore mvcCore;

    private String webRoot = "resources";

    public RequestInfo getClientRequest(BufferedReader reader) throws IOException {
        StringBuilder requestHeader = new StringBuilder();
        String line = reader.readLine();
        if (line != null) {
            String method = line.split(" ")[0];
            while (havingData(line)) {
                requestHeader.append(line + "\n");
                line = reader.readLine();
            }
            if ("POST".equals(method)) {
                while (reader.ready()) {
                    requestHeader.append((char) reader.read());
                }
            }
        }
        return requestHeader.toString().length() > 0 ? new RequestInfo(requestHeader.toString()) : null;
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

    public void responseToClient(RequestInfo request, OutputStream outputStream) throws IOException {
        byte[] response = createResponse(request);
        if (response != null) {
            outputStream.write(response);
        }
    }

    private byte[] createResponse(RequestInfo request) {
        byte[] resource = getResource(request);
        if (resource != null) {
            byte[] info = getInfo(request, resource);
            return concatenateInfoAndResource(info, resource);
        }
        return null;
    }

    private byte[] getResource(RequestInfo request) {
        byte[] resource = null;
        try {
            Map<String, Map<String, Method>> requestMapping = mvcCore.getRequestMapping();
            Map<String, Method> mapping = requestMapping.get(request.getRequestMethod());
            Method mappingMethod = mapping.get(request.getRequestPath());
            if (mappingMethod != null) {
                Object returningObject = null;
                Object clazz = CoreContext.getBean(mappingMethod.getDeclaringClass().getName());
                if (Proxy.isProxyClass(clazz.getClass())) {
                    returningObject = Proxy.getInvocationHandler(clazz).invoke(clazz, mappingMethod, new Object[] {request.getRequestParameters() });
                } else {
                    returningObject = mappingMethod.invoke(clazz, request.getRequestParameters());
                }
                resource = readAndProcessTemplate((String) returningObject, request.getRequestParameters());
            } else {
                resource = Files.readAllBytes(Paths.get(webRoot, request.getRequestPath()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            resource = new String("Error while getting resource!").getBytes();
        }
        return resource;
    }

    private byte[] readAndProcessTemplate(String templateFileName, Map<String, String> model) throws IOException {
        StringBuilder template = new StringBuilder();
        Files.readAllLines(Paths.get(webRoot, "templates", templateFileName)).forEach(line -> {
            if (line.contains("${")) {
                String[] variables = line.split("\\$\\{");
                for (int i = 1; i < variables.length; i++) {
                    String variable = variables[i].split("\\}")[0];
                    line = line.replaceAll(String.format("\\$\\{%s\\}", variable), model.get(variable));
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
        return String.format(infoFormat, resource.length, request.getContentType()).getBytes();
    }

    private byte[] concatenateInfoAndResource(byte[] info, byte[] resource) {
        int responseSize = info.length + resource.length;
        return ByteBuffer.allocate(responseSize).put(info).put(resource).array();
    }

}
