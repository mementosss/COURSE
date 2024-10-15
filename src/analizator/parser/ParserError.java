package analizator.parser;

public class ParserError extends Error {
    public ParserError(String message, int line, int pos){
        super("[ERROR] \"" + message + "\" -> (" + line + " : " + pos + ")");

    }

    public String toString(){
        return getMessage();
    }
}
