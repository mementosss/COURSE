package analizator.lexer;

public class Token {
    public Type type = Type.EMPTY;
    public String word = "";
    public int line = -1;
    public int position = -1;

    public Token(Type type, String word, int line, int position){
        this.type = type;
        this.word = word;
        this.line = line;
        this.position = position;
    }

    public Token(){}

    public String toString(){
        if(this.word == "\n")
            return "(" + this.type + ", " + "\\n" + ", " + this.line + ", " + this.position + ")";
        return "(" + this.type + ", " + this.word + ", " + this.line + ", " + this.position + ")";
    }
}