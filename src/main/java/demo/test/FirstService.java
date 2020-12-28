package demo.test;

import tw.framework.michaelcore.data.JdbcTemplate;
import tw.framework.michaelcore.data.annotation.Transactional;
import tw.framework.michaelcore.data.enumeration.TransactionPropagation;
import tw.framework.michaelcore.ioc.annotation.Autowired;
import tw.framework.michaelcore.ioc.annotation.Service;

@Service
public class FirstService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SecondService secondService;

    @Transactional(propagation = TransactionPropagation.REQUIRED, rollbackFor = Exception.class)
    public void goMethod() throws Exception {

        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('start', 'start')");

        for (int i = 1; i <= 5; i++) {

            try {

                secondService.subMethod(i);

            } catch (Exception e) {

                System.err.println(e);

            }

        }

        jdbcTemplate.execute("INSERT INTO TB_MYTEST(MYKEY, MYVALUE) VALUES ('end', 'end')");

        // jdbcTemplate.execute("INSERT INTO UNKNOWN_TABLE(MYKEY, MYVALUE) VALUES ('end', 'end')");
    }

}