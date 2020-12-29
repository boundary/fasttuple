package com.nickrobison.tuple.codegen;

import com.google.common.collect.Lists;
import com.nickrobison.tuple.FastTuple;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.Java;
import org.codehaus.janino.Java.AbstractCompilationUnit.SingleTypeImportDeclaration;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.nickrobison.tuple.codegen.CodegenUtil.nullConstructor;
import static java.lang.Character.toUpperCase;

/**
 * Created by cliff on 5/3/14.
 */
public abstract class TupleCodeGenerator extends ClassBodyEvaluator {
    public static final String VALUE = "value";
    public static final String INDEX = "index";
    private static AtomicLong counter = new AtomicLong(0L);
    protected static Class[] types = new Class[]{
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

    protected TupleCodeGenerator(Class iface, String[] fieldNames, Class[] fieldTypes) {
        this.loc = new Location("", (short) 0, (short) 0);
        this.iface = iface;
        this.fieldNames = fieldNames.clone();
        this.fieldTypes = fieldTypes.clone();
        this.className = "FastTuple" + counter.getAndIncrement();
        this.setClassName("com.nickrobison.tuple." + className);
        this.setParentClassLoader(this.getClass().getClassLoader());
    }

    protected abstract Java.FieldDeclaration[] generateFields();

    public Class cookToClass() throws CompileException {
        return compileToClass(makeCompilationUnit());
    }

    protected Java.CompilationUnit makeCompilationUnit() throws CompileException {
        Java.CompilationUnit cu = new Java.CompilationUnit(null, new SingleTypeImportDeclaration[]{new SingleTypeImportDeclaration(loc, "com.nickrobison.tuple.unsafe.Coterie".split("\\."))});
        Location cuLoc = new Location("", ((short) 0), ((short) 0));
        cu.setPackageDeclaration(new Java.PackageDeclaration(cuLoc, "com.nickrobison.tuple"));
        Class[] ifaces;
        if (iface != null) {
            ifaces = new Class[]{iface};
        } else {
            ifaces = new Class[]{};
        }
        Java.PackageMemberClassDeclaration cd = new Java.PackageMemberClassDeclaration(
                cuLoc,
                null, //doc
                new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, cuLoc)},
                className,
                null, //type parameters
                classToType(cuLoc, FastTuple.class), //class to extend
                classesToTypes(cuLoc, ifaces)
        );
        cu.addPackageMemberTypeDeclaration(cd);
        for (Java.FieldDeclaration dec : generateFields()) {
            cd.addFieldDeclaration(dec);
        }

