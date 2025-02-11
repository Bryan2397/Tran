package Tran;
import java.util.List;
import java.util.Optional;

public class TokenManager {

    private final List<Token> member;

    public TokenManager(List<Token> tokens) {
        this.member = tokens;
    }

    public boolean done() {
        return member.isEmpty();
    }

    public Optional<Token> matchAndRemove(Token.TokenTypes t) {
        return Optional.ofNullable(member.removeFirst());
    }

    public Optional<Token> peek(int i) {
        return Optional.ofNullable(member.get(i));
    }

    public boolean nextTwoTokensMatch(Token.TokenTypes first, Token.TokenTypes second) {
	    return member.get(0).getType() == first && member.get(1).getType() == second;
    }

    public boolean nextIsEither(Token.TokenTypes first, Token.TokenTypes second) {
        return member.getFirst().getType().equals(first) && member.getFirst().getType().equals(second);
    }

    public int getCurrentLine() {
            return member.getFirst().getLineNumber();
    }

    public int getCurrentColumnNumber() {
            return member.getFirst().getColumnNumber();
    }
}
