package ru.ifmo.rain.vozleev;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Judge {
    private static int SIZE = 100_000;

    private static List<Integer> generate(int size) {
        List<Integer> res = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            int curValue = (int) (Math.random() * 10000);
            res.add(curValue);

        }
        return res;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Invalid count of arguments");
            return;
        }
        // exceptions here NumberFormat
        int count = Integer.parseInt(args[0]);
        int tries = Integer.parseInt(args[1]);
        List<Cockroach> cockroaches = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            cockroaches.add(new Cockroach());
        }
        List<Integer> scores = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            scores.add(0);
        }

        for(int t = 0; t < tries; t++) {
            final List<Thread> threads = new ArrayList<>();
            final List<Integer> integers = generate(SIZE);
            List<Integer> positions = new ArrayList<>();
            for(int i = 0; i < count; i++) {
                int finalI = i;
                threads.add(new Thread(() -> {
                    cockroaches.get(finalI).run(integers);
                    positions.add(finalI);
                }));
            }
            for(int i = 0; i < count; i++) {
                threads.get(i).start();
            }
            for(int i = 0; i < count; i++) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (positions.size() != count) {
                System.err.println("Pizdec");
                return;
            }
            for(int i = 0; i < count; i++) {
                scores.set(i, scores.get(i) + count - i);
                System.out.println("Place is " + i);
                System.out.println("Score is " + scores.get(i));
            }
        }
    }
}
