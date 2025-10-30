package com.absmartly.android.sdk;

import static org.junit.Assert.assertEquals;

import com.absmartly.sdk.internal.hashing.Hashing;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class HashingTest {

    @Test
    public void testHashUnit() {
        assertEquals("H2jvj6o9YcAgNdhKqEbtWw",
                new String(Hashing.hashUnit("4a42766ca6313d26f49985e799ff4f3790fb86efa0fce46edb3ea8fbf1ea3408"),
                        StandardCharsets.US_ASCII));
        assertEquals("DRgslOje35bZMmpaohQjkA",
                new String(Hashing.hashUnit("bleh@absmarty.com"), StandardCharsets.US_ASCII));
        assertEquals("LxcqH5VC15rXfWfA_smreg",
                new String(Hashing.hashUnit("açb↓c"), StandardCharsets.US_ASCII));
        assertEquals("K5I_V6RgP8c6sYKz-TVn8g",
                new String(Hashing.hashUnit("testy"), StandardCharsets.US_ASCII));
        assertEquals("K4uy4bTeCy34W97lmceVRg",
                new String(Hashing.hashUnit(Long.toString(123456778999L)), StandardCharsets.US_ASCII));
    }

    @Test
    public void testHashUnitLarge() {
        String chars = "4a42766ca6313d26f49985e799ff4f3790fb86efa0fce46edb3ea8fbf1ea3408";
        StringBuilder sb = new StringBuilder();

        int count = (2048 + chars.length() - 1) / chars.length();
        for (int i = 0; i < count; i++) {
            sb.append(chars);
        }

        assertEquals("Rxnq-eM9eE1SEoMnkEMOIw",
                new String(Hashing.hashUnit(sb.toString()), StandardCharsets.US_ASCII));
    }

    @Test
    public void testHashUnit_Empty() {
        String result = new String(Hashing.hashUnit(""), StandardCharsets.US_ASCII);
        assertEquals(22, result.length());
    }

    @Test
    public void testHashUnit_Numeric() {
        assertEquals("JfnnlDI7RTiF9RgfG2JNCw",
                new String(Hashing.hashUnit("123456789"), StandardCharsets.US_ASCII));
    }

    @Test
    public void testHashUnit_LongHash() {
        String longInput = "e791e240fcd3df7d238cfc285f475e8152fcc0ec";
        String result = new String(Hashing.hashUnit(longInput), StandardCharsets.US_ASCII);
        assertEquals("pAE3a1i5Drs5mKRNq56adA", result);
    }

    @Test
    public void testHashUnit_Email() {
        String result = new String(Hashing.hashUnit("bleh@absmartly.com"), StandardCharsets.US_ASCII);
        assertEquals("IuqYkNRfEx5yClel4j3NbA", result);
    }

    @Test
    public void testHashUnit_SpecialChars() {
        String result = new String(Hashing.hashUnit("special!@#$%"), StandardCharsets.US_ASCII);
        assertEquals(22, result.length());
    }

    @Test
    public void testHashUnit_Unicode() {
        String result = new String(Hashing.hashUnit("Hello 世界"), StandardCharsets.US_ASCII);
        assertEquals(22, result.length());
    }
}
