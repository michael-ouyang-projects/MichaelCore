package tw.framework.michaelcore.test.data;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.ioc.CoreContext;
import tw.framework.michaelcore.test.utils.MichaelcoreExtension;

@ExtendWith(MichaelcoreExtension.class)
public class TestPropagation {

    private static CoreContext coreContext;
    private JdbcTemplate jdbcTemplate = (JdbcTemplate) coreContext.getBean(JdbcTemplate.class.getName());
    private FirstService firstService = (FirstService) coreContext.getBean(FirstService.class.getName());

    @BeforeAll
    public static void beforeAll() {
        coreContext = MichaelcoreExtension.getCoreContext();
    }

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.execute("DELETE FROM TB_MYTEST");
    }

    @Test
    public void testRequiredWithoutError() throws SQLException {
        firstService.testRequiredWithoutError();
        List<MyTest> dataList = jdbcTemplate.queryObjectList("SELECT MYKEY, MYVALUE FROM TB_MYTEST ORDER BY MYKEY DESC", MyTest.class);
        Assertions.assertEquals(2, dataList.size());
        Assertions.assertEquals("start", dataList.get(0).getMyKey());
        Assertions.assertEquals("start", dataList.get(0).getMyValue());
        Assertions.assertEquals("end", dataList.get(1).getMyKey());
        Assertions.assertEquals("end", dataList.get(1).getMyValue());
    }

    @Test
    public void testRequiredWithError() {
        firstService.testRequiredWithError();
        BigDecimal rowSize = jdbcTemplate.queryValue("SELECT COUNT(*) FROM TB_MYTEST", BigDecimal.class);
        Assertions.assertEquals(0, rowSize.intValue());
    }

    @Test
    public void testCaseRequired() {
        firstService.testCaseRequired();
        BigDecimal rowSize = jdbcTemplate.queryValue("SELECT COUNT(*) FROM TB_MYTEST", BigDecimal.class);
        Assertions.assertEquals(0, rowSize.intValue());
    }

    @Test
    public void testCaseRequiresNewA() {
        firstService.testCaseRequiresNewA();
        List<MyTest> dataList = jdbcTemplate.queryObjectList("SELECT MYKEY, MYVALUE FROM TB_MYTEST ORDER BY MYKEY DESC", MyTest.class);
        Assertions.assertEquals(5, dataList.size());
        Assertions.assertEquals("start", dataList.get(0).getMyKey());
        Assertions.assertEquals("start", dataList.get(0).getMyValue());
        Assertions.assertEquals("end", dataList.get(1).getMyKey());
        Assertions.assertEquals("end", dataList.get(1).getMyValue());
        Assertions.assertEquals("5", dataList.get(2).getMyKey());
        Assertions.assertEquals("5", dataList.get(2).getMyValue());
        Assertions.assertEquals("3", dataList.get(3).getMyKey());
        Assertions.assertEquals("3", dataList.get(3).getMyValue());
        Assertions.assertEquals("1", dataList.get(4).getMyKey());
        Assertions.assertEquals("1", dataList.get(4).getMyValue());
    }

    @Test
    public void testCaseRequiresNewB() {
        firstService.testCaseRequiresNewB();
        List<MyTest> dataList = jdbcTemplate.queryObjectList("SELECT MYKEY, MYVALUE FROM TB_MYTEST ORDER BY MYKEY ASC", MyTest.class);
        Assertions.assertEquals(3, dataList.size());
        Assertions.assertEquals("1", dataList.get(0).getMyKey());
        Assertions.assertEquals("1", dataList.get(0).getMyValue());
        Assertions.assertEquals("3", dataList.get(1).getMyKey());
        Assertions.assertEquals("3", dataList.get(1).getMyValue());
        Assertions.assertEquals("5", dataList.get(2).getMyKey());
        Assertions.assertEquals("5", dataList.get(2).getMyValue());
    }

    @Test
    public void testCaseNestedA() {
        firstService.testCaseNestedA();
        List<MyTest> dataList = jdbcTemplate.queryObjectList("SELECT MYKEY, MYVALUE FROM TB_MYTEST ORDER BY MYKEY DESC", MyTest.class);
        Assertions.assertEquals(5, dataList.size());
        Assertions.assertEquals("start", dataList.get(0).getMyKey());
        Assertions.assertEquals("start", dataList.get(0).getMyValue());
        Assertions.assertEquals("end", dataList.get(1).getMyKey());
        Assertions.assertEquals("end", dataList.get(1).getMyValue());
        Assertions.assertEquals("5", dataList.get(2).getMyKey());
        Assertions.assertEquals("5", dataList.get(2).getMyValue());
        Assertions.assertEquals("3", dataList.get(3).getMyKey());
        Assertions.assertEquals("3", dataList.get(3).getMyValue());
        Assertions.assertEquals("1", dataList.get(4).getMyKey());
        Assertions.assertEquals("1", dataList.get(4).getMyValue());
    }

    @Test
    public void testCaseNestedB() {
        firstService.testCaseNestedB();
        BigDecimal rowSize = jdbcTemplate.queryValue("SELECT COUNT(*) FROM TB_MYTEST", BigDecimal.class);
        Assertions.assertEquals(0, rowSize.intValue());
    }

}
