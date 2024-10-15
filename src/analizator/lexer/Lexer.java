package analizator.lexer;

import java.util.ArrayList;

public class Lexer {
    public ArrayList<Token> tokens = new ArrayList<Token>();
    String word = "";
    String text;
    char symbol;
    boolean description = false;
    int line = 0;
    int pos = 0;

    public Lexer(String text){
        this.text = text + " ";
        this.symbol = '\0';
        decode();
    }

    public void decode(){
        for(int i = 0; i < text.length(); i++){
            Type type = Type.getType(word);
            if (symbol == ' ') {
                description = false;
            }
            if(type == Type.EMPTY){
                addSymbol(i);
            }
            else{
                i--;
                if(type == Type.END_DESCRIPTION)
                    description = false;
                if(!description)
                    tokens.add(new Token(type, word, line, pos - word.length()));
                else{
                    tokens.add(new Token(Type.DESCRIPTION, word, line, pos - word.length()));
                }
                if(type == Type.VAR)
                    description = true;
                word = "";
            }
        }
    }

    public void addSymbol(int i){
        symbol = text.charAt(i);
        pos++;
        if(symbol != ' ' & symbol != '\n' & symbol != ';' & symbol != ',' & symbol != '\t' & symbol != ')' & symbol != ']')
            word += symbol;
        if(symbol == ' ' & word.length() != 0){
            tokens.add(new Token((description ? Type.DESCRIPTION : Type.ID), word, line, pos - word.length()));
            word = "";
        }
        if(symbol == '\n'){
            line++;
            pos = 0;
            word = "";
            tokens.add(new Token(Type.LINE_BREAK, "", line, pos - word.length()));
        }
        if(symbol == ';'){
            if(isInteger(word))
                if(word.indexOf(".") == -1)
                    tokens.add(new Token(Type.INTEGER, word, line, pos - word.length()));
                else {
                    tokens.add(new Token(Type.FLOAT, word, line, pos - word.length()));
                }
            else
            if(word.length() != 0)
                if(!Character.isDigit(word.charAt(0)))
                    tokens.add(new Token(Type.ID, word, line, pos - word.length()));
                else {
                    tokens.add(new Token(Type.ERR, word, line, pos - word.length()));
                    throw new RuntimeException("Ошибка в линии " + line + ", в позиции " + pos);
                }
            tokens.add(new Token(Type.SEMICOLON, ";", line, pos - 1));
            word = "";
        }
        if(symbol == ','){
            if(word.length() != 0)
                if(!Character.isDigit(word.charAt(0)))
                    tokens.add(new Token(Type.ID, word, line, pos - word.length()));
                else
                    tokens.add(new Token(Type.ERR, word, line, pos - word.length()));
            tokens.add(new Token(Type.COMMA, ",", line, pos - 1));
            word = "";
        }
        if(symbol == ')'){
            if(word.length() != 0)
                if(!Character.isDigit(word.charAt(0)))
                    tokens.add(new Token(Type.ID, word, line, pos - word.length()));
                else
                    tokens.add(new Token(Type.ERR, word, line, pos - word.length()));
            tokens.add(new Token(Type.RIGHT_PAREN, ")", line, pos - 1));
            word = "";
        }
        if(symbol == ']'){
            if(word.length() != 0)
                if(!Character.isDigit(word.charAt(0)))
                    tokens.add(new Token(Type.ID, word, line, pos - word.length()));
                else
                    tokens.add(new Token(Type.ERR, word, line, pos - word.length()));
            tokens.add(new Token(Type.END_COMPLEX, "]", line, pos - 1));
            word = "";
        }
    }

    public static boolean isInteger(String str) {
        if(str.length() == 0)
            return false;
        boolean hasDigit = false;
        boolean hasDecimalPoint = false;
        boolean isBinary = isBinarySuffix(str.charAt(str.length() - 1)),
                isOctal = isOctalSuffix(str.charAt(str.length() - 1)),
                isHex = isHexadecimalSuffix(str.charAt(str.length() - 1));

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (i == 0 && (ch == '-' || ch == '+')) {}
            else if (ch == '.') {
                if (hasDecimalPoint) {
                    return false;
                }
                hasDecimalPoint = true;
            }
            else if (ch == 'e' || ch == 'E') {
                return hasDigit && isInteger(str.substring(i + 1));
            }
            else if (isBinarySuffix(ch) || isOctalSuffix(ch) || isHexadecimalSuffix(ch)) {
                if (i > 0 && Character.isDigit(str.charAt(i - 1))) {
                    return true;
                }
            }
            else if (!Character.isDigit(ch) && !isHexadecimalDigit(ch)) {
                return false;
            }
            else if (isBinary){
                if(!isBinaryDigit(ch))
                    return false;
                else
                    hasDigit = true;
            }
            else if (isOctal){
                if(!isOctalDigit(ch))
                    return false;
                else
                    hasDigit = true;
            }
            else if (isHex){
                if(!isHexadecimalDigit(ch))
                    return false;
                else
                    hasDigit = true;
            }
            else {
                hasDigit = true;
            }
        }
        return hasDigit;
    }

    private static boolean isBinarySuffix(char ch) {
        return ch == 'B' || ch == 'b';
    }

    private static boolean isOctalSuffix(char ch) {
        return ch == 'O' || ch == 'o';
    }

    private static boolean isHexadecimalSuffix(char ch) {
        return ch == 'H' || ch == 'h';
    }

    private static boolean isBinaryDigit(char ch) {
        return ch == '0' || ch == '1';
    }

    private static boolean isOctalDigit(char ch) {
        return ch >= '0' && ch <= '7';
    }

    private static boolean isHexadecimalDigit(char ch) {
        return Character.isDigit(ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }
}