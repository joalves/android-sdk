package com.absmartly.android.sdk;

import static org.junit.Assert.assertArrayEquals;

import com.absmartly.sdk.internal.hashing.MD5;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

public class MD5Test {

    @RunWith(Parameterized.class)
    public static class DigestBase64UrlNoPaddingTest extends TestUtils {
        @Parameterized.Parameter(0)
        public String input;

        @Parameterized.Parameter(1)
        public String expected;

        @Parameterized.Parameters(name = "digestBase64UrlNoPadding(\"{0}\") = \"{1}\"")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"", "1B2M2Y8AsgTpgAmY7PhCfg"},
                    {" ", "chXunH2dwinSkhpA6JnsXw"},
                    {"t", "41jvpIn1gGLxDdcxa2Vkng"},
                    {"te", "Vp73JkK-D63XEdakaNaO4Q"},
                    {"tes", "KLZi2IO212_Zbk3cXpungA"},
                    {"test", "CY9rzUYh03PK3k6DJie09g"},
                    {"testy", "K5I_V6RgP8c6sYKz-TVn8g"},
                    {"testy1", "8fT8xGipOhPkZ2DncKU-1A"},
                    {"testy12", "YqRAtOz000gIu61ErEH18A"},
                    {"testy123", "pfV2H07L6WvdqlY0zHuYIw"},
                    {"special characters açb↓c", "4PIrO7lKtTxOcj2eMYlG7A"},
                    {"The quick brown fox jumps over the lazy dog", "nhB9nTcrtoJr2B01QqQZ1g"},
                    {"The quick brown fox jumps over the lazy dog and eats a pie", "iM-8ECRrLUQzixl436y96A"},
                    {"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                            "24m7XOq4f5wPzCqzbBicLA"}
            });
        }

        @Test
        public void testDigestBase64UrlNoPadding() {
            byte[] key = input.getBytes(StandardCharsets.UTF_8);
            byte[] actual = MD5.digestBase64UrlNoPadding(key);
            byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);
            assertArrayEquals("Failed for input=\"" + input + "\"", expectedBytes, actual);
        }

        @Test
        public void testDigestBase64UrlNoPaddingWithOffset() {
            byte[] keyOffset = ("123" + input + "321").getBytes(StandardCharsets.UTF_8);
            byte[] actualOffset = MD5.digestBase64UrlNoPadding(keyOffset, 3, keyOffset.length - 6);
            byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);
            assertArrayEquals("Failed for input=\"" + input + "\" with offset", expectedBytes, actualOffset);
        }
    }

    public static class BasicMD5Test {
        @Test
        public void testDigestEmpty() {
            byte[] key = "".getBytes(StandardCharsets.UTF_8);
            byte[] result = MD5.digestBase64UrlNoPadding(key);
            byte[] expected = "1B2M2Y8AsgTpgAmY7PhCfg".getBytes(StandardCharsets.US_ASCII);
            assertArrayEquals(expected, result);
        }

        @Test
        public void testDigestSimple() {
            byte[] key = "test".getBytes(StandardCharsets.UTF_8);
            byte[] result = MD5.digestBase64UrlNoPadding(key);
            byte[] expected = "CY9rzUYh03PK3k6DJie09g".getBytes(StandardCharsets.US_ASCII);
            assertArrayEquals(expected, result);
        }

        @Test
        public void testDigestUnicode() {
            byte[] key = "special characters açb↓c".getBytes(StandardCharsets.UTF_8);
            byte[] result = MD5.digestBase64UrlNoPadding(key);
            byte[] expected = "4PIrO7lKtTxOcj2eMYlG7A".getBytes(StandardCharsets.US_ASCII);
            assertArrayEquals(expected, result);
        }

        @Test
        public void testDigestLongString() {
            byte[] key = "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8);
            byte[] result = MD5.digestBase64UrlNoPadding(key);
            byte[] expected = "nhB9nTcrtoJr2B01QqQZ1g".getBytes(StandardCharsets.US_ASCII);
            assertArrayEquals(expected, result);
        }

        @Test
        public void testDigestVeryLongString() {
            String input = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
            byte[] key = input.getBytes(StandardCharsets.UTF_8);
            byte[] result = MD5.digestBase64UrlNoPadding(key);
            byte[] expected = "24m7XOq4f5wPzCqzbBicLA".getBytes(StandardCharsets.US_ASCII);
            assertArrayEquals(expected, result);
        }

        @Test
        public void testDigestOutputLength() {
            byte[] key = "test".getBytes(StandardCharsets.UTF_8);
            byte[] result = MD5.digestBase64UrlNoPadding(key);
            assertEquals("MD5 Base64Url output should be 22 bytes", 22, result.length);
        }

        @Test
        public void testDigestDifferentInputs() {
            byte[] key1 = "test1".getBytes(StandardCharsets.UTF_8);
            byte[] key2 = "test2".getBytes(StandardCharsets.UTF_8);

            byte[] result1 = MD5.digestBase64UrlNoPadding(key1);
            byte[] result2 = MD5.digestBase64UrlNoPadding(key2);

            assertFalse("Different inputs should produce different hashes",
                    Arrays.equals(result1, result2));
        }

        private void assertFalse(String message, boolean condition) {
            if (condition) {
                throw new AssertionError(message);
            }
        }

        private void assertEquals(String message, int expected, int actual) {
            if (expected != actual) {
                throw new AssertionError(message + ": expected " + expected + " but was " + actual);
            }
        }
    }
}
