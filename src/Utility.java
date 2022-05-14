public class Utility {
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
}
