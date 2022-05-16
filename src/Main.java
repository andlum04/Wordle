import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

public class Main {

    static HashSet<String> solutions = new HashSet<>();
    static String solution;
    static HashSet<String> words = new HashSet<>();

    public static void main(String[] args){
        try {
            readFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setSolution();

        Scanner sc = new Scanner(System.in);
        for (int i = 0; i < 6; i++) {
            String guess = sc.nextLine().toUpperCase();
            if(guess.length() == 5 && (words.contains(guess) || solutions.contains(guess))){
                System.out.println("\033[1A\r" + genRule(guess));
                if(guess.equals(solution)) return;
            }
            else System.out.println("Invalid Guess");
        }
        System.out.println(solution);
    }


    public static void readFiles() throws IOException {
        try {
            BufferedReader solutionsBR = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/solutions.txt"))));
            String nextLine = solutionsBR.readLine();
            while(nextLine != null){
                solutions.add(nextLine.toUpperCase());
                nextLine = solutionsBR.readLine();
            }

            BufferedReader wordsBR = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/words.txt"))));
            nextLine = wordsBR.readLine();
            while(nextLine != null){
                words.add(nextLine.toUpperCase());
                nextLine = wordsBR.readLine();
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void setSolution(){
        Random rand = new Random();
        int i = rand.nextInt(solutions.size());
        int n = 0;
        for(String s: solutions)
        {
            if (n == i){
                solution = s;
                return;
            }
            n++;
        }
    }

    public static String genRule(String guess){
        int[] charCount = new int[26];
        guess = guess.toUpperCase();
        String[] rule = new String[5];
        
        //check for green letters
        for (int i = 0; i < 5; i++) {
            if(solution.charAt(i) == guess.charAt(i)){
                rule[i] = "\033[1;92m" +  guess.charAt(i);
                charCount[guess.charAt(i) - 65]++;
            }
        }
        //check for yellow letters
        for (int i = 0; i < 5; i++) {
            int currentCharCount = 0;
                for(char c: solution.toCharArray()){
                    if(c == guess.charAt(i)) currentCharCount++;
                }
                if(currentCharCount > charCount[guess.charAt(i) - 65]){
                    rule[i] = "\033[1;33m" + guess.charAt(i);
                    charCount[guess.charAt(i) - 65]++;
                }
        }
        //remaining letters are black
        for (int i = 0; i < 5; i++) {
            if(rule[i] == null) rule[i] = "\033[0;1m" + guess.charAt(i);
        }
        
        StringBuilder output = new StringBuilder();
        for(String s : rule){
            output.append(s);
        }
        return output.toString();
    }




}
