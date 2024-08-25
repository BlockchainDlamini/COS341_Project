public class TOKEN_N {
    TOKEN_N(){
        this.regex_string = "";
    }
    private String regex_string;
    private String [] number_tokens;

    public String[] getNumber_tokens() {
        return number_tokens;
    }

    public void setNumber_tokens(String[] number_tokens) {
        this.number_tokens = number_tokens;
    }
}
