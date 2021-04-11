import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Diff {
    /**
     * Means style of Diff.html. If {@code true} Diff.html will includes 3 columns(old, diff, new).
     * If {@code false} Diff.html will includes 2 columns as it said in task (SplitView: old, diff).
     * Also, in SplitView mode only new version of changed lines are visible
     */
    private static final boolean tripleView = false;

    /**
     * Create Diff.html - diff of two given files
     *
     * @param args 2 path of old and new files
     */
    public static void main(final String[] args) {
        if (args.length != 2) {
            System.err.println("There should be 2 paths in program arguments");
            return;
        }
        final Path oldTextFilePath;
        final Path newTextFilePath;
        try {
            oldTextFilePath = Path.of(args[0]);
            newTextFilePath = Path.of(args[1]);
        } catch (final InvalidPathException e) {
            System.err.println("The paths are invalid");
            return;
        }

        final List<String> oldLines, newLines;
        try {
            oldLines = Files.readAllLines(oldTextFilePath);
            try {
                newLines = Files.readAllLines(newTextFilePath);
            } catch (final IOException e) {
                System.err.println("Can't read new file");
                return;
            }
        } catch (final IOException e) {
            System.err.println("Can't read old file");
            return;
        }
        processDiff(oldLines, newLines);
    }

    /**
     * Get 2 files and render Diff.html of them. Files must be given as {@link List<String>}
     *
     * @param oldLines old file
     * @param newLines new file
     * @see #writeToHtml(StringType[][], List, List)
     */
    private static void processDiff(final List<String> oldLines,
                                    final List<String> newLines) {
        // Table of LCM algorithm
        final long[][] lcmTable;
        // Table of algorithm transitions
        final StringType[][] typesOfString;
        final int oldFileSize = oldLines.size();
        final int newFileSize = newLines.size();
        lcmTable = new long[oldFileSize + 1][newFileSize + 1];
        typesOfString = new StringType[oldFileSize + 1][newFileSize + 1];

        /* LCM algorithm
         *  Check javaDoc of StringType for transition description
         */
        for (int i = 0; i <= oldFileSize; i++) {
            for (int j = 0; j <= newFileSize; j++) {
                if (i == 0 || j == 0) {
                    lcmTable[i][j] = i + j;
                    if (j != 0 || i != 0) {
                        if (j > i) {
                            typesOfString[i][j] = StringType.ADDED;
                        } else {
                            typesOfString[i][j] = StringType.DELETED;
                        }
                    } else {
                        typesOfString[i][j] = StringType.OLD;
                    }
                    continue;
                }
                if (oldLines.get(i - 1).equals(newLines.get(j - 1))) {
                    lcmTable[i][j] = lcmTable[i - 1][j - 1];
                    typesOfString[i][j] = StringType.OLD;
                } else if (lcmTable[i - 1][j - 1] <= lcmTable[i - 1][j] &&
                        lcmTable[i - 1][j - 1] <= lcmTable[i][j - 1]) {
                    lcmTable[i][j] = lcmTable[i - 1][j - 1] + 1;
                    typesOfString[i][j] = StringType.CHANGEDOLD;
                } else if (lcmTable[i - 1][j] <= lcmTable[i][j - 1]) {
                    lcmTable[i][j] = lcmTable[i - 1][j] + 1;
                    typesOfString[i][j] = StringType.DELETED;
                } else {
                    lcmTable[i][j] = lcmTable[i][j - 1] + 1;
                    typesOfString[i][j] = StringType.ADDED;
                }
            }
        }
        writeToHtml(typesOfString, oldLines, newLines);
    }

    /**
     * Write Diff.html file based on transitions of LCM algorithm. Html rendering using Freemarker template.
     *
     * @param typesOfString transitions({@link StringType}) of LCM algorithm
     * @param oldLines      old file
     * @param newLines      new file
     * @see #renderHtml(List, List, List)
     */
    private static void writeToHtml(final StringType[][] typesOfString,
                                    final List<String> oldLines,
                                    final List<String> newLines) {
        int curI = oldLines.size(), curJ = newLines.size();
        final List<StringWithType> diffText = new ArrayList<>();
        final List<StringWithType> changedLinesBuffer = new ArrayList<>();
        // Create answer from the ending in reverse order of lines
        while (curI != 0 || curJ != 0) {
            final StringType stringType = typesOfString[curI][curJ];
            switch (stringType) {
                case OLD -> {
                    diffText.add(new StringWithType(oldLines.get(curI - 1), stringType));
                    curI--;
                    curJ--;
                }
                case CHANGEDOLD -> {
                    diffText.add(new StringWithType(newLines.get(curJ - 1), stringType));
                    if (tripleView) {
                        changedLinesBuffer.add(new StringWithType(oldLines.get(curI - 1), StringType.CHANGEDNEW));
                        if (!typesOfString[curI - 1][curJ - 1].equals(StringType.CHANGEDOLD)) {
                            diffText.addAll(changedLinesBuffer);
                            changedLinesBuffer.clear();
                        }
                    }
                    curI--;
                    curJ--;
                }
                case ADDED -> {
                    diffText.add(new StringWithType(newLines.get(curJ - 1), stringType));
                    curJ--;
                }
                case DELETED -> {
                    diffText.add(new StringWithType(oldLines.get(curI - 1), stringType));
                    curI--;
                }
            }
        }
        Collections.reverse(diffText);
        renderHtml(mapToStringWithDefaultType(oldLines),
                diffText,
                mapToStringWithDefaultType(newLines));
    }

    /**
     * Create Diff.html file using Freemarker template.
     *
     * @param oldText  text of old file
     * @param diffText text of diff file
     * @param newText  text of new file
     */
    private static void renderHtml(final List<StringWithType> oldText, final List<StringWithType> diffText, final List<StringWithType> newText) {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        try {
            cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
        } catch (final IOException e) {
            System.err.println("Can't set \"templates\" directory for Freemarker config");
        }
        final Map<String, Object> view = new HashMap<>();
        view.put("old_text", oldText);
        view.put("diff_text", diffText);
        if (tripleView) {
            view.put("new_text", newText);
        }
        view.put("isTripleView", tripleView);
        final Template template;
        try {
            template = cfg.getTemplate("diff.ftlh");
        } catch (final IOException e) {
            System.err.println("Can't load Freemarker template");
            return;
        }
        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of("Diff.html"))) {
            Objects.requireNonNull(template).process(view, bufferedWriter);
        } catch (final TemplateException | IOException e) {
            System.err.println("Can't render freemarker template");
        }
    }

    /**
     * Map {@link Collection<String>} to {@link Collection<StringWithType>} with {@link StringType#OLD} type.
     *
     * @param collection {@link Collection<String>} to be represented
     * @return {@link String} view of collection
     */
    private static List<StringWithType> mapToStringWithDefaultType(final Collection<String> collection) {
        return collection.stream().map(x -> new StringWithType(x, StringType.OLD)).collect(Collectors.toList());
    }

    /**
     * This class needed to print string as HTML block with custom class.
     * {@link StringWithType#stringType#toString()} - name of CSS class
     */
    public static class StringWithType {
        private final String string;
        private final StringType stringType;

        public StringWithType(final String string, final StringType stringType) {
            this.string = string;
            this.stringType = stringType;
        }

        public String getString() {
            return string;
        }

        public StringType getStringType() {
            return stringType;
        }
    }
}
