package tw.framework.michaelcore.data.orm;

import java.lang.reflect.Method;

import tw.framework.michaelcore.ioc.annotation.components.OrmRepository;

public class OrmData {

    private Method method;
    private OrmRepository ormRepository;

    public OrmData(Method method, OrmRepository ormRepository) {
        this.method = method;
        this.ormRepository = ormRepository;
    }

    public Method getMethod() {
        return method;
    }

    public OrmRepository getOrmRepository() {
        return ormRepository;
    }

}
