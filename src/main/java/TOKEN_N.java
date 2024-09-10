import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TOKEN_N {
    private final String regex_string;
    private String [] number_tokens;

    public TOKEN_N() {
        this.regex_string = "0|0\\.([0-9])*[1-9]|-0\\.([0-9])*[1-9]|[1-9]([0-9])*|-[1-9]([0-9])*|[1-9]([0-9])*.([0-9])*[1-9]|-[1-9]([0-9])*.([0-9])*[1-9]";
        this.number_tokens = new String[0];
    }

    public String[] getNumber_tokens() {
        return number_tokens;
    }

    public void setNumber_tokens(String[] number_tokens) {
        this.number_tokens = number_tokens;
    }

    public String [] splitNumbers(String string)
    {
        Pattern pattern = Pattern.compile(this.regex_string);

        Matcher matcher = pattern.matcher(string);

        List<String> matchedNumbers = new ArrayList<>();
        while (matcher.find()) {
            matchedNumbers.add(matcher.group());
        }

        this.number_tokens = matchedNumbers.toArray(new String[0]);
        return this.number_tokens;
    }
}
