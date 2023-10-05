package org.seungmin.lexer;

import org.seungmin.lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    private final Map<Character, TokenClass> delimitersTokenClassMap = new HashMap<>();
    private final Map<Character, TokenClass> singleOperatorTokenClassMap = new HashMap<>();
    private final HashSet<Character> operatorStartChars = new HashSet<>();
    private final StringBuilder tokenStringBuilder = new StringBuilder();



    public int getErrorCount() {
        return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
        init();
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
        error++;
    }


    public Token nextToken() {
        Token result;
        try {
            result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        } catch (UnrecognizedCharacterException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /*
     * To be completed
     */
    private Token next() throws IOException, UnrecognizedCharacterException {
        int line = scanner.getLine();
        int column = scanner.getColumn();
        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        //operators, comparisons, logical_operators, struct member access
        if (isOperatorStartChar(c)) {
            if(isOperatorChar(c) && c != '/') {
                clearTokenStringBuilder();
                return new Token(singleOperatorTokenClassMap.get(c), "", line, column);
            }
            if (c == '/') {
                if(isComment()){
                    throwAwayComment();
                    return next();
                }
                clearTokenStringBuilder();
                return new Token(singleOperatorTokenClassMap.get(c), "", line, column);
            }

            if (c == '&'){
                expectFullString("&&", c);
                clearTokenStringBuilder();
                return new Token(TokenClass.AND, "", line, column);
            }

            if (c == '|'){
                expectFullString("||", c);
                clearTokenStringBuilder();
                return new Token(TokenClass.OR, "", line, column);
            }

            if (c == '!'){
                expectFullString("!=", c);
                clearTokenStringBuilder();
                return new Token(TokenClass.NE, "",line,column);
            }

            if (c == '='){
                boolean matched = isFirstParamToken("=", "==", c);
                if(matched){
                    clearTokenStringBuilder();
                    return new Token(TokenClass.ASSIGN, "",line,column);
                } else {
                    clearTokenStringBuilder();
                    return new Token(TokenClass.EQ, "",line,column);
                }
            }

            if (c == '<'){
                boolean matched = isFirstParamToken("<","<=", c);
                if(matched){
                    clearTokenStringBuilder();
                    return new Token(TokenClass.LT, "",line,column);
                } else {
                    clearTokenStringBuilder();
                    return new Token(TokenClass.LE, "",line,column);
                }
            }

            if (c == '>'){
                boolean matched = isFirstParamToken(">",">=", c);
                if(matched){
                    clearTokenStringBuilder();
                    return new Token(TokenClass.GT,"",line,column);
                } else {
                    clearTokenStringBuilder();
                    return new Token(TokenClass.GE,"",line,column);
                }
            }
        }



        if (c == '.') {
            clearTokenStringBuilder();
            return new Token(TokenClass.DOT, "", line, column);
        }

        // delimiters
        if (isDelimiterChar(c)) {
            clearTokenStringBuilder();
            return new Token(delimitersTokenClassMap.get(c), "", line, column);
        }

        //include
        if (isInclude(c)) {
            expectFullString("#include", c);
            clearTokenStringBuilder();
            return new Token(TokenClass.INCLUDE, "", line, column);
        }

        //literals
        if (isStringLiteral(c)) {
            passStringLiteral(c);
            return new Token(TokenClass.STRING_LITERAL, getTokenString(), line, column);
        }
        if (isCharLiteral(c)){
            passCharLiteral(c);
            return new Token(TokenClass.CHAR_LITERAL, getTokenString(), line, column);
        }
        if (isIntLiteral(c)) {
            passIntLiteral(c);
            return new Token(TokenClass.INT_LITERAL, getTokenString(), line, column);
        }

        //types keywords identifier
        if (isIdentifierOrKeyword(c) && !Character.isDigit(c)) {
            String token = getIdentifierOrKeyword(c);
            return handleIdentifierOrKeyword(token, line, column);
        }



        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }

    private boolean isFirstParamToken(String first, String second, char currentChar) throws IOException, UnrecognizedCharacterException, EOFException {
        try {
            if (first.charAt(0) == currentChar && second.charAt(0) == currentChar) {
                tokenStringBuilder.append(currentChar);
                currentChar = scanner.next();

                if(second.length() > 1 && second.charAt(1) == currentChar){
                    tokenStringBuilder.append(currentChar);
                    return false;
                }

                return true;

            }else if(first.charAt(0) == currentChar){
                expectFullString(first, currentChar);
                return true;
            } else if (second.charAt(0) == currentChar) {
                expectFullString(second, currentChar);
                return false;
            }else {
                throw new UnrecognizedCharacterException(first.charAt(0) +" or " + second.charAt(0), currentChar);
            }
        }catch (EOFException eof) {
            return true;
        }

    }

    private void throwAwayComment() throws IOException {
        while (scanner.next() != '\n');
    }

    private boolean isComment() throws IOException {
        try {return scanner.peek() == '/';}
        catch (EOFException eof) { return false; }
    }

    private boolean isOperatorChar(char c) {
        return singleOperatorTokenClassMap.containsKey(c);
    }

    private Token handleIdentifierOrKeyword(String token, int line, int column) {
        switch (token) {
            case "int":
                clearTokenStringBuilder();
                return new Token(TokenClass.INT, line, column);
            case "void":
                clearTokenStringBuilder();
                return new Token(TokenClass.VOID, line, column);
            case "char":
                clearTokenStringBuilder();
                return new Token(TokenClass.CHAR, line, column);
            case "if":
                clearTokenStringBuilder();
                return new Token(TokenClass.IF, line, column);
            case "else":
                clearTokenStringBuilder();
                return new Token(TokenClass.ELSE, line, column);
            case "while":
                clearTokenStringBuilder();
                return new Token(TokenClass.WHILE, line, column);
            case "return":
                clearTokenStringBuilder();
                return new Token(TokenClass.RETURN, line, column);
            case "struct":
                clearTokenStringBuilder();
                return new Token(TokenClass.STRUCT, line, column);
            case "sizeof":
                clearTokenStringBuilder();
                return new Token(TokenClass.SIZEOF, line, column);
            default:
                return new Token(TokenClass.IDENTIFIER, getTokenString(), line, column);
        }
    }

    private String getIdentifierOrKeyword(char currentChar) throws IOException {
        tokenStringBuilder.append(currentChar);
        try {
            char c = scanner.peek();

            while (isIdentifierOrKeyword(c)) {
                tokenStringBuilder.append(scanner.next());
                c = scanner.peek();
            }
            return tokenStringBuilder.toString();
        }catch (EOFException eof) {
            return tokenStringBuilder.toString();
        }
    }


    private boolean isOperatorStartChar(char c) {
        return operatorStartChars.contains(c);
    }

    private boolean isStructMemberAccess(char c) {
        return c == '.';
    }

    private boolean isIdentifierOrKeyword(char c) {
        return Character.isLetter(c) || Character.isDigit(c) || c == '_';
    }

    private boolean isCharLiteral(char c) {
        return c == '\'';
    }

    private void passCharLiteral(char currentChar) throws IOException {
        char c = scanner.next();
        tokenStringBuilder.append(currentChar);
        if(!isCharLiteral(c)) tokenStringBuilder.append(c);
        c = scanner.next();
        if(!isCharLiteral(c)) error(c, scanner.getLine(), scanner.getColumn());
        tokenStringBuilder.append(c);
    }

    private void passIntLiteral(char c) throws IOException {
        tokenStringBuilder.append(c);
        try {
            while (true) {
                char nextChar = scanner.peek();
                if (!Character.isDigit(nextChar)) break;
                c = scanner.next();
                tokenStringBuilder.append(c);
            }
        }catch (EOFException exception){}
    }

    private String getTokenString() {
        final String tokenString = tokenStringBuilder.toString();
        clearTokenStringBuilder();
        return tokenString;
    }

    private void clearTokenStringBuilder() {
        tokenStringBuilder.setLength(0);
    }

    private void expectFullString(String str, char currentChar) throws IOException {
        tokenStringBuilder.append(currentChar);
        for (char c: str.substring(1).toCharArray()) {
            if (c != scanner.next()) {
                error(c, scanner.getLine(), scanner.getColumn());
            }
            tokenStringBuilder.append(c);
        }
    }

    private boolean isInclude(char c) {
        return c == '#';
    }

    private void passStringLiteral(char c) throws IOException {
        tokenStringBuilder.append(c);
        while(true) {
            final char nextChar = scanner.next();
            if(nextChar == '"') {
                tokenStringBuilder.append(nextChar);
                break;
            }
            tokenStringBuilder.append(nextChar);
        }

    }

    private boolean isStringLiteral(char c) {
        return c == '"';
    }

    private boolean isIntLiteral(char c) {
        return Character.isDigit(c);
    }

    private void init() {
        initDelimitersTokenClassMap();
        initOperatorTokenClassMap();
        InitOperatorStartChars();
    }

    private void initDelimitersTokenClassMap() {
        delimitersTokenClassMap.put('{', TokenClass.LBRA);
        delimitersTokenClassMap.put('}', TokenClass.RBRA);
        delimitersTokenClassMap.put('(', TokenClass.LPAR);
        delimitersTokenClassMap.put(')', TokenClass.RPAR);
        delimitersTokenClassMap.put('[', TokenClass.LSBR);
        delimitersTokenClassMap.put(']', TokenClass.RSBR);
        delimitersTokenClassMap.put(';', TokenClass.SC);
        delimitersTokenClassMap.put(',', TokenClass.COMMA);
    }

    private void initOperatorTokenClassMap() {
        singleOperatorTokenClassMap.put('+', TokenClass.PLUS);
        singleOperatorTokenClassMap.put('-', TokenClass.MINUS);
        singleOperatorTokenClassMap.put('*', TokenClass.ASTERIX);
        singleOperatorTokenClassMap.put('/', TokenClass.DIV);
        singleOperatorTokenClassMap.put('%', TokenClass.REM);
        singleOperatorTokenClassMap.put('.', TokenClass.DOT);
    }

    private void InitOperatorStartChars() {
        operatorStartChars.add('+');
        operatorStartChars.add('-');
        operatorStartChars.add('*');
        operatorStartChars.add('/');
        operatorStartChars.add('%');
        operatorStartChars.add('=');
        operatorStartChars.add('!');
        operatorStartChars.add('<');
        operatorStartChars.add('>');
        operatorStartChars.add('&');
        operatorStartChars.add('|');
    }

    private boolean isDelimiterChar(char c) {
        return delimitersTokenClassMap.containsKey(c);
    }

    public class UnrecognizedCharacterException extends Exception {
        private static final long serialVersionUID = 1L;

        public UnrecognizedCharacterException(final String message) {
            super(message);
        }
        public UnrecognizedCharacterException(final String expected,final char encountered){
            super("Expected: " + expected + ", but encountered: " + encountered);
        }
    }

}

