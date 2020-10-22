package demo.user;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.data.annotation.Repository;
import tw.framework.michaelcore.ioc.annotation.Autowired;

@Repository
public class UserRepository {

    @Autowired
    public JdbcTemplate jdbcTemplate;

    public void addUser(User user) {
        String sql = String.format("INSERT INTO TT_USER(NAME, AGE) VALUES('%s', '%s')", user.getName(), user.getAge());
        jdbcTemplate.execute(sql);
    }

}
