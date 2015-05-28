/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.curve;

import static com.opengamma.strata.basics.date.Tenor.TENOR_1Y;
import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static com.opengamma.strata.collect.TestHelper.coverBeanEquals;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static com.opengamma.strata.collect.TestHelper.date;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LogLinearInterpolator1D;
import com.opengamma.strata.basics.interpolator.CurveExtrapolator;
import com.opengamma.strata.basics.interpolator.CurveInterpolator;
import com.opengamma.strata.basics.value.ValueAdjustment;

/**
 * Test {@link InterpolatedNodalCurve}.
 */
@Test
@SuppressWarnings("deprecation")
public class InterpolatedNodalCurveTest {

  private static final int SIZE = 3;
  private static final String NAME = "TestCurve";
  private static final CurveName CURVE_NAME = CurveName.of(NAME);
  private static final CurveMetadata META_DATA = CurveMetadata.of(CURVE_NAME);
  private static final CurveMetadata META_DATA_ENTRIES = CurveMetadata.of(CURVE_NAME, CurveParameterMetadata.listOfEmpty(SIZE));
  private static final double[] XVALUES = {1d, 2d, 3d};
  private static final double[] XVALUES2 = {0d, 2d, 3d};
  private static final double[] YVALUES = {5d, 7d, 8d};
  private static final double[] YVALUES_BUMPED = {3d, 5d, 6d};
  private static final CurveInterpolator INTERPOLATOR = new LogLinearInterpolator1D();
  private static final CurveExtrapolator FLAT_EXTRAPOLATOR = new FlatExtrapolator1D();
  private static final Interpolator1D COMBINED =
      CombinedInterpolatorExtrapolator.of(INTERPOLATOR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);

