package Tran;
public class TextManager {
    private String input;
    private int position;
    public TextManager(String input) {
        this.input = input;
        this.position = 0;
    }

    public boolean isAtEnd() {
	    if(position < input.length()){
            return true;
        }
        return false;
    }

    public char dots(int pos){
        int here = pos + position;
        if(here < input.length()){
            char go = input.charAt(here);
            return go;
        }
        return '\0';
    }

    public char peekCharacter() {
            char c = input.charAt(position);
            return c;
    }

    public char peekCharacter(int dist) {
            dist = position + dist;
            return input.charAt(dist);
    }

    public char getCharacter() {
            char c = input.charAt(position);
            position++;
            return c;
    }
}
