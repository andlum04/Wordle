import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Objects;

public class TodayWordle {
    public static void main(String[] args) throws IOException {
        ArrayList<String> words = readWordle();
        LocalDate today = LocalDate.now();
        LocalDate dayOfFirstWordle = LocalDate.of(2021, Month.JUNE, 19);
        System.out.println("Today's Wordle is " + words.get((int) ChronoUnit.DAYS.between(dayOfFirstWordle, today)).toUpperCase());
    }

    public static ArrayList<String> readWordle() throws IOException {
        ArrayList<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(TodayWordle.class.getResourceAsStream("solutions.txt"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }
}
