package analizator.parser;

import analizator.lexer.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Parser {
    static class ID{
        String word;
        boolean declared;
        Type type;

        ID(Token token, boolean declared, Type type){
            this.word = token.word;
            this.declared = declared;
            this.type = type;
        }

        public String toString(){
            return "(" + word + ", " + type + ", " + declared + ")";
        }
    }
    int pos = -1;
    Token token;
    ArrayList<ID> ids = new ArrayList<>();

    public Parser(ArrayList<Token> tokens){

        syntax(tokens);
        semantics(tokens);


    }

    public void syntax(ArrayList<Token> tokens) {
        next(tokens);
        try {
            expect(tokens, Type.BEGIN_PROG, "Пропущен 'program'");
            next(tokens);
            expect(tokens, Type.VAR, "Пропущен 'var'");
            next(tokens);

            parseDescription(tokens);

            begin(tokens);
            next(tokens);

            while (token.type != Type.END_PROG) {
                switch (token.type) {
                    case GEQ, LESS, LEQ, GREATER, EQUAL, NEQ:
                        conditional(tokens);
                        break;

                    case FOR:
                        for_operator(tokens);
                        break;

                    case WHILE:
                        while_operator(tokens);
                        break;

                    case WRITE, READ:
                        rw(tokens);
                        break;

                    case IF:
                        if_operator(tokens);
                        break;

                    case ELSE:
                        else_operator(tokens, false);
                        break;

                    case BEGIN_COMPLEX:
                        complex(tokens);
                        break;

                    case BEGIN_COMMENT:
                        comment(tokens);
                        break;

                    case ASSIGN:
                        assign(tokens);
                        break;

                    case ID:
                        id(tokens);
                        break;

                    case FLOAT, INTEGER:
                        integer(tokens);
                        break;

                    default:
                        break;
                }
                next(tokens);
            }
            end(tokens);
        } catch (ParserError e) {
            System.out.println(e);
        }
    }

    public void parseDescription(ArrayList<Token> tokens) {
        while (token.type == Type.ID) {
            id(tokens);
            next(tokens);

            expect(tokens, Type.COLON, "Ожидалось двоеточие после идентификатора");
            next(tokens);

            if (!(token.type == Type.TYPE_B || token.type == Type.TYPE_F || token.type == Type.TYPE_I /* Другие типы данных */)) {
                throw new ParserError("Неверный тип данных", token.line + 1, token.position + token.word.length());
            }

            next(tokens);
            expect(tokens, Type.SEMICOLON, "Ожидалась точка с запятой после описания типа");
            next(tokens);

            // Проверяем, если есть еще описание переменных или начало программы
            if (token.type != Type.ID && token.type != Type.BEGIN_PROG) {
                throw new ParserError("Неверное описание переменных", token.line + 1, token.position + token.word.length());
            }
        }

        if (token.type == Type.ID) {
            throw new ParserError("Нет завершения описания переменных", token.line + 1, token.position + token.word.length());
        }
    }


    public void expect(ArrayList<Token> tokens, Type expectedType, String errorMessage) {
        if (token.type != expectedType) {
            throw new ParserError(errorMessage, token.line + 1, token.position + token.word.length());
        }
    }

    public void integer(ArrayList<Token> tokens){
        Token tok = tokens.get(pos - 1);
        if(tok.type != Type.LINE_BREAK & tok.type != Type.SEMICOLON & tok.type != Type.ASSIGN & tok.type != Type.COMMA
                & tok.type != Type.EQUAL & tok.type != Type.LESS & tok.type != Type.LEQ & tok.type != Type.GEQ & tok.type != Type.GEQ
                & tok.type != Type.PLUS & tok.type != Type.MINUS)
            throw new ParserError("Ошибка объявления: " + tok.type, token.line + 1, token.position + token.word.length());
    }

    public void id(ArrayList<Token> tokens){
        Token tok = tokens.get(pos - 1);
        if(tok.type != Type.LINE_BREAK & tok.type != Type.SEMICOLON & tok.type != Type.ASSIGN & tok.type != Type.COMMA & tok.type
                != Type.TYPE_I & tok.type != Type.TYPE_F & tok.type != Type.TYPE_B
                & tok.type != Type.EQUAL & tok.type != Type.LESS & tok.type != Type.LEQ & tok.type != Type.GEQ & tok.type != Type.GEQ
                & tok.type != Type.PLUS & tok.type != Type.MINUS)
            throw new ParserError("Ошибка объявления: " + tok.type, token.line + 1, token.position + token.word.length());
    }

    public void conditional(ArrayList<Token> tokens){
        Token tok = tokens.get(pos - 1);
        if(tok.type != Type.ID & tok.type != Type.INTEGER & tok.type != Type.TRUE & tok.type != Type.FALSE)
            throw new ParserError("Ошибка типа в выражении: " + tok.type + " Ожидалось: ID или INTEGER или TRUE или FALSE", token.line + 1, token.position + token.word.length());

        tok = tokens.get(pos + 1);
        if(tok.type != Type.ID & tok.type != Type.INTEGER & tok.type != Type.TRUE & tok.type != Type.FALSE)
            throw new ParserError("Ошибка типа в выражении: " + tok.type + " Ожидалось: ID или INTEGER или TRUE или FALSE", token.line + 1, token.position + token.word.length());
    }

    public void for_operator(ArrayList<Token> tokens){
        Token tok = tokens.get(pos + 1);
        if(tok.type == Type.TO)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: присваивание", token.line + 1, token.position + token.word.length());

        next(tokens);
        while(this.token.type != Type.TO){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK | token.type == Type.SEMICOLON)
                throw new ParserError("Пропущен \'to\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }

        tok = tokens.get(pos + 1);
        if(tok.type == Type.DO)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: выражение", token.line + 1, token.position + token.word.length());

        next(tokens);
        while(this.token.type != Type.DO){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK)
                throw new ParserError("Пропущен \'do\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }

        tok = tokens.get(pos + 1);
        if(tok.type == Type.SEMICOLON)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: оператор", token.line + 1, token.position + token.word.length());

        next(tokens);
        while(this.token.type != Type.SEMICOLON){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK)
                throw new ParserError("Пропущен \';\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }
    }

    public void while_operator(ArrayList<Token> tokens){
        Token tok = tokens.get(pos + 1);
        if(tok.type == Type.DO)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: выражение", token.line + 1, token.position + token.word.length());

        next(tokens);
        while(this.token.type != Type.DO){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK | token.type == Type.SEMICOLON)
                throw new ParserError("Пропущен \'do\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }

        tok = tokens.get(pos + 1);
        if(tok.type == Type.SEMICOLON)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: оператор", token.line + 1, token.position + token.word.length());

        next(tokens);
        while(this.token.type != Type.SEMICOLON){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK)
                throw new ParserError("Пропущен \';\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }
    }

    public void rw(ArrayList<Token> tokens){
        Token tok = tokens.get(pos + 1);
        if(tok.type != Type.LEFT_PAREN)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: \'(\'", token.line + 1, token.position + token.word.length());

        tok = tokens.get(pos + 2);
        if(tok.type == Type.RIGHT_PAREN)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: выражение", token.line + 1, token.position + token.word.length());
        next(tokens);
        while(this.token.type != Type.RIGHT_PAREN){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK | token.type == Type.SEMICOLON)
                throw new ParserError("Пропущен \')\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }
    }

    public void if_operator(ArrayList<Token> tokens){
        Token tok = tokens.get(pos + 1);

        if(tok.type != Type.LEFT_PAREN)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: выражение", token.line + 1, token.position + token.word.length());
        next(tokens);


        while(this.token.type != Type.RIGHT_PAREN){
            if(pos == tokens.size() - 1 | token.type == Type.ASSIGN | token.type == Type.FOR | token.type == Type.READ | token.type == Type.EMPTY )
                throw new ParserError("Пропущен 'оператор сравнения'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }

        while(this.token.type != Type.THEN){
            if((pos == tokens.size() - 1 | token.type == Type.LINE_BREAK | token.type == Type.SEMICOLON | token.type == Type.SEMICOLON) & (token.type != Type.ASSIGN))
                throw new ParserError("Пропущен \'then\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }

        while(this.token.type != Type.ELSE){
            if(pos == tokens.size() - 1 | token.type == Type.FOR | token.type == Type.READ | token.type == Type.EMPTY | token.type == Type.EMPTY)
                throw new ParserError("Пропущен 'оператор сравнения'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }

        tok = tokens.get(pos + 1);
        if(tok.type == Type.SEMICOLON | tok.type == Type.ELSE)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: оператор", token.line + 1, token.position + token.word.length());

        next(tokens);
        while(this.token.type != Type.SEMICOLON & this.token.type != Type.ELSE){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK)
                throw new ParserError("Пропущен \'; или else\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }

        tok = tokens.get(pos);
        if(tok.type == Type.ELSE)
            else_operator(tokens, true);
    }

    public void else_operator(ArrayList<Token> tokens, boolean ifexist){
        if(!ifexist){
            throw new ParserError("Ошибка вызова: Нет IF...THEN", token.line + 1, token.position + token.word.length());
        }

        Token tok = tokens.get(pos + 1);
        if(tok.type == Type.SEMICOLON)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: оператор", token.line + 1, token.position + token.word.length());

        next(tokens);
        while(this.token.type != Type.SEMICOLON){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK)
                throw new ParserError("Пропущен \';\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }
    }

    public void complex(ArrayList<Token> tokens){
        Token tok = tokens.get(pos + 1);
        if(tok.type == Type.END_COMPLEX)
            throw new ParserError("Ошибка вызова: " + tok.type + " Ожидалось: выражение", token.line + 1, token.position + token.word.length());

        while(this.token.type != Type.END_COMPLEX){
            if(pos == tokens.size() - 1 | token.type == Type.LINE_BREAK)
                throw new ParserError("Пропущен \']\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }
    }

    public void comment(ArrayList<Token> tokens){
        while(token.type != Type.END_COMMENT){
            if(pos == tokens.size() - 1)
                throw new ParserError("Пропущен \'}\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }
    }

    public void assign(ArrayList<Token> tokens){
        Token tok = tokens.get(pos - 1);
        if(tok.type != Type.ID)
            throw new ParserError("Ошибка типа в выражении: " + tok.type + " Ожидалось: ID", token.line + 1, token.position + token.word.length());

        if(tok.type != Type.ID & tok.type != Type.INTEGER & tok.type != Type.TRUE & tok.type != Type.FALSE)
            throw new ParserError("Ошибка типа в выражении: " + tok.type + " Ожидалось: ID или INTEGER или TRUE или FALSE", token.line + 1, token.position + token.word.length());
    }

    public void begin(ArrayList<Token> tokens){
        while(token.type != Type.END_DESCRIPTION){
            if(pos == tokens.size() - 1)
                throw new ParserError("Пропущен \'begin\'", token.line + 1, token.position + token.word.length());
            next(tokens);
        }
    }

    public void end(ArrayList<Token> tokens){
        if(tokens.get(tokens.size() - 1).type != Type.END_PROG)
            throw new ParserError("Пропущен \'end.\'", token.line + 1, token.position + token.word.length());
    }

    public void next(ArrayList<Token> tokens){
        pos++;
        token = tokens.get(pos);
    }

    public void semantics(ArrayList<Token> tokens){
        Type type = Type.EMPTY;
        for (Token token : tokens) {
            if(token.type == Type.TYPE_B | token.type == Type.TYPE_F | token.type == Type.TYPE_I)
                type = token.type;
            else if(token.type == Type.ID){
                if(!exist(token))
                    ids.add(new ID(token, true, type));
            }
            else if(token.type == Type.SEMICOLON | token.type == Type.LINE_BREAK){
                type = Type.EMPTY;
            }
        }

        try (FileWriter writer = new FileWriter("ids.json", false)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(ids);
            try (JsonWriter jsonWriter = gson.newJsonWriter(writer)) {
                jsonWriter.setLenient(true);
                jsonWriter.jsonValue(json);
                jsonWriter.flush();
            }
            writer.write("\n");
        } catch (IOException ex) {
            return;
        }
    }

    public boolean exist(Token token){
        for (ID id : ids) {
            if(Objects.equals(id.word, token.word))
                return true;
        }
        return false;
    }
}