package tw.framework.michaelcore.test.data;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.ioc.Core;
import tw.framework.michaelcore.ioc.CoreContext;

public class TestJdbcTemplate {

    private final JdbcTemplate jdbcTemplate = CoreContext.getBean(JdbcTemplate.class);

    @BeforeAll
    public static void beforeAll() {
        Core.start();
    }

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.execute("DELETE FROM TB_MYTEST");
    }

    @Test
    public void testQueryValue() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testQueryValueA', 'testQueryValueB')");
        String myKey = jdbcTemplate.queryValue("SELECT MYKEY FROM TB_MYTEST", String.class);
        String myValue = jdbcTemplate.queryValue("SELECT MYVALUE FROM TB_MYTEST", String.class);
        BigDecimal count = jdbcTemplate.queryValue("SELECT COUNT(*) FROM TB_MYTEST", BigDecimal.class);
        Assertions.assertEquals("testQueryValueA", myKey);
        Assertions.assertEquals("testQueryValueB", myValue);
        Assertions.assertEquals(1, count.intValue());
    }

    @Test
    public void testQueryValueList() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testQueryValueA', 'testQueryValueB')");
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testQueryValueC', 'testQueryValueD')");
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testQueryValueE', 'testQueryValueF')");
        List<String> myKeys = jdbcTemplate.queryValueList("SELECT MYKEY FROM TB_MYTEST ORDER BY MYKEY", String.class);
        List<String> myValues = jdbcTemplate.queryValueList("SELECT MYVALUE FROM TB_MYTEST ORDER BY MYVALUE", String.class);
        Assertions.assertEquals("testQueryValueA", myKeys.get(0));
        Assertions.assertEquals("testQueryValueC", myKeys.get(1));
        Assertions.assertEquals("testQueryValueE", myKeys.get(2));
        Assertions.assertEquals("testQueryValueB", myValues.get(0));
        Assertions.assertEquals("testQueryValueD", myValues.get(1));
        Assertions.assertEquals("testQueryValueF", myValues.get(2));
    }

    @Test
    public void testQueryObject() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testQueryObjectA', 'testQueryObjectB')");
        MyTest myTest = jdbcTemplate.queryObject("SELECT MYKEY, MYVALUE FROM TB_MYTEST", MyTest.class);
        Assertions.assertEquals("testQueryObjectA", myTest.getMyKey());
        Assertions.assertEquals("testQueryObjectB", myTest.getMyValue());
    }

    @Test
    public void testQueryObjectList() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testQueryValueA', 'testQueryValueB')");
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testQueryValueC', 'testQueryValueD')");
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testQueryValueE', 'testQueryValueF')");
        List<MyTest> myTests = jdbcTemplate.queryObjectList("SELECT MYKEY, MYVALUE FROM TB_MYTEST ORDER BY MYKEY, MYVALUE", MyTest.class);
        Assertions.assertEquals("testQueryValueA", myTests.get(0).getMyKey());
        Assertions.assertEquals("testQueryValueC", myTests.get(1).getMyKey());
        Assertions.assertEquals("testQueryValueE", myTests.get(2).getMyKey());
        Assertions.assertEquals("testQueryValueB", myTests.get(0).getMyValue());
        Assertions.assertEquals("testQueryValueD", myTests.get(1).getMyValue());
        Assertions.assertEquals("testQueryValueF", myTests.get(2).getMyValue());
    }

    @Test
    public void testExecute() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('testExecute', 'testExecute')");
        BigDecimal countAfterInsert = jdbcTemplate.queryValue("SELECT COUNT(*) FROM TB_MYTEST", BigDecimal.class);
        Assertions.assertEquals(1, countAfterInsert.intValue());

        jdbcTemplate.execute("DELETE FROM TB_MYTEST");
        BigDecimal countAfterDelete = jdbcTemplate.queryValue("SELECT COUNT(*) FROM TB_MYTEST", BigDecimal.class);
        Assertions.assertEquals(0, countAfterDelete.intValue());
    }

}
