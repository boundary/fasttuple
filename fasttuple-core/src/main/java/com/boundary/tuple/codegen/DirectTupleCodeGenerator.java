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
    protected int[] layout;

    public DirectTupleCodeGenerator(Class iface, String[] fieldNames, Class[] fieldTypes, int[] layout) {
        super(iface, fieldNames, fieldTypes);
        this.layout = layout.clone();
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
    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedGetterImpl() throws CompileException {
        List<Java.SwitchStatement.SwitchBlockStatementGroup> list = Lists.newArrayList();
        for (int i=0; i < fieldNames.length; i++) {
            list.add(
                    new Java.SwitchStatement.SwitchBlockStatementGroup(loc,
                            Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, Integer.toString(i+1))),
                            false,
                            Lists.<Java.BlockStatement>newArrayList(new Java.ReturnStatement(loc,
                                    generateGetInvocation(fieldTypes[i], i)))
                    )
            );
        }
        list.add(generateDefaultCase());
        return list;
    }

    @Override
    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedGetterImpl(Class type) throws CompileException {
        List<Java.SwitchStatement.SwitchBlockStatementGroup> list = Lists.newArrayList();
        for (int n=0; n < fieldNames.length; n++) {
            if (!type.equals(fieldTypes[n])) {
                continue;
            }
            list.add(new Java.SwitchStatement.SwitchBlockStatementGroup(loc,
                    Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, String.valueOf(n+1))),
                    false,
                    Lists.<Java.BlockStatement>newArrayList(
                            new Java.ReturnStatement(loc, generateGetInvocation(type, n))
                    )
            ));
        }
        list.add(generateDefaultCase());
        return list;
    }

    @Override
    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedSetterImpl(String value) throws CompileException {
        List<Java.SwitchStatement.SwitchBlockStatementGroup> list = Lists.newArrayList();
        for (int i=0; i < fieldNames.length; i++) {
            list.add(
                    new Java.SwitchStatement.SwitchBlockStatementGroup(loc,
                            Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, Integer.toString(i+1))),
                            false,
                            Lists.<Java.BlockStatement>newArrayList(
                                    new Java.ExpressionStatement(generateSetInvocation(fieldTypes[i], i, value)),
                                    new Java.BreakStatement(loc, null)
                            )
                    )
            );
        }
        list.add(generateDefaultCase());
        return list;
    }

    @Override
    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedSetterImpl(String value, Class type) throws CompileException {
        List<Java.SwitchStatement.SwitchBlockStatementGroup> list = Lists.newArrayList();
        for (int n=0; n < fieldNames.length; n++) {
            if (!type.equals(fieldTypes[n])) {
                continue;
            }
            list.add(new Java.SwitchStatement.SwitchBlockStatementGroup(loc,
                    Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, String.valueOf(n+1))),
                    false,
                    Lists.<Java.BlockStatement>newArrayList(
                            new Java.ExpressionStatement(generateSetInvocation(type, n, value)),
                            new Java.BreakStatement(loc, null)
                    )
            ));
        }
        list.add(generateDefaultCase());
        return list;
    }

    protected Java.Rvalue generateGetInvocation(Class type, int index) throws CompileException {
        return new Java.MethodInvocation(loc,
                new Java.AmbiguousName(loc, new String[] {"unsafe"}),
                "get" + accessorForType(type),
                new Java.Rvalue[] {
                        new Java.BinaryOperation(loc,
                                new Java.AmbiguousName(loc, new String[] {"address"}),
                                "+",
                                new Java.IntegerLiteral(loc, Integer.toString(layout[index])))
                }
        );
    }

    protected Java.Rvalue generateSetInvocation(Class type, int index, String value) throws CompileException {
        return new Java.MethodInvocation(loc,
                new Java.AmbiguousName(loc, new String[] {"unsafe"}),
                "put" + accessorForType(type),
                new Java.Rvalue[]{
                        new Java.BinaryOperation(loc,
                                new Java.AmbiguousName(loc, new String[]{"address"}),
                                "+",
                                new Java.IntegerLiteral(loc, Integer.toString(layout[index]))),
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
}
