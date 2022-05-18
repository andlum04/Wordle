public class Utility {
    public static final double BASE_2 = Math.log(2);
    public static double log2(double x) {
        return Math.log(x) / BASE_2;
    }
    /**
     * Returns a unique index indicating how word2 matches word1
     * @param word1 the first word
     * @param word2 the second word
     * @param scratch array of size 26 used for computation
     * @return a number representing how the words match
     */
    public static int getGroup(String word1, String word2, int[] scratch) {
        int yellowResult = 0;
        int greenResult = 0;
        for (int i = 0; i < 5; i++) {
            scratch[word1.charAt(i) - 'A'] = 0;
        }
        for (int i = 0; i < 5; i++) {
            greenResult *= 3;
            char c1 = word1.charAt(i);
            char c2 = word2.charAt(i);
            if (c1 == c2) greenResult += 2;
            else scratch[c2 - 'A']++;
        }
        for (int i = 0; i < 5; i++) {
            yellowResult *= 3;
            char c1 = word1.charAt(i);
            char c2 = word2.charAt(i);
            int idx = c1 - 'A';
            if (c1 != c2 && scratch[idx] != 0) {
                scratch[idx]--;
                yellowResult += 1;
            }
        }
        return greenResult + yellowResult;
    }

    public static boolean isValid(String v) {
        for (int i = 0; i < v.length(); i++) {
            switch (v.charAt(i)) {
                case 'y':
                case 'b':
                case 'g': continue;
                default: return false;
            }
        }
        return true;
    }
}
