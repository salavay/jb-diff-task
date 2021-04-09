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
    public static void main(final String[] args) {
        if (args.length != 2) {
            // Print usage?
            System.err.println("There should be 2 paths in program argument");
            return;
        }
        final Path oldTextFilePath;
        final Path newTextFilePath;
        try {
            oldTextFilePath = Path.of(args[0]).toAbsolutePath();
            newTextFilePath = Path.of(args[1]).toAbsolutePath();
        } catch (final InvalidPathException e) {
            // NOTE: Системные сообщения об ошибке
            System.err.println("The paths are invalid");
            return;
        }
        try {
            final List<String> oldLines = Files.readAllLines(oldTextFilePath);
            try {
                final List<String> newLines = Files.readAllLines(newTextFilePath);
                processLcm(oldLines, newLines);
            } catch (final IOException e) {
                // NOTE: Системные сообщения об ошибке
                System.err.println("Can't read new file");
            }
        } catch (final IOException e) {
            // NOTE: Системные сообщения об ошибке
            System.err.println("Can't read old file");
        }
    }

    private static void processLcm(final List<String> oldLines,
                                   final List<String> newLines) {
        // Комментирование кода
        final long[][] lcmTable;
        final StringType[][] typesOfString;
        final int oldFileSize = oldLines.size();
        final int newFileSize = newLines.size();
        lcmTable = new long[oldFileSize + 1][newFileSize + 1];
        typesOfString = new StringType[oldFileSize + 1][newFileSize + 1];

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

    // Неоптимальность? Убедиться
    private static void writeToHtml(final StringType[][] typesOfString,
                                    final List<String> oldLines,
                                    final List<String> newLines) {
        int curI = oldLines.size(), curJ = newLines.size();
        final ArrayList<String> diffText = new ArrayList<>();
        final List<String> changedLinesBuffer = new ArrayList<>();
        while (curI != 0 || curJ != 0) {
            final StringType stringType = typesOfString[curI][curJ];
            switch (stringType) {
                case OLD -> {
                    diffText.add(getHtmlElement(oldLines.get(curI - 1), stringType));
                    curI--;
                    curJ--;
                }
                case CHANGEDOLD -> {
                    diffText.add(getHtmlElement(newLines.get(curJ - 1), stringType));
                    changedLinesBuffer.add(getHtmlElement(oldLines.get(curI - 1), StringType.CHANGEDNEW));
                    curI--;
                    curJ--;
                    if (!typesOfString[curI][curJ].equals(StringType.CHANGEDOLD)) {
                        diffText.addAll(changedLinesBuffer);
                        changedLinesBuffer.clear();
                    }
                }
                case ADDED -> {
                    diffText.add(getHtmlElement(newLines.get(curJ - 1), stringType));
                    curJ--;
                }
                case DELETED -> {
                    diffText.add(getHtmlElement(oldLines.get(curI - 1), stringType));
                    curI--;
                }
            }
        }
        Collections.reverse(diffText);
        renderHtml(collectionsToString(oldLines),
                collectionsToString(diffText),
                collectionsToString(newLines));
    }

    private static String getHtmlElement(final String line, final StringType stringType) {
        return String.format("<div class=\"%s d-flex\">%s</div>", stringType, line);
    }

    private static void renderHtml(final String oldText, final String diffText, final String newText) {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        try {
            cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
        } catch (final IOException e) {
            // Ignored
        }
        final Map<String, Object> view = new HashMap<>();
        view.put("old_text", oldText);
        view.put("diff_text", diffText);
        view.put("new_text", newText);
        Template template = null;
        try {
            template = cfg.getTemplate("diff.ftlh");
        } catch (final IOException e) {
            // Ignored
        }
        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of("diff.html"))) {
            Objects.requireNonNull(template).process(view, bufferedWriter);
        } catch (final TemplateException | IOException e) {
            // Ignored
        }
    }

    public static String showMatrix(final long[][] array) {
        final StringBuilder sb = new StringBuilder();
        for (final long[] i : array) {
            for (final long j : i) {
                sb.append(String.format("%d   ", j));
            }
            sb.append(String.format("%n"));
        }
        return sb.toString();
    }

    public static String showTypes(final StringType[][] types) {
        final StringBuilder sb = new StringBuilder();
        for (final StringType[] i : types) {
            for (final StringType j : i) {
                sb.append(String.format("%s   ", j));
            }
            sb.append(String.format("%n"));
        }
        return sb.toString();
    }

    private static String collectionsToString(final Collection<String> collection) {
        return collection.stream().collect(Collectors.joining(System.lineSeparator()));
    }
}
