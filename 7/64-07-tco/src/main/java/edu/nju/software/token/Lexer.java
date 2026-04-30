package edu.nju.software.token;

import edu.nju.software.stream.CharLookaheadStream;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;

public class Lexer {
    private static boolean isSpecialCharacter(char c) {
        return c == '(' || c == ')' || c == '"' || Character.isWhitespace(c);
    }

    public static ArrayList<Token> lex(CharLookaheadStream input) {
        ArrayList<Token> tokens = new ArrayList<>();
        while (!input.eof()) {
            char c = input.consume();
            if (Character.isWhitespace(c)) continue; // Ignore whitespace
            switch (c) {
                case '(': { // Left paren
                    tokens.add(LeftParen.create());
                    break;
                }
                case ')': { // Right paren
                    tokens.add(RightParen.create());
                    break;
                }
                case '"': { // String
                    StringBuilder builder = new StringBuilder();
                    boolean terminate = false;
                    boolean escape = false;
                    while (!input.eof()) {
                        c = input.consume();
                        if (!escape && c == '"') {
                            terminate = true;
                            break;
                        }
                        builder.append(c);

                        if (escape) escape = false;
                        else if (c == '\\') escape = true;
                    }
                    if (!terminate) throw new RuntimeException("Unterminated string");
                    tokens.add(StringToken.create(StringEscapeUtils.unescapeJava(builder.toString())));
                    break;
                }
                default: {
                    if (Character.isDigit(c) || (c == '-' && !input.eof() && Character.isDigit(input.lookahead(1)))) { // Integer
                        StringBuilder builder = new StringBuilder();
                        builder.append(c);
                        while (!input.eof()) {
                            c = (char) input.lookahead(1);
                            if (!Character.isDigit(c)) break;
                            builder.append(c);
                            input.consume();
                        }
                        tokens.add(IntegerToken.create(Integer.parseInt(builder.toString())));
                    } else { // Symbol
                        StringBuilder builder = new StringBuilder();
                        builder.append(c);
                        while (!input.eof()) {
                            c = (char) input.lookahead(1);
                            if (isSpecialCharacter(c)) break;
                            builder.append(c);
                            input.consume();
                        }
                        tokens.add(SymbolToken.create(builder.toString()));
                    }
                }
            }
        }
        return tokens;
    }
}