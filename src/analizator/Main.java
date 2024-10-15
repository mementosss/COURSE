package analizator;

import analizator.lexer.*;
import analizator.parser.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args){
        Lexer lex;
        String fileName = args[0];

        try(FileReader reader = new FileReader(fileName)){
            int c;
            String string = "";
            while((c = reader.read()) != -1){
                string += (char)c;
            }
            lex = new Lexer(string);
        }
        catch(IOException ex){
            System.err.println("Не удалось открыть файл");
            return;
        }

        try(FileWriter writer = new FileWriter("lexems.txt", false)){
            for (Token token : lex.tokens) {
                writer.write(token.toString() + "\n");
            }
            new Parser(lex.tokens);
        }
        catch(IOException ex){
            System.err.println("Не удалось записать в файл");
            return;
        }

        System.out.println("Разбор завершен");
    }
}