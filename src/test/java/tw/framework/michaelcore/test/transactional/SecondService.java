package tw.framework.michaelcore.test.transactional;

import java.sql.SQLException;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.data.enumeration.TransactionalPropagation;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
public class SecondService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = TransactionalPropagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void subMethod(int i) throws Exception {

        String sqlString = String.format("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('%d', '%d')", i, i);
        jdbcTemplate.execute(sqlString);

        if (i % 2 == 0) {

            throw new SQLException("Failure");

        }

    }

}