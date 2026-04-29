package me.jetby.libb.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Random;

@UtilityClass
public class Randomizer {
    private final Random r = new Random();

    public int rand(int bound) {
        return r.nextInt(bound);
    }

    public int randInteger(int min, int max) {
        return r.nextInt(max - min + 1) + min;
    }

    public double randDouble() {
        return r.nextDouble();
    }

    public double rand(double min, double max) {
        return min + (max - min) * r.nextDouble();
    }

    public boolean randBoolean() {
        return r.nextBoolean();
    }

    public <T> T rand(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(rand(list.size()));
    }

    @SafeVarargs
    public <T> T rand(T... items) {
        if (items == null || items.length == 0) return null;
        return items[rand(items.length)];
    }
}
