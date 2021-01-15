package tw.framework.michaelcore.test.data;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.data.enumeration.TransactionalPropagation;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
public class FirstService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SecondService secondService;

    @Transactional(propagation = TransactionalPropagation.REQUIRED, rollbackFor = Exception.class)
    public void testRequiredWithoutError() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('start', 'start')");
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('end', 'end')");
    }

    @Transactional(propagation = TransactionalPropagation.REQUIRED, rollbackFor = Exception.class)
    public void testRequiredWithError() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('start', 'start')");
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('end', 'end')");
        jdbcTemplate.execute("INSERT INTO ERROR(MYKEY, MYVALUE) VALUES ('error', 'error')");
    }

    @Transactional(propagation = TransactionalPropagation.REQUIRED, rollbackFor = Exception.class)
    public void testCaseRequired() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('start', 'start')");
        for (int i = 1; i <= 5; i++) {
            try {
                secondService.requiredMethod(i);
            } catch (Exception e) {
            }
        }
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('end', 'end')");
    }

    @Transactional(propagation = TransactionalPropagation.REQUIRED, rollbackFor = Exception.class)
    public void testCaseRequiresNewA() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('start', 'start')");
        for (int i = 1; i <= 5; i++) {
            try {
                secondService.requiresNewMethod(i);
            } catch (Exception e) {
            }
        }
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('end', 'end')");
    }

    @Transactional(propagation = TransactionalPropagation.REQUIRED, rollbackFor = Exception.class)
    public void testCaseRequiresNewB() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('start', 'start')");
        for (int i = 1; i <= 5; i++) {
            try {
                secondService.requiresNewMethod(i);
            } catch (Exception e) {
            }
        }
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('end', 'end')");
        jdbcTemplate.execute("INSERT INTO ERROR(MYKEY, MYVALUE) VALUES ('error', 'error')");
    }

    @Transactional(propagation = TransactionalPropagation.REQUIRED, rollbackFor = Exception.class)
    public void testCaseNestedA() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('start', 'start')");
        for (int i = 1; i <= 5; i++) {
            try {
                secondService.requiresNested(i);
            } catch (Exception e) {
            }
        }
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('end', 'end')");
    }

    @Transactional(propagation = TransactionalPropagation.REQUIRED, rollbackFor = Exception.class)
    public void testCaseNestedB() {
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('start', 'start')");
        for (int i = 1; i <= 5; i++) {
            try {
                secondService.requiresNested(i);
            } catch (Exception e) {
            }
        }
        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('end', 'end')");
        jdbcTemplate.execute("INSERT INTO ERROR(MYKEY, MYVALUE) VALUES ('error', 'error')");
    }

}