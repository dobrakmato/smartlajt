package eu.matejkormuth.smartlajt;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Slf4j
public final class RuleParser {

    public static void main(String[] args) {
        new RuleParser("when light level is less than 20 and my phone is present for 3 times turn on the led light").parse();
        new RuleParser("at 09:00 turn off the led light").parse();
    }

    private static final Pattern PATTERN_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
    private final String[] tokens;
    private int index;

    public RuleParser(String str) {
        tokens = str.trim().split("\\s+");
        index = 0;
    }

    private String peek() {
        try {
            return tokens[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuleParseException("Unexpected end of rule.");
        }
    }

    private String next() {
        return tokens[index++];
    }

    private boolean hasNextToken() {
        return tokens.length > index;
    }

    private void parse() {
        val actual = expectKeyword("when", "at");
        if (actual.equalsIgnoreCase("when")) {
            expectDeviceNameOrUUID();
            expectKeyword("is");
            expectFilterExpression();
            while (expectFilterAugmentOrConjunction()) ;
        } else {
            val time = expectTime();

            System.out.println("At (time): " + time);
        }
        expectAction();
    }

    private LocalTime expectTime() {
        val token = next();
        try {
            return LocalTime.parse(token);
        } catch (DateTimeParseException e) {
            throw new RuleParseException("Provided time '" + token + "' is in invalid format!", e);
        }
    }

    private void expectAction() {
        expectOptionalKeyword("trigger", "do");
        val exactMatch = expectCommandNameOrUUID();
        expectKeyword("the");
        expectCommandDeviceNameOrUUID(exactMatch);
    }

    private boolean expectFilterAugmentOrConjunction() {
        val expected = expectKeyword(false,
                "for",
                "and", "or"
        );

        if (expected == null) {
            return false;
        }

        if (expected.equalsIgnoreCase("for")) {
            val times = expectLong();

            System.out.println(" Filter Augmentation: TimesFilter(times=" + times + ")");
        } else if (expected.equalsIgnoreCase("and")) {
            System.out.println("And");
            expectDeviceNameOrUUID();
            expectKeyword("is");
            expectFilterExpression();
        } else if (expected.equalsIgnoreCase("or")) {
            throw new UnsupportedOperationException("Or is unsupported. Please create additional rule manually.");
        }
        return true;
    }

    private void expectFilterExpression() {
        val EXPECTED_KEYWORDS = new String[]{
                "equal",
                "less", "smaller",
                "more", "greater",
                "true", "on", "present", "available",
                "false", "off", "unavailable"
        };
        val EXPECTED_KEYWORDS_NOT = new String[]{
                "not",
                "equal",
                "less", "smaller",
                "more", "greater",
                "true", "on", "present", "available",
                "false", "off", "unavailable"
        };
        val expected = expectKeyword(EXPECTED_KEYWORDS_NOT);

        val opposite = expected.equalsIgnoreCase("not");
        val keyword = opposite ? expectKeyword(EXPECTED_KEYWORDS) : expected;

        if (keyword.equalsIgnoreCase("equal")) {
            expectKeyword("to");

            val value = nextNumericIsDouble() ? expectDouble() : expectLong();

            System.out.println("Filter: x " + (opposite ? "!=" : "==") + " " + value);
        } else if (keyword.equalsIgnoreCase("less") || keyword.equalsIgnoreCase("smaller")) {
            expectKeyword("than");

            val value = nextNumericIsDouble() ? expectDouble() : expectLong();

            System.out.println("Filter: x " + (opposite ? ">=" : "<=") + " " + value);
        } else if (keyword.equalsIgnoreCase("more") || keyword.equalsIgnoreCase("greater")) {
            expectKeyword("than");

            val value = nextNumericIsDouble() ? expectDouble() : expectLong();

            System.out.println("Filter: x " + (opposite ? "<=" : ">=") + " " + value);
        } else if (keyword.equalsIgnoreCase("true") || keyword.equalsIgnoreCase("on") ||
                keyword.equalsIgnoreCase("present") || keyword.equalsIgnoreCase("available")) {
            System.out.println("Filter: x " + (opposite ? "== false" : "== true"));
        } else if (keyword.equalsIgnoreCase("false") || keyword.equalsIgnoreCase("off") ||
                keyword.equalsIgnoreCase("unavailable")) {
            System.out.println("Filter: x " + (opposite ? "== true" : "== false"));
        }
    }

    private double expectDouble() {
        return Double.valueOf(next());
    }

    private long expectLong() {
        try {
            return Long.valueOf(next());
        } catch (NumberFormatException e) {
            throw new RuleParseException("Expected Long number is in invalid format!", e);
        }
    }

    private boolean nextNumericIsDouble() {
        try {
            return peek().contains(".");
        } catch (NumberFormatException e) {
            throw new RuleParseException("Expected Double number is in invalid format!", e);
        }
    }

    private void expectCommandDeviceNameOrUUID(boolean optional) {
        if (!hasNextToken() && optional) {
            return;
        }

        val possibleUUID = next();

        if (PATTERN_UUID.matcher(possibleUUID).matches()) {
            System.out.println(" Device UUID: " + possibleUUID);
        } else {
            val deviceName = new StringBuilder(possibleUUID);
            while (hasNextToken()) {
                deviceName.append(" ").append(next());
            }
            System.out.println(" Device Name: " + deviceName.toString());
        }
    }

    private void expectDeviceNameOrUUID() {
        val possibleUUID = next();

        if (PATTERN_UUID.matcher(possibleUUID).matches()) {
            System.out.println("Device UUID: " + possibleUUID);
        } else {
            val deviceName = new StringBuilder(possibleUUID);
            while (!peek().equalsIgnoreCase("is")) {
                deviceName.append(" ").append(next());
            }
            System.out.println("Device Name: " + deviceName.toString());
        }
    }

    private boolean expectCommandNameOrUUID() {
        val possibleUUID = next();

        if (PATTERN_UUID.matcher(possibleUUID).matches()) {
            System.out.println("Command UUID: " + possibleUUID);
            return true;
        } else {
            val deviceName = new StringBuilder(possibleUUID);
            while (hasNextToken() && !peek().equalsIgnoreCase("is") && !peek().equalsIgnoreCase("the")) {
                deviceName.append(" ").append(next());
            }
            System.out.println("Command Name: " + deviceName.toString());
            return false;
        }
    }

    private String expectKeyword(String... keywords) {
        return expectKeyword(true, keywords);
    }

    private String expectKeyword(boolean fail, String... keywords) {
        val token = next();

        for (val keyword : keywords) {
            if (keyword.equalsIgnoreCase(token)) {
                return keyword;
            }
        }
        if (fail) {
            throw new RuleParseException("Unexpected token '" + token + "'! Expected keyword(s) '"
                    + String.join("' , '", keywords) + "' instead.");
        }
        return null;
    }

    private void expectOptionalKeyword(String... keywords) {
        val token = peek();

        for (val keyword : keywords) {
            if (keyword.equalsIgnoreCase(token)) {
                next();
                return;
            }
        }
    }
}
