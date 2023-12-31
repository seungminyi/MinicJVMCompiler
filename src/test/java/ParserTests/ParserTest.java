package ParserTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.seungmin.lexer.Scanner;
import org.seungmin.lexer.Tokeniser;
import org.seungmin.parser.Parser;
import org.seungmin.parser.ParserPart1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

    Scanner testScanner;
    Tokeniser testTokeniser;
    ParserPart1 testParser;
    @TempDir
    Path tempDir;
    Path tempFile;

    protected void assertNoParserErrors(){
        assertEquals(0,testParser.getErrorCount(),"Expected 0 errors");
    }

    protected void assertParserErrorsCount(int count){
        assertEquals(count, testParser.getErrorCount(),"Expected "+ count + " errors");
    }

    protected void setupTokenizer(String fileContent) throws FileNotFoundException,IOException {
        tempFile = tempDir.resolve("testFile");
        Files.writeString(tempFile,fileContent);
        testScanner = new Scanner(tempFile.toFile());
        testTokeniser = new Tokeniser(testScanner);
    }

    protected void setupParser(String fileContent) throws FileNotFoundException, IOException {
        setupTokenizer(fileContent);
        testParser = new ParserPart1(testTokeniser);
    }

    @Test
    public void testIncludes() throws FileNotFoundException, IOException {
        setupParser("#include \"hello\" \n #include \"helloagain\"");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testIncludesError() throws FileNotFoundException, IOException {
        setupParser("#include 2");

        testParser.parse();
        assertParserErrorsCount(1);
    }

    @Test
    public void testStructDefs() throws FileNotFoundException, IOException {
        setupParser("struct hello { int asd; }; struct hello { int asd; char asd[2]; void asd; struct hello hstruc;};");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testStructDefsError() throws FileNotFoundException, IOException {
        setupParser("struct hello { }; struct hello { int asd; char asd[2]; void asd; struct hello mamma;;");
        testParser.parse();
        assertParserErrorsCount(1);
    }

    @Test
    public void testVarDecls() throws FileNotFoundException, IOException {
        setupParser("int hello; char hello; void hello; struct hello stryboi; int hello[2]; char hello[2]; void hello[2]; struct hello struboi[2];");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testFunDeclsOne() throws FileNotFoundException, IOException {
        setupParser("char createsumfin(){void hello;mamma=2+2;}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testFunDeclsMany() throws FileNotFoundException, IOException {
        setupParser("int createSomething(){}char createsumfin(){void hello;mamma=2+2;}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testIf() throws FileNotFoundException, IOException {
        setupParser("void main(){if(asd == 2){return 0;}}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testIfElse() throws FileNotFoundException, IOException {
        setupParser("void main(){if(asd == 2){return 0;}else {return 1;}}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testWhile() throws FileNotFoundException, IOException {
        setupParser("void main(){while(asd == 2){return 0;}}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testVariousVariables() throws FileNotFoundException, IOException {
        setupParser("int mammamia[2]; char* mamma[2];  void main(){void* hello[2]; struct asd* gleb[2];}");
        testParser.parse();
        assertNoParserErrors();
    }

    @Test
    public void testFieldAccess() throws FileNotFoundException, IOException {
        setupParser("void main(){gleb[2];*hleb.str;}");
        testParser.parse();
        assertNoParserErrors();
    }
}
