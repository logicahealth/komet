package sh.isaac.provider.xslt.extension;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

public class ShiftLeft extends ExtensionFunctionDefinition {
    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName("eg", "http://example.com/saxon-extension", "shift-left");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_INTEGER, SequenceType.SINGLE_INTEGER};
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_INTEGER;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                long v0 = ((IntegerValue)arguments[0]).longValue();
                long v1 = ((IntegerValue)arguments[1]).longValue();
                long result = v0<<v1;
                return Int64Value.makeIntegerValue(result);
            }
        };
    }
}