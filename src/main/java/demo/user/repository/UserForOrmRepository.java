package demo.user.repository;

import demo.user.model.UserForOrm;
import tw.framework.michaelcore.data.orm.CrudRepository;
import tw.framework.michaelcore.ioc.annotation.components.OrmRepository;

@OrmRepository(table = "TT_USER", entity = UserForOrm.class, id = String.class)
public class UserForOrmRepository implements CrudRepository<UserForOrm, String> {

}
