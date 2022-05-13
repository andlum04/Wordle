public class Utility {
    /**
     * Returns a unique index indicating how word2 matches word1
     * @param word1 the first word
     * @param word2 the second word
     * @param scratch array of size 26 used for computation
     * @return a number representing how the words match
     */
    public static int getGroup(String word1, String word2, int[] scratch) {
        int result = 0;
        for (int i = 0; i < 5; i++) {
            scratch[word1.charAt(i) - 'A'] = 0;
        }
        for (int i = 0; i < 5; i++) {
            scratch[word1.charAt(i) - 'A']++;
        }
        for (int i = 0; i < 5; i++) {
            char c1 = word1.charAt(i);
            char c2 = word2.charAt(i);
            int idx = c1 - 'A';
            if (c1 == c2) {
                result += 2;
                scratch[idx]--;
            } else if (scratch[idx] != 0) {
                scratch[idx]--;
                result += 1;
            }
            result *= 3;
        }
        return result;
    }
}
