package demo.test;

import java.util.List;

import demo.user.model.User;
import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.data.enumeration.TransactionalIsolation;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
public class ThirdService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(isolation = TransactionalIsolation.SERIALIZABLE)
    public void testSerializable() throws Exception {
        jdbcTemplate.execute("UPDATE TT_USER SET AGE = '200' WHERE NAME = 'AAA'");
        List<User> users = jdbcTemplate.queryList("SELECT NAME, AGE FROM TT_USER", User.class);
        users.forEach(user -> {
            System.out.println(String.format("%s: %s, %s", "A", user.getName(), user.getAge()));
        });

        Thread.sleep(10000);
        System.out.println("--------------------------");

        jdbcTemplate.execute("UPDATE TT_USER SET AGE = '200' WHERE NAME = 'BBB'");
        List<User> usersb = jdbcTemplate.queryList("SELECT NAME, AGE FROM TT_USER", User.class);
        usersb.forEach(user -> {
            System.out.println(String.format("%s: %s, %s", "A", user.getName(), user.getAge()));
        });
    }

    @Transactional(isolation = TransactionalIsolation.READ_COMMITTED)
    public void testReadCommitted() throws Exception {
        jdbcTemplate.execute("UPDATE TT_USER SET AGE = '200' WHERE NAME = 'AAA'");
        List<User> users = jdbcTemplate.queryList("SELECT NAME, AGE FROM TT_USER", User.class);
        users.forEach(user -> {
            System.out.println(String.format("%s: %s, %s", "A", user.getName(), user.getAge()));
        });

        Thread.sleep(10000);
        System.out.println("--------------------------");

        jdbcTemplate.execute("UPDATE TT_USER SET AGE = '200' WHERE NAME = 'BBB'");
        List<User> usersb = jdbcTemplate.queryList("SELECT NAME, AGE FROM TT_USER", User.class);
        usersb.forEach(user -> {
            System.out.println(String.format("%s: %s, %s", "A", user.getName(), user.getAge()));
        });
    }

}
