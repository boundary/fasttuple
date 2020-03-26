package com.nickrobison.tuple.codegen;

import com.google.common.collect.Lists;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.Java;

/**
 * Created by cliff on 5/14/14.
 */
public final class CodegenUtil {

    public static final String PUBLIC = "public";

    public static Java.ConstructorDeclarator nullConstructor(Location loc) {
        return new Java.ConstructorDeclarator(
                loc,
                null,
                new Java.AccessModifier[]{new Java.AccessModifier(PUBLIC, loc)},
                new Java.FunctionDeclarator.FormalParameters(
                        loc,
                        new Java.FunctionDeclarator.FormalParameter[0],
                        false
                ),
                new Java.Type[0],
                null,
                Lists.newArrayList()
        );
    }

    public static Java.FunctionDeclarator.FormalParameters emptyParams(Location loc) {
        return new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[0], false);
    }
}
