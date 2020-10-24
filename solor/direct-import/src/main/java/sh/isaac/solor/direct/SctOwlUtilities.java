package sh.isaac.solor.direct;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.logic.assertions.SomeRole;
import sh.isaac.api.logic.assertions.connectors.And;
import sh.isaac.api.util.UuidT3Generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_WORD;
import static java.io.StreamTokenizer.TT_EOL;
import static java.io.StreamTokenizer.TT_NUMBER;
import static sh.isaac.api.logic.LogicalExpressionBuilder.*;

public class SctOwlUtilities {

    public static StreamTokenizer getParser(String stringToParse) {
        BufferedReader sctOwlReader = new BufferedReader(new StringReader(stringToParse));
        StreamTokenizer t = new StreamTokenizer(sctOwlReader);
        t.resetSyntax();
        t.wordChars('0', '9');
        t.wordChars('a', 'z');
        t.wordChars('A', 'Z');
        t.wordChars(128 + 32, 255);
        t.whitespaceChars(0, ' ');
        t.commentChar('#');
        t.eolIsSignificant(false);
        t.quoteChar('"');
        t.slashSlashComments(true);
        t.slashStarComments(true);
        return t;
    }

    public static LogicalExpression sctToLogicalExpression(int conceptNid,
                                                           String owlClassExpressionsToProcess,
                                                           String owlPropertyExpressionsToProcess) throws IOException {


        String originalExpression = owlClassExpressionsToProcess + " " + owlPropertyExpressionsToProcess;

        final LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService()
                .getLogicalExpressionBuilder();


        StreamTokenizer t = getParser(owlClassExpressionsToProcess);
        t.nextToken();
        while (t.ttype == TT_WORD) {
            switch (t.sval.toLowerCase()) {
                case "equivalentclasses":
                    SufficientSet(processSet(leb, t, originalExpression));
                    break;
                case "subclassof":
                    NecessarySet(processSet(leb, t, originalExpression));
                    break;

                case "transitiveobjectproperty":
                case "reflexiveobjectproperty":
                    NecessarySet(processObjectProperties(leb, t, originalExpression));
                    t.pushBack();
                    break;

                default:
                    throwIllegalStateException("Expecting equivalentclasses or subclassof.", t, originalExpression);

            }
            t.nextToken();
            if (t.ttype != ')') {
                if (t.ttype == TT_EOF) {
                    // OK alternative to conclude processing of expressions.
                } else {
                    throwIllegalStateException("Expecting closure of set with ).", t, originalExpression);
                }
            }
            while (t.ttype == ')') {
                t.nextToken();
            }
        }
        if (t.ttype != TT_EOF) {
            throwIllegalStateException("Expecting TT_WORD. Found: ", t, originalExpression);
        }


        t = getParser(owlPropertyExpressionsToProcess);
        t.nextToken();
        while (t.ttype == TT_WORD) {
            switch (t.sval.toLowerCase()) {

                case "subobjectpropertyof": // TODO: Temporary addition, pending discussion with Michael Lawley
                case "reflexiveobjectproperty":
                case "transitiveobjectproperty":
                    t.pushBack();
                    PropertySet(processPropertySet(leb, t, originalExpression));
                    break;

                default:
                    throwIllegalStateException("Expecting equivalentclasses or subclassof.", t, originalExpression);

            }
            t.nextToken();
            if (t.ttype != ')') {
                if (t.ttype == TT_EOF) {
                    // OK alternative to conclude processing of expressions.
                } else {
                    throwIllegalStateException("Expecting closure of set with ). ", t, originalExpression);
                }
            }
            while (t.ttype == ')') {
                t.nextToken();
            }
        }
        if (t.ttype != TT_EOF) {
            throwIllegalStateException("Expecting TT_WORD. ", t, originalExpression);
        }
        LogicalExpression expression = leb.build();
        expression.setConceptBeingDefinedNid(conceptNid);
        return expression;
    }

