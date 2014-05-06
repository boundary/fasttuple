package com.boundary.tuple.codegen;

import com.boundary.tuple.FastTuple;
import com.google.common.collect.Lists;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.Java;
import org.codehaus.janino.Mod;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cliff on 5/3/14.
 */
public abstract class TupleCodeGenerator extends ClassBodyEvaluator {
    private static AtomicLong counter = new AtomicLong(0l);
    protected Class iface;
    protected String[] fieldNames;
    protected Class[] fieldTypes;
    protected int[] indexes;
    protected Location loc;
    protected String className;

    public TupleCodeGenerator(Class iface, String[] fieldNames, Class[] fieldTypes, int[] indexes) {
        this.loc = new Location("", (short)0, (short)0);
        this.iface = iface;
        this.fieldNames = fieldNames.clone();
        this.fieldTypes = fieldTypes.clone();
        this.indexes = indexes.clone();
        this.className = "FastTuple" + counter.getAndIncrement();
        this.setClassName("com.boundary.tuple." + className);
        this.setParentClassLoader(this.getClass().getClassLoader());
    }

    protected abstract Java.FieldDeclaration[] generateFields();

    public Class cookToClass() throws CompileException {
        return compileToClass(makeCompilationUnit());
    }

    protected Java.CompilationUnit makeCompilationUnit() throws CompileException {
        Java.CompilationUnit cu = new Java.CompilationUnit(null);
        Location loc = new Location(null, ((short) 0), ((short) 0));
        cu.setPackageDeclaration(new Java.PackageDeclaration(loc, "com.boundary.tuple"));
        cu.addImportDeclaration(new Java.CompilationUnit.SingleTypeImportDeclaration(loc, "com.boundary.tuple.unsafe.Coterie".split("\\.")));
        Class[] ifaces;
        if (iface != null) {
            ifaces = new Class[] {iface};
        } else {
            ifaces = new Class[] {};
        }
        Java.PackageMemberClassDeclaration cd = new Java.PackageMemberClassDeclaration(
                loc,
                null, //doc
                new Java.Modifiers(Mod.PUBLIC),
                className,
                null, //type parameters
                classToType(loc, FastTuple.class), //class to extend
                classesToTypes(loc, ifaces)
        );
        cu.addPackageMemberTypeDeclaration(cd);
        for (Java.FieldDeclaration dec : generateFields()) {
            cd.addFieldDeclaration(dec);
        }
        cd.addConstructor(new Java.ConstructorDeclarator(
                loc,
                null, //doc
                new Java.Modifiers(Mod.PUBLIC),
                new Java.FunctionDeclarator.FormalParameters(
                        loc,
                        new Java.FunctionDeclarator.FormalParameter[] {},
                        false //variable arity
                ),
                new Java.Type[] {},
                null,
                Lists.<Java.BlockStatement>newArrayList())
        );
        for (int i = 0; i < fieldNames.length; i++) {
            String name = fieldNames[i];
            Class type = fieldTypes[i];
            int index = indexes[i];

            cd.addDeclaredMethod(generateGetter(name, type, index));
            cd.addDeclaredMethod(generateSetter(name, type, index));
        }
        cd.addDeclaredMethod(generateIndexedGetter());
        return cu;
    }

    protected Java.MethodDeclarator generateIndexedGetter() {
         return new Java.MethodDeclarator(
                 loc,
                 null,
                 new Java.Modifiers(Mod.PUBLIC),
                 classToType(loc, Object.class),
                 "indexedGet",
                 new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {
                         new Java.FunctionDeclarator.FormalParameter(loc,true, classToType(loc, Integer.TYPE), "index")}, false),
                 new Java.Type[] {},
                 generateIndexedGetterImpl("index")
         );
    }

    protected abstract List<Java.BlockStatement> generateIndexedGetterImpl(String paramName);

    protected Java.MethodDeclarator generateGetter(String name, Class type, int index) {
        // unsafe().get* (long)
        Java.BlockStatement st = new Java.ReturnStatement(loc, generateGetInvocation(type, index));
        return new Java.MethodDeclarator(
                loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                classToType(loc, type),
                name,
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {}, false),
                new Java.Type[] {},
                Lists.newArrayList(st)
        );
    }

    protected Java.MethodDeclarator generateSetter(String name, Class type, int index) throws CompileException {
        Java.BlockStatement st = new Java.ExpressionStatement(generateSetInvocation(type, index));
        return new Java.MethodDeclarator(
                loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                classToType(loc, Void.TYPE),
                name,
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {
                    new Java.FunctionDeclarator.FormalParameter(loc, true, classToType(loc, type), "value")
                }, false),
                new Java.Type[] {},
                Lists.newArrayList(st)
        );
    }

    protected abstract Java.MethodInvocation generateGetInvocation(Class type, int index);
    protected abstract Java.MethodInvocation generateSetInvocation(Class type, int index);
}
