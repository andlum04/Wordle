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
        while(true){
            String guess = sc.nextLine();
            try{
                System.out.println("\033[1A\r" + genRule(guess));
            } catch (StringIndexOutOfBoundsException e){
                System.out.println("word must be length 5");

            }

        }

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
                words.add(nextLine);
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

    public static String genRule(String s){
        s = s.toUpperCase();
        StringBuilder rule = new StringBuilder();
        for (int i = 0; i < 5; i++) {

            if(solution.charAt(i) == s.charAt(i)) rule.append("\033[1;42m");
            //TODO fix rules for duplicate letters
            else if(solution.contains(String.valueOf(s.charAt(i)))) rule.append("\033[1;43m");
            else rule.append("\033[0;1m");
            rule.append(s.charAt(i));
        }
        rule.append("\033[0m");
        return rule.toString();
    }




}
