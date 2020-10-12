package tw.framework.michaelcore.mvc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import tw.framework.michaelcore.core.annotation.Configuration;
import tw.framework.michaelcore.ioc.annotation.Bean;

@Configuration
public class MvcCore {

    @Bean
    public Map<String, Map<String, Method>> requestMapping() {
        Map<String, Map<String, Method>> requestMapping = new HashMap<>();
        requestMapping.put("GET", new HashMap<>());
        requestMapping.put("POST", new HashMap<>());
        requestMapping.put("PUT", new HashMap<>());
        requestMapping.put("DELETE", new HashMap<>());
        return requestMapping;
    }

}
