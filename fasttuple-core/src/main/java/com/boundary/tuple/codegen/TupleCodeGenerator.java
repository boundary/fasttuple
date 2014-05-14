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
import static java.lang.Character.toUpperCase;


/**
 * Created by cliff on 5/3/14.
 */
public abstract class TupleCodeGenerator extends ClassBodyEvaluator {
    private static AtomicLong counter = new AtomicLong(0l);
    protected static Class[] types = new Class[] {
            Long.TYPE,
            Integer.TYPE,
            Short.TYPE,
            Character.TYPE,
            Byte.TYPE,
            Float.TYPE,
            Double.TYPE
    };
    protected Class iface;
    protected String[] fieldNames;
    protected Class[] fieldTypes;
    protected Location loc;
    protected String className;

    public TupleCodeGenerator(Class iface, String[] fieldNames, Class[] fieldTypes) {
        this.loc = new Location("", (short)0, (short)0);
        this.iface = iface;
        this.fieldNames = fieldNames.clone();
        this.fieldTypes = fieldTypes.clone();
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
        Location loc = new Location("", ((short) 0), ((short) 0));
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


            cd.addDeclaredMethod(generateGetter(name, type, i));
            cd.addDeclaredMethod(generateSetter(name, type, i));
        }
        cd.addDeclaredMethod(generateIndexedGetter());
        cd.addDeclaredMethod(generateIndexedSetter());
        for (Java.MethodDeclarator method : generateIndexedTypedGetters()) {
            cd.addDeclaredMethod(method);
        }
        for (Java.MethodDeclarator method : generateIndexedTypedSetters()) {
            cd.addDeclaredMethod(method);
        }
        return cu;
    }

    protected Java.MethodDeclarator generateIndexedGetter() throws CompileException {
         return new Java.MethodDeclarator(
                 loc,
                 null,
                 new Java.Modifiers(Mod.PUBLIC),
                 classToType(loc, Object.class),
                 "get",
                 new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {
                         new Java.FunctionDeclarator.FormalParameter(loc,true, classToType(loc, Integer.TYPE), "index")}, false),
                 new Java.Type[] {},
                 Lists.<Java.BlockStatement>newArrayList(
                         new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[] {"index"}), generateIndexedGetterImpl())
                 )
         );
    }

    protected List<Java.MethodDeclarator> generateIndexedTypedGetters() throws CompileException {
        List<Java.MethodDeclarator> methods = Lists.newArrayList();
        for (int i=0; i<types.length; i++) {
            methods.add(new Java.MethodDeclarator(
                loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                new Java.BasicType(loc, primIndex(types[i])),
                "get" + capitalize(types[i].getName()),
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {
                        new Java.FunctionDeclarator.FormalParameter(loc, true, new Java.BasicType(loc, Java.BasicType.INT), "index")}, false),
                new Java.Type[] {},
                Lists.<Java.BlockStatement>newArrayList(
                        new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[] {"index"}), generateIndexedGetterImpl(types[i]))
                )
            ));
        }
        return methods;
    }

    protected List<Java.MethodDeclarator> generateIndexedTypedSetters() throws CompileException {
        List<Java.MethodDeclarator> methods = Lists.newArrayList();
        for (int i=0; i<types.length; i++) {
            methods.add(new Java.MethodDeclarator(
                loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                new Java.BasicType(loc, Java.BasicType.VOID),
                "set" + capitalize(types[i].getName()),
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {
                        new Java.FunctionDeclarator.FormalParameter(loc, true, new Java.BasicType(loc, Java.BasicType.INT), "index"),
                        new Java.FunctionDeclarator.FormalParameter(loc, true, new Java.BasicType(loc, primIndex(types[i])), "value")
                }, false),
                new Java.Type[] {},
                Lists.<Java.BlockStatement>newArrayList(
                        new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[] {"index"}), generateIndexedSetterImpl("value", types[i]))
                )
            ));
        }
        return methods;
    }

    protected Java.MethodDeclarator generateIndexedSetter() throws CompileException {
        return new Java.MethodDeclarator(
                loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                classToType(loc, Void.TYPE),
                "set",
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[] {
                        new Java.FunctionDeclarator.FormalParameter(loc, true, classToType(loc, Integer.TYPE), "index"),
                        new Java.FunctionDeclarator.FormalParameter(loc, true, classToType(loc, Object.class), "value")
                },false),
                new Java.Type[] {},
                Lists.<Java.BlockStatement>newArrayList(
                    new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[] {"index"}), generateIndexedSetterImpl("value"))
                )
        );
    }

    protected abstract List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedGetterImpl() throws CompileException;
    protected abstract List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedGetterImpl(Class type) throws CompileException;
    protected abstract List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedSetterImpl(String value) throws CompileException;
    protected abstract List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedSetterImpl(String value, Class type) throws CompileException;

    protected Java.MethodDeclarator generateGetter(String name, Class type, int index) throws CompileException {
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
        Java.BlockStatement st = new Java.ExpressionStatement(generateSetInvocation(type, index, "value"));
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

    protected abstract Java.Rvalue generateGetInvocation(Class type, int index) throws CompileException;
    protected abstract Java.Rvalue generateSetInvocation(Class type, int index, String value) throws CompileException;

    protected String capitalize(String st) {
        return String.valueOf(toUpperCase(st.charAt(0))) + st.substring(1);
    }


    protected int primIndex(Class type) {
        if (type.equals(Long.TYPE)) return Java.BasicType.LONG;
        if (type.equals(Integer.TYPE)) return Java.BasicType.INT;
        if (type.equals(Short.TYPE)) return Java.BasicType.SHORT;
        if (type.equals(Character.TYPE)) return Java.BasicType.CHAR;
        if (type.equals(Byte.TYPE)) return Java.BasicType.BYTE;
        if (type.equals(Float.TYPE)) return Java.BasicType.FLOAT;
        if (type.equals(Double.TYPE)) return Java.BasicType.DOUBLE;
        return 0;
    }

    protected String primToBox(Class type) {
        if (type.equals(Long.TYPE)) return "Long";
        if (type.equals(Integer.TYPE)) return "Integer";
        if (type.equals(Short.TYPE)) return "Short";
        if (type.equals(Character.TYPE)) return "Character";
        if (type.equals(Byte.TYPE)) return "Byte";
        if (type.equals(Float.TYPE)) return "Float";
        if (type.equals(Double.TYPE)) return "Double";
        return null;
    }

    protected Java.SwitchStatement.SwitchBlockStatementGroup generateDefaultCase() {
        return new Java.SwitchStatement.SwitchBlockStatementGroup(
                loc,
                Lists.<Java.Rvalue>newArrayList(),
                true,
                Lists.<Java.BlockStatement>newArrayList(
                        new Java.ThrowStatement(
                                loc,
                                new Java.NewClassInstance(
                                        loc,
                                        null,
                                        new Java.ReferenceType(loc, new String[] {"IllegalArgumentException"}, null),
                                        new Java.Rvalue[0]))
                ));
    }

    protected Java.Type classToRefType(Class type) {
        if (type.isPrimitive()) {
            return new Java.ReferenceType(loc, primToBox(type).split("\\."), null);
        } else {
            return new Java.ReferenceType(loc, type.getCanonicalName().split("\\."), null);
        }
    }
}
