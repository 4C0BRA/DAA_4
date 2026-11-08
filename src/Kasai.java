import java.util.Arrays;

public class Kasai {

    public static long comparisons = 0;

    public static int[] buildLcp(String s, int[] sa) {
        int n = s.length();
        int[] lcp = new int[n];
        int[] rank = new int[n];

        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n) {
                    comparisons++;
                    if (s.charAt(i + h) != s.charAt(j + h)) {
                        break;
                    }
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) {
                    h--;
                }
            } else {
                h = 0;
            }
        }

        return lcp;
    }

    public static int[] buildSuffixArray(String s) {
        int n = s.length();
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }

        Arrays.sort(arr, (x, y) -> compareSuffixes(s, x, y));

        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = arr[i];
        }
        return sa;
    }

    private static int compareSuffixes(String s, int x, int y) {
        int n = s.length();
        while (x < n && y < n) {
            if (s.charAt(x) != s.charAt(y)) {
                return s.charAt(x) - s.charAt(y);
            }
            x++;
            y++;
        }
        if (x == n && y == n) {
            return 0;
        }
        if (x == n) {
            return -1;
        }
        return 1;
    }
}
