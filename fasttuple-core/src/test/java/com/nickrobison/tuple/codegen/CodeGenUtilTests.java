package com.nickrobison.tuple.codegen;

import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.Java;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by nickrobison on 12/27/20.
 */
public class CodeGenUtilTests {

    @Test
    void testNullConstructor() {
        final Java.ConstructorDeclarator constructor = CodegenUtil.nullConstructor(new Location(null, 0, 1));
        assertAll(() -> assertEquals("<init>", constructor.name, "Should have init name"),
                () -> assertEquals(0, constructor.formalParameters.parameters.length, "Should not have parameters"));
    }
}
