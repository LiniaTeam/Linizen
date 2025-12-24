package org.linia.linizen.utils;

import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.*;

public class WeightedRandomList<E> {

    private final Map<E, Integer> weights;
    private int sumWeights = 0;

    public WeightedRandomList() {
        weights = new HashMap<>();
    }

    public void add(E e, int weight) {
        weights.put(e, weight);
        sumWeights += weight;
    }

    public void add(E e) {
        add(e, 1);
    }

    public E getRandom() {
        double r = CoreUtilities.getRandom().nextDouble() * this.sumWeights;
        int c = 0;
        for (Map.Entry<E, Integer> e : this.weights.entrySet()) {
            c += e.getValue();
            if (c >= r) {
                return e.getKey();
            }
        }
        return null;
    }

    public List<E> getRandom(int n) {
        ArrayList<E> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(getRandom());
        }
        return list;
    }

    public Set<E> getKeys() {
        return weights.keySet();
    }
}
