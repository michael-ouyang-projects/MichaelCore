package demo.test;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Configuration;
import tw.framework.michaelcore.ioc.annotation.ExecuteAfterContextStartup;

@Configuration
public class TestTransactional {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private FirstService firstService;

    @ExecuteAfterContextStartup(order = 1)
    public void deleteData() throws Exception {
        jdbcTemplate.execute("delete from TB_MYTEST");
    }
    
    @ExecuteAfterContextStartup(order = 2)
    public void testTransactional() throws Exception {
        firstService.goMethod();
    }

}
