#!/bin/bash

CLASS_NAME="${1:-MyClass}"
LINE_RANGE="${2:-10-50}"
SOURCE_FILE_PATH="${3:-src/main/java/com/example/MyClass.java}"
SUFFIX="${4:-}"
PROMPT_CHOICE="${5:-no_ablation}"
PROMPT_TO_USE=""

# JUnit 4.13 Test Class Generation Prompt Template - No Ablation: Include persona and include few shot
PROMPT_NO_ABLATION="Act as an experienced Java developer specializing in test-driven development and quality assurance.

Generate a comprehensive JUnit 4.13 test class for the production class $CLASS_NAME.

## Requirements:

### 1. Target Code to Test
- Production Class: $CLASS_NAME
- Specific lines to cover: $LINE_RANGE
- Source file: $SOURCE_FILE_PATH

### 2. JUnit Version
- Use JUnit 4.13 exclusively
- Import annotations from \`org.junit\` package
- Use \`@Test\`, \`@Before\`, \`@After\`, \`@BeforeClass\`, \`@AfterClass\` as needed

### 3.  Naming Convention
- Test class name format: Test$CLASS_NAME$SUFFIX
- Base name: Test$CLASS_NAME
- If a test class already exists with that name, append a suffix:
  - Numeric suffixes: \`_1\`, \`_2\`, \`_3\`, etc.
  - Alphabetic suffixes: \`_A\`, \`_B\`, \`_C\`, etc.
- Examples:
  - First test class: \`TestUserService\`
  - Second test class: \`TestUserService_1\` or \`TestUserService_A\`
  - Third test class: \`TestUserService_2\` or \`TestUserService_B\`

### 4. Test Coverage
- Cover all public methods in lines $LINE_RANGE
- Include positive test cases (happy path)
- Include negative test cases (error conditions)
- Include edge cases and boundary conditions
- Test null inputs where applicable
- Use descriptive test method names following pattern: \`test<MethodName>_<Scenario>_<ExpectedResult>\`

### 5.  Code Structure
- Include necessary imports
- Set up test fixtures in \`@Before\` method if needed
- Clean up resources in \`@After\` method if needed
- Use meaningful assertions with assertion messages
- Add comments explaining complex test scenarios

### 6. Best Practices
- Follow AAA pattern (Arrange, Act, Assert)
- One assertion concept per test method
- Use appropriate JUnit 4. 13 assertions: \`assertEquals\`, \`assertTrue\`, \`assertFalse\`, \`assertNull\`, \`assertNotNull\`, \`assertThrows\` (via expected exception pattern)
- Mock external dependencies if necessary (mention mock framework if needed)

## Example Output

\`\`\`java
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

/**
 * Test class for Calculator
 * Covers lines 15-45 of Calculator.java
 */
public class TestCalculator {

    private Calculator calculator;

    @Before
    public void setUp() {
        calculator = new Calculator();
    }

    @After
    public void tearDown() {
        calculator = null;
    }

    @Test
    public void testAdd_PositiveNumbers_ReturnsCorrectSum() {
        // Arrange
        int a = 5;
        int b = 3;
        int expected = 8;

        // Act
        int actual = calculator.add(a, b);

        // Assert
        assertEquals(\"Addition of positive numbers should return correct sum\",
                     expected, actual);
    }

    @Test
    public void testAdd_NegativeNumbers_ReturnsCorrectSum() {
        // Arrange
        int a = -5;
        int b = -3;
        int expected = -8;

        // Act
        int actual = calculator.add(a, b);

        // Assert
        assertEquals(\"Addition of negative numbers should return correct sum\",
                     expected, actual);
    }

    @Test
    public void testDivide_ByZero_ThrowsArithmeticException() {
        // Arrange
        int a = 10;
        int b = 0;

        // Act & Assert
        try {
            calculator.divide(a, b);
            fail(\"Expected ArithmeticException to be thrown\");
        } catch (ArithmeticException e) {
            assertEquals(\"Division by zero should throw ArithmeticException\",
                        \"Cannot divide by zero\", e.getMessage());
        }
    }

    @Test
    public void testDivide_ValidNumbers_ReturnsCorrectQuotient() {
        // Arrange
        int a = 10;
        int b = 2;
        int expected = 5;

        // Act
        int actual = calculator.divide(a, b);

        // Assert
        assertEquals(\"Division should return correct quotient\",
                     expected, actual);
    }
}
\`\`\`"

# JUnit 4.13 Test Class Generation Prompt Template - Ablation 1: Remove persona and keep few shot
PROMPT_ABLATION_ONE="Generate a comprehensive JUnit 4.13 test class for the production class $CLASS_NAME.

## Requirements:

### 1. Target Code to Test
- Production Class: $CLASS_NAME
- Specific lines to cover: $LINE_RANGE
- Source file: $SOURCE_FILE_PATH

### 2. JUnit Version
- Use JUnit 4.13 exclusively
- Import annotations from \`org.junit\` package
- Use \`@Test\`, \`@Before\`, \`@After\`, \`@BeforeClass\`, \`@AfterClass\` as needed

### 3.  Naming Convention
- Test class name format: Test$CLASS_NAME$SUFFIX
- Base name: Test$CLASS_NAME
- If a test class already exists with that name, append a suffix:
  - Numeric suffixes: \`_1\`, \`_2\`, \`_3\`, etc.
  - Alphabetic suffixes: \`_A\`, \`_B\`, \`_C\`, etc.
- Examples:
  - First test class: \`TestUserService\`
  - Second test class: \`TestUserService_1\` or \`TestUserService_A\`
  - Third test class: \`TestUserService_2\` or \`TestUserService_B\`

### 4. Test Coverage
- Cover all public methods in lines $LINE_RANGE
- Include positive test cases (happy path)
- Include negative test cases (error conditions)
- Include edge cases and boundary conditions
- Test null inputs where applicable
- Use descriptive test method names following pattern: \`test<MethodName>_<Scenario>_<ExpectedResult>\`

### 5.  Code Structure
- Include necessary imports
- Set up test fixtures in \`@Before\` method if needed
- Clean up resources in \`@After\` method if needed
- Use meaningful assertions with assertion messages
- Add comments explaining complex test scenarios

### 6. Best Practices
- Follow AAA pattern (Arrange, Act, Assert)
- One assertion concept per test method
- Use appropriate JUnit 4. 13 assertions: \`assertEquals\`, \`assertTrue\`, \`assertFalse\`, \`assertNull\`, \`assertNotNull\`, \`assertThrows\` (via expected exception pattern)
- Mock external dependencies if necessary (mention mock framework if needed)

## Example Output

\`\`\`java
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

/**
 * Test class for Calculator
 * Covers lines 15-45 of Calculator.java
 */
public class TestCalculator {

    private Calculator calculator;

    @Before
    public void setUp() {
        calculator = new Calculator();
    }

    @After
    public void tearDown() {
        calculator = null;
    }

    @Test
    public void testAdd_PositiveNumbers_ReturnsCorrectSum() {
        // Arrange
        int a = 5;
        int b = 3;
        int expected = 8;

        // Act
        int actual = calculator.add(a, b);

        // Assert
        assertEquals(\"Addition of positive numbers should return correct sum\",
                     expected, actual);
    }

    @Test
    public void testAdd_NegativeNumbers_ReturnsCorrectSum() {
        // Arrange
        int a = -5;
        int b = -3;
        int expected = -8;

        // Act
        int actual = calculator.add(a, b);

        // Assert
        assertEquals(\"Addition of negative numbers should return correct sum\",
                     expected, actual);
    }

    @Test
    public void testDivide_ByZero_ThrowsArithmeticException() {
        // Arrange
        int a = 10;
        int b = 0;

        // Act & Assert
        try {
            calculator.divide(a, b);
            fail(\"Expected ArithmeticException to be thrown\");
        } catch (ArithmeticException e) {
            assertEquals(\"Division by zero should throw ArithmeticException\",
                        \"Cannot divide by zero\", e.getMessage());
        }
    }

    @Test
    public void testDivide_ValidNumbers_ReturnsCorrectQuotient() {
        // Arrange
        int a = 10;
        int b = 2;
        int expected = 5;

        // Act
        int actual = calculator.divide(a, b);

        // Assert
        assertEquals(\"Division should return correct quotient\",
                     expected, actual);
    }
}
\`\`\`"

# JUnit 4.13 Test Class Generation Prompt Template - Ablation 2: Keep persona and remove few shot
PROMPT_ABLATION_TWO="Act as an experienced Java developer specializing in test-driven development and quality assurance.

Generate a comprehensive JUnit 4.13 test class for the production class $CLASS_NAME.

## Requirements:

### 1. Target Code to Test
- Production Class: $CLASS_NAME
- Specific lines to cover: $LINE_RANGE
- Source file: $SOURCE_FILE_PATH

### 2. JUnit Version
- Use JUnit 4.13 exclusively
- Import annotations from \`org.junit\` package
- Use \`@Test\`, \`@Before\`, \`@After\`, \`@BeforeClass\`, \`@AfterClass\` as needed

### 3.  Naming Convention
- Test class name format: Test$CLASS_NAME$SUFFIX
- Base name: Test$CLASS_NAME
- If a test class already exists with that name, append a suffix:
  - Numeric suffixes: \`_1\`, \`_2\`, \`_3\`, etc.
  - Alphabetic suffixes: \`_A\`, \`_B\`, \`_C\`, etc.
- Examples:
  - First test class: \`TestUserService\`
  - Second test class: \`TestUserService_1\` or \`TestUserService_A\`
  - Third test class: \`TestUserService_2\` or \`TestUserService_B\`

### 4. Test Coverage
- Cover all public methods in lines $LINE_RANGE
- Include positive test cases (happy path)
- Include negative test cases (error conditions)
- Include edge cases and boundary conditions
- Test null inputs where applicable
- Use descriptive test method names following pattern: \`test<MethodName>_<Scenario>_<ExpectedResult>\`

### 5.  Code Structure
- Include necessary imports
- Set up test fixtures in \`@Before\` method if needed
- Clean up resources in \`@After\` method if needed
- Use meaningful assertions with assertion messages
- Add comments explaining complex test scenarios

### 6. Best Practices
- Follow AAA pattern (Arrange, Act, Assert)
- One assertion concept per test method
- Use appropriate JUnit 4. 13 assertions: \`assertEquals\`, \`assertTrue\`, \`assertFalse\`, \`assertNull\`, \`assertNotNull\`, \`assertThrows\` (via expected exception pattern)
- Mock external dependencies if necessary (mention mock framework if needed)
"

# JUnit 4.13 Test Class Generation Prompt Template - Ablation 3: Remove persona and remove few shot
PROMPT_ABLATION_THREE="Generate a comprehensive JUnit 4.13 test class for the production class $CLASS_NAME.

## Requirements:

### 1. Target Code to Test
- Production Class: $CLASS_NAME
- Specific lines to cover: $LINE_RANGE
- Source file: $SOURCE_FILE_PATH

### 2. JUnit Version
- Use JUnit 4.13 exclusively
- Import annotations from \`org.junit\` package
- Use \`@Test\`, \`@Before\`, \`@After\`, \`@BeforeClass\`, \`@AfterClass\` as needed

### 3.  Naming Convention
- Test class name format: Test$CLASS_NAME$SUFFIX
- Base name: Test$CLASS_NAME
- If a test class already exists with that name, append a suffix:
  - Numeric suffixes: \`_1\`, \`_2\`, \`_3\`, etc.
  - Alphabetic suffixes: \`_A\`, \`_B\`, \`_C\`, etc.
- Examples:
  - First test class: \`TestUserService\`
  - Second test class: \`TestUserService_1\` or \`TestUserService_A\`
  - Third test class: \`TestUserService_2\` or \`TestUserService_B\`

### 4. Test Coverage
- Cover all public methods in lines $LINE_RANGE
- Include positive test cases (happy path)
- Include negative test cases (error conditions)
- Include edge cases and boundary conditions
- Test null inputs where applicable
- Use descriptive test method names following pattern: \`test<MethodName>_<Scenario>_<ExpectedResult>\`

### 5.  Code Structure
- Include necessary imports
- Set up test fixtures in \`@Before\` method if needed
- Clean up resources in \`@After\` method if needed
- Use meaningful assertions with assertion messages
- Add comments explaining complex test scenarios

### 6. Best Practices
- Follow AAA pattern (Arrange, Act, Assert)
- One assertion concept per test method
- Use appropriate JUnit 4. 13 assertions: \`assertEquals\`, \`assertTrue\`, \`assertFalse\`, \`assertNull\`, \`assertNotNull\`, \`assertThrows\` (via expected exception pattern)
- Mock external dependencies if necessary (mention mock framework if needed)
"

if [[ $PROMPT_CHOICE = "no_ablation" ]]; then
  echo Selected prompt with no ablation - Included persona and included few shot:
  echo
  PROMPT_TO_USE="$PROMPT_NO_ABLATION"
elif [[ $PROMPT_CHOICE = "ablation_one" ]]; then
  echo Selected prompt with one ablation - Removed persona and kept few shot:
  echo
  PROMPT_TO_USE="$PROMPT_ABLATION_ONE"
elif [[ $PROMPT_CHOICE = "ablation_two" ]]; then
  echo Selected prompt with one ablation - Kept persona and removed few shot:
  echo
  PROMPT_TO_USE="$PROMPT_ABLATION_TWO"
elif [[ $PROMPT_CHOICE = "ablation_three" ]]; then
  echo Selected prompt with two ablations - Removed persona and removed few shot:
  echo
  PROMPT_TO_USE="$PROMPT_ABLATION_THREE"
else
  echo "Please provide a valid prompt choice: no_ablation, ablation_one, ablation_two, ablation_three"
  exit 1
fi

echo "$PROMPT_TO_USE"
copilot -p "$PROMPT_TO_USE" --allow-all-paths --allow-all-tools
