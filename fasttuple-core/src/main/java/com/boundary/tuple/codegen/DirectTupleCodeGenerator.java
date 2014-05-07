package com.boundary.tuple.codegen;

import com.google.common.collect.Lists;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.Java;
import org.codehaus.janino.Mod;
import sun.misc.Unsafe;

import java.util.List;

/**
 * Created by cliff on 5/5/14.
 */
public class DirectTupleCodeGenerator extends TupleCodeGenerator {

    public DirectTupleCodeGenerator(Class iface, String[] fieldNames, Class[] fieldTypes, int[] indexes) {
        super(iface, fieldNames, fieldTypes, indexes);
    }

    protected Java.FieldDeclaration[] generateFields() {
        return new Java.FieldDeclaration[] {
                new Java.FieldDeclaration(
                        loc,
                        null,
                        new Java.Modifiers(Mod.PUBLIC),
                        new Java.BasicType(loc, Java.BasicType.LONG),
                        new Java.VariableDeclarator[] {new Java.VariableDeclarator(loc, "address", 0, null)}),
                new Java.FieldDeclaration(loc,
                        null,
                        new Java.Modifiers((short)(Mod.STATIC + Mod.PRIVATE)),
                        classToType(loc, Unsafe.class),
                        new Java.VariableDeclarator[] {
                            new Java.VariableDeclarator(loc,
                                    "unsafe",
                                    0,
                                    new Java.MethodInvocation(loc,
                                            new Java.AmbiguousName(loc, new String[] {"Coterie"}),
                                            "unsafe",
                                            new Java.Rvalue[0]))
                })
        };
    }

    @Override
    protected List<Java.BlockStatement> generateIndexedGetterImpl(String paramName) throws CompileException {
        return Lists.<Java.BlockStatement>newArrayList(
                new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[] {paramName}), generateIndexedGetSwitch()),
                new Java.ThrowStatement(loc, new Java.NewClassInstance(
                        loc,
                        null,
                        new Java.ReferenceType(loc, new String[] {"IllegalArgumentException"}, null),
                        new Java.Rvalue[0]))
        );
    }

    @Override
    protected List<Java.BlockStatement> generateIndexedSetterImpl(String index, String value) throws CompileException {
        List<Java.SwitchStatement.SwitchBlockStatementGroup> cases = generateIndexedSetSwitch(value);
        cases.add(new Java.SwitchStatement.SwitchBlockStatementGroup(loc, Lists.<Java.Rvalue>newArrayList(),true,
                Lists.< Java.BlockStatement>newArrayList(new Java.ThrowStatement(loc, new Java.NewClassInstance(
                    loc,
                    null,
                    new Java.ReferenceType(loc, new String[] {"IllegalArgumentException"}, null),
                    new Java.Rvalue[0])))));
        return Lists.<Java.BlockStatement>newArrayList(
                new Java.SwitchStatement(loc, new Java.AmbiguousName(loc, new String[] {index}), cases)
        );
    }

    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedGetSwitch() throws CompileException {
        List list = Lists.newArrayList();
        for (int i=0; i < fieldNames.length; i++) {
            list.add(
                new Java.SwitchStatement.SwitchBlockStatementGroup(loc,
                        Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, Integer.toString(i+1))),
                        false,
                        Lists.<Java.BlockStatement>newArrayList(new Java.ReturnStatement(loc,
                                generateGetInvocation(fieldTypes[i], indexes[i])))
                )
            );
        }
        return list;
    }

    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedSetSwitch(String value) throws CompileException {
        List list = Lists.newArrayList();
        for (int i=0; i < fieldNames.length; i++) {
            list.add(
                new Java.SwitchStatement.SwitchBlockStatementGroup(loc,
                        Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, Integer.toString(i+1))),
                        false,
                        Lists.<Java.BlockStatement>newArrayList(
                                new Java.ExpressionStatement(generateSetInvocation(fieldTypes[i], indexes[i], value)),
                                new Java.BreakStatement(loc, null)
                        )
                )
            );
        }
        return list;
    }

    protected Java.MethodInvocation generateGetInvocation(Class type, int index) throws CompileException {
        return new Java.MethodInvocation(loc,
                new Java.AmbiguousName(loc, new String[] {"unsafe"}),
                "get" + accessorForType(type),
                new Java.Rvalue[] {
                        new Java.BinaryOperation(loc,
                                new Java.AmbiguousName(loc, new String[] {"address"}),
                                "+",
                                new Java.IntegerLiteral(loc, Integer.toString(index)))
                }
        );
    }

    protected Java.MethodInvocation generateSetInvocation(Class type, int index, String value) throws CompileException {
        return new Java.MethodInvocation(loc,
                new Java.AmbiguousName(loc, new String[] {"unsafe"}),
                "put" + accessorForType(type),
                new Java.Rvalue[]{
                        new Java.BinaryOperation(loc,
                                new Java.AmbiguousName(loc, new String[]{"address"}),
                                "+",
                                new Java.IntegerLiteral(loc, Integer.toString(index))),
                        new Java.Cast(loc, classToRefType(type), new Java.AmbiguousName(loc, new String[] {value}))
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

    protected Java.Type classToRefType(Class type) {
        if (type.isPrimitive()) {
            return new Java.ReferenceType(loc, primToBox(type).split("\\."), null);
        } else {
            return new Java.ReferenceType(loc, type.getCanonicalName().split("\\."), null);
        }
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
}
