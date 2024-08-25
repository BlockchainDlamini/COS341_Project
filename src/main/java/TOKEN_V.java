public class TOKEN_V {
    TOKEN_V(){
        this.regex_string = "";
    }
    private String regex_string;
    private String [] variable_tokens;

    public String[] getVariable_tokens() {
        return variable_tokens;
    }

    public void setVariable_tokens(String[] variable_tokens) {
        this.variable_tokens = variable_tokens;
    }
}
