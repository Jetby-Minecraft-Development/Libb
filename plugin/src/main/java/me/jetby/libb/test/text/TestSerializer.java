package me.jetby.libb.test.text;

import me.jetby.libb.color.Serializer;

import java.util.ArrayList;
import java.util.List;

public class TestSerializer {

    public static void main(String[] args) {
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();
        test();

    }

    private static void test() {
        List<String> strings = new ArrayList<>() ;

        for (int i = 0; i < 1_000_000; i++) {
            strings.add("<red>&a&lTEST: " + i);
        }
        Serializer.MINIMAL.cacheAll(strings);

        for (String s : strings) Serializer.MINIMAL.deserialize(s);

        long start = System.nanoTime();

        for (int i = 0; i < 1_000_000; i++) {
            Serializer.MINIMAL.deserialize(strings.get(i));
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("1M cache hits: " + ms + " ms");
        System.out.println("avg: " + (ms / 1_000_000.0) + " ms/вызов");
    }
}
