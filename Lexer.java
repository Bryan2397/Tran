package Tran;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Lexer {
    private TextManager text;
    int prevIndent = 0;
    int lineNumber = 1;
    int columnNumber = 0;

    HashMap<String, Token.TokenTypes> KeyWords = new HashMap<String, Token.TokenTypes>();

    public Lexer(String input) {
        this.text = new TextManager(input);
    }

    {
        KeyWords.put("=", Token.TokenTypes.ASSIGN);
        KeyWords.put("class", Token.TokenTypes.CLASS);
        KeyWords.put("construct", Token.TokenTypes.CONSTRUCT);
        KeyWords.put(".", Token.TokenTypes.DOT);
        KeyWords.put("if", Token.TokenTypes.IF);
        KeyWords.put("/", Token.TokenTypes.DIVIDE);
        KeyWords.put("implements", Token.TokenTypes.IMPLEMENTS);
        KeyWords.put("\t", Token.TokenTypes.INDENT);
        KeyWords.put("-", Token.TokenTypes.MINUS);
        KeyWords.put("else", Token.TokenTypes.ELSE);
        KeyWords.put("<", Token.TokenTypes.LESSTHAN);
        KeyWords.put("new", Token.TokenTypes.NEW);
        KeyWords.put("interface", Token.TokenTypes.INTERFACE);
        KeyWords.put("==", Token.TokenTypes.EQUAL);
        KeyWords.put("shared", Token.TokenTypes.SHARED);
        KeyWords.put(">=", Token.TokenTypes.GREATERTHANEQUAL);
        KeyWords.put("loop", Token.TokenTypes.LOOP);
        KeyWords.put("<=", Token.TokenTypes.LESSTHANEQUAL);
        KeyWords.put(">", Token.TokenTypes.GREATERTHAN);
        KeyWords.put("(", Token.TokenTypes.LPAREN);
        KeyWords.put(")", Token.TokenTypes.RPAREN);
        KeyWords.put("private", Token.TokenTypes.PRIVATE);
        KeyWords.put("+", Token.TokenTypes.PLUS);
        KeyWords.put("!=", Token.TokenTypes.NOTEQUAL);
        KeyWords.put("%", Token.TokenTypes.MODULO);
        KeyWords.put("\n", Token.TokenTypes.NEWLINE);
        KeyWords.put("*", Token.TokenTypes.TIMES);
        KeyWords.put(",", Token.TokenTypes.COMMA);
        KeyWords.put("\'", Token.TokenTypes.QUOTEDCHARACTER);
        KeyWords.put("\"", Token.TokenTypes.QUOTEDSTRING);
        KeyWords.put(":", Token.TokenTypes.COLON);
    }

    public List<Token> Lex() throws Exception {
        var retVal = new LinkedList<Token>();

        while (text.isAtEnd()) {
            char x = text.peekCharacter();

            if (x == '\n') { // collects all \n tokens
                int currIndent = 0;
                text.getCharacter();
                lineNumber++;
                columnNumber = 0;
                Token newline = new Token(Token.TokenTypes.NEWLINE, lineNumber, columnNumber);
                retVal.add(newline);

                if (!text.isAtEnd() && prevIndent > currIndent) { // for when the input ends after \n and we have dedent tokens to collect
                    for (int i = 0; i < prevIndent - currIndent; i++) {
                        Token dedent = new Token(Token.TokenTypes.DEDENT, lineNumber, columnNumber);
                        retVal.add(dedent);
                    }
                    return retVal;
                } else if (!text.isAtEnd()) { // for when the end of the input is just \n an we have no indents or dedents
                    return retVal;
                }

                    x = text.peekCharacter(); // to check if the next character is a \n
                    if((x == '\t') || (x == ' ')){
                        currIndent = tabsAndSpaces();
                    }
                    x = text.peekCharacter();
                    if(x != '\n'){ // if what's next is not a newline
                        if (currIndent > prevIndent) { // if we have more current indents than the last line
                            for (int i = 0; i < currIndent - prevIndent; i++) {
                                Token indent = new Token(Token.TokenTypes.INDENT, lineNumber, columnNumber);
                                retVal.add(indent);
                            }
                        } else if (prevIndent > currIndent) { // if we have more prevents indents than the last line
                            for (int i = 0; i < prevIndent - currIndent; i++) {
                                Token dedent = new Token(Token.TokenTypes.DEDENT, lineNumber, columnNumber);
                                retVal.add(dedent);
                            }
                        }
                        prevIndent = currIndent; // sets the current indent after making indent/dedent tokens
                    }

            } else if (x == '"') {
                retVal.add(readQuotes());
            } else if (Character.isLetter(x)) {
                retVal.add(readWord());
            } else if (Character.isDigit(x) || x == '.') {
                retVal.add(readNumber());
            } else if (x == '{') {
                readComments();
            } else if (x == ' ' || x == '\r') {
                text.getCharacter();
                columnNumber++;
            } else if(KeyWords.containsKey(String.valueOf(x))){
                retVal.add(readPunctuation());
            }

        }
        int currIndent = tabsAndSpaces();
        if (currIndent > prevIndent) { // if we have more current indents than the last line
            for (int i = 0; i < currIndent - prevIndent; i++) {
                Token indent = new Token(Token.TokenTypes.INDENT, lineNumber, columnNumber);
                retVal.add(indent);
            }
        } else if (prevIndent > currIndent) { // if we have more prevents indents than the last line
            for (int i = 0; i < prevIndent - currIndent; i++) {
                Token dedent = new Token(Token.TokenTypes.DEDENT, lineNumber, columnNumber);
                retVal.add(dedent);
            }
        }
        return retVal;
    }

    private int tabsAndSpaces(){
        int count = 0;
        int foo = 0;
        if(!text.isAtEnd()){
            return 0;
        }
        while (text.peekCharacter() == ' ' || text.peekCharacter() == '\t'){
            String a = String.valueOf(text.peekCharacter());
            if (a.equals("\t")) { // collects tabs and adjusts current indentation
                char y = text.peekCharacter();
                while (y == '\t') {
                    text.getCharacter();
                    columnNumber++;
                    foo++;
                    y = text.peekCharacter();
                }

            }else if (a.equals(" ")) { // collects spaces and adjusts current indentation
                count++;
                char y = text.peekCharacter();

                while (y == ' ') {
                    if (count % 4 == 0) {
                        foo++;
                        count = count - 4;
                    }
                    text.getCharacter();
                    count++;
                    columnNumber++;
                    y = text.peekCharacter();
                }
            }
        }
        return foo;
    }

    private Token readWord() {
        String CurrentWord = "";

        while (text.isAtEnd()) {
            char b = text.peekCharacter();

            if (KeyWords.containsKey(CurrentWord)) {
                return new Token(KeyWords.get(CurrentWord), lineNumber, columnNumber, CurrentWord);
            }
            String s = String.valueOf(b);
            if (KeyWords.containsKey(s) || s.equals(" ") && !KeyWords.containsKey(CurrentWord)) {
                return new Token(Token.TokenTypes.WORD, lineNumber, columnNumber, CurrentWord);
            }

            char c = text.getCharacter();
            columnNumber++;


            if (Character.isLetter(c) || Character.isDigit(c)) {
                CurrentWord = CurrentWord + c;
            } else if (!Character.isLetter(c) && !Character.isDigit(c)) {
                return new Token(Token.TokenTypes.WORD, lineNumber, columnNumber, CurrentWord);
            }

        }


        if (!CurrentWord.isEmpty()) {
            if (KeyWords.containsKey(CurrentWord)) {
                return new Token(KeyWords.get(CurrentWord), lineNumber, columnNumber, CurrentWord);
            } else {
                return new Token(Token.TokenTypes.WORD, lineNumber, columnNumber, CurrentWord);
            }
        }

        return null;
    }

    private Token readNumber() {
        String CurrentWord = "";
        int two = 0;

        while (text.isAtEnd()) {
            String C = String.valueOf(text.getCharacter());
            columnNumber++;
            if (!Character.isDigit(text.dots(0)) && C.equals(".")) {
                return new Token(Token.TokenTypes.DOT, lineNumber, columnNumber);
            }

            if (C.equals(".")) {
                two++;
            }
            if (C.equals("-")) {
                CurrentWord = CurrentWord + C;
            }

            if (!text.isAtEnd()) {
                CurrentWord = CurrentWord + C;
                return new Token(Token.TokenTypes.NUMBER, lineNumber, columnNumber, CurrentWord);
            }

            String d = String.valueOf(text.peekCharacter());
            CurrentWord = CurrentWord + C;


            if (d.equals(" ")) {
                return new Token(Token.TokenTypes.NUMBER, lineNumber, columnNumber, CurrentWord);
            }
            if (d.equals(".") && two < 1) {
                C = String.valueOf(text.getCharacter());
                lineNumber++;
                CurrentWord = CurrentWord + C;
                two++;
            } else if (KeyWords.containsKey(d)) {
                return new Token(Token.TokenTypes.NUMBER, lineNumber, columnNumber, CurrentWord);
            }
        }
        return null;
    }

    private Token readPunctuation() {
        while (text.isAtEnd()) {
            char C = text.getCharacter();
            String CurrPun = String.valueOf(C);
            if (!text.isAtEnd()) {
                return new Token(KeyWords.get(CurrPun), lineNumber, columnNumber);
            }
            char d = text.peekCharacter();
            if (!CurrPun.isEmpty() && d == ' ') {
                return new Token(KeyWords.get(CurrPun), lineNumber, columnNumber);
            }
            CurrPun = CurrPun + d;

            if (KeyWords.containsKey(CurrPun)) {
                char a = text.getCharacter();
                columnNumber++;
                return new Token(KeyWords.get(CurrPun), lineNumber, columnNumber);
            } else {
                CurrPun = String.valueOf(C);
                if (KeyWords.containsKey(CurrPun)) {
                    return new Token(KeyWords.get(CurrPun), lineNumber, columnNumber);
                }
            }
        }
        return null;
    }

    private void readComments() {
        int count = 0;
        while (text.isAtEnd()) {
            char c = text.getCharacter();
            columnNumber++;
            if (c == '{') {
                count++;
            }
            if (c == '}') {
                count--;
            }
            if (count == 0) {
                break;
            }
        }
    }

    private Token readQuotes() {
        String CurrentWord = "";
        int two = 0;

        while (text.isAtEnd()) {
            char c = text.getCharacter();
            lineNumber++;
            if (c == '"') {
                two++;
            }
            if (c == '"' && two == 2) {
                return new Token(Token.TokenTypes.QUOTEDSTRING, lineNumber, columnNumber, CurrentWord);
            }

            if (Character.isLetter(c) || c == ' ') {
                CurrentWord = CurrentWord + c;
            }
        }
        return null;
    }
}
