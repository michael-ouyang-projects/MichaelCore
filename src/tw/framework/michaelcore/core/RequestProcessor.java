package tw.framework.michaelcore.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import tw.framework.michaelcore.ioc.SingletonBeanFactory;

public class RequestProcessor implements Runnable {

    private Socket socket;
    private String webRoot;
    private File logFile;

    public RequestProcessor(Socket socket, String webRoot, File logFile) {
        this.socket = socket;
        this.webRoot = webRoot;
        this.logFile = logFile;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream outputStream = socket.getOutputStream()) {
            Request request = getClientRequest(reader);
            if (request != null) {
                logRequest(request);
                responseToClient(request, outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Request getClientRequest(BufferedReader reader) throws IOException {
        StringBuilder requestInfo = new StringBuilder();
        String line = reader.readLine();
        if(line != null) {
            if (line.startsWith("GET")) {
                while (line != null && line.length() > 0) {
                    requestInfo.append(line + "\n");
                    line = reader.readLine();
                }
            } else if (line.startsWith("POST")) {
                while (line != null && line.length() > 0) {
                    requestInfo.append(line + "\n");
                    line = reader.readLine();
                }
                while (reader.ready()) {
                    requestInfo.append((char) reader.read());
                }
            }
        }
        return requestInfo.toString().length() > 0 ? new Request(requestInfo.toString()) : null;
    }

    private void logRequest(Request request) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(request.getRequestHeader() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void responseToClient(Request request, OutputStream outputStream) throws IOException {
        byte[] response = createResponse(request);
        if (response != null) {
            outputStream.write(response);
        }
    }

    private byte[] createResponse(Request request) {
        byte[] resource = getResource(request);
        if (resource != null) {
            byte[] info = getInfo(request, resource);
            return concatenateInfoAndResource(info, resource);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private byte[] getResource(Request request) {
        byte[] resource = null;
        try {
            Map<String, Map<String, Method>> requestMapping = (Map<String, Map<String, Method>>) SingletonBeanFactory.getBean("requestMapping");
            Map<String, Method> mapping = requestMapping.get(request.getRequestMethod());
            Method mappingMethod = mapping.get(request.getRequestPath());
            if (mappingMethod != null) {
                Object returningObject = mappingMethod.invoke(SingletonBeanFactory.getBean(mappingMethod.getDeclaringClass().getName()), request.getRequestParameters());
                resource = readAndProcessTemplate((String) returningObject, request.getRequestParameters());
            } else {
                resource = Files.readAllBytes(Paths.get(webRoot, request.getRequestPath()));
            }
        } catch (Exception e) {
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

    private byte[] getInfo(Request request, byte[] resource) {
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
