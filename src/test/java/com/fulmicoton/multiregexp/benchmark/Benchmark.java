package com.fulmicoton.multiregexp.benchmark;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Benchmark {


    public static void main(final String[] args) throws Exception {

        // Using the same test as http://lh3lh3.users.sourceforge.net/reb.shtml

        final String txttFilePath = args[0];
        final String patternFilePath = args[1];

        final String txt = new String(Files.readAllBytes(Paths.get(txttFilePath)), StandardCharsets.UTF_8);
        final List<String> patterns = Files.readAllLines(Paths.get(patternFilePath), StandardCharsets.UTF_8);


        while (true) {
            for (final PatternMethods patternMethod: PatternMethods.values()) {
                System.out.println("---------------------------");
                System.out.println(patternMethod.name());
                long start = System.currentTimeMillis();
                final PatternMethods.PatternMatchingMethod method = patternMethod.make(patterns);
                long end = System.currentTimeMillis();
                System.out.println("build time (ms):   " + (end - start));
                {
                    start = System.currentTimeMillis();
                    final int[] patternCounts = method.matchCounts(txt);
                    end = System.currentTimeMillis();
                    System.out.println("match time (ms):   " + (end - start));
                    for (int patternCount: patternCounts) {
                        System.out.print(patternCount + " ");
                    }
                    System.out.println();
                }
            }
        }
    }

}
