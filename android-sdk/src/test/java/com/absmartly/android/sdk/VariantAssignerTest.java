package com.absmartly.android.sdk;

import static org.junit.Assert.assertEquals;

import com.absmartly.sdk.internal.VariantAssigner;
import com.absmartly.sdk.internal.hashing.Hashing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class VariantAssignerTest extends TestUtils {

    @Test
    public void testChooseVariant() {
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.0, 1.0}, 0.0));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.0, 1.0}, 0.5));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.0, 1.0}, 1.0));

        assertEquals(0, VariantAssigner.chooseVariant(new double[]{1.0, 0.0}, 0.0));
        assertEquals(0, VariantAssigner.chooseVariant(new double[]{1.0, 0.0}, 0.5));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{1.0, 0.0}, 1.0));

        assertEquals(0, VariantAssigner.chooseVariant(new double[]{0.5, 0.5}, 0.0));
        assertEquals(0, VariantAssigner.chooseVariant(new double[]{0.5, 0.5}, 0.25));
        assertEquals(0, VariantAssigner.chooseVariant(new double[]{0.5, 0.5}, 0.49999999));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.5, 0.5}, 0.5));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.5, 0.5}, 0.50000001));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.5, 0.5}, 0.75));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.5, 0.5}, 1.0));

        assertEquals(0, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.0));
        assertEquals(0, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.25));
        assertEquals(0, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.33299999));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.333));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.33300001));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.5));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.66599999));
        assertEquals(2, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.666));
        assertEquals(2, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.66600001));
        assertEquals(2, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 0.75));
        assertEquals(2, VariantAssigner.chooseVariant(new double[]{0.333, 0.333, 0.334}, 1.0));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.0, 1.0}, 0.0));
        assertEquals(1, VariantAssigner.chooseVariant(new double[]{0.0, 1.0}, 1.0));
    }

    @RunWith(Parameterized.class)
    public static class AssignmentsMatchTest extends TestUtils {
        @Parameterized.Parameter(0)
        public Object unitUID;

        @Parameterized.Parameter(1)
        public List<Integer> expectedVariants;

        @Parameterized.Parameters(name = "testAssignmentsMatch_{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {123456789, listOf(1, 0, 1, 1, 1, 0, 0, 2, 1, 2, 2, 2, 0, 0)},
                    {"bleh@absmartly.com", listOf(0, 1, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 1, 1)},
                    {"e791e240fcd3df7d238cfc285f475e8152fcc0ec", listOf(1, 0, 1, 1, 0, 0, 0, 2, 0, 2, 1, 0, 0, 1)}
            });
        }

        @Test
        public void testAssignmentsMatch() {
            List<List<Double>> splits = listOf(
                    listOf(0.5, 0.5),
                    listOf(0.5, 0.5),
                    listOf(0.5, 0.5),
                    listOf(0.5, 0.5),
                    listOf(0.5, 0.5),
                    listOf(0.5, 0.5),
                    listOf(0.5, 0.5),
                    listOf(0.33, 0.33, 0.34),
                    listOf(0.33, 0.33, 0.34),
                    listOf(0.33, 0.33, 0.34),
                    listOf(0.33, 0.33, 0.34),
                    listOf(0.33, 0.33, 0.34),
                    listOf(0.33, 0.33, 0.34),
                    listOf(0.33, 0.33, 0.34));

            List<List<Integer>> seeds = listOf(
                    listOf(0x00000000, 0x00000000),
                    listOf(0x00000000, 0x00000001),
                    listOf(0x8015406f, 0x7ef49b98),
                    listOf(0x3b2e7d90, 0xca87df4d),
                    listOf(0x52c1f657, 0xd248bb2e),
                    listOf(0x865a84d0, 0xaa22d41a),
                    listOf(0x27d1dc86, 0x845461b9),
                    listOf(0x00000000, 0x00000000),
                    listOf(0x00000000, 0x00000001),
                    listOf(0x8015406f, 0x7ef49b98),
                    listOf(0x3b2e7d90, 0xca87df4d),
                    listOf(0x52c1f657, 0xd248bb2e),
                    listOf(0x865a84d0, 0xaa22d41a),
                    listOf(0x27d1dc86, 0x845461b9));

            byte[] unitHash = Hashing.hashUnit(unitUID.toString());
            VariantAssigner assigner = new VariantAssigner(unitHash);

            for (int i = 0; i < seeds.size(); i++) {
                List<Integer> frags = seeds.get(i);
                double[] split = new double[splits.get(i).size()];
                for (int j = 0; j < splits.get(i).size(); j++) {
                    split[j] = splits.get(i).get(j);
                }
                int variant = assigner.assign(split, frags.get(0), frags.get(1));
                assertEquals("Failed for unit=" + unitUID + " at index " + i,
                        expectedVariants.get(i), Integer.valueOf(variant));
            }
        }
    }
}
