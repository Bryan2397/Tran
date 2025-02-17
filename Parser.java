package Tran;
import AST.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private TokenManager toke;
    private TranNode tran;
    public Parser(TranNode top, List<Token> tokens) {
        this.toke = new TokenManager(tokens);
        this.tran = top;
    }

    public void Tran() throws SyntaxErrorException {
            Optional<Token> n = toke.peek(0);
            if(n.get().getType().equals(Token.TokenTypes.INTERFACE)){
                tran.Interfaces.add(Interface());
            }

    }


    private InterfaceNode Interface() throws SyntaxErrorException{
        InterfaceNode node = new InterfaceNode();
        List<MethodHeaderNode> no = new ArrayList<>();
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.INTERFACE)){
            toke.matchAndRemove(Token.TokenTypes.INTERFACE);
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
            Optional<Token> A = toke.matchAndRemove(Token.TokenTypes.WORD);
            node.name = A.get().getValue();
        }else {
            throw new SyntaxErrorException("Not a word Token", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.NEWLINE)){
            RequireNewLine();
            if(toke.peek(0).get().getType().equals(Token.TokenTypes.INDENT)){
                toke.matchAndRemove(Token.TokenTypes.INDENT);
            }
        }else {
            throw new SyntaxErrorException("no newline at:", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }

        while(!toke.peek(0).get().getType().equals(Token.TokenTypes.DEDENT)){
            no.add(MethodHeader());
        }
        toke.matchAndRemove(Token.TokenTypes.DEDENT);
        node.methods = no;
        return node;
    }

    private MethodHeaderNode MethodHeader() throws SyntaxErrorException {
        MethodHeaderNode node = new MethodHeaderNode();
        Optional<Token> s = toke.matchAndRemove(Token.TokenTypes.WORD);
        if (s.isPresent()){
            node.name = s.get().getValue();
        }else {
            throw new SyntaxErrorException("not a method name", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }

        if (toke.peek(0).get().getType().equals(Token.TokenTypes.LPAREN)){
            toke.matchAndRemove(Token.TokenTypes.LPAREN);
        }else {
            throw new SyntaxErrorException("not a left parenthesis", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        node.parameters = ParameterVariableDeclarations();

        if(toke.peek(0).get().getType().equals(Token.TokenTypes.RPAREN)){
            toke.matchAndRemove(Token.TokenTypes.RPAREN);
        }else {
            throw new SyntaxErrorException("not a right parenthesis", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }

        if(toke.peek(0).get().getType().equals(Token.TokenTypes.COLON)){
            toke.matchAndRemove(Token.TokenTypes.COLON);
            node.returns = ParameterVariableDeclarations();
            if(toke.peek(0).get().getType().equals(Token.TokenTypes.NEWLINE)){
                RequireNewLine();
            }
        }

        if(toke.peek(0).get().getType().equals(Token.TokenTypes.NEWLINE)){
            RequireNewLine();
        }

        return node;
    }

    private VariableDeclarationNode ParameterVariableDeclaration() throws SyntaxErrorException {
        VariableDeclarationNode node = new VariableDeclarationNode();
         if (toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)) {
             Optional<Token> I = toke.matchAndRemove(Token.TokenTypes.WORD);
             if(I.isPresent()){
                 node.type = I.get().getValue();
             }else{
                 throw new SyntaxErrorException("Not a ParameterVariable Declaration word", toke.getCurrentLine(), toke.getCurrentColumnNumber());
             }
         }
        if (toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
           Optional<Token> D = toke.matchAndRemove(Token.TokenTypes.WORD);
           if(D.isPresent()){
               node.name = D.get().getValue();

           }else{
               throw new SyntaxErrorException("Not a ParameterVariable Declaration word", toke.getCurrentLine(), toke.getCurrentColumnNumber());
           }
        }
        return node;
    }

    private List<VariableDeclarationNode> ParameterVariableDeclarations() throws SyntaxErrorException {
        List<VariableDeclarationNode> enter = new ArrayList<>();
        VariableDeclarationNode node = ParameterVariableDeclaration();
        enter.add(node);

        while(toke.peek(0).equals(Token.TokenTypes.COMMA)){
            toke.matchAndRemove(Token.TokenTypes.COMMA);
            node = ParameterVariableDeclaration();
            enter.add(node);
        }
        return enter;
    }

    private void RequireNewLine(){
        while(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.NEWLINE) ){
            toke.matchAndRemove(Token.TokenTypes.NEWLINE);
        }
    }


}