    private static ConceptAssertion handleSubclassOf(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer t, String original) throws IOException {
        if (t.nextToken() != '(') {
            throw new IllegalStateException("Expecting '(' found: " + t.ttype + " " + t.sval);
        }
        if (t.nextToken() != ':') {
            throw new IllegalStateException("Expecting ':' found: " + t.ttype + " " + t.sval);
        }
        if (t.nextToken() != TT_WORD) {
            throw new IllegalStateException("Expecting concept identifier found: " + t.ttype + " " + t.sval);
        }
        if (t.nextToken() != ':') {
            throw new IllegalStateException("Expecting ':' found: " + t.ttype + " " + t.sval);
        }
        if (t.nextToken() == TT_WORD) {
            return logicalExpressionBuilder.conceptAssertion(
                    Get.nidForUuids(UuidT3Generator.fromSNOMED(t.sval)));
        } else {
            throwIllegalStateException("Expecting concept identifier. ", t, original);
        }
        throw new IllegalStateException("unreachable");
    }


    private static And processObjectProperties(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer t, String original) throws IOException {
        List<Assertion> andList = new ArrayList<>();

         switch (t.sval.toLowerCase()) {

             case "transitiveobjectproperty":
                 parseAndDiscardOpenParen(t, original);
                 parseAndDiscardColon(t, original);
                 parseAndDiscardWord(t,original);
                 andList.add(logicalExpressionBuilder.conceptAssertion(MetaData.TRANSITIVE_FEATURE____SOLOR));
                 break;
             case "reflexiveobjectproperty":
                 parseAndDiscardOpenParen(t, original);
                 parseAndDiscardColon(t, original);
                 parseAndDiscardWord(t,original);
                 andList.add(logicalExpressionBuilder.conceptAssertion(MetaData.REFLEXIVE_FEATURE____SOLOR));
                 break;
             case "subclassof":
                 andList.add(handleSubclassOf(logicalExpressionBuilder, t, original));
                 break;
            default:
                throwIllegalStateException("Expecting identifier start or ObjectIntersectionOf. ", t, original);
        }

         while (t.ttype != TT_EOF) {
             t.nextToken();
             if (t.ttype == TT_WORD) {
                 handleNextObjectPropertyClause(logicalExpressionBuilder, t, original, andList);
                 parseToCloseParen(t);
             }
         }

        return And(andList.toArray(new Assertion[andList.size()]));
    }

    private static void handleNextObjectPropertyClause(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer t, String original, List<Assertion> andList) throws IOException {
        switch (t.ttype) {
            case TT_EOF:
                break;
            case TT_WORD:
                switch (t.sval.toLowerCase()) {

                    case "transitiveobjectproperty":
                        parseAndDiscardOpenParen(t, original);
                        parseAndDiscardColon(t, original);
                        parseAndDiscardWord(t,original);
                        andList.add(logicalExpressionBuilder.conceptAssertion(MetaData.TRANSITIVE_FEATURE____SOLOR));
                        break;
                    case "reflexiveobjectproperty":
                        parseAndDiscardOpenParen(t, original);
                        parseAndDiscardColon(t, original);
                        parseAndDiscardWord(t,original);
                        andList.add(logicalExpressionBuilder.conceptAssertion(MetaData.REFLEXIVE_FEATURE____SOLOR));
                        break;
                    case "subclassof":
                        andList.add(handleSubclassOf(logicalExpressionBuilder, t, original));
                        break;
                    default:
                        throwIllegalStateException("Expecting identifier start or ObjectIntersectionOf.", t, original);
                }
                parseToCloseParen(t);
                break;
            default:
                throwIllegalStateException(t, original);
        }
    }

