import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Reserved_Keywords_Token {
    Reserved_Keywords_Token(){
        this.regex_string = String.join("|", this.keywords);
    }
    private final String regex_string;
    private final String [] keywords = {"for", "not","sqrt", "or", "and", "eq", "grt", "add", "sub", "mul", "div", "num", "void", "begin", "end", "halt","\\)","\\{", "print", "\\<input", "\\=", "if", "then", "else", "green","main", "text", "\\(","\\}", "\\,", "\\;"};
    private String [] reserved_keyword_tokens;

    public String[] getReserved_Keywords_Token() {
        return reserved_keyword_tokens;
    }

    public String [] splitKeywords(String string)
    {
        Pattern pattern = Pattern.compile(this.regex_string);

        Matcher matcher = pattern.matcher(string);

        List<String> matchedKeywords = new ArrayList<>();
        while (matcher.find()) {
            matchedKeywords.add(matcher.group());
        }

        this.reserved_keyword_tokens = matchedKeywords.toArray(new String[0]);
        return this.reserved_keyword_tokens;
    }

    public void setReserved_Keywords_Token(String[] rk_tokens) {
        this.reserved_keyword_tokens = rk_tokens;
    }

    public static void main(String[] args) {
        Reserved_Keywords_Token v = new Reserved_Keywords_Token();
        String [] res = v.splitKeywords("for <input the only things( i do if and only if (the main ) ((can help it then i cant help it;sqrt;".replaceAll("\\s+",""));
        System.out.println(Arrays.toString(res));
    }
}
