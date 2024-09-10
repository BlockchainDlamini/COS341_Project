import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Lexer {
    private final List<Token> tokens = new ArrayList<>();

    public static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public void tokenize(String input) throws LexicalError {
        int position = 0;
        while (position < input.length()) {
            boolean matched = false;
            for (TokenType type : TokenType.values()) {
                Matcher matcher = type.getPattern().matcher(input);
                matcher.region(position, input.length());
                if (matcher.lookingAt()) {
                    tokens.add(new Token(type, matcher.group()));
                    position = matcher.end();
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                throw new LexicalError("Unexpected character at position " + position);
            }
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void generateXML(String outputFilePath) throws IOException {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write("<TOKENSTREAM>\n");
            int id = 1;
            for (Token token : tokens) {
                writer.write("  <TOK>\n");
                writer.write("    <ID>" + id++ + "</ID>\n");
                writer.write("    <CLASS>" + token.getType() + "</CLASS>\n");
                writer.write("    <WORD>" + token.getValue() + "</WORD>\n");
                writer.write("  </TOK>\n");
            }
            writer.write("</TOKENSTREAM>\n");
        }
    }
}