    private static void throwIllegalStateException(StreamTokenizer t, String original) {
        throwIllegalStateException(Optional.empty(), t, original);
    }
    private static void throwIllegalStateException(String prefix, StreamTokenizer t, String original) {
        throwIllegalStateException(Optional.of(prefix), t, original);
    }
    private static void throwIllegalStateException(Optional<String> prefix, StreamTokenizer t, String original) {
        StringBuilder sb = new StringBuilder();
        if (prefix.isPresent()) {
            sb.append(prefix.get());
            sb.append(" ");
        }
        sb.append("Found: ");
        switch (t.ttype) {
            case TT_EOF:
                sb.append("TT_EOF");
                break;
            case TT_EOL:
                sb.append("TT_EOL");
                break;
            case TT_NUMBER:
                sb.append("TT_NUMBER: ");
                sb.append(t.nval);
                break;
            case TT_WORD:
                sb.append("TT_WORD: ");
                sb.append(t.sval);
                break;
            default:
                for (char c: Character.toChars(t.ttype)) {
                    sb.append(c);
                }
        }
        sb.append("\nOriginal: ");
        sb.append(original);
        throw new IllegalStateException(sb.toString());
    }

    private static And processPropertySet(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        List<Assertion> andList = new ArrayList<>();
        while (tokenizer.nextToken() != TT_EOF) {
            switch (tokenizer.ttype) {
                case TT_WORD:
                    switch (tokenizer.sval.toLowerCase()) {
                        case "subobjectpropertyof":
                            // SubObjectPropertyOf(ObjectPropertyChain(:127489000 :738774007) :127489000)
                            // SubPropertyOf( ObjectPropertyChain( :locatedIn :partOf ) :locatedIn )
                            // If x is located in y and y is part of z then x is located in z, for example a disease located in a part is located in the whole.
                            // If x is "located in" y and y is "part of" z then x is "located in" z, for example a disease located in a part is located in the whole.

                            // If X "located in" Y and Y "part of" Z then X "located in" Z, for example a disease located in a part is located in the whole.
                            // WHEN PATTERN THEN IMPLICATION
                            // PATTERN = NODE LIST? Ordered list as opposed to unordered and...
                            // SubObjectPropertyOf( ObjectPropertyChain( a:hasMother a:hasSister ) a:hasAunt )
                            //
                            // SubObjectPropertyOf(:738774007 :762705008)
                            if (tokenizer.nextToken() != '(') {
                                throwIllegalStateException("Expecting (.", tokenizer, original);
                            }
                            switch (tokenizer.nextToken()) {
                                case ':':
                                    // Skip concept id...
                                    if (tokenizer.nextToken() != TT_WORD) {
                                        throwIllegalStateException("Expecting Word.", tokenizer, original);
                                    }
                                    if (tokenizer.nextToken() != ':') {
                                        throwIllegalStateException("Expecting :.", tokenizer, original);
                                    }
                                    if (tokenizer.nextToken() != TT_WORD) {
                                        throwIllegalStateException("Expecting Word.", tokenizer, original);
                                    }
                                    andList.add(logicalExpressionBuilder.conceptAssertion(
                                            Get.nidForUuids(UuidT3Generator.fromSNOMED(tokenizer.sval))));
                                    parseToCloseParen(tokenizer);

                                    break;
                                case TT_WORD:
                                    if (!tokenizer.sval.toLowerCase().equals("objectpropertychain")) {
                                        throwIllegalStateException("Expected ObjectPropertyChain.", tokenizer, original);
                                    }
                                    andList.add(processObjectPropertyChain(logicalExpressionBuilder, tokenizer, original));
                                    parseToCloseParen(tokenizer);
                                    break;
                            }

                            break;

                        case "reflexiveobjectproperty":
                            andList.add(logicalExpressionBuilder.conceptAssertion(MetaData.REFLEXIVE_FEATURE____SOLOR));
                            parseToCloseParen(tokenizer);
                            break;

                        case "transitiveobjectproperty":
                            andList.add(logicalExpressionBuilder.conceptAssertion(MetaData.TRANSITIVE_FEATURE____SOLOR));
                            // TransitiveObjectProperty(:774081006)
                            parseToCloseParen(tokenizer);
                            break;

                        default:
                            throwIllegalStateException("Expecting ObjectIntersectionOf.", tokenizer, original);
                    }
                    break;

                default:
                    throwIllegalStateException("Expecting identifier start or ObjectIntersectionOf.", tokenizer, original);
            }
        }
        return And(andList.toArray(new Assertion[andList.size()]));
    }

