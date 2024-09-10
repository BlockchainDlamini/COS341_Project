import java.util.regex.Pattern;

public enum TokenType {
    RESERVED_KEYWORD(new Reserved_Keywords_Token().getRegexString()),
    VARIABLE(new TOKEN_V().getRegexString()),
    FUNCTION(new TOKEN_F().getRegexString()),
    NUMBER(new TOKEN_N().getRegexString()),
    TEXT(new TOKEN_T().getRegexString());
    private final String pattern;

    TokenType(String pattern) {
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return Pattern.compile(pattern);
    }
}
