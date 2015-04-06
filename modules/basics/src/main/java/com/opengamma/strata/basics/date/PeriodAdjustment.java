/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.basics.date;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.collect.ArgChecker;

/**
 * An adjustment that alters a date by adding a period of calendar days, months and years.
 * <p>
 * This adjustment adds a {@link Period} to the input date using an addition convention,
 * followed by an adjustment to ensure the result is a valid business day.
 * <p>
 * Addition is performed using standard calendar addition.
 * It is not possible to add a number of business days using this class.
 * See {@link DaysAdjustment} for an alternative that can handle addition of business days.
 * <p>
 * There are two steps in the calculation:
 * <p>
 * In step one, the period is added using the specified {@link PeriodAdditionConvention}.
 * <p>
 * In step two, the result of step one is optionally adjusted to be a business day
 * using a {@code BusinessDayAdjustment}.
 * <p>
 * For example, a rule represented by this class might be: "the end date is 5 years after
 * the start date, with end-of-month rule based on the last business day of the month,
 * adjusted to be a valid London business day using the 'ModifiedFollowing' convention".
 * 
 * <h4>Usage</h4>
 * {@code PeriodAdjustment} implements {@code TemporalAdjuster} allowing it to directly adjust a date:
 * <pre>
 *  LocalDate adjusted = baseDate.with(periodAdjustment);
 * </pre>
 */
