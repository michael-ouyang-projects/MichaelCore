package tw.ouyang;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tw.ouyang.annotation.Controller;
import tw.ouyang.annotation.Get;
import tw.ouyang.annotation.Post;

public class WebServer {

	private String webRootPath = "resources";
	private File logFile;
	
	public static void main(String[] args) {
		new WebServer().run();
	}

	public void run() {
		createNecessaryFile();
		initializeContainer();
		startServer(8080);
	}

	@SuppressWarnings("unchecked")
	private void initializeContainer() {
		Map<String, Method> getMapping = (Map<String, Method>) SingletonBeanFactory.addBean("getMapping", new HashMap<>());
		Map<String, Method> postMapping = (Map<String, Method>) SingletonBeanFactory.addBean("postMapping", new HashMap<>());

		try {
//			String applicationPath = this.getClass().getResource("/").getPath().substring(1);
			String applicationPath = this.getClass().getResource("/").getPath();
			Files.walk(Paths.get(applicationPath))
			.filter(Files::isRegularFile)
			.filter(classFile -> {
				if (classFile.getParent().toString().replaceAll("\\\\", "/").split(applicationPath).length == 2) {
					return classFile.getFileName().toString().endsWith(".class");
				} else {
					return false;
				}
			}).map(classFile -> {
				String packagePath = classFile.getParent().toString().replaceAll("\\\\", "/").split(applicationPath)[1].replace("/", ".");
				String className = classFile.getFileName().toString().split("\\.")[0];
				return String.format("%s.%s", packagePath, className);
			}).forEach(fqcn -> {
				try {
					Class<?> loopClass = Class.forName(fqcn);
					if (loopClass.isAnnotationPresent(Controller.class)) {
						SingletonBeanFactory.addBean(loopClass.getName(), loopClass.newInstance());
						for (Method method : loopClass.getMethods()) {
							if (method.isAnnotationPresent(Get.class)) {
								getMapping.put(method.getAnnotation(Get.class).value(), method);
							} else if (method.isAnnotationPresent(Post.class)) {
								postMapping.put(method.getAnnotation(Post.class).value(), method);
							}
						}
					}
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startServer(int port) {
		ExecutorService executor = Executors.newCachedThreadPool();
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				executor.submit(new RequestProcessor(serverSocket.accept(), webRootPath, logFile));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createNecessaryFile() {
		try {
			Path wwwPath = Paths.get(webRootPath, "www");
			Path logPath = Paths.get(webRootPath, "log");
			File defaultPage = new File(wwwPath.toString(), "default.html");
			logFile = new File(logPath.toString(), "httpd.log");
			createTemplatesDirectoryAndDefaultPage(wwwPath, defaultPage);
			createLogDirectoryAndLogPage(logPath, logFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createTemplatesDirectoryAndDefaultPage(Path wwwDirectory, File welcomePage) throws IOException {
		if (needToCreateDirectory(wwwDirectory)) {
			Files.createDirectories(wwwDirectory);
		}
		if (needToCreateFile(welcomePage)) {
			createWelcomeFile(welcomePage);
		}
	}
	
	private void createWelcomeFile(File welcomePage) {
		try (FileWriter writer = new FileWriter(welcomePage)) {
			writer.write("Web Server Implemented Using Java!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createLogDirectoryAndLogPage(Path logDirectory, File logPage) throws IOException {
		if (needToCreateDirectory(logDirectory)) {
			Files.createDirectories(logDirectory);
		}
		if (needToCreateFile(logPage)) {
			logPage.createNewFile();
		}
	}

	private boolean needToCreateDirectory(Path directory) {
		return !(Files.exists(directory) && Files.isDirectory(directory));
	}

	private boolean needToCreateFile(File file) {
		return !(file.exists() && file.isFile());
	}

}
