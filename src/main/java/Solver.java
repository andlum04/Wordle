import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.function.Predicate;

public class Solver {
    static ArrayList<String> words = Main.words;
    static ArrayList<String> solutions = Main.solutions;
    static ArrayList<String> possibleSolutions = new ArrayList<>();

    static {
        possibleSolutions.addAll(solutions);
    }

    public static void main(String[] args) throws IOException {
        Main.readFiles();
        // because solutions are also valid inputs
        words.addAll(solutions);
        solve();
    }

    public static void solve() throws IOException {
        possibleSolutions.clear();
        possibleSolutions.addAll(solutions);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Rules rules = new Rules();
        // according to 3blue1brown, this is the best starting word after simulating
        // the game for all words for the 250 top candidates
        String best = "SALET";
        while (true) {
            System.out.println("Best word: " + best);
            System.out.println("Possible solutions: " + possibleSolutions.size());
            // print a max number of 10 lines for the solutions
            if (possibleSolutions.size() > 9) {
                for (int i = 0; i < 9; i++) {
                    String possibleSolution = possibleSolutions.get(i);
                    System.out.println(" • " + possibleSolution + " (" + Main.genRule(possibleSolution, best) + ")");
                }
                System.out.println(" ...");
            } else {
                for (String solution : possibleSolutions) {
                    System.out.println(" • " + solution + " (" + Main.genRule(solution, best) + ")");
                }
            }
            String validation;
            while (true) {
                System.out.print("Enter guess result (ex: yybby): ");
                validation = reader.readLine();
                if (validation.length() == 5 && Utility.isValid(validation)) break;
                System.out.println("Can only have letters 'y', 'b', and 'g'");
            }
            rules.addRule(best, validation);
            // filter our possible solutions
            possibleSolutions.removeIf(Predicate.not(rules::match));
            if (possibleSolutions.size() == 1) break;
            // get next best word
            best = getBest();
        }
        System.out.println("Solution: " + possibleSolutions.get(0));
    }

    static class Best {
        String bestWord;
        double bestBits;

        ArrayList<String> rankedWords;

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
                    bits -= Utility.log2(prob) * prob;
                }
            }
            b.updateBest(guess, bits, isAnswer);
        });
        return b.bestWord;
    }

    public static void solveOne(String word, int rule) throws IOException {
        Rules rules = new Rules();
        rules.addRule(word, rule);
        // filter our possible solutions
        possibleSolutions.removeIf(Predicate.not(rules::match));
        if (possibleSolutions.size() == 1){
            System.out.println("Solution: " + possibleSolutions.get(0));
            return;
        }
        // get next best word
        String best = getBest();

        System.out.println("Best word: " + best);
        System.out.println("Possible solutions: " + possibleSolutions.size());
        // print a max number of 10 lines for the solutions
        if (possibleSolutions.size() > 9) {
            for (int i = 0; i < 9; i++) {
                String possibleSolution = possibleSolutions.get(i);
                System.out.println(" • " + possibleSolution + " (" + Main.genRule(possibleSolution, best) + ")");
            }
            System.out.println(" ...");
        } else {
            for (String solution : possibleSolutions) {
                System.out.println(" • " + solution + " (" + Main.genRule(solution, best) + ")");
            }
        }
    }
}
