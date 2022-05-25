import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.*;

public class Main {

    static ArrayList<String> solutions = new ArrayList<>();
    static String solution;
    static ArrayList<String> words = new ArrayList<>();
    static HashSet<String> wordSet = new HashSet<>();

    public static void main(String[] args) {
        try {
            readFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        wordSet.addAll(words);
        wordSet.addAll(solutions);
        setSolution();
        initGUI();
    }

    public static void initGUI() {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            // dark title bar on macOS
            //   - "system": use current macOS appearance (light or dark)
            //   - "NSAppearanceNameAqua": use light appearance
            //   - "NSAppearanceNameDarkAqua": use dark appearance
            System.setProperty( "apple.awt.application.appearance", "NSAppearanceNameDarkAqua" );
        } else if (System.getProperty("os.name").equals("Linux")) {
            // decorate title bar on linux
            JFrame.setDefaultLookAndFeelDecorated( true );
            JDialog.setDefaultLookAndFeelDecorated( true );
        }
        SwingUtilities.invokeLater(() -> {
            FlatLaf.registerCustomDefaultsSource( "themes");
            FlatDarkLaf.setup();
            JFrame f = new JFrame();

            JPanel panel = (JPanel) f.getContentPane();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

            JLabel title = new JLabel("Wordle", SwingConstants.CENTER);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.setFont(new Font("Courier", Font.BOLD, 40));
            title.setForeground(Color.WHITE);
            panel.add(title);

            JSeparator separator = new JSeparator();
            separator.setForeground(new Color(0x3a3a3c));
            panel.add(separator);

            panel.add(Box.createVerticalGlue());

            WordlePanel wordlePanel = new WordlePanel();
            // cannot center align, so used another JPanel
            JPanel temp = new JPanel();
            temp.add(wordlePanel);
            panel.add(temp);

            panel.add(Box.createVerticalGlue());

            KeyboardPanel keyboardPanel = new KeyboardPanel(wordlePanel);
            // cannot center align, so used another JPanel
            temp = new JPanel();
            temp.add(keyboardPanel);
            panel.add(temp);

            f.setContentPane(panel);
            f.pack();
            f.setResizable(false);
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.setVisible(true);
            f.setTitle("Wordle (Java Swing Edition)");
            wordlePanel.requestFocus();
        });
    }

    public static void cliApp() {
        Scanner sc = new Scanner(System.in);
        for (int i = 0; i < 6; i++) {
            String guess = sc.nextLine().toUpperCase();
            if (guess.length() == 5 && (wordSet.contains(guess))) {
                System.out.println("\033[1A\r" + genRule(guess));
                if (guess.equals(solution)) return;
            } else System.out.println("Invalid Guess");
        }
        System.out.println(solution);
    }

    public static void readFiles() throws IOException {
        try (
                BufferedReader solutionsBR =
                     new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/solutions.txt"))));
                BufferedReader wordsBR =
                     new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/words.txt"))))
        ) {
            String nextLine;
            while ((nextLine = solutionsBR.readLine()) != null) {
                solutions.add(nextLine.toUpperCase());
            }
            while ((nextLine = wordsBR.readLine()) != null) {
                words.add(nextLine.toUpperCase());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void setSolution() {
        solution = solutions.get(ThreadLocalRandom.current().nextInt(solutions.size()));
    }

    public static String genRule(String solution, String guess) {
        int[] charCount = new int[26];
        guess = guess.toUpperCase();
        String[] rule = new String[5];

        //check for green letters
        for (int i = 0; i < 5; i++) {
            if (solution.charAt(i) == guess.charAt(i)) {
                rule[i] = "\033[1;42m" + guess.charAt(i);
            } else {
                charCount[solution.charAt(i) - 'A']++;
            }
        }
        //check for yellow letters
        for (int i = 0; i < 5; i++) {
            char sc = solution.charAt(i);
            char gc = guess.charAt(i);
            int idx = gc - 'A';
            if (charCount[idx] != 0 && sc != gc) {
                charCount[idx]--;
                rule[i] = "\033[1;43m" + gc;
            }
        }
        //remaining letters are black
        for (int i = 0; i < 5; i++) {
            if (rule[i] == null) rule[i] = "\033[0;1m" + guess.charAt(i);
        }

        StringBuilder output = new StringBuilder();
        for (String s : rule) {
            output.append(s);
        }
        output.append("\033[0m");
        return output.toString();
    }

    public static String genRule(String guess) {
        return genRule(solution, guess);
    }
}
