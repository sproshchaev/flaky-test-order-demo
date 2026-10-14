import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * How often does one class land right before another when 30 classes are
 * shuffled? Counts it over 100,000 shuffles with a fixed seed, the same
 * Collections.shuffle(list, new Random(seed)) that Surefire uses for
 * runOrder=random, and prints it next to the exact value 1/30.
 *
 *   java tools/OrderOdds.java
 */
public class OrderOdds {

    public static void main(String[] args) {
        int classes = 30;
        int shuffles = 100_000;
        int neighbour = 0;
        int target = 1;

        Random seeds = new Random(147);
        int hits = 0;
        for (int i = 0; i < shuffles; i++) {
            List<Integer> order = new ArrayList<>();
            for (int c = 0; c < classes; c++) {
                order.add(c);
            }
            Collections.shuffle(order, new Random(seeds.nextLong()));
            if (order.indexOf(target) == order.indexOf(neighbour) + 1) {
                hits++;
            }
        }
        System.out.printf(Locale.ROOT, "model: %d of %d shuffles, 1 in %.1f (exact 1 in %d)%n",
                hits, shuffles, (double) shuffles / hits, classes);
    }
}
