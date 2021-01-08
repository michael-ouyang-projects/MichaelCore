package tw.framework.michaelcore.test.transactional;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Configuration;

@Configuration
public class TestTransactional {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FirstService firstService;

    @Autowired
    private ThirdService thirdService;

    // @ExecuteAfterContextStartup(order = 1)
    // public void deleteData() throws Exception {
    // jdbcTemplate.execute("delete from TB_MYTEST");
    // jdbcTemplate.execute("delete from TT_USER");
    // jdbcTemplate.execute("INSERT INTO TT_USER (NAME, AGE) VALUES ('AAA', '100')");
    // jdbcTemplate.execute("INSERT INTO TT_USER (NAME, AGE) VALUES ('BBB', '100')");
    // jdbcTemplate.execute("INSERT INTO TT_USER (NAME, AGE) VALUES ('CCC', '100')");
    // }
    //
    // @ExecuteAfterContextStartup(order = 2)
    // public void testTransactional1() throws Exception {
    // firstService.goMethod();
    // }
    //
    // @ExecuteAfterContextStartup(order = 3)
    // public void testTransactional2() throws Exception {
    // thirdService.testReadCommitted();
    // }

}
