package tw.framework.michaelcore.test.data;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.data.enumeration.TransactionalPropagation;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.components.Service;

@Service
public class SecondService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = TransactionalPropagation.REQUIRED, rollbackFor = Exception.class)
    public void requiredMethod(int i) throws Exception {
        String sqlString = String.format("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('%d', '%d')", i, i);
        jdbcTemplate.execute(sqlString);
        if (i % 2 == 0) {
            jdbcTemplate.execute("INSERT INTO ERROR(MYKEY, MYVALUE) VALUES ('error', 'error')");
        }
    }

    @Transactional(propagation = TransactionalPropagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void requiresNewMethod(int i) throws Exception {
        String sqlString = String.format("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('%d', '%d')", i, i);
        jdbcTemplate.execute(sqlString);
        if (i % 2 == 0) {
            jdbcTemplate.execute("INSERT INTO ERROR(MYKEY, MYVALUE) VALUES ('error', 'error')");
        }
    }

    @Transactional(propagation = TransactionalPropagation.NESTED, rollbackFor = Exception.class)
    public void requiresNested(int i) throws Exception {
        String sqlString = String.format("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('%d', '%d')", i, i);
        jdbcTemplate.execute(sqlString);
        if (i % 2 == 0) {
            jdbcTemplate.execute("INSERT INTO ERROR(MYKEY, MYVALUE) VALUES ('error', 'error')");
        }
    }

}