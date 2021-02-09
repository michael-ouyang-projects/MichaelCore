package tw.framework.michaelcore.data.orm;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.InvocationHandler;
import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.AopHandler;
import tw.framework.michaelcore.ioc.annotation.components.OrmRepository;

@AopHandler
public class OrmAopHandler implements InvocationHandler {

    @Autowired
    private CoreContext coreContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            switch (method.getName()) {
            case "queryAll":
                Class<?> repositoryClazz = Class.forName(proxy.getClass().getName().split("\\$\\$EnhancerByCGLIB\\$\\$")[0]);
                String table = repositoryClazz.getAnnotation(OrmRepository.class).table();
                Class<?> entityClazz = repositoryClazz.getAnnotation(OrmRepository.class).entity();
                return jdbcTemplate.queryObjectList("SELECT * FROM " + table, entityClazz);
            }
        }
        return method.invoke(coreContext.getRealBean(proxy), args);
    }

}