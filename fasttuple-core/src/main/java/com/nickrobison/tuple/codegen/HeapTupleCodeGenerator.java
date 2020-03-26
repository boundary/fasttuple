package com.nickrobison.tuple.codegen;

import com.google.common.collect.Lists;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.Java;

import java.util.List;

/**
 * Created by cliff on 5/9/14.
 */
public class HeapTupleCodeGenerator extends TupleCodeGenerator {

    public HeapTupleCodeGenerator(Class iface, String[] fieldName, Class[] fieldType) {
        super(iface, fieldName, fieldType);
    }

    @Override
    protected Java.FieldDeclaration[] generateFields() {
        Java.FieldDeclaration[] declarations = new Java.FieldDeclaration[fieldTypes.length];
        for (int i = 0; i < fieldTypes.length; i++) {
            declarations[i] = new Java.FieldDeclaration(
                    loc,
                    null,
                    new Java.AccessModifier[]{new Java.AccessModifier("public", loc)},
                    classToType(loc, fieldTypes[i]),
                    new Java.VariableDeclarator[]{
                            new Java.VariableDeclarator(loc, fieldNames[i], 0, null)
                    }
            );
        }

        return declarations;
    }

    @Override
    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedGetterImpl() throws CompileException {
        List<Java.SwitchStatement.SwitchBlockStatementGroup> list = Lists.newArrayList();
        for (int i = 0; i < fieldTypes.length; i++) {
            list.add(
                    new Java.SwitchStatement.SwitchBlockStatementGroup(
                            loc,
                            Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, String.valueOf(i + 1))),
                            false,
                            Lists.<Java.BlockStatement>newArrayList(new Java.ReturnStatement(loc, generateGetInvocation(fieldTypes[i], i)))
                    )
            );
        }
        list.add(generateDefaultCase());
        return list;
    }

    @Override
    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedGetterImpl(Class type) throws CompileException {
        List<Java.SwitchStatement.SwitchBlockStatementGroup> list = Lists.newArrayList();
        for (int i = 0; i < fieldTypes.length; i++) {
            if (!type.equals(fieldTypes[i])) {
                continue;
            }
            list.add(
                    new Java.SwitchStatement.SwitchBlockStatementGroup(
                            loc,
                            Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, String.valueOf(i + 1))),
                            false,
                            Lists.<Java.BlockStatement>newArrayList(new Java.ReturnStatement(loc, generateGetInvocation(fieldTypes[i], i)))
                    )
            );
        }
        list.add(generateDefaultCase());
        return list;
    }

    @Override
    protected List<Java.SwitchStatement.SwitchBlockStatementGroup> generateIndexedSetterImpl(String value) throws CompileException {
        List<Java.SwitchStatement.SwitchBlockStatementGroup> list = Lists.newArrayList();
        for (int i = 0; i < fieldTypes.length; i++) {
            list.add(
                    new Java.SwitchStatement.SwitchBlockStatementGroup(
                            loc,
                            Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, String.valueOf(i + 1))),
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
        for (int i = 0; i < fieldTypes.length; i++) {
            if (!type.equals(fieldTypes[i])) {
                continue;
            }
            list.add(
                    new Java.SwitchStatement.SwitchBlockStatementGroup(
                            loc,
                            Lists.<Java.Rvalue>newArrayList(new Java.IntegerLiteral(loc, String.valueOf(i + 1))),
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
    protected Java.Rvalue generateGetInvocation(Class type, int index) throws CompileException {
        return new Java.FieldAccessExpression(loc, new Java.ThisReference(loc), fieldNames[index]);
    }

    @Override
    protected Java.Rvalue generateSetInvocation(Class type, int index, String value) throws CompileException {
        return new Java.Assignment(loc, new Java.FieldAccessExpression(loc, new Java.ThisReference(loc), fieldNames[index]), "=",
                new Java.Cast(loc, classToRefType(fieldTypes[index]), new Java.AmbiguousName(loc, new String[]{value})));
    }
}
