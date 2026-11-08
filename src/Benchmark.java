import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Benchmark {

    static final String ALPHABET = "abcd";
    static final long SEED = 42;
    static final int RUNS = 5;
    static final int WARMUP = 20;
    static final int PATTERN_LENGTH = 12;

    static long sink = 0;

    static String randomString(Random rnd, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    static double timeLcs(String a, String b) {
        for (int i = 0; i < WARMUP; i++) {
            sink += LCS.lcs(a, b);
        }
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            sink += LCS.lcs(a, b);
            long end = System.nanoTime();
            total += end - start;
        }
        return total / (double) RUNS / 1_000_000.0;
    }

    static double timeEdit(String a, String b) {
        for (int i = 0; i < WARMUP; i++) {
            sink += EditDistance.distance(a, b);
        }
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            sink += EditDistance.distance(a, b);
            long end = System.nanoTime();
            total += end - start;
        }
        return total / (double) RUNS / 1_000_000.0;
    }

    static double timeNaive(String text, String pattern) {
        for (int i = 0; i < WARMUP; i++) {
            sink += NaiveSearch.search(text, pattern).size();
        }
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            sink += NaiveSearch.search(text, pattern).size();
            long end = System.nanoTime();
            total += end - start;
        }
        return total / (double) RUNS / 1_000_000.0;
    }

    static double timeKmp(String text, String pattern) {
        for (int i = 0; i < WARMUP; i++) {
            sink += KMP.search(text, pattern).size();
        }
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            sink += KMP.search(text, pattern).size();
            long end = System.nanoTime();
            total += end - start;
        }
        return total / (double) RUNS / 1_000_000.0;
    }

    static double timeRabinKarp(String text, String pattern) {
        for (int i = 0; i < WARMUP; i++) {
            sink += RabinKarp.search(text, pattern).size();
        }
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            sink += RabinKarp.search(text, pattern).size();
            long end = System.nanoTime();
            total += end - start;
        }
        return total / (double) RUNS / 1_000_000.0;
    }

    static double timeKasai(String s, int[] sa) {
        for (int i = 0; i < WARMUP; i++) {
            sink += Kasai.buildLcp(s, sa)[s.length() - 1];
        }
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            sink += Kasai.buildLcp(s, sa)[s.length() - 1];
            long end = System.nanoTime();
            total += end - start;
        }
        return total / (double) RUNS / 1_000_000.0;
    }

    static void runDp(List<String> csv) {
        System.out.println("=== LCS and Edit Distance ===");
        System.out.println("n | LCS ms | LCS cells | LCS length | Edit ms | Edit cells | Edit distance");
        csv.add("n,lcs_ms,lcs_cells,lcs_length,edit_ms,edit_cells,edit_distance");

        int[] sizes = {100, 500, 1000};
        for (int n : sizes) {
            Random rnd = new Random(SEED + n);
            String a = randomString(rnd, n);
            String b = randomString(rnd, n);

            LCS.cells = 0;
            int lcsLength = LCS.lcs(a, b);
            long lcsCells = LCS.cells;

            EditDistance.cells = 0;
            int dist = EditDistance.distance(a, b);
            long editCells = EditDistance.cells;

            double lcsMs = timeLcs(a, b);
            double editMs = timeEdit(a, b);

            String line = n + "," + String.format("%.3f", lcsMs) + "," + lcsCells + "," + lcsLength + ","
                    + String.format("%.3f", editMs) + "," + editCells + "," + dist;
            csv.add(line);
            System.out.println(line.replace(",", " | "));
        }
        System.out.println();
    }

    static void matchingRow(List<String> csv, String caseName, int n, String text, String pattern) {
        NaiveSearch.comparisons = 0;
        List<Integer> r1 = NaiveSearch.search(text, pattern);
        long naiveCmp = NaiveSearch.comparisons;

        KMP.comparisons = 0;
        List<Integer> r2 = KMP.search(text, pattern);
        long kmpCmp = KMP.comparisons;

        RabinKarp.comparisons = 0;
        RabinKarp.hashHits = 0;
        RabinKarp.collisions = 0;
        List<Integer> r3 = RabinKarp.search(text, pattern);
        long rkCmp = RabinKarp.comparisons;
        long rkHits = RabinKarp.hashHits;
        long rkCollisions = RabinKarp.collisions;

        if (!r1.equals(r2) || !r2.equals(r3)) {
            System.out.println("ERROR: algorithms gave different results for " + caseName + " n=" + n);
        }

        double naiveMs = timeNaive(text, pattern);
        double kmpMs = timeKmp(text, pattern);
        double rkMs = timeRabinKarp(text, pattern);

        String line = caseName + "," + n + "," + pattern.length() + "," + r1.size() + ","
                + String.format("%.3f", naiveMs) + "," + naiveCmp + ","
                + String.format("%.3f", kmpMs) + "," + kmpCmp + ","
                + String.format("%.3f", rkMs) + "," + rkCmp + "," + rkHits + "," + rkCollisions;
        csv.add(line);
        System.out.println(line.replace(",", " | "));
    }

    static void runMatching(List<String> csv) {
        System.out.println("=== String matching ===");
        System.out.println("case | n | m | found | naive ms | naive cmp | kmp ms | kmp cmp | rk ms | rk cmp | rk hash hits | rk collisions");
        csv.add("case,n,m,found,naive_ms,naive_cmp,kmp_ms,kmp_cmp,rk_ms,rk_cmp,rk_hash_hits,rk_collisions");

        int[] sizes = {10000, 50000, 100000};
        for (int n : sizes) {
            Random rnd = new Random(SEED + n);
            String text = randomString(rnd, n);

            String present = text.substring(n / 2, n / 2 + PATTERN_LENGTH);

            String absent = randomString(rnd, PATTERN_LENGTH);
            while (text.contains(absent)) {
                absent = randomString(rnd, PATTERN_LENGTH);
            }

            matchingRow(csv, "present", n, text, present);
            matchingRow(csv, "absent", n, text, absent);
        }

        for (int n : sizes) {
            String text = repeat('a', n);
            String pattern = repeat('a', 99) + "b";
            matchingRow(csv, "worst_absent", n, text, pattern);
        }

        for (int n : sizes) {
            String text = repeat('a', n);
            String pattern = repeat('a', 100);
            matchingRow(csv, "worst_present", n, text, pattern);
        }
        System.out.println();
    }

    static void runKasai(List<String> csv) {
        System.out.println("=== Kasai LCP ===");
        System.out.println("n | kasai ms | char comparisons | max lcp");
        csv.add("n,kasai_ms,comparisons,max_lcp");

        int[] sizes = {1000, 5000, 10000};
        for (int n : sizes) {
            Random rnd = new Random(SEED + n);
            String s = randomString(rnd, n);
            int[] sa = Kasai.buildSuffixArray(s);

            Kasai.comparisons = 0;
            int[] lcp = Kasai.buildLcp(s, sa);
            long cmp = Kasai.comparisons;

            int maxLcp = 0;
            for (int x : lcp) {
                if (x > maxLcp) {
                    maxLcp = x;
                }
            }

            double ms = timeKasai(s, sa);

            String line = n + "," + String.format("%.3f", ms) + "," + cmp + "," + maxLcp;
            csv.add(line);
            System.out.println(line.replace(",", " | "));
        }
        System.out.println();
    }

    static void writeCsv(String path, List<String> lines) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(path));
        for (String line : lines) {
            out.println(line);
        }
        out.close();
    }

    public static void main(String[] args) throws IOException {
        new File("results").mkdirs();

        List<String> dp = new ArrayList<>();
        List<String> matching = new ArrayList<>();
        List<String> kasai = new ArrayList<>();

        runDp(dp);
        runMatching(matching);
        runKasai(kasai);

        writeCsv("results/dp.csv", dp);
        writeCsv("results/matching.csv", matching);
        writeCsv("results/kasai.csv", kasai);

        System.out.println("Saved results/dp.csv, results/matching.csv, results/kasai.csv");
        System.out.println("(sink = " + sink + ")");
    }
}
