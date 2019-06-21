package ru.ifmo.rain.vozleev;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Judge {
    private static int SIZE = 100_000;

    private static List<Integer> generate(int size) {
        List<Integer> res = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            int curValue = (int) (Math.random() * 1000000);
            res.add(curValue);

        }
        return res;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Invalid count of arguments");
            return;
        }
        int count;
        try {
            count = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Incorrect format of number!");
            return;
        }
        int tries;
        try {
            tries = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Incorrect format of number!");
            return;
        }
        List<Cockroach> cockroaches = new ArrayList<>(Collections.nCopies(count, new Cockroach()));
        List<Integer> scores = new ArrayList<>(Collections.nCopies(SIZE, 0));
        for(int t = 0; t < tries; t++) {
            System.out.println("Round: " + (t + 1));
            System.out.println("---------------------------------------");
            final List<Thread> threads = new ArrayList<>();
            final List<Integer> integers = generate(SIZE);
            List<Integer> positions = new ArrayList<>();
            for(int i = 0; i < count; i++) {
                int finalI = i;
                threads.add(new Thread(() -> {
                    cockroaches.get(finalI).run(integers);
                    synchronized (positions) {
                        positions.add(finalI);
                    }
                }));
            }
            for(int i = 0; i < count; i++) {
                threads.get(i).start();
            }
            for(int i = 0; i < count; i++) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    System.err.println("Can't join a thread!");
                }
            }
            if (positions.size() != count) {
                System.err.println("Error, ups!");
                return;
            }
            for(int i = 0; i < count; i++) {
                int number = positions.get(i);
                scores.set(number, scores.get(number) + count - i);
                System.out.println("Place is " + (i + 1) + ". Number of cockroach is " + (number + 1));
                System.out.println("Score is " + scores.get(number));
                System.out.println();
            }
            System.out.println("---------------------------------------");
        }
    }
}
