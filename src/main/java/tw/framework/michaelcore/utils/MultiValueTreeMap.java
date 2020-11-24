package tw.framework.michaelcore.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class MultiValueTreeMap<K, V> {

    private TreeMap<K, List<V>> map = new TreeMap<>();

    public void put(K key, V value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            map.put(key, new LinkedList<>(Arrays.asList(value)));
        }
    }

    public List<V> getAllByOrder() {
        List<V> returninglist = new LinkedList<>();
        map.forEach((key, value) -> {
            value.forEach(v -> {
                returninglist.add(v);
            });
        });
        return returninglist;
    }

}