    private static Assertion processObjectPropertyChain(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        // parse pattern; then parse implication.
        // objectpropertychain
        // ObjectPropertyChain(:363701004 :738774007) :363701004

        if (tokenizer.nextToken() != '(') {
            throwIllegalStateException("Expected (.", tokenizer, original);
        }
        List<Integer> propertyPatternList = new ArrayList<>();

        while (tokenizer.nextToken() == ':') {
            if (tokenizer.nextToken() != TT_WORD) {
                throwIllegalStateException("Expected TT_WORD.", tokenizer, original);
            }
            propertyPatternList.add(Get.nidForUuids(UuidT3Generator.fromSNOMED(tokenizer.sval)));
        }

        if (tokenizer.ttype != ')') {
            throwIllegalStateException("Expected ).", tokenizer, original);
        }
        if (tokenizer.nextToken() != ':') {
            throwIllegalStateException("Expected :.", tokenizer, original);
        }
        if (tokenizer.nextToken() != TT_WORD) {
            throwIllegalStateException("Expected TT_WORD.", tokenizer, original);
        }
        int propertyImplication = Get.nidForUuids(UuidT3Generator.fromSNOMED(tokenizer.sval));
        int[]  propertyPattern = new int[propertyPatternList.size()];
        for (int i = 0; i < propertyPattern.length; i++) {
            propertyPattern[i] = propertyPatternList.get(i);
        }
        return logicalExpressionBuilder.propertyPatternImplication(propertyPattern, propertyImplication);




    }
    private static void parseToCloseParen(StreamTokenizer tokenizer) throws IOException {

        while (tokenizer.ttype != ')' && tokenizer.ttype != TT_EOF) {
            // loop
            tokenizer.nextToken();
        }
    }



    private static And processSet(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() != '(') {
            throwIllegalStateException("Expecting (.", tokenizer, original);
        }
        switch (tokenizer.nextToken()) {
            case ':':
                break;
            case TT_WORD:
                switch (tokenizer.sval.toLowerCase()) {
                    case "objectintersectionof":
                        // in this case, the order of the AND and is swapped, and the identifier of the
                        // component being defined comes last...
                        List<Assertion> andList = new ArrayList<>();
                        andList.addAll(processObjectIntersectionOf(logicalExpressionBuilder, tokenizer, original));
                        // now an identifier for the concept being defined should be found...
                        if (tokenizer.nextToken() != ':') {
                            throw new IllegalStateException("Expecting :. Found: " + tokenizer + "\n" + original);
                        }
                        if (tokenizer.nextToken() != TT_WORD) {
                            throw new IllegalStateException("Expecting identifier. Found: " + tokenizer + "\n" + original);
                        }

                        return And(andList.toArray(new Assertion[andList.size()]));

                    default:
                        throwIllegalStateException("Expecting ObjectIntersectionOf.", tokenizer, original);
                }

            default:
                throwIllegalStateException("Expecting identifier start or ObjectIntersectionOf.", tokenizer, original);
        }
        if (tokenizer.nextToken() == TT_WORD) {
            // the identifier for the concept being defined.

        } else {
            throwIllegalStateException("Expecting identifier.", tokenizer, original);
        }

        List<Assertion> andList = new ArrayList<>();
        // can be either ObjectIntersectionOf or : for single concept
        switch (tokenizer.nextToken()) {
            case ':':
                andList.add(getConceptAssertion(logicalExpressionBuilder, tokenizer, original));
                break;
            case TT_WORD:
                if (tokenizer.sval.toLowerCase().equals("objectintersectionof")) {
                    andList.addAll(processObjectIntersectionOf(logicalExpressionBuilder, tokenizer, original));
                } else {
                    throwIllegalStateException("Expecting ObjectIntersectionOf.", tokenizer, original);
                }

                break;
            default:
                throwIllegalStateException("Expecting identifier or ObjectIntersectionOf.", tokenizer, original);
        }


