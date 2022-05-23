import java.util.Arrays;

public class Rules {
    private final int[] counts = new int[26];
    private final char[] positions = new char[5];
    private final boolean[][] nonPositions = new boolean[5][26];
    private final int[] scratch = new int[26];
    private final boolean[] isExact = new boolean[26];
    private final int[] tempCounts = new int[26];

    public Rules(String rule) {
        addRule(rule);
    }

    public Rules(String... rules) {
        for (String rule : rules) {
            addRule(rule);
        }
    }

    public void addRule(String rule) {
        for (int i = 0; i < 5; i++) {
            // reset counts
            tempCounts[rule.charAt(i * 2) - 'A'] = 0;
        }
        for (int i = 0; i < 5; i++) {
            char currentChar = rule.charAt(i * 2);
            int idx = currentChar - 'A';
            switch (rule.charAt(i * 2 + 1)) {
                case 'g' -> {
                    positions[i] = currentChar;
                    tempCounts[idx]++;
                }
                case 'y' -> {
                    nonPositions[i][idx] = true;
                    tempCounts[idx]++;
                }
                case 'b' -> isExact[idx] = true;
            }
        }
        for (int i = 0; i < 5; i++) {
            int idx = rule.charAt(i * 2) - 'A';
            counts[idx] = Math.max(counts[idx], tempCounts[idx]);
        }
    }

    /**
     * Adds a rule
     * @param word the word we just checked
     * @param match contains the letters 'g', 'y', and 'b' for validation
     */
    public void addRule(String word, String match) {
        for (int i = 0; i < 5; i++) {
            tempCounts[word.charAt(i) - 'A'] = 0;
        }
        for (int i = 0; i < 5; i++) {
            char currentChar = word.charAt(i);
            int idx = currentChar - 'A';
            switch (match.charAt(i)) {
                case 'g' -> {
                    positions[i] = currentChar;
                    tempCounts[idx]++;
                }
                case 'y' -> {
                    nonPositions[i][idx] = true;
                    tempCounts[idx]++;
                }
                case 'b' -> isExact[idx] = true;
            }
        }
        for (int i = 0; i < 5; i++) {
            int idx = word.charAt(i) - 'A';
            counts[idx] = Math.max(counts[idx], tempCounts[idx]);
        }
    }

    public boolean match(String word) {
        for (int i = 0; i < 5; i++) {
            char c = word.charAt(i);
            // if green, check if it matches, if yellow, make sure it doesn't
            if ((positions[i] != 0 && positions[i] != c) || nonPositions[i][c - 'A']) return false;
        }
        // check if word count matches
        Arrays.fill(scratch, 0);
        for (int i = 0; i < 5; i++) {
            scratch[word.charAt(i) - 'A']++;
        }
        for (int i = 0; i < 26; i++) {
            int count = scratch[i];
            if ((isExact[i] && count != counts[i]) || count < counts[i]) {
                return false;
            }
        }
        return true;
    }
}