        cd.addConstructor(nullConstructor(cuLoc));

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
                new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)},
                null,
                classToType(loc, Object.class),
                "get",
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[]{
                        new Java.FunctionDeclarator.FormalParameter(loc, new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)}, classToType(loc, Integer.TYPE), INDEX)}, false),
                new Java.Type[]{},
                null,
                Lists.<Java.BlockStatement>newArrayList(
                        new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[]{INDEX}), generateIndexedGetterImpl())
                )
        );
    }

    protected List<Java.MethodDeclarator> generateIndexedTypedGetters() throws CompileException {
        List<Java.MethodDeclarator> methods = Lists.newArrayList();
        for (Class type : types) {
            methods.add(new Java.MethodDeclarator(
                    loc,
                    null,
                    new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)},
                    null,
                    new Java.PrimitiveType(loc, primIndex(type)),
                    "get" + capitalize(type.getName()),
                    new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[]{
                            new Java.FunctionDeclarator.FormalParameter(loc, new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)}, new Java.PrimitiveType(loc, Java.Primitive.INT), INDEX)}, false),
                    new Java.Type[]{},
                    null,
                    Lists.<Java.BlockStatement>newArrayList(
                            new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[]{INDEX}), generateIndexedGetterImpl(type))
                    )
            ));
        }
        return methods;
    }

    protected List<Java.MethodDeclarator> generateIndexedTypedSetters() throws CompileException {
        List<Java.MethodDeclarator> methods = Lists.newArrayList();
        for (Class type : types) {
            methods.add(new Java.MethodDeclarator(
                    loc,
                    null,
                    new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)},
                    null,
                    new Java.PrimitiveType(loc, Java.Primitive.VOID),
                    "set" + capitalize(type.getName()),
                    new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[]{
                            new Java.FunctionDeclarator.FormalParameter(loc, new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)}, new Java.PrimitiveType(loc, Java.Primitive.INT), INDEX),
                            new Java.FunctionDeclarator.FormalParameter(loc, new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)}, new Java.PrimitiveType(loc, primIndex(type)), VALUE)
                    }, false),
                    new Java.Type[]{},
                    null,
                    Lists.<Java.BlockStatement>newArrayList(
                            new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[]{INDEX}), generateIndexedSetterImpl(VALUE, type))
                    )
            ));
        }
        return methods;
    }

    protected Java.MethodDeclarator generateIndexedSetter() throws CompileException {
        return new Java.MethodDeclarator(
                loc,
                null,
                new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)},
                null,
                classToType(loc, Void.TYPE),
                "set",
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[]{
                        new Java.FunctionDeclarator.FormalParameter(loc, new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)}, classToType(loc, Integer.TYPE), INDEX),
                        new Java.FunctionDeclarator.FormalParameter(loc, new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)}, classToType(loc, Object.class), VALUE)
                }, false),
                new Java.Type[]{},
                null,
                Lists.<Java.BlockStatement>newArrayList(
                        new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[]{INDEX}), generateIndexedSetterImpl(VALUE))
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
                new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)},
                null,
                classToType(loc, type),
                name,
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[]{}, false),
                new Java.Type[]{},
                null,
                Lists.newArrayList(st)
        );
    }

    protected Java.MethodDeclarator generateSetter(String name, Class type, int index) throws CompileException {
        Java.BlockStatement st = new Java.ExpressionStatement(generateSetInvocation(type, index, VALUE));
        return new Java.MethodDeclarator(
                loc,
                null,
                new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)},
                null,
                classToType(loc, Void.TYPE),
                name,
                new Java.FunctionDeclarator.FormalParameters(loc, new Java.FunctionDeclarator.FormalParameter[]{
                        new Java.FunctionDeclarator.FormalParameter(loc, new Java.AccessModifier[]{new Java.AccessModifier(CodegenUtil.PUBLIC, loc)}, classToType(loc, type), VALUE)
                }, false),
                new Java.Type[]{},
                null,
                Lists.newArrayList(st)
        );
    }

    protected abstract Java.Rvalue generateGetInvocation(Class type, int index) throws CompileException;

    protected abstract Java.Rvalue generateSetInvocation(Class type, int index, String value) throws CompileException;

    protected String capitalize(String st) {
        return toUpperCase(st.charAt(0)) + st.substring(1);
    }


    protected Java.Primitive primIndex(Class type) {
        if (type.equals(Long.TYPE)) return Java.Primitive.LONG;
        if (type.equals(Integer.TYPE)) return Java.Primitive.INT;
        if (type.equals(Short.TYPE)) return Java.Primitive.SHORT;
        if (type.equals(Character.TYPE)) return Java.Primitive.CHAR;
        if (type.equals(Byte.TYPE)) return Java.Primitive.BYTE;
        if (type.equals(Float.TYPE)) return Java.Primitive.FLOAT;
        if (type.equals(Double.TYPE)) return Java.Primitive.DOUBLE;
        return Java.Primitive.VOID;
    }

    protected String primToBox(Class type) {
        if (type.equals(Long.TYPE)) return "Long";
        if (type.equals(Integer.TYPE)) return "Integer";
        if (type.equals(Short.TYPE)) return "Short";
        if (type.equals(Character.TYPE)) return "Character";
        if (type.equals(Byte.TYPE)) return "Byte";
        if (type.equals(Float.TYPE)) return "Float";
        if (type.equals(Double.TYPE)) return "Double";
        throw new IllegalArgumentException(String.format("Unsupported type: %s", type.getSimpleName()));
    }

    protected Java.SwitchStatement.SwitchBlockStatementGroup generateDefaultCase() {
        return new Java.SwitchStatement.SwitchBlockStatementGroup(
                loc,
                Lists.newArrayList(),
                true,
                Lists.newArrayList(
                        new Java.ThrowStatement(
                                loc,
                                new Java.NewClassInstance(
                                        loc,
                                        null,
                                        new Java.ReferenceType(loc, new Java.NormalAnnotation[]{}, new String[]{"IllegalArgumentException"}, null),
                                        new Java.Rvalue[0]))
                ));
    }

    protected Java.Type classToRefType(Class type) {
        if (type.isPrimitive()) {
            return new Java.ReferenceType(loc, new Java.NormalAnnotation[]{}, primToBox(type).split("\\."), null);
        } else {
            return new Java.ReferenceType(loc, new Java.NormalAnnotation[]{}, type.getCanonicalName().split("\\."), null);
        }
    }
}