        return And(andList.toArray(new Assertion[andList.size()]));
    }

    private static List<Assertion> processObjectIntersectionOf(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        List<Assertion> assertionList = new ArrayList<>();
        tokenizer.nextToken();
        while (tokenizer.ttype != ')' ) {
            switch (tokenizer.ttype) {
                case ':':
                    assertionList.add(getConceptAssertion(logicalExpressionBuilder, tokenizer, original));
                    break;
                case TT_WORD:
                    switch (tokenizer.sval.toLowerCase()) {
                        case "objectintersectionof":
                            processObjectIntersectionOf(logicalExpressionBuilder, tokenizer, original);
                            break;
                        case "objectsomevaluesfrom":
                            assertionList.add(getSomeRole(logicalExpressionBuilder, tokenizer, original));
                            break;
                    }
                    break;
            }

            tokenizer.nextToken();
        }
        //TODO finish...
        return assertionList;
    }

    private static ConceptAssertion getConceptAssertion(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() != TT_WORD) {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting SNOMED identifier.", tokenizer, original);
        }
        return logicalExpressionBuilder.conceptAssertion(Get.nidForUuids(UuidT3Generator.fromSNOMED(tokenizer.sval)));
    }

    private static SomeRole getSomeRole(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() != '(') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting (.", tokenizer, original);
        }
        if (tokenizer.nextToken() != ':') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting :.", tokenizer, original);
        }
        if (tokenizer.nextToken() != TT_WORD) {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting SNOMED identifier.", tokenizer, original);
        }

        SomeRole someRole = logicalExpressionBuilder.someRole(Get.nidForUuids(UuidT3Generator.fromSNOMED(tokenizer.sval)), getRestriction(logicalExpressionBuilder, tokenizer, original));
        if (tokenizer.nextToken() != ')') {
            // the identifier for the concept being defined.
            throwIllegalStateException("Expecting ).", tokenizer, original);
        }
        return someRole;
    }

    private static Assertion getRestriction(LogicalExpressionBuilder logicalExpressionBuilder, StreamTokenizer tokenizer, String original) throws IOException {
        switch (tokenizer.nextToken()) {
            case ':':
                return getConceptAssertion(logicalExpressionBuilder, tokenizer, original);
            case TT_WORD:
                switch (tokenizer.sval.toLowerCase()) {
                    case "objectintersectionof":
                        return And(processObjectIntersectionOf(logicalExpressionBuilder, tokenizer, original).toArray(new Assertion[1]));
                    case "objectsomevaluesfrom":
                        return And(getSomeRole(logicalExpressionBuilder, tokenizer, original));
                }
            default:
                throwIllegalStateException(tokenizer, original);

        }
        throw new IllegalStateException("unreachable");
    }


    public static  String logicalExpressionToSctOwlStr(LogicalExpression expression) {
        throw new UnsupportedOperationException();
    }



//        while (tokenizer.nextToken() != TT_EOF) {
//            switch (tokenizer.ttype) {
//                case '(':
//                    System.out.println('(');
//                    break;
//                case ')':
//                    System.out.println(')');
//                    break;
//                case ':':
//                    System.out.println(':');
//                    break;
//                case TT_WORD:
//                    System.out.println(tokenizer.sval);
//                    break;
//                default:
//                    System.out.println("Unrecognized ttype: " + tokenizer.ttype);
//            }
//
//        }

    private static void parseAndDiscardOpenParen(StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() == '(') {
            return;
        }
        throwIllegalStateException("Expecting '('.", tokenizer, original);
    }

    private static void parseAndDiscardColon(StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() == ':') {
            return;
        }
        throwIllegalStateException("Expecting ':'.", tokenizer, original);
    }
    private static void parseAndDiscardWord(StreamTokenizer tokenizer, String original) throws IOException {
        if (tokenizer.nextToken() == TT_WORD) {
            return;
        }
        throwIllegalStateException("Expecting ':'.", tokenizer, original);
    }
}
