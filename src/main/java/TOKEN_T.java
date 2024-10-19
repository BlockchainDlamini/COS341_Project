import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TOKEN_T {
    private final String regex_string;
    private String [] text_tokens;

    public TOKEN_T() {
        regex_string = "\"[A-Z][a-z]{1,8}\"";
        text_tokens = new String[0];
    }

    public String[] getText_tokens() {
        return text_tokens;
    }

    public void setText_tokens(String[] text_tokens) {
        this.text_tokens = text_tokens;
    }

    public String [] splitText(String string)
    {
        Pattern pattern = Pattern.compile(regex_string);

        Matcher matcher = pattern.matcher(string);

        List<String> matchedText = new ArrayList<>();
        while (matcher.find()) {
            matchedText.add(matcher.group());
        }

        text_tokens = matchedText.toArray(new String[0]);
        return text_tokens;
    }

    public String getRegexString() {
        return regex_string;
    }
}
