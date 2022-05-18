import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * To solve multiple words at once, such as quordle or octordle
 */
public class MultiSolver {
    public static final int NUM_WORDS = 4;
    @SuppressWarnings("unchecked")
    private static final ArrayList<String>[] possibleSolutions = new ArrayList[NUM_WORDS];
    /**
     * Whether we have found a solution for that word
     */
    private static final boolean[] foundSolution = new boolean[NUM_WORDS];
    private static final Rules[] rules = new Rules[NUM_WORDS];
    private static ArrayList<String> solutions;
    private static ArrayList<String> words;


    public static void main(String[] args) throws IOException {
        Main.readFiles();
        solutions = Main.solutions.stream().map(String::toUpperCase).collect(Collectors.toCollection(ArrayList::new));
        words = Main.words.stream().map(String::toUpperCase).collect(Collectors.toCollection(ArrayList::new));
        // because solutions are also valid inputs
        words.addAll(solutions);
        for (int i = 0; i < NUM_WORDS; i++) {
            possibleSolutions[i] = new ArrayList<>();
        }
        solve();
    }

    public static void solve() throws IOException {
        Arrays.fill(foundSolution, false);
        for (ArrayList<String> arrayList : possibleSolutions) {
            arrayList.clear();
            arrayList.addAll(solutions);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        for (int i = 0; i < NUM_WORDS; i++) {
            rules[i] = new Rules();
        }
        String best = "SALET";
        Queue<String> q = new ArrayDeque<>(4);
        while (true) {
            for (int i = 0; i < NUM_WORDS; i++) {
                System.out.println("Possible solutions for word " + (i+1) + ": " + possibleSolutions[i].size());
            }
            System.out.println();
            System.out.println("Best word: " + best);
            System.out.println();
            int numFound = 0;
            for (int i = 0; i < NUM_WORDS; i++) {
                if (foundSolution[i]) numFound++;
                else {
                    String validation;
                    while (true) {
                        System.out.print("Enter guess result for word " + (i + 1) + " (ex: yybby): ");
                        validation = reader.readLine();
                        if (validation.length() == 5 && Utility.isValid(validation)) break;
                        System.out.println("Can only have letters 'y', 'b', and 'g'");
                    }
                    rules[i].addRule(best, validation);
                    possibleSolutions[i].removeIf(Predicate.not(rules[i]::match));
                    if (possibleSolutions[i].size() == 1) {
                        foundSolution[i] = true;
                        q.add(possibleSolutions[i].get(0));
                        numFound++;
                    }
                }
            }
            System.out.println();
            if (numFound == NUM_WORDS) break;
            if (q.isEmpty()) best = getBest();
            else best = q.remove();
        }
        for (int i = 0; i < NUM_WORDS; i++) {
            System.out.println("Solution for word " + (i + 1) + ": " + possibleSolutions[i].get(0));
        }
    }

    static class Best {
        String bestWord;
        double bestBits;
        int bestNumber;

        synchronized void updateBest(String word, double bits, int numMatch) {
            if (numMatch > bestNumber && bits >= bestBits) {
                bestNumber = numMatch;
                bestBits = bits;
                bestWord = word;
            } else if (bits > bestBits) {
                bestBits = bits;
                bestWord = word;
            }
        }
    }

    public static String getBest() {
        Best b = new Best();
        words.parallelStream().forEach((guess) -> {
            int[] map = new int[243];
            int[] scratch = new int[26];
            int numMatch = 0;
            double bits = 0;
            for (int i = 0; i < NUM_WORDS; i++) {
                if (foundSolution[i]) continue;
                Arrays.fill(map, 0);
                for (String word : possibleSolutions[i]) {
                    if (word.equals(guess)) numMatch++;
                    map[Utility.getGroup(guess, word, scratch)]++;
                }
                for (int num : map) {
                    if (num != 0) {
                        double prob = (double)num / possibleSolutions[i].size();
                        bits -= Utility.log2(prob) * prob;
                    }
                }
            }
            b.updateBest(guess, bits, numMatch);
        });
        return b.bestWord;
    }
}
