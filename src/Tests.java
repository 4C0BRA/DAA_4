import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Tests {

    static int passed = 0;
    static int failed = 0;

    static void check(boolean ok, String name) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    static void testLcs() {
        check(LCS.lcs("abcde", "abcde") == 5, "LCS identical strings");
        check(LCS.lcs("abc", "xyz") == 0, "LCS completely different strings");
        check(LCS.lcs("", "abc") == 0, "LCS first string empty");
        check(LCS.lcs("abc", "") == 0, "LCS second string empty");
        check(LCS.lcs("", "") == 0, "LCS both empty");
        check(LCS.lcs("ABCBDAB", "BDCABA") == 4, "LCS ABCBDAB / BDCABA = 4");
        check(LCS.lcs("AGGTAB", "GXTXAYB") == 4, "LCS AGGTAB / GXTXAYB = 4");
        check(LCS.lcsString("AGGTAB", "GXTXAYB").equals("GTAB"), "LCS string GTAB");
        check(LCS.lcs("a", "a") == 1, "LCS one char same");
        check(LCS.lcs("a", "b") == 0, "LCS one char different");
    }

    static void testEditDistance() {
        check(EditDistance.distance("hello", "hello") == 0, "Edit identical strings");
        check(EditDistance.distance("", "abc") == 3, "Edit first string empty");
        check(EditDistance.distance("abc", "") == 3, "Edit second string empty");
        check(EditDistance.distance("", "") == 0, "Edit both empty");
        check(EditDistance.distance("cat", "cut") == 1, "Edit one substitution");
        check(EditDistance.distance("cat", "cart") == 1, "Edit one insertion");
        check(EditDistance.distance("cart", "cat") == 1, "Edit one deletion");
        check(EditDistance.distance("kitten", "sitting") == 3, "Edit kitten / sitting = 3");
        check(EditDistance.distance("sunday", "saturday") == 3, "Edit sunday / saturday = 3");
        check(EditDistance.distance("abc", "xyz") == 3, "Edit all different = 3");
    }

    static void checkMatch(String text, String pattern, List<Integer> expected, String name) {
        List<Integer> a = NaiveSearch.search(text, pattern);
        List<Integer> b = KMP.search(text, pattern);
        List<Integer> c = RabinKarp.search(text, pattern);
        check(a.equals(expected), "Naive: " + name);
        check(b.equals(expected), "KMP: " + name);
        check(c.equals(expected), "RabinKarp: " + name);
        check(a.equals(b) && b.equals(c), "All three same: " + name);
    }

    static void testMatching() {
        checkMatch("abcdefgh", "abc", Arrays.asList(0), "pattern at beginning");
        checkMatch("abcdefgh", "def", Arrays.asList(3), "pattern in middle");
        checkMatch("abcdefgh", "fgh", Arrays.asList(5), "pattern at end");
        checkMatch("abcabcabc", "abc", Arrays.asList(0, 3, 6), "multiple occurrences");
        checkMatch("aaaaa", "aa", Arrays.asList(0, 1, 2, 3), "overlapping occurrences");
        checkMatch("abcdefgh", "xyz", new ArrayList<>(), "pattern absent");
        checkMatch("abc", "abcdef", new ArrayList<>(), "pattern longer than text");
        checkMatch("abc", "", new ArrayList<>(), "empty pattern");
        checkMatch("", "", new ArrayList<>(), "empty text and empty pattern");
        checkMatch("", "a", new ArrayList<>(), "empty text");
        checkMatch("abc", "abc", Arrays.asList(0), "pattern equals text");
        checkMatch("ababcabab", "abab", Arrays.asList(0, 5), "KMP fallback case");
        checkMatch("aaaaab", "aab", Arrays.asList(3), "repeated prefix");

        Random rnd = new Random(7);
        boolean allSame = true;
        for (int t = 0; t < 200; t++) {
            String text = randomString(rnd, 300, 2);
            String pattern = randomString(rnd, 1 + rnd.nextInt(5), 2);
            List<Integer> a = NaiveSearch.search(text, pattern);
            List<Integer> b = KMP.search(text, pattern);
            List<Integer> c = RabinKarp.search(text, pattern);
            if (!a.equals(b) || !b.equals(c)) {
                allSame = false;
            }
        }
        check(allSame, "200 random tests: Naive, KMP and RabinKarp give same indices");

        String text = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        RabinKarp.collisions = 0;
        List<Integer> smallMod = RabinKarp.search(text, "xyz", 7);
        check(RabinKarp.collisions > 0, "RabinKarp with mod 7 has hash collisions");
        check(smallMod.equals(NaiveSearch.search(text, "xyz")), "RabinKarp with mod 7 still correct");
    }

    static void testKasai() {
        String s = "banana";
        int[] expectedSa = {5, 3, 1, 0, 4, 2};
        int[] expectedLcp = {0, 1, 3, 0, 0, 2};
        int[] sa = Kasai.buildSuffixArray(s);
        check(Arrays.equals(sa, expectedSa), "Suffix array of banana");
        check(Arrays.equals(Kasai.buildLcp(s, expectedSa), expectedLcp), "Kasai LCP of banana");

        String s2 = "mississippi";
        int[] expectedSa2 = {10, 7, 4, 1, 0, 9, 8, 6, 3, 5, 2};
        int[] expectedLcp2 = {0, 1, 1, 4, 0, 0, 1, 0, 2, 1, 3};
        check(Arrays.equals(Kasai.buildSuffixArray(s2), expectedSa2), "Suffix array of mississippi");
        check(Arrays.equals(Kasai.buildLcp(s2, expectedSa2), expectedLcp2), "Kasai LCP of mississippi");

        check(Arrays.equals(Kasai.buildLcp("a", new int[]{0}), new int[]{0}), "Kasai one char");
        check(Arrays.equals(Kasai.buildLcp("aaaa", new int[]{3, 2, 1, 0}), new int[]{0, 1, 2, 3}), "Kasai aaaa");

        Random rnd = new Random(11);
        boolean ok = true;
        for (int t = 0; t < 50; t++) {
            String r = randomString(rnd, 50, 3);
            int[] sa3 = Kasai.buildSuffixArray(r);
            int[] lcp = Kasai.buildLcp(r, sa3);
            for (int i = 1; i < r.length(); i++) {
                if (lcp[i] != slowLcp(r, sa3[i - 1], sa3[i])) {
                    ok = false;
                }
            }
        }
        check(ok, "50 random strings: Kasai equals slow LCP");
    }

    static int slowLcp(String s, int x, int y) {
        int k = 0;
        while (x + k < s.length() && y + k < s.length() && s.charAt(x + k) == s.charAt(y + k)) {
            k++;
        }
        return k;
    }

    static String randomString(Random rnd, int length, int letters) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + rnd.nextInt(letters)));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        testLcs();
        testEditDistance();
        testMatching();
        testKasai();
        System.out.println();
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
