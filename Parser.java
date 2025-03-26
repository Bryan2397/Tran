package Tran;
import AST.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public void Tran() throws SyntaxErrorException {
        while(!toke.done()){
            if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.INTERFACE)){
                tran.Interfaces.add(Interface());
            }
            if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.NEWLINE)){
                RequireNewLine();}
            if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.CLASS)){
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
            throw new SyntaxErrorException("Not a word Token for interface", toke.getCurrentLine(), toke.getCurrentColumnNumber());
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
        node.methods = no;
        toke.matchAndRemove(Token.TokenTypes.DEDENT);

        if(toke.done()){
            return node;
        }

        while(toke.peek(0).get().getType().equals(Token.TokenTypes.DEDENT)){
            if(toke.peek(0).get().getType().equals(Token.TokenTypes.DEDENT)){toke.matchAndRemove(Token.TokenTypes.DEDENT);}
        }

        return node;
    }

    // Class =  "class" IDENTIFIER ( "implements" IDENTIFIER ( "," IDENTIFIER )* )? NEWLINE INDENT ( Constructor | MethodDeclaration | Member )* DEDENT
    private ClassNode classes() throws SyntaxErrorException{
        ClassNode node = new ClassNode();
        List<String> inter = new ArrayList<>();
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
            while(toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
                Optional<Token> s = toke.matchAndRemove(Token.TokenTypes.WORD);
                inter.add(s.get().getValue());
                toke.matchAndRemove(Token.TokenTypes.COMMA);

            }
        }

        node.interfaces = inter;
        RequireNewLine();
        toke.matchAndRemove(Token.TokenTypes.INDENT);
        while (!toke.done() && !toke.peek(0).get().getType().equals(Token.TokenTypes.DEDENT)) {
            if (toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
                member.add(member());
                RequireNewLine();
            }
            node.members = member;
            if (toke.peek(0).get().getType().equals(Token.TokenTypes.CONSTRUCT)) {
                struct.add(construct());
            }
            node.constructors = struct;

            if (!toke.done() && toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)) {
                method.add(MethodDec());
            }
        }
        node.methods = method;

        while (!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.DEDENT)){
            toke.matchAndRemove(Token.TokenTypes.DEDENT);
        }

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

       Optional<Token> D = toke.matchAndRemove(Token.TokenTypes.WORD);
        node.name = D.get().getValue();
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.ASSIGN)){
            toke.matchAndRemove(Token.TokenTypes.ASSIGN);
            ExpressionNode n = expression();
            node.initializer = Optional.ofNullable(n);
        }

        return node;
    }





    // MethodDeclaration = "private"? "shared"? MethodHeader NEWLINE MethodBody
    private MethodDeclarationNode MethodDec() throws SyntaxErrorException{
        MethodDeclarationNode node = new MethodDeclarationNode();
        List<VariableDeclarationNode> nodes = new ArrayList<>();
        List<StatementNode> no = new ArrayList<>();
        boolean pivot = true;
        boolean share = true;
        if (toke.peek(0).get().getType().equals(Token.TokenTypes.PRIVATE)){
            node.isPrivate = pivot;
        }
        if (toke.peek(0).get().getType().equals(Token.TokenTypes.SHARED)) {
            toke.matchAndRemove(Token.TokenTypes.SHARED);
            node.isShared = share;
        }

        MethodHeaderNode name = MethodHeader();
        if (toke.peek(0).get().getType().equals(Token.TokenTypes.INDENT)){
            toke.matchAndRemove(Token.TokenTypes.INDENT);
        }
        node.name = name.name;
        node.parameters = name.parameters;
        node.returns = name.returns;
        RequireNewLine();

        while (toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            nodes.add(VariableDeclaration());
            node.locals = nodes;
            RequireNewLine();
        }
        node.statements = statements();

        return node;
    }


    // Statement = If | Loop | MethodCall | Assignment
    private StatementNode statement() throws SyntaxErrorException{
        StatementNode node = null;

        if(!toke.peek(0).get().getType().equals(Token.TokenTypes.IF) && !toke.peek(0).get().getType().equals(Token.TokenTypes.LOOP)){
            node = disambiguate();
        }

        if(toke.peek(0).get().getType().equals(Token.TokenTypes.IF)){
            node = MethodIF();
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.LOOP)){
            node = MethodLOOP();
        }
        return node;
    }


    private StatementNode disambiguate() throws SyntaxErrorException {
        StatementNode node = null;

        if(!toke.done() && toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.DOT) || toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)){
            node = MethodCallState();
        }
        if(!toke.done() && toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)){
            node = Assign();
        }
        if(!toke.done() && toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.COMMA)){
            node = MethodCallState();
        }
        return node;
    }

    private MethodCallStatementNode MethodCallState() throws SyntaxErrorException{
        MethodCallStatementNode node = new MethodCallStatementNode();
        List<VariableReferenceNode> no = new ArrayList<>();

        no.add(Reference());
        while(toke.peek(0).get().getType().equals(Token.TokenTypes.COMMA)){
            toke.matchAndRemove(Token.TokenTypes.COMMA);
            no.add(Reference());
        }
        toke.matchAndRemove(Token.TokenTypes.ASSIGN);

        MethodCallExpressionNode n;
        n = MethodCallExp();
        node.returnValues = no;
        node.parameters = n.parameters;
        node.methodName = n.methodName;
        node.objectName = n.objectName;
        RequireNewLine();
        return node;
    }

    // MethodCallExpression =  (IDENTIFIER ".")? IDENTIFIER "(" (Expression ("," Expression )* )? ")"
    private MethodCallExpressionNode MethodCallExp() throws SyntaxErrorException{
        MethodCallExpressionNode node = new MethodCallExpressionNode();
        List<ExpressionNode> exp = new ArrayList<>();
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
            Optional<Token> a = toke.matchAndRemove(Token.TokenTypes.WORD);
            node.methodName = a.get().getValue();

            if (toke.peek(0).get().getType().equals(Token.TokenTypes.DOT)){
                toke.matchAndRemove(Token.TokenTypes.DOT);
                if(toke.peek(0).isPresent()) {
                   Optional<Token> b = toke.matchAndRemove(Token.TokenTypes.WORD);
                    node.objectName = Optional.ofNullable(b.get().getValue());
                }else {
                    throw new SyntaxErrorException("missing word after dot in method call expression", toke.getCurrentLine(), toke.getCurrentColumnNumber());
                }
            }
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.LPAREN)){
            toke.matchAndRemove(Token.TokenTypes.LPAREN);
        }else {
            throw new SyntaxErrorException("missing left parenthesis at method call exp", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        exp.add(expression());
        while(toke.peek(0).get().getType().equals(Token.TokenTypes.COMMA)){
            toke.matchAndRemove(Token.TokenTypes.COMMA);
            exp.add(expression());
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.RPAREN)){
            toke.matchAndRemove(Token.TokenTypes.RPAREN);
        }else {
            throw new SyntaxErrorException("missing right parenthesis at method call exp", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        node.parameters = exp;
        return node;
    }

    //Assignment = VariableReference "=" Expression NEWLINE
    private AssignmentNode Assign() throws SyntaxErrorException{
        AssignmentNode node = new AssignmentNode();

        node.target = Reference();
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.ASSIGN)){
            toke.matchAndRemove(Token.TokenTypes.ASSIGN);
        }else {
            throw new SyntaxErrorException("missing equals at assign", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        node.expression = expression();
        RequireNewLine();
        return node;
    }

    private VariableReferenceNode Reference(){
        VariableReferenceNode node = new VariableReferenceNode();
        Optional<Token> a = toke.matchAndRemove(Token.TokenTypes.WORD);
        node.name = a.get().getValue();
        return node;
    }

    //BoolExpTerm = MethodCallExpression | (Expression ( "==" | "!=" | "<=" | ">=" | ">" | "<" ) Expression) | VariableReference
    private ExpressionNode BoolExpTerm() throws SyntaxErrorException {
        ExpressionNode node = null;
        CompareNode no = new CompareNode();

        if(!toke.done() && toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.NEWLINE)){
            node = Reference();
            return node;
        }

        if(toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.DOT) || toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)){
            node = MethodCallExp();
            return node;
        }

        no.left = expression();
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.LESSTHAN)){
            Optional<Token> a = toke.matchAndRemove(Token.TokenTypes.LESSTHAN);
            no.op = CompareNode.CompareOperations.lt;
        }else if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.LESSTHANEQUAL)){
            Optional<Token> b = toke.matchAndRemove(Token.TokenTypes.LESSTHANEQUAL);
            no.op = CompareNode.CompareOperations.le;
        } else if (!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.EQUAL)) {
            Optional<Token> c = toke.matchAndRemove(Token.TokenTypes.EQUAL);
            no.op = CompareNode.CompareOperations.eq;
        } else if (!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.GREATERTHANEQUAL)) {
            Optional<Token> d = toke.matchAndRemove(Token.TokenTypes.GREATERTHANEQUAL);
            no.op = CompareNode.CompareOperations.ge;
        } else if (!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.GREATERTHAN)) {
            Optional<Token> e = toke.matchAndRemove(Token.TokenTypes.GREATERTHAN);
            no.op = CompareNode.CompareOperations.gt;
        } else if (!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.NOTEQUAL)) {
            Optional<Token> f = toke.matchAndRemove(Token.TokenTypes.NOTEQUAL);
            no.op = CompareNode.CompareOperations.ne;
        }
        no.right = expression();
        node = no;
        RequireNewLine();
        return node;
    }

    //Expression = Term ( ("+"|"-") Term )*
    private ExpressionNode expression() throws SyntaxErrorException {
        ExpressionNode node = null;
        MathOpNode no = new MathOpNode();

        node = Term();
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.PLUS)){
            toke.matchAndRemove(Token.TokenTypes.PLUS);
            no.op = MathOpNode.MathOperations.add;
            no.left = node;
            no.right = Term();
            return no;
        }
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.MINUS)){
            toke.matchAndRemove(Token.TokenTypes.MINUS);
            no.left = node;
            no.op = MathOpNode.MathOperations.subtract;
            no.right = Term();
            return no;
        }

        return node;
    }
    //Term = Factor ( ("*"|"/"|"%") Factor )*
    private ExpressionNode Term() throws SyntaxErrorException{
        ExpressionNode node = null;
        MathOpNode no = new MathOpNode();
        node = Factor();
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.TIMES)){
            toke.matchAndRemove(Token.TokenTypes.TIMES);
            no.left = node;
            no.op = MathOpNode.MathOperations.multiply;
            no.right = Factor();
        }
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.DIVIDE)){
            toke.matchAndRemove(Token.TokenTypes.DIVIDE);
            no.left = node;
            no.op = MathOpNode.MathOperations.divide;
            no.right = Factor();
        }
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.MODULO)){
            toke.matchAndRemove(Token.TokenTypes.MODULO);
            no.left = node;
            no.op = MathOpNode.MathOperations.modulo;
            no.right = Factor();
        }
        return node;
    }
    //Factor = NUMBER | VariableReference |  STRINGLITERAL | CHARACTERLITERAL | MethodCallExpression | "(" Expression ")" | "new" IDENTIFIER "(" (Expression ("," Expression )*)? ")"
    private ExpressionNode Factor() throws SyntaxErrorException{
        ExpressionNode node = null;
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.WORD)){
            node = Reference();
        }
        if(!toke.done() && toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.DOT) || toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)){
            node = MethodCallExp();
            return node;
        }
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.NUMBER)){
            NumericLiteralNode no = new NumericLiteralNode();
            Optional<Token> a = toke.matchAndRemove(Token.TokenTypes.NUMBER);
            no.value = Float.parseFloat(a.get().getValue());
            return no;
        }
        return node;
    }
    // Loop = "loop" (VariableReference "=" )?  ( BoolExpTerm ) NEWLINE Statements
    private LoopNode MethodLOOP() throws SyntaxErrorException{
        LoopNode node = new LoopNode();
        toke.matchAndRemove(Token.TokenTypes.LOOP);
        if(!toke.done() && toke.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)){
            VariableReferenceNode a = Reference();
            node.assignment = Optional.ofNullable(a);
            toke.matchAndRemove(Token.TokenTypes.ASSIGN);
        }
        if(toke.peek(0).get().getType().equals(Token.TokenTypes.NEWLINE)){
            throw new SyntaxErrorException("missing condition for if loop", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }
        node.expression = BoolExpTerm();
        RequireNewLine();
        node.statements = statements();

        return node;
    }

    // If = "if" BoolExpTerm NEWLINE Statements ("else" NEWLINE (Statement | Statements))?
    private IfNode MethodIF() throws SyntaxErrorException {
        IfNode node = new IfNode();
        if(!toke.done() && toke.peek(0).get().getType().equals(Token.TokenTypes.IF)){
            toke.matchAndRemove(Token.TokenTypes.IF);
        }else {
            throw new SyntaxErrorException("no if statement at method if", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }

        if(toke.peek(0).get().getType().equals(Token.TokenTypes.NEWLINE)){
            throw new SyntaxErrorException("missing condition for if loop", toke.getCurrentLine(), toke.getCurrentColumnNumber());
        }

        node.condition = BoolExpTerm();
        RequireNewLine();
        node.statements = statements();
        if(toke.peek(0).get().getValue().equals("else")){
            node.elseStatement = Optional.of(Else());
        }else {
            node.elseStatement = Optional.empty();
        }
        RequireNewLine();

        return node;
    }

    //Statements = INDENT Statement*  DEDENT
    private List<StatementNode> statements() throws SyntaxErrorException{
        List<StatementNode> node = new ArrayList<>();
        while (toke.peek(0).get().getType().equals(Token.TokenTypes.INDENT)){
            toke.matchAndRemove(Token.TokenTypes.INDENT);}
        while (!toke.done() && !toke.peek(0).get().getType().equals(Token.TokenTypes.DEDENT)){
            node.add(statement());
        }
        toke.matchAndRemove(Token.TokenTypes.DEDENT);
        return node;
    }


    private ElseNode Else() throws SyntaxErrorException{
        ElseNode node = new ElseNode();
        toke.matchAndRemove(Token.TokenTypes.WORD);
        RequireNewLine();
        node.statements = statements();
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
