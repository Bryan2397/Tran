package Tran;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Lexer {
    private TextManager text;
    int prevIndent = 0;
    int currIndent = 0;
    int lineNumber = 1;
    int columnNumber = 0;

    HashMap <String,Token.TokenTypes> KeyWords = new HashMap<String,Token.TokenTypes>();
    public Lexer(String input) {
        this.text = new TextManager(input);
    }
    {

        KeyWords.put("number", Token.TokenTypes.NUMBER);
        KeyWords.put("=", Token.TokenTypes.ASSIGN);
        KeyWords.put("class", Token.TokenTypes.CLASS);
        KeyWords.put("construct",Token.TokenTypes.CONSTRUCT);
        KeyWords.put(".",Token.TokenTypes.DOT);
        KeyWords.put("if",Token.TokenTypes.IF);
        KeyWords.put("/",Token.TokenTypes.DIVIDE);
        KeyWords.put("implements",Token.TokenTypes.IMPLEMENTS);
        KeyWords.put("\t",Token.TokenTypes.INDENT);
        KeyWords.put("-",Token.TokenTypes.MINUS);
        KeyWords.put("else",Token.TokenTypes.ELSE);
        KeyWords.put("<",Token.TokenTypes.LESSTHAN);
        KeyWords.put("new",Token.TokenTypes.NEW);
        KeyWords.put("interface",Token.TokenTypes.INTERFACE);
        KeyWords.put("==",Token.TokenTypes.EQUAL);
        KeyWords.put("shared",Token.TokenTypes.SHARED);
        KeyWords.put(">=",Token.TokenTypes.GREATERTHANEQUAL);
        KeyWords.put("loop",Token.TokenTypes.LOOP);
        KeyWords.put("<=",Token.TokenTypes.LESSTHANEQUAL);
        KeyWords.put(">", Token.TokenTypes.GREATERTHAN);
        KeyWords.put("(",Token.TokenTypes.LPAREN);
        KeyWords.put(")", Token.TokenTypes.RPAREN);
        KeyWords.put("private",Token.TokenTypes.PRIVATE);
        KeyWords.put("+",Token.TokenTypes.PLUS);
        KeyWords.put("!=",Token.TokenTypes.NOTEQUAL);
        KeyWords.put("%", Token.TokenTypes.MODULO);
        KeyWords.put("\n", Token.TokenTypes.NEWLINE);
        KeyWords.put("*",Token.TokenTypes.TIMES);
        KeyWords.put(",",Token.TokenTypes.COMMA);
        KeyWords.put("\'",Token.TokenTypes.QUOTEDCHARACTER);
        KeyWords.put("\"",Token.TokenTypes.QUOTEDSTRING);
        KeyWords.put(":", Token.TokenTypes.COLON);
    }
    public List<Token> Lex() throws Exception {
        var retVal = new LinkedList<Token>();


        while (text.isAtEnd()) {
            char x = text.peekCharacter();


            if(x == '{'){
                while (text.isAtEnd() ){
                    int count = 0;
                    char c = text.getCharacter();
                    columnNumber++;
                    if(c == '{'){
                        count++;
                    }
                    if(c == '}'){
                        count--;
                    }
                    if (!text.isAtEnd() && count == 0){
                        break;
                    }

                }
            } else if(x == '\n'){
                lineNumber++;
                columnNumber = 0;
                String a = String.valueOf(text.getCharacter());
                columnNumber++;
                Token newline = new Token(Token.TokenTypes.NEWLINE,lineNumber,columnNumber, a);
                retVal.add(newline);
                if(!text.isAtEnd() && prevIndent > currIndent){
                    for (int i = 0; i < prevIndent - currIndent; i++){
                        Token dedent = new Token(Token.TokenTypes.DEDENT,lineNumber,columnNumber);
                        retVal.add(dedent);
                    }
                }
                if(text.isAtEnd()){
                    x = text.peekCharacter();
                    if(x != ' '){
                        for(int i = 0; i < prevIndent - currIndent; i++){
                            Token dedent = new Token(Token.TokenTypes.DEDENT,0,0);
                            retVal.add(dedent);
                        }
                    }
                    if(x == ' '){
                        char y = text.peekCharacter();
                        while(y == ' '){
                            text.getCharacter();
                            columnNumber++;
                            currIndent++;
                            y = text.peekCharacter();
                            if(y == '\t'){
                                columnNumber = columnNumber + 4;
                                currIndent = currIndent+4;
                            }
                        }
                        if(currIndent % 4 == 0){
                            currIndent = currIndent / 4;
                            if(currIndent > prevIndent){
                                for(int i = 0; i < currIndent - prevIndent; i++){
                                    Token indent = new Token(Token.TokenTypes.INDENT,lineNumber,columnNumber);
                                    retVal.add(indent);
                                }
                            }else if(prevIndent > currIndent){
                                for (int i = 0; i < prevIndent - currIndent; i++){
                                    Token dedent = new Token(Token.TokenTypes.DEDENT,lineNumber,columnNumber);
                                    retVal.add(dedent);
                                }
                            }
                        }

                        prevIndent = currIndent;
                        currIndent = 0;
                    }else if(x == '\t'){
                        char y = text.peekCharacter();
                        while(y == '\t'){
                            text.getCharacter();
                            columnNumber++;
                            currIndent++;
                            y = text.peekCharacter();

                        }

                        if(currIndent > prevIndent){
                            for(int i = 0; i < currIndent - prevIndent; i++){
                                Token indent = new Token(Token.TokenTypes.INDENT,lineNumber,columnNumber);
                                retVal.add(indent);
                            }
                        } else if (prevIndent > currIndent) {
                            for(int i = 0; i < prevIndent - currIndent; i++){
                                Token dedent = new Token(Token.TokenTypes.DEDENT,lineNumber,columnNumber);
                                retVal.add(dedent);
                            }
                        }
                        prevIndent = currIndent;
                        currIndent = 0;
                    }
                }
            } else if (x == '"') {
                retVal.add(readQuotes());
            } else if (Character.isLetter(x)) {
                retVal.add(readWord());
            } else if (Character.isDigit(x) || x == '.') {
                retVal.add(readNumber());
            }else if(x == ' '){
                char z = text.getCharacter();
                columnNumber++;
            }else{
                retVal.add(readPunctuation());
            }
        }
        return retVal;
    }


    public Token readWord(){
        String CurrentWord = "";

        while(text.isAtEnd()){
            char b = text.peekCharacter();


            if(KeyWords.containsKey(CurrentWord)) {
                return new Token(KeyWords.get(CurrentWord),lineNumber,columnNumber);
            }

            String s = String.valueOf(b);
            if(KeyWords.containsKey(s)){
                return new Token(Token.TokenTypes.WORD,lineNumber,columnNumber, CurrentWord);
            }
            char c = text.getCharacter();
            columnNumber++;



            if(Character.isLetter(c)){
                CurrentWord = CurrentWord + c;
            } else if (!CurrentWord.isEmpty() && !Character.isLetter(c)) {
                return new Token(Token.TokenTypes.WORD,lineNumber,columnNumber,CurrentWord);
            }

        }

        if(!CurrentWord.isEmpty()){
            if(KeyWords.containsKey(CurrentWord)){
                return new Token(KeyWords.get(CurrentWord),lineNumber,columnNumber, CurrentWord);
            }else{
                return new Token(Token.TokenTypes.WORD,lineNumber,columnNumber, CurrentWord);
            }
        }

        return null;
    }

    public Token readNumber(){
        String CurrentWord = "";
        int two = 0;

        while(text.isAtEnd()){
            String C = String.valueOf(text.getCharacter());
            lineNumber++;
            if(C.equals(".")){
                two++;
            }
            if(C.equals("-")){
                CurrentWord = CurrentWord + C;
            }


            if(!text.isAtEnd()){
                CurrentWord = CurrentWord + C;
                return new Token(Token.TokenTypes.NUMBER,lineNumber,columnNumber, CurrentWord);
            }
            String d = String.valueOf(text.peekCharacter());
            CurrentWord = CurrentWord + C;

            if(d.equals(" ")){
                return new Token(Token.TokenTypes.NUMBER,lineNumber,columnNumber, CurrentWord);
            }

            if(d.equals(".") && two < 1){
                C = String.valueOf(text.getCharacter());
                lineNumber++;
                CurrentWord = CurrentWord + C;
                two++;
            } else if (KeyWords.containsKey(d)) {
                return new Token(Token.TokenTypes.NUMBER,lineNumber,columnNumber, CurrentWord);
            }
        }
        return null;
    }

    public Token readPunctuation(){
        while (text.isAtEnd()){
            char C = text.getCharacter();

            String CurrPun = String.valueOf(C);
            if(!text.isAtEnd()){
                return new Token(KeyWords.get(CurrPun),lineNumber,columnNumber);
            }
            char d = text.peekCharacter();
            if(!CurrPun.isEmpty() && d == ' '){
                return new Token(KeyWords.get(CurrPun),lineNumber,columnNumber);
            }
            CurrPun = CurrPun + d;

            if(KeyWords.containsKey(CurrPun)){
                char a = text.getCharacter();
                columnNumber++;
                return new Token(KeyWords.get(CurrPun),lineNumber,columnNumber);
            } else {
                CurrPun = String.valueOf(C);
                if(KeyWords.containsKey(CurrPun)){
                    return new Token(KeyWords.get(CurrPun),lineNumber,columnNumber);
                }
            }

        }

        return null;
    }

    public Token readQuotes() {
        String CurrentWord = "";
        int two = 0;

        while(text.isAtEnd()){
            char c = text.getCharacter();
            lineNumber++;
            if(c == '"'){
                two++;
            }
            if(c == '"' && two == 2){
                return new Token(Token.TokenTypes.QUOTEDSTRING,lineNumber,columnNumber,CurrentWord);
            }else {

            }

            if(Character.isLetter(c)){
                CurrentWord = CurrentWord + c;
            }

        }

        return null;
    }

}
