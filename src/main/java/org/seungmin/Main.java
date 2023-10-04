package org.seungmin;

import org.seungmin.ast.ASTPrinter;
import org.seungmin.ast.Program;
import org.seungmin.gen.CodeGenerator;
import org.seungmin.lexer.Scanner;
import org.seungmin.lexer.Token;
import org.seungmin.lexer.Tokeniser;
import org.seungmin.parser.Parser;
import org.seungmin.SemanticAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * The java.Main file implies an interface for the subsequent components, e.g.
 *   * The Tokeniser must have a constructor which accepts a Scanner,
 *     moreover Tokeniser must provide a public method getErrorCount
 *     which returns the total number of lexing errors.
 */
public class                Main {
	private static final int FILE_NOT_FOUND = 2;
    private static final int MODE_FAIL      = 254;
    private static final int LEXER_FAIL     = 250;
    private static final int PARSER_FAIL    = 245;
    private static final int SEM_FAIL       = 240;
    private static final int PASS           = 0;
    
    private enum Mode {
        LEXER, PARSER, AST, SEMANTICANALYSIS, GEN
    }

    private static void usage() {
        System.out.println("Usage: java "+Main.class.getSimpleName()+" pass inputfile outputfile");
        System.out.println("where pass is either: -java.lexer, -java.parser, -java.ast, -java.sem or -java.gen");
        System.exit(-1);
    }

    public static void main(String[] args) {
        args = new String[]{"-java.parser", "tests/fibonacci.c", "tests/test"};

        if (args.length != 3)
            usage();

        Mode mode = null;
        switch (args[0]) {
            case "-java.lexer": mode = Mode.LEXER; break;	case "-java.parser": mode = Mode.PARSER; break;
            case "-java.ast":   mode = Mode.AST; break;		case "-java.sem":    mode = Mode.SEMANTICANALYSIS; break;
            case "-java.gen":   mode = Mode.GEN; break;
            default:
                usage();
                break;
        }

        File inputFile = new File(args[1]);
        File outputFile = new File(args[2]);

        Scanner scanner;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File "+inputFile.toString()+" does not exist.");
            System.exit(FILE_NOT_FOUND);
            return;
        }

        Tokeniser tokeniser = new Tokeniser(scanner);
        if (mode == Mode.LEXER) {
            for (Token t = tokeniser.nextToken(); t.tokenClass != Token.TokenClass.EOF; t = tokeniser.nextToken()) 
            	System.out.println(t);
            if (tokeniser.getErrorCount() == 0)
        		System.out.println("Lexing: pass");
    	    else
        		System.out.println("Lexing: failed ("+tokeniser.getErrorCount()+" errors)");	
            System.exit(tokeniser.getErrorCount() == 0 ? PASS : LEXER_FAIL);
        } else if (mode == Mode.PARSER) {
		    Parser parser = new Parser(tokeniser);
		    parser.parse();
		    if (parser.getErrorCount() == 0)
		    	System.out.println("Parsing: pass");
		    else
		    	System.out.println("Parsing: failed ("+parser.getErrorCount()+" errors)");
		    System.exit(parser.getErrorCount() == 0 ? PASS : PARSER_FAIL);
        }  else if (mode == Mode.AST) {
            Parser parser = new Parser(tokeniser);
            Program programAst = parser.parse();
            if (parser.getErrorCount() == 0) {
                System.out.println("Parsing: pass");
                System.out.println("Printing out AST:");
                PrintWriter writer;
                StringWriter sw = new StringWriter();
                try {
                    writer = new PrintWriter(sw);
                    programAst.accept(new ASTPrinter(writer));
                    writer.flush();
                    System.out.print(sw.toString());
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                System.out.println("Parsing: failed ("+parser.getErrorCount()+" errors)");
            System.exit(parser.getErrorCount() == 0 ? PASS : PARSER_FAIL);
        } else if (mode == Mode.SEMANTICANALYSIS) {
            Parser parser = new Parser(tokeniser);
            Program programAst = parser.parse();
            if (parser.getErrorCount() == 0) {
                SemanticAnalyzer sem = new SemanticAnalyzer();
                int errors = sem.analyze(programAst);
                if (errors == 0)
                    System.out.println("Semantic analysis: Pass");
                else
                    System.out.println("Semantic analysis: Failed (" + errors + ")");
                System.exit(errors == 0 ? PASS : SEM_FAIL);
            } else
                System.exit(PARSER_FAIL);
        } else if (mode == Mode.GEN) {
            Parser parser = new Parser(tokeniser);
            Program programAst = parser.parse();
            if (parser.getErrorCount() > 0)
                System.exit(PARSER_FAIL);
            SemanticAnalyzer sem = new SemanticAnalyzer();
            int errors = sem.analyze(programAst);
            if (errors > 0)
                System.exit(SEM_FAIL);
            CodeGenerator codegen = new CodeGenerator();
            try {
                codegen.emitProgram(programAst, outputFile);
            } catch (FileNotFoundException e) {
                System.out.println("File "+outputFile.toString()+" does not exist.");
                System.exit(FILE_NOT_FOUND);
            }
        } else {
        	System.exit(MODE_FAIL);
        }
    }
}