public class TOKEN_T {
    TOKEN_T(){
        this.regex_string = "";
    }
    private String regex_string;
    private String [] text_tokens;

    public String[] getText_tokens() {
        return text_tokens;
    }

    public void setText_tokens(String[] text_tokens) {
        this.text_tokens = text_tokens;
    }
}
