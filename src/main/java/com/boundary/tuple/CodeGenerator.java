package com.boundary.tuple;

import com.google.common.collect.Lists;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.Java;
import org.codehaus.janino.Mod;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cliff on 5/3/14.
 */
public class CodeGenerator extends ClassBodyEvaluator {
    private static AtomicLong counter = new AtomicLong(0l);
    private Class iface;
    private String className;
    private String[] fieldNames;
    private Class[] fieldTypes;
    private int[] indexes;

    public CodeGenerator(Class iface, String[] fieldNames, Class[] fieldTypes, int[] indexes) {
        this.iface = iface;
        this.fieldNames = fieldNames.clone();
        this.fieldTypes = fieldTypes.clone();
        this.indexes = indexes.clone();
        this.className = "FastTuple" + counter.getAndDecrement();
    }

    public Class cookToClass() throws CompileException {
        return compileToClass(makeCompilationUnit());
    }

    protected Java.CompilationUnit makeCompilationUnit() throws CompileException {
        Java.CompilationUnit cu = new Java.CompilationUnit(null);
        Location loc = new Location(null, ((short) 0), ((short) 0));
        cu.setPackageDeclaration(new Java.PackageDeclaration(loc, "com.boundary.fasttuple"));
        cu.addImportDeclaration(new Java.CompilationUnit.StaticImportOnDemandDeclaration(loc,
                new String[] {"com.boundary.fasttuple.unsafe.Coterie.*"}));
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
        cd.addFieldDeclaration(new Java.FieldDeclaration(
                loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                new Java.BasicType(loc, Java.BasicType.LONG),
                new Java.VariableDeclarator[] {new Java.VariableDeclarator(loc, "address", 0, null)}));
        cd.addConstructor(new Java.ConstructorDeclarator(
                loc,
                null, //doc
                new Java.Modifiers(Mod.PUBLIC),
                new Java.FunctionDeclarator.FormalParameters(
                        loc,
                        new Java.FunctionDeclarator.FormalParameter[] {
                                new Java.FunctionDeclarator.FormalParameter(
                                        loc,
                                        true, //final
                                        new Java.BasicType(loc, Java.BasicType.LONG),
                                        "address")
                        },
                        false //variable arity
                ),
                new Java.Type[] {},
                null,
                Lists.<Java.BlockStatement>newArrayList(
                        new Java.ExpressionStatement(new Java.Assignment(
                                loc,
                                new Java.FieldAccessExpression(loc, new Java.ThisReference(loc), "address"),
                                "=",
                                new Java.AmbiguousName(loc, new String[]{"address"}))
                ))
        ));
        for (int i = 0; i < fieldNames.length; i++) {
            String name = fieldNames[i];
            Class type = fieldTypes[i];
            int index = indexes[i];

            cd.addDeclaredMethod(generateGetter(loc, name, type, index));
            cd.addDeclaredMethod(generateSetter(loc, name, type, index));
        }
        return cu;
    }

    protected Java.MethodDeclarator generateGetter(Location loc, String name, Class type, int index) {
        // unsafe().get* (long)
        Java.ReturnStatement st = new Java.ReturnStatement(loc, getInvocation(loc, type, index));
        return new Java.MethodDeclarator(
                loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                classToType(loc, type),
                name,
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {}, false),
                null,
                Lists.<Java.BlockStatement>newArrayList(st)
        );
    }

    protected Java.MethodDeclarator generateSetter(Location loc, String name, Class type, int index) {
        Java.ReturnStatement st = new Java.ReturnStatement(loc, setInvocation(loc, type, index));
        return new Java.MethodDeclarator(
                loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                classToType(loc, type),
                name,
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {
                    new Java.FunctionDeclarator.FormalParameter(loc, true, classToType(loc, type), "value")
                }, false),
                null,
                Lists.<Java.BlockStatement>newArrayList(st)
        );
    }

    protected Java.MethodInvocation getInvocation(Location loc, Class type, int index) {
        return new Java.MethodInvocation(loc,
                new Java.MethodInvocation(loc, null, "unsafe", new Java.Rvalue[] {}),
                "get" + accessorForType(type),
                new Java.Rvalue[] {
                        new Java.BinaryOperation(loc,
                                new Java.AmbiguousName(loc, new String[] {"address"}),
                                "+",
                                new Java.IntegerLiteral(loc, Integer.toString(index)))
                }
        );
    }

    protected Java.MethodInvocation setInvocation(Location loc, Class type, int index) {
        return new Java.MethodInvocation(loc,
                new Java.MethodInvocation(loc, null, "unsafe", new Java.Rvalue[]{}),
                "put" + accessorForType(type),
                new Java.Rvalue[]{
                        new Java.BinaryOperation(loc,
                                new Java.AmbiguousName(loc, new String[]{"address"}),
                                "+",
                                new Java.IntegerLiteral(loc, Integer.toString(index))),
                        new Java.AmbiguousName(loc, new String[] {"value"})
                }
        );
    }

    protected String accessorForType(Class type) {
        if (type.equals(Byte.TYPE)) {
            return "Byte";
        } else if (type.equals(Character.TYPE)) {
            return "Char";
        } else if (type.equals(Short.TYPE)) {
            return "Short";
        } else if (type.equals(Integer.TYPE)) {
            return "Int";
        } else if (type.equals(Float.TYPE)) {
            return "Float";
        } else if (type.equals(Double.TYPE)) {
            return "Double";
        } else if (type.equals(Long.TYPE)) {
            return "Long";
        }
        return null;
    }
}
