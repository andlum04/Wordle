import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Solver {
    static ArrayList<String> words = new ArrayList<>();
    static ArrayList<String> solutions;
    static ArrayList<String> possibleSolutions = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Main.readFiles();
        solutions = Main.solutions.stream().map(String::toUpperCase).collect(Collectors.toCollection(ArrayList::new));
        // because they are all valid inputs
        words.addAll(solutions);
        words.addAll(Main.words.stream().map(String::toUpperCase).toList());
        solve();
    }

    public static void solve() throws IOException {
        possibleSolutions.clear();
        possibleSolutions.addAll(solutions);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Rules rules = new Rules();
        while (possibleSolutions.size() > 1) {
            System.out.println("Possible solutions: " + possibleSolutions.size());
            // get best word
            String best = getBest();
            System.out.println("Best word: " + best);
            System.out.print("Enter guess result (ex: yybby): ");
            String validation = reader.readLine();
            while (validation.length() != 5 || !isValid(validation)) {
                System.out.println("Can only have letters 'y', 'b', and 'g'");
                System.out.print("Enter guess result (ex: yybby): ");
                validation = reader.readLine();
            }
            rules.addRule(best, validation);
            // filter our possible solutions
            possibleSolutions.removeIf(Predicate.not(rules::match));
        }
        System.out.println("Solution: " + possibleSolutions.get(0));
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

    static class Best {
        String bestWord;
        double bestBits;

        synchronized void updateBest(String word, double bits, boolean isAnswer) {
            if ((isAnswer && bits == bestBits) || bits > bestBits) {
                bestBits = bits;
                bestWord = word;
            }
        }
    }

    public static String getBest() {
        Best b = new Best();
        int size = possibleSolutions.size();
        words.parallelStream().forEach((guess) -> {
            int[] map = new int[243];
            int[] scratch = new int[26];
            boolean isAnswer = false;
            for (String word : possibleSolutions) {
                if (word.equals(guess)) isAnswer = true;
                map[Utility.getGroup(guess, word, scratch)]++;
            }
            double bits = 0;
            for (int num : map) {
                if (num != 0) {
                    double prob = (double)num / size;
                    bits -= Math.log(prob) * prob;
                }
            }
            b.updateBest(guess, bits, isAnswer);
        });
        return b.bestWord;
    }
}
