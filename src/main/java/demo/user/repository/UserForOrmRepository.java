package demo.user.repository;

import java.util.List;

import demo.user.model.UserForOrm;
import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.data.orm.CrudRepository;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.OrmRepository;

@OrmRepository(table = "TT_USER", entity = UserForOrm.class, id = String.class)
public class UserForOrmRepository implements CrudRepository<UserForOrm, String> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<UserForOrm> queryAgeUnder(int age) {
        return jdbcTemplate.queryObjectList("SELECT * FROM TT_USER WHERE AGE < " + age, UserForOrm.class);
    }

}
