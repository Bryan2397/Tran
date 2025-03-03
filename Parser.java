package Tran;
import AST.*;

import java.util.ArrayList;
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
        while(!toke.done()){
            if(toke.peek(0).get().getType().equals(Token.TokenTypes.INTERFACE)){
                tran.Interfaces.add(Interface());
            }
            if(toke.peek(0).get().getType().equals(Token.TokenTypes.CLASS)){
                tran.Classes.add(classes());
            }
        }
    }


    private InterfaceNode Interface() throws SyntaxErrorException{
        InterfaceNode node = new InterfaceNode();
        List<MethodHeaderNode> no = new ArrayList<>();

        toke.matchAndRemove(Token.TokenTypes.INTERFACE);

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

    // Class =  "class" IDENTIFIER ( "implements" IDENTIFIER ( "," IDENTIFIER )* )? NEWLINE INDENT ( Constructor | MethodDeclaration | Member )* DEDENT
    private ClassNode classes() throws SyntaxErrorException{
        ClassNode node = new ClassNode();
        List<ConstructorNode> struct = new ArrayList<>();
        List<MethodDeclarationNode> method = new ArrayList<>();
        List<MemberNode> member = new ArrayList<>();
        toke.matchAndRemove(Token.TokenTypes.CLASS);
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
            Optional<Token> n = toke.matchAndRemove(Token.TokenTypes.WORD);
            node.name = n.get().getValue();
        }else{
            throw new SyntaxErrorException("no class name after class keyword", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }

        if(toke.peek(0).get().getType().equals(Token.TokenTypes.IMPLEMENTS)){
            toke.matchAndRemove(Token.TokenTypes.IMPLEMENTS);
            toke.matchAndRemove(Token.TokenTypes.WORD);
            if(toke.peek(0).equals(Token.TokenTypes.COMMA)){
                while(toke.peek(0).get().getType().equals(Token.TokenTypes.COMMA)){
                    toke.matchAndRemove(Token.TokenTypes.COMMA);
                    toke.matchAndRemove(Token.TokenTypes.WORD);
                }
            }
        }


        RequireNewLine();
        toke.matchAndRemove(Token.TokenTypes.INDENT);
        while(toke.peek(0).get().getType().equals(Token.TokenTypes.CONSTRUCT)){
            struct.add(construct());
        }
        node.constructors = struct;
        while(toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)){
            method.add(MethodDec());
        }
        node.methods = method;
        while (toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)){
            member.add(member());
        }
        node.members = member;
        toke.matchAndRemove(Token.TokenTypes.DEDENT);

        return node;
    }

    // Constructor = "construct" "(" ParameterVariableDeclarations ")" NEWLINE MethodBody
    private ConstructorNode construct() throws SyntaxErrorException{
        ConstructorNode node = new ConstructorNode();
        List<VariableDeclarationNode> nodes;
        List<VariableDeclarationNode> lokals = new ArrayList<>();
        List<StatementNode> n = new ArrayList<>();

        toke.matchAndRemove(Token.TokenTypes.CONSTRUCT);

        if(toke.peek(0).get().getType().equals(Token.TokenTypes.LPAREN)){
            toke.matchAndRemove(Token.TokenTypes.LPAREN);
        }else {
            throw new SyntaxErrorException("no left parenthesis after construct", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        nodes = ParameterVariableDeclarations();
        node.parameters = nodes;

        if (toke.peek(0).get().getType().equals(Token.TokenTypes.RPAREN)){
            toke.matchAndRemove(Token.TokenTypes.RPAREN);
        }else {
            throw new SyntaxErrorException("no right parenthesis after construct", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.NEWLINE)){
            RequireNewLine();
        }

        // MethodBody = INDENT ( VariableDeclarations )*  Statement* DEDENT
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.INDENT)){
            toke.matchAndRemove(Token.TokenTypes.INDENT);
        }else {
            throw new SyntaxErrorException("missing indent after newline in construct", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }

        while(toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
            lokals.add(VariableDeclaration());
        }
        node.locals = lokals;
        node.statements = statements();
        toke.matchAndRemove(Token.TokenTypes.DEDENT);

        return node;
    }


    // Member = VariableDeclarations
    private MemberNode member() throws SyntaxErrorException{
        MemberNode node = new MemberNode();
        node.declaration = ParameterVariableDeclaration();

        return node;
    }

    // VariableDeclarations =  IDENTIFIER VariableNameValue ("," VariableNameValue)* NEWLINE
    private VariableDeclarationNode VariableDeclaration() throws SyntaxErrorException{
        VariableDeclarationNode node = new VariableDeclarationNode();

        Optional<Token> I = toke.matchAndRemove(Token.TokenTypes.WORD);
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
            node.type = I.get().getValue();
        }else{
            throw new SyntaxErrorException("missing first word in variable declarations", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.ASSIGN)){
            toke.matchAndRemove(Token.TokenTypes.ASSIGN);
           /* if(toke.peek(0).isPresent()){
                node.initializer = toke.matchAndRemove();
            }*/
        }
       Optional<Token> D = toke.matchAndRemove(Token.TokenTypes.WORD);
        node.name = D.get().getValue();
        return node;
    }





    // MethodDeclaration = "private"? "shared"? MethodHeader NEWLINE MethodBody
    private MethodDeclarationNode MethodDec() throws SyntaxErrorException{
        MethodDeclarationNode node = new MethodDeclarationNode();
        List<VariableDeclarationNode> nodes = new ArrayList<>();
        boolean pivot = true;
        boolean share = true;
        if (toke.matchAndRemove(Token.TokenTypes.PRIVATE).isPresent()){
            node.isPrivate = pivot;
        }
        if (toke.matchAndRemove(Token.TokenTypes.SHARED).isPresent()) {
            toke.matchAndRemove(Token.TokenTypes.SHARED);
            node.isShared = share;
        }

        MethodHeaderNode name = MethodHeader();
        node.name = name.name;
        node.parameters = name.parameters;
        node.returns = name.returns;
        RequireNewLine();
        while(toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
            nodes = ParameterVariableDeclarations();
            node.locals = nodes;
        }
        node.statements = statements();
        return node;
    }

    // Statement = If | Loop | MethodCall | Assignment
    private StatementNode statement() throws SyntaxErrorException{
        StatementNode node = null;
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.IF)){
            node = MethodIF();
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.LOOP)){
            node = MethodLOOP();
        }
        /*if(toke.peek(0).get().getType().equals(Token.TokenTypes.ASSIGN)){
           node = Assign();
        }*/

        return node;
    }

    // Loop = "loop" (VariableReference "=" )?  ( BoolExpTerm ) NEWLINE Statements
    private LoopNode MethodLOOP() {
        LoopNode node = new LoopNode();

        return node;
    }

    //Statements = INDENT Statement*  DEDENT
    private List<StatementNode> statements() throws SyntaxErrorException{
        List<StatementNode> node = new ArrayList<>();
        toke.matchAndRemove(Token.TokenTypes.INDENT);
        while (toke.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()){
            node.add(statement());
        }
        toke.matchAndRemove(Token.TokenTypes.DEDENT);
        return node;
    }


    // If = "if" BoolExpTerm NEWLINE Statements ("else" NEWLINE (Statement | Statements))?
    private IfNode MethodIF() throws SyntaxErrorException {
        IfNode node = new IfNode();
        toke.matchAndRemove(Token.TokenTypes.IF);
        BoolExpTerm();
        RequireNewLine();
        node.statements = statements();
        if(toke.peek(0).get().getValue().equals("else")){
            node.elseStatement = Optional.of(Else());
        }
        RequireNewLine();

        return node;
    }

    private ElseNode Else() throws SyntaxErrorException{
        ElseNode node = new ElseNode();
        toke.matchAndRemove(Token.TokenTypes.WORD);
        RequireNewLine();
        node.statements = statements();
        return node;
    }

    private void BoolExpTerm() {
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

        while(toke.peek(0).get().getType().equals(Token.TokenTypes.COMMA)){
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
