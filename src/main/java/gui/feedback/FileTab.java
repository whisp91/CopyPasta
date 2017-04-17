package gui.feedback;

import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.ViewActions;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.model.TwoDimensional;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Richard Sundqvist on 12/04/2017.
 */
public class FileTab extends Tab {

    //region strings
    private static final String[] KEYWORDS = new String[]{
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "null",
            "->"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );
    //endregion

    private final CodeArea codeArea;
    private final int firstNumber; //Set to 1 to set the first line to 1.

    public FileTab (String fileName, String content) {
        this(fileName, content, false);
    }

    /**
     * @param fileName Name of the file.
     * @param content Content of the file
     * @param startFromZero If {@code true}, row count begins at zero.
     */
    public FileTab (String fileName, String content, boolean startFromZero) {
        firstNumber = startFromZero ? 0 : 1;

        setText(fileName);
        setClosable(false);

        codeArea = new CodeArea();
        codeArea.setStyle("-fx-background-color: #dddddd;");
        codeArea.setEditable(false);
        codeArea.setShowCaret(ViewActions.CaretVisibility.ON);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
                });
        codeArea.replaceText(0, 0, content);
        setContent(new VirtualizedScrollPane<>(codeArea));
    }

    public String getFileName () {
        return getText();
    }

    public int getCaretLine () {
        int offset = codeArea.getCaretPosition();
        TwoDimensional.Position pos = codeArea.offsetToPosition(offset, TwoDimensional.Bias.Forward);
        return pos.getMajor() + firstNumber;
    }

    public String getCodeAreaContent () {
        return codeArea.getText();
    }

    public int getCaretColumn () {
        int offset = codeArea.getCaretPosition();
        TwoDimensional.Position pos = codeArea.offsetToPosition(offset, TwoDimensional.Bias.Forward);
        return pos.getMinor() + firstNumber;
    }

    public int getCaretPosition () {
        return codeArea.getCaretPosition();
    }

    public void setEditable (boolean value) {
        codeArea.setEditable(value);
    }

    // @formatter:off
    private static StyleSpans<Collection<String>> computeHighlighting (String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    // @formatter:on
}
