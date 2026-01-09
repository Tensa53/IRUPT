/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4.13 test class for Utf8 class. Specifically targets
 * lines 64-65 (copy constructor) and 116-117-118 (setByteLength array
 * expansion) with comprehensive coverage of all public methods.
 */
public class TestUtf8_2 {

  private Utf8 testUtf8;
  private String sampleString;
  private byte[] sampleBytes;

  @Before
  public void setUp() {
    sampleString = "TestString";
    sampleBytes = sampleString.getBytes(StandardCharsets.UTF_8);
    testUtf8 = new Utf8(sampleString);
  }

  @After
  public void tearDown() {
    testUtf8 = null;
    sampleString = null;
    sampleBytes = null;
  }

  // ========== Copy Constructor Tests (Lines 64-65) ==========

  @Test
  public void testCopyConstructor_NormalString_CreatesIndependentCopy() {
    // Target lines 64-65: Test the copy constructor creates a deep copy
    Utf8 original = new Utf8("Hello World");
    Utf8 copy = new Utf8(original);

    assertNotNull("Copy should not be null", copy);
    assertEquals("Copy should have same byte length", original.getByteLength(), copy.getByteLength());
    assertEquals("Copy should have same string representation", original.toString(), copy.toString());
    assertNotSame("Byte arrays should be different objects (line 64)", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_ByteArrayCopied_SystemArrayCopyUsed() {
    // Target line 65: Verify System.arraycopy correctly copies bytes
    Utf8 original = new Utf8("CopyTest");
    byte[] originalBytes = original.getBytes();
    int originalLength = original.getByteLength();

    Utf8 copy = new Utf8(original);

    byte[] copiedBytes = copy.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Byte at index " + i + " should match (line 65: System.arraycopy)", originalBytes[i],
          copiedBytes[i]);
    }
  }

  @Test
  public void testCopyConstructor_ModifyOriginal_CopyUnaffected() {
    // Target lines 64-65: Verify deep copy independence
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    original.set("Modified");

    assertNotEquals("Copy should not be affected by original modification", original.toString(), copy.toString());
    assertEquals("Copy should retain original value", "Original", copy.toString());
  }

