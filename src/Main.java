import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.StringTokenizer;

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
        System.out.println(solution);

    }


    public static void readFiles() throws IOException {
        try {
            BufferedReader solutionsBR = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/solutions.txt"))));
            String nextLine = solutionsBR.readLine();
            while(nextLine != null){
                solutions.add(nextLine);
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




}