  //-------------------------------------------------------------------------
  public void test_of_String() {
    InterpolatedNodalCurve test = InterpolatedNodalCurve.of(NAME, XVALUES, YVALUES, INTERPOLATOR);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE);
    assertThat(test.getExtrapolatorLeft().getName()).isEqualTo(FLAT_EXTRAPOLATOR.getName());
    assertThat(test.getInterpolator().getName()).isEqualTo(INTERPOLATOR.getName());
    assertThat(test.getExtrapolatorRight().getName()).isEqualTo(FLAT_EXTRAPOLATOR.getName());
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(InterpolatedDoublesCurve.from(XVALUES, YVALUES, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(XVALUES);
    assertThat(test.getYValues()).containsExactly(YVALUES);
  }

  public void test_of_CurveName() {
    InterpolatedNodalCurve test = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE);
    assertThat(test.getExtrapolatorLeft().getName()).isEqualTo(FLAT_EXTRAPOLATOR.getName());
    assertThat(test.getInterpolator().getName()).isEqualTo(INTERPOLATOR.getName());
    assertThat(test.getExtrapolatorRight().getName()).isEqualTo(FLAT_EXTRAPOLATOR.getName());
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(InterpolatedDoublesCurve.from(XVALUES, YVALUES, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(XVALUES);
    assertThat(test.getYValues()).containsExactly(YVALUES);
  }

  public void test_of_CurveMetadata() {
    InterpolatedNodalCurve test = InterpolatedNodalCurve.of(META_DATA_ENTRIES, XVALUES, YVALUES, INTERPOLATOR);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE);
    assertThat(test.getExtrapolatorLeft().getName()).isEqualTo(FLAT_EXTRAPOLATOR.getName());
    assertThat(test.getInterpolator().getName()).isEqualTo(INTERPOLATOR.getName());
    assertThat(test.getExtrapolatorRight().getName()).isEqualTo(FLAT_EXTRAPOLATOR.getName());
    assertThat(test.getMetadata()).isEqualTo(META_DATA_ENTRIES);
    assertThat(test.getUnderlyingCurve()).isEqualTo(InterpolatedDoublesCurve.from(XVALUES, YVALUES, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(XVALUES);
    assertThat(test.getYValues()).containsExactly(YVALUES);
  }

  public void test_of_invalid() {
    // not enough nodes
    assertThrowsIllegalArg(() -> InterpolatedNodalCurve.of(CURVE_NAME, new double[] {1d}, new double[] {1d}, INTERPOLATOR));
    // x node size != y node size
    assertThrowsIllegalArg(() -> InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, new double[] {1d, 3d}, INTERPOLATOR));
    // parameter metadata size != node size
    assertThrowsIllegalArg(() -> InterpolatedNodalCurve.of(
        META_DATA_ENTRIES, new double[] {1d, 3d}, new double[] {1d, 3d}, INTERPOLATOR));
  }

  //-------------------------------------------------------------------------
  public void test_lookup() {
    InterpolatedNodalCurve test = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    InterpolatedDoublesCurve underlying = InterpolatedDoublesCurve.from(XVALUES, YVALUES, COMBINED, NAME);
    assertThat(test.yValue(XVALUES[0])).isEqualTo(YVALUES[0]);
    assertThat(test.yValue(XVALUES[1])).isEqualTo(YVALUES[1]);
    assertThat(test.yValue(XVALUES[2])).isEqualTo(YVALUES[2]);
    assertThat(test.yValue(10d)).isEqualTo(underlying.getYValue(10d));

    assertThat(test.yValueParameterSensitivity(10d)).isEqualTo(underlying.getYValueParameterSensitivity(10d));

    assertThat(test.firstDerivative(10d)).isEqualTo(underlying.getDyDx(10d));
  }

  //-------------------------------------------------------------------------
  public void test_withYValues() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    InterpolatedNodalCurve test = base.withYValues(YVALUES_BUMPED);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE);
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(
        InterpolatedDoublesCurve.from(XVALUES, YVALUES_BUMPED, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(XVALUES);
    assertThat(test.getYValues()).containsExactly(YVALUES_BUMPED);
  }

  public void test_withYValues_badSize() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    assertThrowsIllegalArg(() -> base.withYValues(new double[0]));
    assertThrowsIllegalArg(() -> base.withYValues(new double[] {4d, 6d}));
  }

  //-------------------------------------------------------------------------
  public void test_shiftedBy_operator() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    InterpolatedNodalCurve test = base.shiftedBy((x, y) -> y - 2d);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE);
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(
        InterpolatedDoublesCurve.from(XVALUES, YVALUES_BUMPED, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(XVALUES);
    assertThat(test.getYValues()).containsExactly(YVALUES_BUMPED);
  }

  public void test_shiftedBy_adjustment() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    ImmutableList<ValueAdjustment> adjustments = ImmutableList.of(
        ValueAdjustment.ofAbsoluteAmount(3d), ValueAdjustment.ofDeltaAmount(-2d), ValueAdjustment.ofDeltaAmount(-2d));
    InterpolatedNodalCurve test = base.shiftedBy(adjustments);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE);
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(
        InterpolatedDoublesCurve.from(XVALUES, YVALUES_BUMPED, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(XVALUES);
    assertThat(test.getYValues()).containsExactly(YVALUES_BUMPED);
  }

  public void test_shiftedBy_adjustment_longList() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    ImmutableList<ValueAdjustment> adjustments = ImmutableList.of(
        ValueAdjustment.ofAbsoluteAmount(3d),
        ValueAdjustment.ofDeltaAmount(-2d),
        ValueAdjustment.ofDeltaAmount(-2d),
        ValueAdjustment.ofDeltaAmount(2d));
    InterpolatedNodalCurve test = base.shiftedBy(adjustments);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE);
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(
        InterpolatedDoublesCurve.from(XVALUES, YVALUES_BUMPED, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(XVALUES);
    assertThat(test.getYValues()).containsExactly(YVALUES_BUMPED);
  }

  public void test_shiftedBy_adjustment_shortList() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    ImmutableList<ValueAdjustment> adjustments = ImmutableList.of(
        ValueAdjustment.ofAbsoluteAmount(3d));
    double[] bumped = new double[] {3d, 7d, 8d};
    InterpolatedNodalCurve test = base.shiftedBy(adjustments);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE);
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(
        InterpolatedDoublesCurve.from(XVALUES, bumped, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(XVALUES);
    assertThat(test.getYValues()).containsExactly(bumped);
  }

  //-------------------------------------------------------------------------
  public void test_withNode_atStart_noMetadata() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(META_DATA_ENTRIES, XVALUES, YVALUES, INTERPOLATOR);
    InterpolatedNodalCurve test = base.withNode(0, 0.5d, 4d);
    double[] x = {0.5d, 1d, 2d, 3d};
    double[] y = {4d, 5d, 7d, 8d};
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE + 1);
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(InterpolatedDoublesCurve.from(x, y, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(x);
    assertThat(test.getYValues()).containsExactly(y);
  }

  public void test_withNode_atEnd_noMetadata() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(META_DATA_ENTRIES, XVALUES, YVALUES, INTERPOLATOR);
    InterpolatedNodalCurve test = base.withNode(SIZE, 4d, 9d);
    double[] x = {1d, 2d, 3d, 4d};
    double[] y = {5d, 7d, 8d, 9d};
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE + 1);
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(InterpolatedDoublesCurve.from(x, y, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(x);
    assertThat(test.getYValues()).containsExactly(y);
  }

  public void test_withNode_atStart_metadata() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(META_DATA_ENTRIES, XVALUES, YVALUES, INTERPOLATOR);
    TenorCurveNodeMetadata item = TenorCurveNodeMetadata.of(date(2015, 6, 30), TENOR_1Y);
    InterpolatedNodalCurve test = base.withNode(0, item, 0.5d, 4d);
    double[] x = {0.5d, 1d, 2d, 3d};
    double[] y = {4d, 5d, 7d, 8d};
    List<CurveParameterMetadata> list = new ArrayList<>();
    list.add(item);
    list.addAll(CurveParameterMetadata.listOfEmpty(SIZE));
    CurveMetadata metadata = CurveMetadata.of(CURVE_NAME, list);
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE + 1);
    assertThat(test.getMetadata()).isEqualTo(metadata);
    assertThat(test.getUnderlyingCurve()).isEqualTo(InterpolatedDoublesCurve.from(x, y, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(x);
    assertThat(test.getYValues()).containsExactly(y);
  }

  public void test_withNode_atEnd_metadata_onCurveWithoutMetadata() {
    InterpolatedNodalCurve base = InterpolatedNodalCurve.of(META_DATA, XVALUES, YVALUES, INTERPOLATOR);
    TenorCurveNodeMetadata item = TenorCurveNodeMetadata.of(date(2015, 6, 30), TENOR_1Y);
    InterpolatedNodalCurve test = base.withNode(0, item, 0.5d, 4d);
    double[] x = {0.5d, 1d, 2d, 3d};
    double[] y = {4d, 5d, 7d, 8d};
    assertThat(test.getName()).isEqualTo(CURVE_NAME);
    assertThat(test.getParameterCount()).isEqualTo(SIZE + 1);
    assertThat(test.getMetadata()).isEqualTo(META_DATA);
    assertThat(test.getUnderlyingCurve()).isEqualTo(InterpolatedDoublesCurve.from(x, y, COMBINED, NAME));
    assertThat(test.getXValues()).containsExactly(x);
    assertThat(test.getYValues()).containsExactly(y);
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    InterpolatedNodalCurve test = InterpolatedNodalCurve.of(CURVE_NAME, XVALUES, YVALUES, INTERPOLATOR);
    coverImmutableBean(test);
    InterpolatedNodalCurve test2 = InterpolatedNodalCurve.builder()
        .metadata(CurveMetadata.of("Coverage"))
        .xValues(XVALUES2)
        .yValues(YVALUES_BUMPED)
        .extrapolatorLeft(Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR_INSTANCE)
        .interpolator(Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE)
        .extrapolatorRight(Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR_INSTANCE)
        .build();
    coverBeanEquals(test, test2);
  }

}