package com.nickrobison.tuple.codegen;

import com.nickrobison.tuple.FastTuple;
import com.google.common.collect.Lists;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.Java;

import static com.nickrobison.tuple.codegen.CodegenUtil.*;

/**
 * Created by cliff on 5/14/14.
 */
public class TupleAllocatorGenerator extends ClassBodyEvaluator {
    private static final String packageName = "com.nickrobison.tuple";

    public interface TupleAllocator {
        FastTuple allocate();
    }

    private final Class allocatorClass;

    public TupleAllocatorGenerator(Class tupleClass) throws Exception {
        setParentClassLoader(tupleClass.getClassLoader());
        String className = tupleClass.getName() + "Allocator";
        setClassName(packageName + "." + className);
        Java.CompilationUnit cu = new Java.CompilationUnit(null);
        Location loc = new Location(null, (short) 0, (short) 0);
        cu.setPackageDeclaration(new Java.PackageDeclaration(loc, packageName));
        cu.addPackageMemberTypeDeclaration(makeClassDefinition(loc, tupleClass, className));
        allocatorClass = compileToClass(cu);
    }

    public TupleAllocator createAllocator() throws Exception {
        return (TupleAllocator) allocatorClass.getConstructor().newInstance();
    }

    private Java.PackageMemberClassDeclaration makeClassDefinition(Location loc, Class tupleClass, String className) {
        Java.PackageMemberClassDeclaration cd = new Java.PackageMemberClassDeclaration(
                loc,
                null,
                new Java.AccessModifier[]{new Java.AccessModifier("public", loc)},
                className,
                null,
                null,
                new Java.Type[]{
                        classToType(loc, TupleAllocator.class)
                });

        cd.addConstructor(nullConstructor(loc));
        cd.addDeclaredMethod(new Java.MethodDeclarator(
                loc,
                null,
                new Java.AccessModifier[]{new Java.AccessModifier("public", loc)},
                null,
                classToType(loc, FastTuple.class),
                "allocate",
                emptyParams(loc),
                new Java.Type[0],
                null,
                Lists.<Java.BlockStatement>newArrayList(
                        new Java.ReturnStatement(loc,
                                new Java.NewClassInstance(
                                        loc,
                                        null,
                                        new Java.ReferenceType(loc, new Java.NormalAnnotation[]{}, tupleClass.getCanonicalName().split("\\."), new Java.TypeArgument[0]),
                                        new Java.Rvalue[0]))
                )
        ));

        return cd;
    }
}
