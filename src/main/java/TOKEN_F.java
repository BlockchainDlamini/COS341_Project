import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TOKEN_F {
    private final String regex_string;
    private String [] function_tokens;

    public TOKEN_F() {
        this.regex_string = "F_[a-z]([a-z]|[0-9])*";
        this.function_tokens = new String[0];
    }

    public String[] getFunction_tokens() {
        return function_tokens;
    }

    public void setFunction_tokens(String[] function_tokens) {
        this.function_tokens = function_tokens;
    }

    public String [] splitFunctions(String string)
    {
        Pattern pattern = Pattern.compile(this.regex_string);

        Matcher matcher = pattern.matcher(string);

        List<String> matchedFunctions = new ArrayList<>();
        while (matcher.find()) {
            matchedFunctions.add(matcher.group());
        }

        this.function_tokens = matchedFunctions.toArray(new String[0]);
        return this.function_tokens;
    }

    public String getRegexString() {
        return regex_string;
    }
}