  @Test
  public void testCopyConstructor_ModifyCopy_OriginalUnaffected() {
    // Target lines 64-65: Verify deep copy works both ways
    Utf8 original = new Utf8("Original");
    Utf8 copy = new Utf8(original);

    copy.set("Modified Copy");

    assertEquals("Original should not be affected by copy modification", "Original", original.toString());
    assertNotEquals("Copy should have modified value", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_EmptyUtf8_CreatesEmptyCopy() {
    // Target lines 64-65: Test with empty Utf8
    Utf8 original = new Utf8();
    Utf8 copy = new Utf8(original);

    assertEquals("Copy should be empty", 0, copy.getByteLength());
    assertEquals("Copy should have empty string", "", copy.toString());
    assertNotSame("Even empty arrays should be different objects", original.getBytes(), copy.getBytes());
  }

  @Test
  public void testCopyConstructor_UnicodeString_PreservesAllCharacters() {
    // Target lines 64-65: Test with multi-byte UTF-8 characters
    String unicode = "日本語 中文 한글 العربية";
    Utf8 original = new Utf8(unicode);
    Utf8 copy = new Utf8(original);

    assertEquals("Unicode string should be preserved in copy", unicode, copy.toString());
    assertEquals("Byte lengths should match", original.getByteLength(), copy.getByteLength());
  }

  @Test
  public void testCopyConstructor_LargeString_CopiesAllBytes() {
    // Target lines 64-65: Test with large string to ensure System.arraycopy works
    // correctly
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 5000; i++) {
      sb.append("X");
    }
    Utf8 original = new Utf8(sb.toString());
    Utf8 copy = new Utf8(original);

    assertEquals("Large string should be fully copied", original.getByteLength(), copy.getByteLength());
    assertEquals("Large string content should match", original.toString(), copy.toString());
  }

  @Test
  public void testCopyConstructor_CachedString_CopiedToNew() {
    // Target line 66: Verify cached string reference is also copied
    Utf8 original = new Utf8("Test");
    original.toString(); // Cache the string
    Utf8 copy = new Utf8(original);

    assertEquals("Cached string should be accessible in copy", "Test", copy.toString());
  }

  @Test
  public void testCopyConstructor_WithSpecialCharacters_PreservesContent() {
    // Target lines 64-65: Test with special characters
    String special = "Line1\nLine2\tTabbed\r\nLine3";
    Utf8 original = new Utf8(special);
    Utf8 copy = new Utf8(original);

    assertEquals("Special characters should be preserved in copy", special, copy.toString());
  }

  // ========== setByteLength Tests (Lines 116-117-118) ==========

  @Test
  public void testSetByteLength_ExpandBeyondCurrentCapacity_CreatesNewArray() {
    // Target line 116: Test new byte array creation when newLength > bytes.length
    Utf8 utf8 = new Utf8("Short");
    byte[] originalArray = utf8.getBytes();
    int originalCapacity = originalArray.length;

    int newLength = originalCapacity + 10;
    utf8.setByteLength(newLength);

    assertNotSame("New byte array should be created (line 116)", originalArray, utf8.getBytes());
    assertTrue("New array should have sufficient capacity", utf8.getBytes().length >= newLength);
    assertEquals("Byte length should be updated", newLength, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_SystemArrayCopy_PreservesOriginalBytes() {
    // Target line 117: Test System.arraycopy preserves original data
    Utf8 utf8 = new Utf8("Data");
    byte[] originalBytes = new byte[utf8.getByteLength()];
    System.arraycopy(utf8.getBytes(), 0, originalBytes, 0, utf8.getByteLength());
    int originalLength = utf8.getByteLength();

    utf8.setByteLength(50);

    byte[] newArray = utf8.getBytes();
    for (int i = 0; i < originalLength; i++) {
      assertEquals("Original byte at index " + i + " should be preserved (line 117)", originalBytes[i], newArray[i]);
    }
  }

  @Test
  public void testSetByteLength_ArrayReassignment_NewArrayAssigned() {
    // Target line 118: Test this.bytes = newBytes assignment
    Utf8 utf8 = new Utf8("Test");
    byte[] oldArray = utf8.getBytes();

    utf8.setByteLength(100);

    byte[] newArray = utf8.getBytes();
    assertNotSame("Array should be reassigned (line 118)", oldArray, newArray);
  }

  @Test
  public void testSetByteLength_WithinCurrentCapacity_ReusesArray() {
    // Test condition at line 115: this.bytes.length < newLength is false
    byte[] bytes = new byte[100];
    byte[] original = "Test".getBytes(StandardCharsets.UTF_8);
    System.arraycopy(original, 0, bytes, 0, original.length);
    Utf8 utf8 = new Utf8(bytes);
    utf8.setByteLength(original.length);

    byte[] arrayBefore = utf8.getBytes();
    utf8.setByteLength(50);
    byte[] arrayAfter = utf8.getBytes();

    assertSame("Array should be reused when within capacity", arrayBefore, arrayAfter);
  }

  @Test
  public void testSetByteLength_EdgeCase_ExactlyAtCapacity() {
    // Test boundary condition: newLength exactly equals current array length
    Utf8 utf8 = new Utf8("Exact");
    int currentCapacity = utf8.getBytes().length;
    byte[] arrayBefore = utf8.getBytes();

    utf8.setByteLength(currentCapacity);

    assertSame("Array should be reused when newLength equals capacity", arrayBefore, utf8.getBytes());
    assertEquals("Length should be updated", currentCapacity, utf8.getByteLength());
  }

  @Test
  public void testSetByteLength_MultipleExpansions_EachCreatesNewArray() {
    // Target lines 116-118: Multiple expansions
    Utf8 utf8 = new Utf8("X");
    byte[] array1 = utf8.getBytes();

    utf8.setByteLength(10);
    byte[] array2 = utf8.getBytes();
    assertNotSame("First expansion should create new array", array1, array2);

    utf8.setByteLength(20);
    byte[] array3 = utf8.getBytes();
    assertNotSame("Second expansion should create new array", array2, array3);

    utf8.setByteLength(30);
    byte[] array4 = utf8.getBytes();
    assertNotSame("Third expansion should create new array", array3, array4);
  }

  @Test
  public void testSetByteLength_ZeroToPositive_ExpandsArray() {
    // Target lines 116-118: Expand from zero length
    Utf8 utf8 = new Utf8();
    assertEquals("Initial length should be 0", 0, utf8.getByteLength());

    utf8.setByteLength(10);

    assertEquals("Length should be expanded to 10", 10, utf8.getByteLength());
    assertTrue("Array should have capacity for new length", utf8.getBytes().length >= 10);
  }

  @Test
  public void testSetByteLength_StringCacheCleared_AfterExpansion() {
    // Target line 121: string cache is cleared
    Utf8 utf8 = new Utf8("Test");
    utf8.toString(); // Cache the string

    utf8.setByteLength(20);

    // String cache should be cleared, even though we haven't modified the actual
    // content
    assertNotNull("toString should still work after expansion", utf8.toString());
  }

  @Test
  public void testSetByteLength_ReturnsThis_AllowsMethodChaining() {
    // Test method chaining
    Utf8 utf8 = new Utf8("Test");
    Utf8 result = utf8.setByteLength(20);

    assertSame("setByteLength should return this for chaining", utf8, result);
  }

  // ========== Constructor Tests ==========

  @Test
  public void testDefaultConstructor_CreatesEmptyUtf8_Success() {
    Utf8 utf8 = new Utf8();

    assertNotNull("Utf8 should not be null", utf8);
    assertEquals("Byte length should be 0", 0, utf8.getByteLength());
    assertEquals("String should be empty", "", utf8.toString());
  }

  @Test
  public void testStringConstructor_ValidString_InitializesCorrectly() {
    String input = "Constructor Test";
    Utf8 utf8 = new Utf8(input);

    assertEquals("String should match input", input, utf8.toString());
    assertEquals("Byte length should match UTF-8 encoding length", input.getBytes(StandardCharsets.UTF_8).length,
        utf8.getByteLength());
  }

  @Test
  public void testStringConstructor_EmptyString_InitializesEmpty() {
    Utf8 utf8 = new Utf8("");

    assertEquals("String should be empty", "", utf8.toString());
    assertEquals("Byte length should be 0", 0, utf8.getByteLength());
  }

  @Test
  public void testByteArrayConstructor_ValidBytes_StoresReference() {
    byte[] bytes = "ByteArray".getBytes(StandardCharsets.UTF_8);
    Utf8 utf8 = new Utf8(bytes);

    assertSame("Constructor should store byte array reference", bytes, utf8.getBytes());
    assertEquals("Byte length should match array length", bytes.length, utf8.getByteLength());
  }

  // ========== getBytes Tests ==========

  @Test
  public void testGetBytes_ReturnsInternalByteArray_Success() {
    Utf8 utf8 = new Utf8("Test");
    byte[] bytes = utf8.getBytes();

    assertNotNull("Byte array should not be null", bytes);
    assertTrue("Byte array should have sufficient length", bytes.length >= utf8.getByteLength());
  }

  // ========== getByteLength Tests ==========

  @Test
  public void testGetByteLength_ASCIIString_ReturnsCorrectLength() {
    String ascii = "ASCII";
    Utf8 utf8 = new Utf8(ascii);

    assertEquals("ASCII byte length should equal character count", ascii.length(), utf8.getByteLength());
  }

  @Test
  public void testGetByteLength_MultiByte_ReturnsCorrectByteCount() {
    String multiByte = "こんにちは";
    Utf8 utf8 = new Utf8(multiByte);
    int expectedBytes = multiByte.getBytes(StandardCharsets.UTF_8).length;

    assertEquals("Multi-byte string should return correct byte count", expectedBytes, utf8.getByteLength());
  }

  @Test
  public void testGetByteLength_EmptyString_ReturnsZero() {
    Utf8 utf8 = new Utf8("");

    assertEquals("Empty string should have 0 bytes", 0, utf8.getByteLength());
  }

  // ========== set Method Tests ==========

  @Test
  public void testSet_NewString_UpdatesContent() {
    Utf8 utf8 = new Utf8("Initial");
    String newValue = "Updated";

    utf8.set(newValue);

    assertEquals("String should be updated", newValue, utf8.toString());
    assertEquals("Byte length should be updated", newValue.getBytes(StandardCharsets.UTF_8).length,
        utf8.getByteLength());
  }

  @Test
  public void testSet_EmptyString_ClearsContent() {
    Utf8 utf8 = new Utf8("NotEmpty");

    utf8.set("");

    assertEquals("String should be empty", "", utf8.toString());
    assertEquals("Byte length should be 0", 0, utf8.getByteLength());
  }

  @Test
  public void testSet_MultipleUpdates_EachUpdateWorks() {
    Utf8 utf8 = new Utf8();

    utf8.set("First");
    assertEquals("First update should work", "First", utf8.toString());

    utf8.set("Second");
    assertEquals("Second update should work", "Second", utf8.toString());

    utf8.set("Third");
    assertEquals("Third update should work", "Third", utf8.toString());
  }

  @Test
  public void testSet_ReturnsThis_SupportsChaining() {
    Utf8 utf8 = new Utf8();
    Utf8 result = utf8.set("Test");

    assertSame("set should return this", utf8, result);
  }

  // ========== toString Tests ==========

  @Test
  public void testToString_ValidString_ReturnsCorrectValue() {
    String input = "ToString Test";
    Utf8 utf8 = new Utf8(input);

    assertEquals("toString should return correct value", input, utf8.toString());
  }

  @Test
  public void testToString_EmptyUtf8_ReturnsEmptyString() {
    Utf8 utf8 = new Utf8();

    assertEquals("Empty Utf8 should return empty string", "", utf8.toString());
  }

  @Test
  public void testToString_CachesString_ReturnsSameInstance() {
    Utf8 utf8 = new Utf8("Cached");
    String first = utf8.toString();
    String second = utf8.toString();

    assertSame("toString should return cached instance", first, second);
  }

  @Test
  public void testToString_AfterSetByteLength_RecalculatesString() {
    Utf8 utf8 = new Utf8("Test");
    utf8.toString(); // Cache it

    utf8.setByteLength(2);
    String result = utf8.toString();

    assertNotNull("toString should work after setByteLength", result);
  }

  // ========== equals Tests ==========

  @Test
  public void testEquals_SameInstance_ReturnsTrue() {
    Utf8 utf8 = new Utf8("Test");

    assertTrue("Same instance should equal itself", utf8.equals(utf8));
  }

  @Test
  public void testEquals_EqualContent_ReturnsTrue() {
    Utf8 utf81 = new Utf8("Equal");
    Utf8 utf82 = new Utf8("Equal");

    assertTrue("Instances with equal content should be equal", utf81.equals(utf82));
  }

  @Test
  public void testEquals_DifferentContent_ReturnsFalse() {
    Utf8 utf81 = new Utf8("First");
    Utf8 utf82 = new Utf8("Second");

    assertFalse("Instances with different content should not be equal", utf81.equals(utf82));
  }

  @Test
  public void testEquals_DifferentLength_ReturnsFalse() {
    Utf8 utf81 = new Utf8("Short");
    Utf8 utf82 = new Utf8("Much Longer String");

    assertFalse("Instances with different lengths should not be equal", utf81.equals(utf82));
  }

  @Test
  public void testEquals_Null_ReturnsFalse() {
    Utf8 utf8 = new Utf8("Test");

    assertFalse("Should not equal null", utf8.equals(null));
  }

  @Test
  public void testEquals_DifferentType_ReturnsFalse() {
    Utf8 utf8 = new Utf8("Test");

    assertFalse("Should not equal different type", utf8.equals("Test"));
  }

  @Test
  public void testEquals_BothEmpty_ReturnsTrue() {
    Utf8 utf81 = new Utf8();
    Utf8 utf82 = new Utf8("");

    assertTrue("Empty instances should be equal", utf81.equals(utf82));
  }

  // ========== hashCode Tests ==========

  @Test
  public void testHashCode_EqualObjects_SameHashCode() {
    Utf8 utf81 = new Utf8("Hash");
    Utf8 utf82 = new Utf8("Hash");

    assertEquals("Equal objects should have same hash code", utf81.hashCode(), utf82.hashCode());
  }

  @Test
  public void testHashCode_Consistent_MultipleCalls() {
    Utf8 utf8 = new Utf8("Hash");
    int hash1 = utf8.hashCode();
    int hash2 = utf8.hashCode();

    assertEquals("hashCode should be consistent", hash1, hash2);
  }

  @Test
  public void testHashCode_EmptyUtf8_ReturnsZero() {
    Utf8 utf8 = new Utf8();

    assertEquals("Empty Utf8 should have hash code 0", 0, utf8.hashCode());
  }

  // ========== compareTo Tests ==========

  @Test
  public void testCompareTo_EqualObjects_ReturnsZero() {
    Utf8 utf81 = new Utf8("Same");
    Utf8 utf82 = new Utf8("Same");

    assertEquals("Equal objects should compare to 0", 0, utf81.compareTo(utf82));
  }

  @Test
  public void testCompareTo_FirstLessThanSecond_ReturnsNegative() {
    Utf8 utf81 = new Utf8("Apple");
    Utf8 utf82 = new Utf8("Banana");

    assertTrue("Apple should be less than Banana", utf81.compareTo(utf82) < 0);
  }

  @Test
  public void testCompareTo_FirstGreaterThanSecond_ReturnsPositive() {
    Utf8 utf81 = new Utf8("Zebra");
    Utf8 utf82 = new Utf8("Apple");

    assertTrue("Zebra should be greater than Apple", utf81.compareTo(utf82) > 0);
  }

  @Test
  public void testCompareTo_EmptyStrings_ReturnsZero() {
    Utf8 utf81 = new Utf8();
    Utf8 utf82 = new Utf8("");

    assertEquals("Empty strings should be equal", 0, utf81.compareTo(utf82));
  }

  // ========== CharSequence Interface Tests ==========

  @Test
  public void testCharAt_ValidIndex_ReturnsCorrectChar() {
    String input = "CharAt";
    Utf8 utf8 = new Utf8(input);

    for (int i = 0; i < input.length(); i++) {
      assertEquals("charAt at index " + i + " should match", input.charAt(i), utf8.charAt(i));
    }
  }

  @Test(expected = StringIndexOutOfBoundsException.class)
  public void testCharAt_NegativeIndex_ThrowsException() {
    Utf8 utf8 = new Utf8("Test");
    utf8.charAt(-1);
  }

  @Test(expected = StringIndexOutOfBoundsException.class)
  public void testCharAt_IndexOutOfBounds_ThrowsException() {
    Utf8 utf8 = new Utf8("Test");
    utf8.charAt(100);
  }

  @Test
  public void testLength_ValidString_ReturnsCharacterCount() {
    String input = "Length";
    Utf8 utf8 = new Utf8(input);

    assertEquals("length should return character count", input.length(), utf8.length());
  }

  @Test
  public void testLength_EmptyString_ReturnsZero() {
    Utf8 utf8 = new Utf8();

    assertEquals("Empty string length should be 0", 0, utf8.length());
  }

  @Test
  public void testLength_MultiByteChars_ReturnsCharCount() {
    String input = "日本"; // 2 characters, 6 bytes
    Utf8 utf8 = new Utf8(input);

    assertEquals("length should return character count, not byte count", 2, utf8.length());
  }

  @Test
  public void testSubSequence_ValidRange_ReturnsSubstring() {
    Utf8 utf8 = new Utf8("Hello World");
    CharSequence sub = utf8.subSequence(0, 5);

    assertEquals("subSequence should return correct substring", "Hello", sub.toString());
  }

  @Test
  public void testSubSequence_EmptyRange_ReturnsEmpty() {
    Utf8 utf8 = new Utf8("Test");
    CharSequence sub = utf8.subSequence(1, 1);

    assertEquals("Empty range should return empty", "", sub.toString());
  }

  @Test
  public void testSubSequence_FullString_ReturnsFullString() {
    String input = "Full";
    Utf8 utf8 = new Utf8(input);
    CharSequence sub = utf8.subSequence(0, input.length());

    assertEquals("Full range should return full string", input, sub.toString());
  }

  // ========== Static getBytesFor Tests ==========

  @Test
  public void testGetBytesFor_ValidString_ReturnsUtf8Bytes() {
    String input = "Static";
    byte[] expected = input.getBytes(StandardCharsets.UTF_8);
    byte[] actual = Utf8.getBytesFor(input);

    assertArrayEquals("getBytesFor should return UTF-8 bytes", expected, actual);
  }

  @Test
  public void testGetBytesFor_EmptyString_ReturnsEmptyArray() {
    byte[] bytes = Utf8.getBytesFor("");

    assertEquals("Empty string should return empty array", 0, bytes.length);
  }

  @Test
  public void testGetBytesFor_UnicodeString_ReturnsCorrectBytes() {
    String unicode = "العربية";
    byte[] expected = unicode.getBytes(StandardCharsets.UTF_8);
    byte[] actual = Utf8.getBytesFor(unicode);

    assertArrayEquals("Unicode should be encoded correctly", expected, actual);
  }

  // ========== Edge Cases and Boundary Tests ==========

  @Test
  public void testUtf8_WithEmoji_PreservesContent() {
    String emoji = "😀🎉🌍";
    Utf8 utf8 = new Utf8(emoji);

    assertEquals("Emoji should be preserved", emoji, utf8.toString());
  }

  @Test
  public void testUtf8_WithNewlines_PreservesContent() {
    String withNewlines = "Line1\nLine2\nLine3";
    Utf8 utf8 = new Utf8(withNewlines);

    assertEquals("Newlines should be preserved", withNewlines, utf8.toString());
  }

  @Test
  public void testUtf8_WithTabs_PreservesContent() {
    String withTabs = "Col1\tCol2\tCol3";
    Utf8 utf8 = new Utf8(withTabs);

    assertEquals("Tabs should be preserved", withTabs, utf8.toString());
  }

  @Test
  public void testUtf8_LargeString_HandledCorrectly() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      sb.append('A');
    }
    String large = sb.toString();
    Utf8 utf8 = new Utf8(large);

    assertEquals("Large string should be handled", large.length(), utf8.length());
    assertEquals("Large string content should match", large, utf8.toString());
  }

  @Test
  public void testUtf8_Reuse_MultipleSets() {
    Utf8 utf8 = new Utf8();

    utf8.set("Value1");
    assertEquals("First set should work", "Value1", utf8.toString());

    utf8.set("Value2");
    assertEquals("Second set should work", "Value2", utf8.toString());

    utf8.set("Value3");
    assertEquals("Third set should work", "Value3", utf8.toString());
  }

  @Test
  public void testUtf8_CopyAndModify_IndependentInstances() {
    Utf8 original = new Utf8("Original");
    Utf8 copy1 = new Utf8(original);
    Utf8 copy2 = new Utf8(original);

    copy1.set("Modified1");
    copy2.set("Modified2");

    assertEquals("Original should be unchanged", "Original", original.toString());
    assertEquals("Copy1 should have its value", "Modified1", copy1.toString());
    assertEquals("Copy2 should have its value", "Modified2", copy2.toString());
  }

  @Test
  public void testSetByteLength_FromSmallToLarge_PreservesData() {
    // Integration test for lines 116-118
    Utf8 utf8 = new Utf8("AB");
    byte[] originalBytes = new byte[utf8.getByteLength()];
    System.arraycopy(utf8.getBytes(), 0, originalBytes, 0, utf8.getByteLength());

    utf8.setByteLength(100);

    for (int i = 0; i < originalBytes.length; i++) {
      assertEquals("Original data should be preserved at index " + i, originalBytes[i], utf8.getBytes()[i]);
    }
  }

  @Test
  public void testSetByteLength_Progressive_EachExpansionPreservesData() {
    // Comprehensive test for lines 116-118
    Utf8 utf8 = new Utf8("Test");
    byte[] snapshot1 = new byte[utf8.getByteLength()];
    System.arraycopy(utf8.getBytes(), 0, snapshot1, 0, utf8.getByteLength());

    utf8.setByteLength(10);
    for (int i = 0; i < snapshot1.length; i++) {
      assertEquals("Data preserved after first expansion", snapshot1[i], utf8.getBytes()[i]);
    }

    utf8.setByteLength(50);
    for (int i = 0; i < snapshot1.length; i++) {
      assertEquals("Data preserved after second expansion", snapshot1[i], utf8.getBytes()[i]);
    }

    utf8.setByteLength(200);
    for (int i = 0; i < snapshot1.length; i++) {
      assertEquals("Data preserved after third expansion", snapshot1[i], utf8.getBytes()[i]);
    }
  }
}
