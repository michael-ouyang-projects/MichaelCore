package demo.user.repository;

import java.util.List;

import demo.user.model.User;
import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.data.annotation.Repository;
import tw.framework.michaelcore.ioc.annotation.Autowired;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> queryAll() {
        return jdbcTemplate.queryObjectList("SELECT NAME, AGE FROM TT_USER", User.class);
    }

    public void save(User user) {
        String sql = String.format("INSERT INTO TT_USER(NAME, AGE) VALUES('%s', '%s')", user.getName(), user.getAge());
        jdbcTemplate.execute(sql);
    }

    public void error() {
        jdbcTemplate.execute("INSERT INTO UNKNOWN_TABLE(NAME, AGE) VALUES('HI', 'HELLO')");
    }

}
