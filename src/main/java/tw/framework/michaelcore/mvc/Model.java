package tw.framework.michaelcore.mvc;

import java.util.HashMap;
import java.util.Map;

public class Model {

    private String template;
    private Map<String, Object> model = new HashMap<>();

    public Model(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    public void add(String key, Object value) {
        model.put(key, value);
    }

    public Object get(String key) {
        return model.get(key);
    }

}
