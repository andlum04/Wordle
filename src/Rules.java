public class Rules {
    private final int[] counts = new int[26];
    private final char[] positions = new char[5];
    private final char[] nonPositions = new char[5];
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
                    nonPositions[i] = currentChar;
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

    public boolean match(String word) {
        for (int i = 0; i < 5; i++) {
            char c = word.charAt(i);
            // if green, check if it matches, if yellow, make sure it doesn't
            if ((positions[i] != 0 && positions[i] != c) || nonPositions[i] == c) return false;
            // and reset scratch
            scratch[c - 'A'] = 0;
        }
        // check if word count matches
        for (int i = 0; i < 5; i++) {
            scratch[word.charAt(i) - 'A']++;
        }
        for (int i = 0; i < 5; i++) {
            int idx = word.charAt(i) - 'A';
            int count = scratch[idx];
            if ((isExact[idx] && count != counts[idx]) || count < counts[idx]) {
                return false;
            }
        }
        return true;
    }
}