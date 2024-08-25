public class TOKEN_F {
    TOKEN_F(){
        this.regex_string = "";
    }
    private String regex_string;
    private String [] function_tokens;

    public String[] getFunction_tokens() {
        return function_tokens;
    }

    public void setFunction_tokens(String[] function_tokens) {
        this.function_tokens = function_tokens;
    }
}
