package tw.framework.michaelcore.data.orm;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<Entity, ID> {

    public default Optional<Entity> queryById(ID id) {
        return null;
    }

    public default List<Entity> queryAll() {
        return null;
    }

    public default <EntityObject extends Entity> void save(EntityObject entity) {
    }

    public default void deleteById(ID id) {
    }

    public default void delete(Entity entity) {
    }

    public default void deleteAll() {
    }

}