@BeanDefinition
public final class PeriodAdjustment
    implements ImmutableBean, DateAdjuster, Serializable {

  /**
   * An instance that performs no adjustment.
   */
  public static final PeriodAdjustment NONE = new PeriodAdjustment(
      Period.ZERO, PeriodAdditionConventions.NONE, BusinessDayAdjustment.NONE);

  /**
   * The period to be added.
   * <p>
   * When the adjustment is performed, this period will be added to the input date.
   */
  @PropertyDefinition(validate = "notNull")
  private final Period period;
  /**
   * The addition convention to apply.
   * <p>
   * When the adjustment is performed, this convention is used to refine the adjusted date.
   * The most common convention is to move the end date to the last business day of the month
   * if the start date is the last business day of the month.
   */
  @PropertyDefinition(validate = "notNull")
  private final PeriodAdditionConvention additionConvention;
  /**
   * The business day adjustment that is performed to the result of the addition.
   * <p>
   * This adjustment is applied to the result of the addition calculation.
   * <p>
   * If no adjustment is required, use the 'None' business day adjustment.
   */
  @PropertyDefinition(validate = "notNull")
  private final BusinessDayAdjustment adjustment;

  //-------------------------------------------------------------------------
  /**
   * Obtains a period adjustment that can adjust a date by the specified period.
   * <p>
   * When the adjustment is performed, the period will be added to the input date.
   * The business day adjustment will then be used to ensure the result is a valid business day.
   * 
   * @param period  the period to add to the input date
   * @param additionConvention  the convention used to perform the addition
   * @param adjustment  the business day adjustment to apply to the result of the addition
   * @return the period adjustment
   */
  public static PeriodAdjustment of(
      Period period, PeriodAdditionConvention additionConvention, BusinessDayAdjustment adjustment) {
    return new PeriodAdjustment(period, additionConvention, adjustment);
  }

  /**
   * Obtains a period adjustment that can adjust a date by the specified period using the
   * last day of month convention.
   * <p>
   * When the adjustment is performed, the period will be added to the input date.
   * The business day adjustment will then be used to ensure the result is a valid business day.
   * <p>
   * The period must consist only of months and/or years.
   * 
   * @param period  the period to add to the input date
   * @param adjustment  the business day adjustment to apply to the result of the addition
   * @return the period adjustment
   */
  public static PeriodAdjustment ofLastDay(Period period, BusinessDayAdjustment adjustment) {
    return new PeriodAdjustment(period, PeriodAdditionConventions.LAST_DAY, adjustment);
  }

  /**
   * Obtains a period adjustment that can adjust a date by the specified period using the
   * last business day of month convention.
   * <p>
   * When the adjustment is performed, the period will be added to the input date.
   * The business day adjustment will then be used to ensure the result is a valid business day.
   * <p>
   * The period must consist only of months and/or years.
   * 
   * @param period  the period to add to the input date
   * @param adjustment  the business day adjustment to apply to the result of the addition
   * @return the period adjustment
   */
  public static PeriodAdjustment ofLastBusinessDay(Period period, BusinessDayAdjustment adjustment) {
    return new PeriodAdjustment(period, PeriodAdditionConventions.LAST_BUSINESS_DAY, adjustment);
  }

  //-------------------------------------------------------------------------
  @ImmutableValidator
  private void validate() {
    if (additionConvention.isMonthBased() && period.getDays() != 0) {
      throw new IllegalArgumentException("Period must not contain days when addition convention is month-based");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adjusts the date, adding the period and then applying the business day adjustment.
   * <p>
   * The addition is performed by the {@link PeriodAdditionConvention}.
   * 
   * @param date  the date to adjust
   * @return the adjusted temporal
   */
  @Override
  public LocalDate adjust(LocalDate date) {
    ArgChecker.notNull(date, "date");
    LocalDate unadjusted = additionConvention.adjust(date, period, adjustment.getCalendar());
    return adjustment.adjust(unadjusted);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string describing the adjustment.
   * 
   * @return the descriptive string
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append(period);
    if (additionConvention != PeriodAdditionConventions.NONE) {
      buf.append(" with ").append(additionConvention);
    }
    if (adjustment.equals(BusinessDayAdjustment.NONE) == false) {
      buf.append(" then apply ").append(adjustment);
    }
    return buf.toString();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code PeriodAdjustment}.
   * @return the meta-bean, not null
   */
  public static PeriodAdjustment.Meta meta() {
    return PeriodAdjustment.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(PeriodAdjustment.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static PeriodAdjustment.Builder builder() {
    return new PeriodAdjustment.Builder();
  }

  private PeriodAdjustment(
      Period period,
      PeriodAdditionConvention additionConvention,
      BusinessDayAdjustment adjustment) {
    JodaBeanUtils.notNull(period, "period");
    JodaBeanUtils.notNull(additionConvention, "additionConvention");
    JodaBeanUtils.notNull(adjustment, "adjustment");
    this.period = period;
    this.additionConvention = additionConvention;
    this.adjustment = adjustment;
    validate();
  }

  @Override
  public PeriodAdjustment.Meta metaBean() {
    return PeriodAdjustment.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the period to be added.
   * <p>
   * When the adjustment is performed, this period will be added to the input date.
   * @return the value of the property, not null
   */
  public Period getPeriod() {
    return period;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the addition convention to apply.
   * <p>
   * When the adjustment is performed, this convention is used to refine the adjusted date.
   * The most common convention is to move the end date to the last business day of the month
   * if the start date is the last business day of the month.
   * @return the value of the property, not null
   */
  public PeriodAdditionConvention getAdditionConvention() {
    return additionConvention;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the business day adjustment that is performed to the result of the addition.
   * <p>
   * This adjustment is applied to the result of the addition calculation.
   * <p>
   * If no adjustment is required, use the 'None' business day adjustment.
   * @return the value of the property, not null
   */
  public BusinessDayAdjustment getAdjustment() {
    return adjustment;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PeriodAdjustment other = (PeriodAdjustment) obj;
      return JodaBeanUtils.equal(getPeriod(), other.getPeriod()) &&
          JodaBeanUtils.equal(getAdditionConvention(), other.getAdditionConvention()) &&
          JodaBeanUtils.equal(getAdjustment(), other.getAdjustment());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getPeriod());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAdditionConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAdjustment());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PeriodAdjustment}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code period} property.
     */
    private final MetaProperty<Period> period = DirectMetaProperty.ofImmutable(
        this, "period", PeriodAdjustment.class, Period.class);
    /**
     * The meta-property for the {@code additionConvention} property.
     */
    private final MetaProperty<PeriodAdditionConvention> additionConvention = DirectMetaProperty.ofImmutable(
        this, "additionConvention", PeriodAdjustment.class, PeriodAdditionConvention.class);
    /**
     * The meta-property for the {@code adjustment} property.
     */
    private final MetaProperty<BusinessDayAdjustment> adjustment = DirectMetaProperty.ofImmutable(
        this, "adjustment", PeriodAdjustment.class, BusinessDayAdjustment.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "period",
        "additionConvention",
        "adjustment");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -991726143:  // period
          return period;
        case 1652975501:  // additionConvention
          return additionConvention;
        case 1977085293:  // adjustment
          return adjustment;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public PeriodAdjustment.Builder builder() {
      return new PeriodAdjustment.Builder();
    }

    @Override
    public Class<? extends PeriodAdjustment> beanType() {
      return PeriodAdjustment.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code period} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Period> period() {
      return period;
    }

    /**
     * The meta-property for the {@code additionConvention} property.
     * @return the meta-property, not null
     */
    public MetaProperty<PeriodAdditionConvention> additionConvention() {
      return additionConvention;
    }

    /**
     * The meta-property for the {@code adjustment} property.
     * @return the meta-property, not null
     */
    public MetaProperty<BusinessDayAdjustment> adjustment() {
      return adjustment;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -991726143:  // period
          return ((PeriodAdjustment) bean).getPeriod();
        case 1652975501:  // additionConvention
          return ((PeriodAdjustment) bean).getAdditionConvention();
        case 1977085293:  // adjustment
          return ((PeriodAdjustment) bean).getAdjustment();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code PeriodAdjustment}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<PeriodAdjustment> {

    private Period period;
    private PeriodAdditionConvention additionConvention;
    private BusinessDayAdjustment adjustment;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(PeriodAdjustment beanToCopy) {
      this.period = beanToCopy.getPeriod();
      this.additionConvention = beanToCopy.getAdditionConvention();
      this.adjustment = beanToCopy.getAdjustment();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -991726143:  // period
          return period;
        case 1652975501:  // additionConvention
          return additionConvention;
        case 1977085293:  // adjustment
          return adjustment;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -991726143:  // period
          this.period = (Period) newValue;
          break;
        case 1652975501:  // additionConvention
          this.additionConvention = (PeriodAdditionConvention) newValue;
          break;
        case 1977085293:  // adjustment
          this.adjustment = (BusinessDayAdjustment) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public PeriodAdjustment build() {
      return new PeriodAdjustment(
          period,
          additionConvention,
          adjustment);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code period} property in the builder.
     * @param period  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder period(Period period) {
      JodaBeanUtils.notNull(period, "period");
      this.period = period;
      return this;
    }

    /**
     * Sets the {@code additionConvention} property in the builder.
     * @param additionConvention  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder additionConvention(PeriodAdditionConvention additionConvention) {
      JodaBeanUtils.notNull(additionConvention, "additionConvention");
      this.additionConvention = additionConvention;
      return this;
    }

    /**
     * Sets the {@code adjustment} property in the builder.
     * @param adjustment  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder adjustment(BusinessDayAdjustment adjustment) {
      JodaBeanUtils.notNull(adjustment, "adjustment");
      this.adjustment = adjustment;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("PeriodAdjustment.Builder{");
      buf.append("period").append('=').append(JodaBeanUtils.toString(period)).append(',').append(' ');
      buf.append("additionConvention").append('=').append(JodaBeanUtils.toString(additionConvention)).append(',').append(' ');
      buf.append("adjustment").append('=').append(JodaBeanUtils.toString(adjustment));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}