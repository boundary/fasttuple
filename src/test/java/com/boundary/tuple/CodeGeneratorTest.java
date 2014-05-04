package com.boundary.tuple;

import org.codehaus.janino.Java;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.junit.Test;

import java.io.FileReader;

import static org.junit.Assert.assertTrue;

/**
 * Created by cliff on 5/3/14.
 */
public class CodeGeneratorTest {

    public CodeGeneratorTest() {

    }

    @Test
    public void testParser() throws Exception {
        Java.CompilationUnit cu = new Parser(
                new Scanner(null,
                    new FileReader(
                            "/Users/cliff/projects/fasttuple/src/main/java/com/boundary/tuple/TupleSchema.java"))).
                parseCompilationUnit();

        assertTrue(false);
    }
}
