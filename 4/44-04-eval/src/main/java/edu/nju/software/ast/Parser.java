package edu.nju.software.ast;

import edu.nju.software.stream.TokenReadStream;
import edu.nju.software.token.*;

import java.util.ArrayList;

public class Parser {
    public static LObject parse(ArrayList<Token> tokens) {
        return parseForm(new TokenReadStream(tokens));
    }

    static LObject parseForm(TokenReadStream rdr) {
        Token token = rdr.peek();
        if (token instanceof LeftParen) {
            return parseList(rdr);
        } else {
            return parseAtom(rdr);
        }
    }

    static LObject parseList(TokenReadStream rdr) {
        rdr.next();
        Token token;
        ArrayList<LObject> lst = new ArrayList<>();
        while ((token = rdr.peek()) != null && !(token instanceof RightParen)) {
            lst.add(parseForm(rdr));
        }
        rdr.next();
        return new LList(lst);
    }

    static LObject parseAtom(TokenReadStream rdr) {
        Token token = rdr.next();
        if (token instanceof StringToken) {
            return new LString(((StringToken) token).value);
        } else if (token instanceof IntegerToken) {
            return new LInteger(((IntegerToken) token).value);
        } else if (token instanceof SymbolToken) {
            return new LSymbol(((SymbolToken) token).name);
        }
        throw new RuntimeException("Unknown Token Type");
    }
}
