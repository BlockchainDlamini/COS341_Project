import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TOKEN_V {
    private final String regex_string;
    private String [] variable_tokens;

    public TOKEN_V() {
        this.regex_string = "V_[a-z]([a-z]|[0-9])*";
        this.variable_tokens = new String[0];
    }

    public String[] getVariable_tokens() {
        return variable_tokens;
    }

    public void setVariable_tokens(String[] variable_tokens) {
        this.variable_tokens = variable_tokens;
    }

    public String [] splitVariables(String string)
    {
        Pattern pattern = Pattern.compile(this.regex_string);

        Matcher matcher = pattern.matcher(string);

        List<String> matchedVariables = new ArrayList<>();
        while (matcher.find()) {
            matchedVariables.add(matcher.group());
        }

        this.variable_tokens = matchedVariables.toArray(new String[0]);
        return this.variable_tokens;
    }

    public String getRegexString() {
        return regex_string;
    }
}
