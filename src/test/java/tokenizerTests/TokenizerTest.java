package tokenizerTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.seungmin.lexer.Scanner;
import org.seungmin.lexer.Token;
import org.seungmin.lexer.Tokeniser;
import org.seungmin.lexer.Token.TokenClass;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenizerTest {

    Scanner testScanner;
    Tokeniser testTokeniser;
    @TempDir
    Path tempDir;
    Path tempFile;

    protected void setupTokenizer(String fileContent) throws FileNotFoundException,IOException {
        tempFile = tempDir.resolve("testFile");
        Files.writeString(tempFile,fileContent);
        testScanner = new Scanner(tempFile.toFile());
        testTokeniser = new Tokeniser(testScanner);
    }

    protected void assertTokenizerOutput(Token[] expectedOutput, String input) throws FileNotFoundException, IOException {
        setupTokenizer(input);
        ArrayList<Token> currOutput = new ArrayList<Token>();
        for (Token token : expectedOutput) {
            currOutput.add(token);
            assertTokenEquals("Tokens did not match at: " + token.position.getLine() + ":" + token.position.getColumn(),token, testTokeniser.nextToken());
        }

        assertFalse(currOutput.size() < expectedOutput.length,"output is too short, not enough tokens." );
        assertFalse(currOutput.size() > expectedOutput.length,"output is too long, too many tokens." );
    }

    protected void assertTokenEquals(String message,Token expected, Token result){
        assertAll( message,
                ()->{assertEquals(expected.data, result.data,"Token data did not match");},
                ()->{assertEquals(expected.position, result.position,"Token position did not match");},
                ()->{assertEquals(expected.tokenClass, result.tokenClass,"Token class did not match");}
        );
    }

    @Test
    public void testSTRING_LITERAL() throws FileNotFoundException,IOException {
        String data = "\"i\'m a string,420, \t,\b \n \r \f \0 \"";
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.STRING_LITERAL,data,1,0),
                new Token(TokenClass.EOF,"",3,6)
        }, data);

    }

    @Test
    public void testINT_LITERAL() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.INT_LITERAL, "1234567890",1,0),
                new Token(TokenClass.EOF,"", 1,10)
        }, "1234567890");
    }

    @Test
    public void testCHAR_LITERAL() throws FileNotFoundException, IOException{
        String data = "\'c\'";
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.CHAR_LITERAL,data, 1, 0),
                new Token(TokenClass.EOF,"",1,3)
        }, data);
    }

    @Test
    public void testIDENTIFIER() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.IDENTIFIER,"new_int_1234",1,0),
                new Token(TokenClass.EOF,"",1,12)
        }, "new_int_1234");
    }

    @Test
    public void testINT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.INT,"",1,0),
                new Token(TokenClass.EOF,"",1,3)
        }, "int");
    }


    @Test
    public void testVOID() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.VOID,"",1,0),
                new Token(TokenClass.EOF,"",1,4)
        }, "void");
    }

    @Test
    public void testCHAR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.CHAR,"",1,0),
                new Token(TokenClass.EOF,"",1,4)
        }, "char");
    }
    @Test
    public void testIF() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.IF,"",1,0),
                new Token(TokenClass.EOF,"",1,2)
        }, "if");
    }
    @Test
    public void testELSE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.ELSE,"",1,0),
                new Token(TokenClass.EOF,"",1,4)
        }, "else");
    }
    @Test
    public void testWHILE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.WHILE,"",1,0),
                new Token(TokenClass.EOF,"",1,5)
        }, "while");
    }

    @Test
    public void testRETURN() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.RETURN,"",1,0),
                new Token(TokenClass.EOF,"",1,6)
        }, "return");
    }
    @Test
    public void testSTRUCT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.STRUCT,"",1,0),
                new Token(TokenClass.EOF,"",1,6)
        }, "struct");
    }
    @Test
    public void testSIZEOF() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.SIZEOF,"",1,0),
                new Token(TokenClass.EOF,"",1,6)
        }, "sizeof");
    }

    @Test
    public void testAND() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.AND,"",1,0),
                new Token(TokenClass.EOF,"",1,2)
        }, "&&");
    }

    @Test
    public void testOR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.OR,"",1,0),
                new Token(TokenClass.EOF,"",1,2)
        }, "||");
    }


    @Test
    public void testEQ() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.EQ,"",1,0),
                new Token(TokenClass.EOF,"",1,2)
        }, "==");
    }


    @Test
    public void testNE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.NE,"",1,0),
                new Token(TokenClass.EOF,"",1,2)
        }, "!=");
    }


    @Test
    public void testLT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.LT,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "<");
    }


    @Test
    public void testGT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.GT,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, ">");
    }


    @Test
    public void testLE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.LE,"",1,0),
                new Token(TokenClass.EOF,"",1,2)
        }, "<=");
    }


    @Test
    public void testGE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.GE,"",1,0),
                new Token(TokenClass.EOF,"",1,2)
        }, ">=");
    }


    @Test
    public void testASSIGN() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.ASSIGN,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "=");
    }


    @Test
    public void testPLUS() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.PLUS,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "+");
    }


    @Test
    public void testMINUS() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.MINUS,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "-");
    }

    @Test
    public void testASTERIX() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.ASTERIX,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "*");
    }


    @Test
    public void testDIV() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.DIV,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "/");
    }


    @Test
    public void testREM() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.REM,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "%");
    }


    @Test
    public void testDOT() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.DOT,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, ".");
    }

    @Test
    public void testLBRA() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.LBRA,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "{");
    }

    @Test
    public void testRBRA() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.RBRA,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "}");
    }

    @Test
    public void testLPAR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.LPAR,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "(");
    }


    @Test
    public void testRPAR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.RPAR,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, ")");
    }


    @Test
    public void testLSBR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.LSBR,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "[");
    }


    @Test
    public void testRSBR() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.RSBR,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, "]");
    }


    @Test
    public void testSC() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.SC,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, ";");
    }

    @Test
    public void testCOMMA() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.COMMA,"",1,0),
                new Token(TokenClass.EOF,"",1,1)
        }, ",");
    }

    @Test
    public void testINCLUDE() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.INCLUDE,"",1,0),
                new Token(TokenClass.EOF,"",1,8)
        }, "#include");
    }

    @Test
    public void testOneLineComment() throws FileNotFoundException, IOException {
        assertTokenizerOutput(new Token[]{
                new Token(TokenClass.EOF,"",1,38)
        }, "// asdasdasda sdasdas dasd asd asd asd");
    }
}
