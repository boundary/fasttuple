package com.boundary.tuple.codegen;

import com.boundary.tuple.FastTuple;
import com.boundary.tuple.TupleSchema;
import com.google.common.collect.Lists;
import org.codehaus.commons.compiler.Location;
import org.codehaus.janino.*;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.boundary.tuple.codegen.CodegenUtil.*;

/**
 * Created by cliff on 5/12/14.
 */
public class TupleExpressionGenerator extends ClassBodyEvaluator {
    public static interface TupleExpression {
        public void evaluate(FastTuple tuple);
    }

    public static interface ObjectTupleExpression {
        public Object evaluate(FastTuple tuple);
    }

    public static interface LongTupleExpression {
        public long evaluate(FastTuple tuple);
    }

    public static interface IntTupleExpression {
        public long evaluate(FastTuple tuple);
    }

    public static interface ShortTupleExpression {
        public short evaluate(FastTuple tuple);
    }

    public static interface CharTupleExpression {
        public char evaluate(FastTuple tuple);
    }

    public static interface ByteTupleExpression {
        public byte evaluate(FastTuple tuple);
    }

    public static interface FloatTupleExpression {
        public byte evaluate(FastTuple tuple);
    }

    public static interface DoubleTupleExpression {
        public byte evaluate(FastTuple tuple);
    }

    public static interface BooleanTupleExpression {
        public boolean evaluate(FastTuple tuple);
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

        public TupleExpression returnVoid() throws Exception {
            return (TupleExpression) new TupleExpressionGenerator(schema, expression, TupleExpression.class, Void.TYPE).evaluator();
        }

        public ObjectTupleExpression returnObject() throws Exception {
            return (ObjectTupleExpression) new TupleExpressionGenerator(schema, expression, ObjectTupleExpression.class, Object.class).evaluator();
        }

        public LongTupleExpression returnLong() throws Exception {
            return (LongTupleExpression) new TupleExpressionGenerator(schema, expression, LongTupleExpression.class, Long.TYPE).evaluator();
        }

        public IntTupleExpression returnInt() throws Exception {
            return (IntTupleExpression) new TupleExpressionGenerator(schema, expression, IntTupleExpression.class, Integer.TYPE).evaluator();
        }

        public ShortTupleExpression returnShort() throws Exception {
            return (ShortTupleExpression) new TupleExpressionGenerator(schema, expression, ShortTupleExpression.class, Short.TYPE).evaluator();
        }

        public CharTupleExpression returnChar() throws Exception {
            return (CharTupleExpression) new TupleExpressionGenerator(schema, expression, CharTupleExpression.class, Character.TYPE).evaluator();
        }

        public ByteTupleExpression returnByte() throws Exception {
            return (ByteTupleExpression) new TupleExpressionGenerator(schema, expression, ByteTupleExpression.class, Byte.TYPE).evaluator();
        }

        public FloatTupleExpression returnFloat() throws Exception {
            return (FloatTupleExpression) new TupleExpressionGenerator(schema, expression, FloatTupleExpression.class, Float.TYPE).evaluator();
        }

        public DoubleTupleExpression returnDouble() throws Exception {
            return (DoubleTupleExpression) new TupleExpressionGenerator(schema, expression, DoubleTupleExpression.class, Double.TYPE).evaluator();
        }

        public BooleanTupleExpression returnBoolean() throws Exception {
            return (BooleanTupleExpression) new TupleExpressionGenerator(schema, expression, BooleanTupleExpression.class, Boolean.TYPE).evaluator();
        }

    }

    private TupleExpressionGenerator(TupleSchema schema, String expression, Class iface, Class returnType) throws Exception {
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
        String className = "TupleExpression" + counter.incrementAndGet();
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
        cd.addConstructor(nullConstructor(loc));
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
