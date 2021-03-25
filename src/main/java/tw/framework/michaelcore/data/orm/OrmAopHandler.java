package tw.framework.michaelcore.data.orm;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.AopHandler;

@AopHandler
public class OrmAopHandler {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static ThreadLocal<OrmData> ormData = new ThreadLocal<>();

    public Object after() {
        if (ormData.get().getMethod().isDefault()) {
            switch (ormData.get().getMethod().getName()) {
            case "queryAll":
                String table = ormData.get().getOrmRepository().table();
                Class<?> entityClazz = ormData.get().getOrmRepository().entity();
                return jdbcTemplate.queryObjectList("SELECT * FROM " + table, entityClazz);
            }
        }
        ormData.remove();
        return null;
    }

    public void attachNewOrmDataToThread(OrmData newOrmData) {
        ormData.set(newOrmData);
    }

}