public class LexicalError extends Exception {
    private final String token;

    public LexicalError(String message, String token) {
        super(message);
        this.token = token;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Token: \"" + token + "\"";
    }
}