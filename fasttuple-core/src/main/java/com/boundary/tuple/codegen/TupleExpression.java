package com.boundary.tuple.codegen;

import com.boundary.tuple.FastTuple;
import com.boundary.tuple.TupleSchema;
import com.google.common.collect.Lists;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.*;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cliff on 5/12/14.
 */
public class TupleExpression extends ClassBodyEvaluator {
    public static interface Evaluator {
        public void evaluate(FastTuple tuple);
    }

    public static interface ObjectEvaluator {
        public Object evaluate(FastTuple tuple);
    }

    public static interface LongEvaluator {
        public long evaluate(FastTuple tuple);
    }

    public static interface IntEvaluator {
        public long evaluate(FastTuple tuple);
    }

    public static interface ShortEvaluator {
        public short evaluate(FastTuple tuple);
    }

    public static interface CharEvaluator {
        public char evaluate(FastTuple tuple);
    }

    public static interface ByteEvaluator {
        public byte evaluate(FastTuple tuple);
    }

    public static interface FloatEvaluator {
        public byte evaluate(FastTuple tuple);
    }

    public static interface DoubleEvaluator {
        public byte evaluate(FastTuple tuple);
    }

    private static String packageName = "com.boundary.tuple";
    private static AtomicLong counter = new AtomicLong(0);
    private String expression;
    private TupleSchema schema;
    private Class evaluatorClass;
    private Object evaluator;
    private Class iface;
    private Class returnType;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String expression = null;
        private TupleSchema schema = null;

        public Builder() {}

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public Builder schema(TupleSchema schema) {
            this.schema = schema;
            return this;
        }

        public Evaluator returnVoid() throws Exception {
            return (Evaluator) new TupleExpression(schema, expression, Evaluator.class, Void.TYPE).evaluator();
        }

        public ObjectEvaluator returnObject() throws Exception {
            return (ObjectEvaluator) new TupleExpression(schema, expression, ObjectEvaluator.class, Object.class).evaluator();
        }

        public LongEvaluator returnLong() throws Exception {
            return (LongEvaluator) new TupleExpression(schema, expression, LongEvaluator.class, Long.TYPE).evaluator();
        }

        public IntEvaluator returnInt() throws Exception {
            return (IntEvaluator) new TupleExpression(schema, expression, IntEvaluator.class, Integer.TYPE).evaluator();
        }

        public ShortEvaluator returnShort() throws Exception {
            return (ShortEvaluator) new TupleExpression(schema, expression, ShortEvaluator.class, Short.TYPE).evaluator();
        }

        public CharEvaluator returnChar() throws Exception {
            return (CharEvaluator) new TupleExpression(schema, expression, CharEvaluator.class, Character.TYPE).evaluator();
        }

        public ByteEvaluator returnByte() throws Exception {
            return (ByteEvaluator) new TupleExpression(schema, expression, ByteEvaluator.class, Byte.TYPE).evaluator();
        }

        public FloatEvaluator returnFloat() throws Exception {
            return (FloatEvaluator) new TupleExpression(schema, expression, FloatEvaluator.class, Float.TYPE).evaluator();
        }

        public DoubleEvaluator returnDouble() throws Exception {
            return (DoubleEvaluator) new TupleExpression(schema, expression, DoubleEvaluator.class, Double.TYPE).evaluator();
        }

    }

    private TupleExpression(TupleSchema schema, String expression, Class iface, Class returnType) throws Exception {
        this.schema = schema;
        this.expression = expression;
        this.iface = iface;
        this.returnType = returnType;
        setParentClassLoader(schema.getClassLoader());
        generateEvaluatorClass();
    }

    private void generateEvaluatorClass() throws Exception {
        Scanner scanner = new Scanner(null, new StringReader(expression));
        Parser parser = new Parser(scanner);
        Location loc = parser.location();
        String className = "Evaluator" + counter.incrementAndGet();
        setClassName(packageName + "." + className);
        Java.CompilationUnit cu = makeCompilationUnit(parser);
        cu.setPackageDeclaration(new Java.PackageDeclaration(loc, packageName));
        Java.PackageMemberClassDeclaration cd = new Java.PackageMemberClassDeclaration(loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                className,
                null,
                null,
                new Java.Type[] {
                        classToType(loc, iface)
                }
        );
        cu.addPackageMemberTypeDeclaration(cd);
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
        cd.addDeclaredMethod(generateFrontendMethod(loc));
        cd.addDeclaredMethod(generateBackendMethod(parser));
        this.evaluatorClass = compileToClass(cu);
        this.evaluator = evaluatorClass.getConstructor().newInstance();
    }

    private Java.MethodDeclarator generateFrontendMethod(Location loc) throws Exception {
        return new Java.MethodDeclarator(loc,
                null,
                new Java.Modifiers(Mod.PUBLIC),
                classToType(loc, returnType),
                "evaluate",
                generateArgs(loc, FastTuple.class),
                new Java.Type[0],
                Lists.<Java.BlockStatement>newArrayList(
                        maybeGenerateReturn(loc,
                                new Java.MethodInvocation(
                                        loc,
                                        null,
                                        "doEval",
                                        new Java.Rvalue[] {
                                            new Java.Cast(
                                                    loc,
                                                    new Java.ReferenceType(loc, schema.tupleClass().getCanonicalName().split("\\."), null),
                                                    new Java.AmbiguousName(loc, new String[] {"tuple"})
                                            )
                                        }
                                )
                        )
                )
        );
    }

    private Java.MethodDeclarator generateBackendMethod(Parser parser) throws Exception {
        Location loc = parser.location();
        List<Java.BlockStatement> statements = Lists.newArrayList();
        Java.Rvalue[] exprs = parser.parseExpressionList();
        for (int i=0; i<exprs.length; i++) {
            if (i == exprs.length - 1) {
                statements.add(maybeGenerateReturn(loc, exprs[i]));
            } else {
                statements.add(new Java.ExpressionStatement(exprs[i]));
            }
        }

        return new Java.MethodDeclarator(loc,
                null,
                new Java.Modifiers(Mod.PRIVATE),
                classToType(loc, returnType),
                "doEval",
                generateArgs(loc, schema.tupleClass()),
                new Java.Type[0],
                statements
        );
    }

    private Java.BlockStatement maybeGenerateReturn(Location loc, Java.Rvalue statement) throws Exception {
        if (returnType.equals(Void.TYPE)) {
            return new Java.ExpressionStatement(statement);
        } else {
            return new Java.ReturnStatement(loc, statement);
        }
    }

    private Java.FunctionDeclarator.FormalParameters generateArgs(Location loc, Class c) {
        return new Java.FunctionDeclarator.FormalParameters(
                loc,
                new Java.FunctionDeclarator.FormalParameter[] {
                        new Java.FunctionDeclarator.FormalParameter(
                                loc,
                                true,
                                classToType(loc, c),
                                "tuple"
                        )
                },
                false
        );
    }

    public Object evaluator() {
        return evaluator;
    }
}
