package com.taltechleon.sudoku;

import com.taltechleon.sudoku.model.SudokuModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * <a href="https://www.kaggle.com/datasets/bryanpark/sudoku">Test puzzles from base Kaggle base 10000000 games</a>
 */
@Disabled("Disabled because takes long time")
public class KaggleSetTest {
    private static final List<KaggleSudokuRecord> kaggleSetRecords = new ArrayList<>(1024 * 1024);

    @BeforeAll
    public static void beforeAll() throws Exception {
        System.out.println("Loading Kaggle puzzle set...");
        final long startTime = System.currentTimeMillis();
        try (final InputStream stream = new GZIPInputStream(Objects.requireNonNull(KaggleSetTest.class.getResourceAsStream("/sudoku.csv.gz")))) {
            final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            int counter = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] split = line.split(",");
                if (split.length != 2) {
                    throw new IOException("Unexpectedly read line splitted not to two parts: " + line);
                }
                if (counter != 0) {
                    final KaggleSudokuRecord newRecord =
                            new KaggleSudokuRecord(split[0].trim(), split[1].trim());
                    kaggleSetRecords.add(newRecord);
                }
                counter++;
            }
        }
        System.out.println("Loaded " + kaggleSetRecords.size() + " puzzle(s), spent time " +
                (System.currentTimeMillis() - startTime) + " ms");
    }

    @Test
    void testOverKaggleSet() {
        final SudokuModel model = new SudokuModel(3, 3);
        int counter = 0;
        System.out.println("Start test, " + kaggleSetRecords.size() + " records");
        final long timeStart = System.currentTimeMillis();
        for (final KaggleSudokuRecord record : kaggleSetRecords) {
            model.loadFromArray(record.puzzle);
            final Set<SudokuModel> solutions = model.solve(Integer.MAX_VALUE);
            Assertions.assertEquals(1, solutions.size(), "Expected only solution");
            Assertions.assertArrayEquals(record.solution,
                    solutions.stream().findFirst().orElseThrow().cellsAsArray());
            counter++;
            if (counter % 10000 == 0) {
                System.out.print(".");
            }
        }
        final long spentTime = System.currentTimeMillis() - timeStart;
        System.out.println(
                "\nCompleted, spent time " + spentTime + " ms, approx time per puzzle " +
                        ((double) spentTime / (double) kaggleSetRecords.size()) + " ms");
    }

    private static final class KaggleSudokuRecord {
        final byte[] puzzle;
        final byte[] solution;

        KaggleSudokuRecord(final String puzzle, final String solution) {
            if (puzzle.length() != solution.length()) {
                throw new IllegalArgumentException("Puzzle and solution has different size");
            }
            this.puzzle = new byte[puzzle.length()];
            this.solution = new byte[solution.length()];

            for (int i = 0; i < puzzle.length(); i++) {
                final int p = puzzle.charAt(i) - '0';
                final int s = solution.charAt(i) - '0';
                if (p < 0 || p > 9) {
                    throw new IllegalArgumentException(
                            "Detected unexpected char for puzzle: " + puzzle.charAt(i));
                }
                if (s < 0 || s > 9) {
                    throw new IllegalArgumentException(
                            "Detected unexpected char for solution: " + solution.charAt(i));
                }
                this.puzzle[i] = (byte) p;
                this.solution[i] = (byte) s;
            }
        }
    }

}
