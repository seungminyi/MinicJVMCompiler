package org.seungmin.parser;

import org.seungmin.lexer.Token;
import org.seungmin.lexer.Tokeniser;
import org.seungmin.lexer.Token.TokenClass;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author cdubach
 */
public class ParserPart1 {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;



    public ParserPart1(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public void parse() {
        // get the first token
        nextToken();
        parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt=1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }

        error(expected);
        return null;
    }

    /*
     * Returns true if the current token is equals to any of the expected ones.
     */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }


    private void parseProgram() {
        parseIncludes();
        parseStructDecls();
        parseVarDecls();
        parseFunDecls();
        expect(TokenClass.EOF);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private void parseStructDecls() {
        // to be completed ...
        if (accept(TokenClass.STRUCT)) {
            parseStructDecl();
            parseStructDecls();
        }
    }

    private void parseStructDecl() {
        parseStructType();
        expect(TokenClass.LBRA);
        parseVarDecls();
        expect(TokenClass.RBRA);
        expect(TokenClass.SC);
    }

    private void parseStructType() {
        expect(TokenClass.STRUCT);
        expect(TokenClass.IDENTIFIER);
    }

    private void parseVarDecls() {
        TokenClass lookAheadToken = lookAhead(2).tokenClass;
        boolean isNotFundecl = lookAheadToken != TokenClass.LPAR;
        if (isNotFundecl && isTypeToken()) {
            parseVarDecl();
            parseVarDecls();
        }
    }

    private void parseVarDecl() {
        parseType();
        accept(TokenClass.IDENTIFIER);
        nextToken();
        if (accept(TokenClass.SC)) {
            expect(TokenClass.SC);
        } else if (accept(TokenClass.LSBR)) {
            nextToken();
            expect(TokenClass.INT_LITERAL);
            expect(TokenClass.RSBR);
            expect(TokenClass.SC);
        }
    }

    private void parseFunDecls() {
        TokenClass lookAheadToken = lookAhead(2).tokenClass;
        boolean isFunDecl = lookAheadToken == TokenClass.LPAR;
        if (isFunDecl) {
            parseFuncDecl();
            parseFunDecls();
        }
    }

    private void parseFuncDecl() {
        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseParams();
        expect(TokenClass.RPAR);
        parseBlock();
    }

    private void parseParams() {
        if (isTypeToken()) {
            parseType();
            expect(TokenClass.IDENTIFIER);
            if (accept(TokenClass.COMMA)) {
                expect(TokenClass.COMMA);
                parseParams();
            }
        }
    }

    private void parseBlock() {
        if (accept(TokenClass.LBRA)) {
            nextToken();
            parseVarDecls();
            parseStmts();
            expect(TokenClass.RBRA);
        }
    }

    private void parseStmts() {
        if (isCurrTokenStmtFirst()) {
            parseStmt();
            parseStmts();
        }
    }

    private void parseStmt() {
        switch (token.tokenClass) {
            case LBRA:
                parseBlock();
                break;
            case WHILE:
                nextToken();
                expect(TokenClass.LPAR);
                parseExp();
                expect(TokenClass.RPAR);
                parseStmt();
                break;
            case IF:
                nextToken();
                expect(TokenClass.LPAR);
                parseExp();
                expect(TokenClass.RPAR);
                parseStmt();
                if (accept(TokenClass.ELSE)) {
                    nextToken();
                    parseStmt();
                }
                break;
            case RETURN:
                nextToken();
                if (isCurrTokenExpAble()) {
                    parseExp();
                }
                expect(TokenClass.SC);
                break;
            default:
                parseExp();
                if (accept(TokenClass.ASSIGN)) {
                    nextToken();
                    parseExp();
                }
                expect(TokenClass.SC);
        }
    }

    private void parseExp() {
        if (isCurrTokenExpAble()){
            TokenClass lookAheadTokenClass = lookAhead(1).tokenClass;
            switch (token.tokenClass) {
                case LPAR:
                    if (isTypeToken(lookAheadTokenClass)) {
                        parseTypeCast();
                    } else {
                        parseExp();
                        nextToken();
                        expect(TokenClass.RPAR);
                    }
                    break;

                case IDENTIFIER:
                    if (lookAheadTokenClass == TokenClass.LPAR) {
                        parseFuncall();
                    }else if(lookAheadTokenClass == TokenClass.LSBR) {
                        nextToken();
                        expect(TokenClass.LSBR);
                        parseExp();
                        expect(TokenClass.RSBR);
                    }else{
                        nextToken();
                        if(isOperatorToken()) {
                            parseExp();
                        }
                    }
                    break;

                case INT_LITERAL:
                    nextToken();
                    break;

                case MINUS:
                    nextToken();
                    parseExp();
                    break;
                case CHAR_LITERAL:
                    nextToken();
                    break;

                case STRING_LITERAL:
                    nextToken();
                    break;

                case SIZEOF:
                    nextToken();
                    expect(TokenClass.LPAR);
                    parseType();
                    expect(TokenClass.RPAR);
                    break;
                case ASTERIX:
                    nextToken();
                    parseExp();
                    break;
                case LSBR:
                    expect(TokenClass.LSBR);
                    parseExp();
                    expect(TokenClass.RSBR);
                    break;
                case DOT:
                    nextToken();
                    expect(TokenClass.IDENTIFIER);
                    break;
                default:
                    if (isOperatorToken()){
                        nextToken();
                    }
                    if (isCurrTokenExpAble()){
                        if (token.tokenClass == TokenClass.ASTERIX){
                            parseValueAt();
                        } else if (token.tokenClass == TokenClass.SIZEOF) {
                            parseSizeOf();
                        }
                        parseExp();
                    }
            }
            parseExp();
        }
    }

    private boolean isTypeToken() {
        TokenClass tokenClass = token.tokenClass;
        if (tokenClass == TokenClass.INT ||
                tokenClass == TokenClass.CHAR ||
                tokenClass == tokenClass.VOID ||
                tokenClass == TokenClass.STRUCT) {

            return true;
        }

        return false;
    }

    private boolean isTypeToken(TokenClass tokenClass) {
        if (tokenClass == TokenClass.INT ||
                tokenClass == TokenClass.CHAR ||
                tokenClass == tokenClass.VOID ||
                tokenClass == TokenClass.STRUCT) {

            return true;
        }

        return false;
    }

    private void parseOprator() {
        switch (token.tokenClass) {
            case LT:
                nextToken();
                break;
            case GT:
                nextToken();
                break;
            case AND:
                nextToken();
                break;
            case OR:
                nextToken();
                break;
            case EQ:
                nextToken();
                break;
            case NE:
                nextToken();
                break;
            case LE:
                nextToken();
                break;
            case GE:
                nextToken();
                break;
            case PLUS:
                nextToken();
                break;
            case MINUS:
                nextToken();
                break;
            case ASTERIX:
                nextToken();
                break;
            case DIV:
                nextToken();
                break;
            case REM:
                nextToken();
                break;
        }
    }

    private boolean isOperatorToken() {
        switch (token.tokenClass) {
            case LT:
            case GT:
            case AND:
            case OR:
            case EQ:
            case NE:
            case LE:
            case GE:
            case PLUS:
            case MINUS:
            case ASTERIX:
            case DIV:
            case REM:
                return true;
            default:
                return false;
        }
    }

    private void parseFuncall() {
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseExp();
        while (accept(TokenClass.COMMA)) {
            nextToken();
            parseExp();
        }
        expect(TokenClass.RPAR);
    }

    private void parseArrayAccess() {
        parseExp();
        expect(TokenClass.LSBR);
        parseExp();
        expect(TokenClass.RSBR);
    }

    private void parseFieldAccess() {
        parseExp();
        expect(TokenClass.DOT);
        expect(TokenClass.IDENTIFIER);
    }

    private void parseValueAt() {
        if(accept(TokenClass.ASTERIX)){
            nextToken();
            parseExp();
        }
    }

    private void parseSizeOf() {
        if (accept(TokenClass.SIZEOF)) {
            nextToken();
            expect(TokenClass.LPAR);
            parseType();
            expect(TokenClass.RPAR);
        }
    }

    private void parseTypeCast() {
        if (accept(TokenClass.LPAR)) {
            nextToken();
            parseType();
            expect(TokenClass.RPAR);
            parseExp();
        }
    }

    private boolean isCurrTokenExpAble() {
        return isOperatorToken() || accept(TokenClass.LPAR,
                TokenClass.MINUS,
                TokenClass.IDENTIFIER,
                TokenClass.INT_LITERAL,
                TokenClass.ASTERIX,
                TokenClass.SIZEOF,
                TokenClass.CHAR_LITERAL,
                TokenClass.STRING_LITERAL,
                TokenClass.DOT);
    }

    private boolean isCurrTokenStmtFirst() {
        return isCurrTokenExpAble() || accept(TokenClass.LBRA,TokenClass.WHILE,TokenClass.IF,TokenClass.RETURN, TokenClass.ASTERIX);
    }

    private void parseType() {
        if (accept(TokenClass.CHAR, TokenClass.INT, TokenClass.VOID)){
            nextToken();
            if(accept(TokenClass.ASTERIX)) {
                expect(TokenClass.ASTERIX);
            }
        } else if (accept(TokenClass.STRUCT)) {
            parseStructType();
        }
    }
}
