public class Utility {
    public static final double BASE_2 = Math.log(2);
    public static double log2(double x) {
        return Math.log(x) / BASE_2;
    }
    /**
     * Returns a unique index indicating how solution matches guess
     * @param guess the first word
     * @param solution the second word
     * @param scratch array of size 26 used for computation
     * @return a number representing how the words match
     */
    public static int getGroup(String guess, String solution, int[] scratch) {
        int yellowResult = 0;
        int greenResult = 0;
        for (int i = 0; i < 5; i++) {
            scratch[guess.charAt(i) - 'A'] = 0;
        }
        for (int i = 0; i < 5; i++) {
            greenResult *= 3;
            char c1 = guess.charAt(i);
            char c2 = solution.charAt(i);
            if (c1 == c2) greenResult += 2;
            else scratch[c2 - 'A']++;
        }
        for (int i = 0; i < 5; i++) {
            yellowResult *= 3;
            char c1 = guess.charAt(i);
            char c2 = solution.charAt(i);
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

    public static double normalPdf(double mean, double stdev, double x) {
        double zScore = (x - mean) / stdev;
        return 1 / (stdev * Math.sqrt(2 * Math.PI)) * Math.exp(-0.5 * zScore * zScore);
    }
}
