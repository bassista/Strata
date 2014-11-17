/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.platform.finance.swap;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableList;
import com.opengamma.basics.PayReceive;
import com.opengamma.basics.currency.Currency;
import com.opengamma.basics.schedule.PeriodicSchedule;
import com.opengamma.basics.schedule.Schedule;

/**
 * An interest rate swap leg.
 * <p>
 * This defines a single swap leg paying an interest rate.
 * The rate may be fixed or floating, see {@link FixedRateCalculation},
 * {@link IborRateCalculation} and {@link OvernightRateCalculation}.
 * <p>
 * Interest is calculated based on <i>accrual periods</i> which follow a regular schedule
 * with optional initial and final stubs.
 * Coupon payment is made based on <i>payment periods</i> with are typically the same as the accrual periods.
 * If the payment period is longer than the accrual period then compounding may apply.
 */
@BeanDefinition
public final class RateSwapLeg
    implements SwapLeg, ImmutableBean, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Whether the leg is pay or receive.
   * <p>
   * A value of 'Pay' implies that the resulting amount is paid to the counterparty.
   * A value of 'Receive' implies that the resulting amount is received from the counterparty.
   * Note that negative interest rates can result in a payment in the opposite
   * direction to that implied by this indicator.
   */
  @PropertyDefinition(validate = "notNull")
  private final PayReceive payReceive;
  /**
   * The accrual periods.
   * <p>
   * This is used to define the accrual periods of the swap.
   * These are used directly or indirectly to determine other dates in the swap.
   */
  @PropertyDefinition(validate = "notNull")
  private final PeriodicSchedule accrualPeriods;
  /**
   * The payment periods.
   * <p>
   * This is used to define the payment periods, including any compounding.
   * The payment period dates are based on the accrual schedule.
   */
  @PropertyDefinition(validate = "notNull")
  private final PaymentSchedule paymentPeriods;
  /**
   * The notional of the swap leg.
   * <p>
   * The notional amount of the swap leg, which can vary during the lifetime of the swap.
   * In most cases, the notional amount is not exchanged, with only the net difference being exchanged.
   * However, in certain cases, initial, final or intermediate amounts are exchanged.
   */
  @PropertyDefinition(validate = "notNull")
  private final NotionalAmount notional;
  /**
   * The interest rate accrual calculation.
   * <p>
   * Different kinds of swap leg are determined by the subclass used here.
   * See {@link FixedRateCalculation}, {@link IborRateCalculation} and {@link OvernightRateCalculation}.
   */
  @PropertyDefinition(validate = "notNull")
  private final RateCalculation calculation;

  //-------------------------------------------------------------------------
  /**
   * Gets the start date of the leg.
   * <p>
   * This is the first accrual date in the leg, often known as the effective date.
   * This date has been adjusted to be a valid business day.
   * 
   * @return the start date of the period
   */
  @Override
  public LocalDate getStartDate() {
    return accrualPeriods.getStartDate();
  }

  /**
   * Gets the end date of the leg.
   * <p>
   * This is the last accrual date in the leg, often known as the maturity date.
   * This date has been adjusted to be a valid business day.
   * 
   * @return the end date of the period
   */
  @Override
  public LocalDate getEndDate() {
    return accrualPeriods.getEndDate();
  }

  /**
   * Gets the currency of the swap leg.
   * 
   * @return the currency
   */
  @Override
  public Currency getCurrency() {
    return notional.getCurrency();
  }

  /**
   * Converts this swap leg to the equivalent expanded swap leg.
   * 
   * @return the equivalent expanded swap leg
   * @throws RuntimeException if the swap leg is invalid
   */
  @Override
  public ExpandedSwapLeg toExpanded() {
    Schedule schedule = accrualPeriods.createSchedule();
    ImmutableList<RateAccrualPeriod> accrualPeriods = calculation.toExpanded(schedule);
    return ExpandedSwapLeg.builder()
        .paymentPeriods(paymentPeriods.createPaymentPeriods(schedule, accrualPeriods, notional, payReceive))
        .notionalExchange(NotionalExchange.NO_EXCHANGE)  // TODO
        .build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RateSwapLeg}.
   * @return the meta-bean, not null
   */
  public static RateSwapLeg.Meta meta() {
    return RateSwapLeg.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RateSwapLeg.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static RateSwapLeg.Builder builder() {
    return new RateSwapLeg.Builder();
  }

  private RateSwapLeg(
      PayReceive payReceive,
      PeriodicSchedule accrualPeriods,
      PaymentSchedule paymentPeriods,
      NotionalAmount notional,
      RateCalculation calculation) {
    JodaBeanUtils.notNull(payReceive, "payReceive");
    JodaBeanUtils.notNull(accrualPeriods, "accrualPeriods");
    JodaBeanUtils.notNull(paymentPeriods, "paymentPeriods");
    JodaBeanUtils.notNull(notional, "notional");
    JodaBeanUtils.notNull(calculation, "calculation");
    this.payReceive = payReceive;
    this.accrualPeriods = accrualPeriods;
    this.paymentPeriods = paymentPeriods;
    this.notional = notional;
    this.calculation = calculation;
  }

  @Override
  public RateSwapLeg.Meta metaBean() {
    return RateSwapLeg.Meta.INSTANCE;
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
   * Gets whether the leg is pay or receive.
   * <p>
   * A value of 'Pay' implies that the resulting amount is paid to the counterparty.
   * A value of 'Receive' implies that the resulting amount is received from the counterparty.
   * Note that negative interest rates can result in a payment in the opposite
   * direction to that implied by this indicator.
   * @return the value of the property, not null
   */
  public PayReceive getPayReceive() {
    return payReceive;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the accrual periods.
   * <p>
   * This is used to define the accrual periods of the swap.
   * These are used directly or indirectly to determine other dates in the swap.
   * @return the value of the property, not null
   */
  public PeriodicSchedule getAccrualPeriods() {
    return accrualPeriods;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment periods.
   * <p>
   * This is used to define the payment periods, including any compounding.
   * The payment period dates are based on the accrual schedule.
   * @return the value of the property, not null
   */
  public PaymentSchedule getPaymentPeriods() {
    return paymentPeriods;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the notional of the swap leg.
   * <p>
   * The notional amount of the swap leg, which can vary during the lifetime of the swap.
   * In most cases, the notional amount is not exchanged, with only the net difference being exchanged.
   * However, in certain cases, initial, final or intermediate amounts are exchanged.
   * @return the value of the property, not null
   */
  public NotionalAmount getNotional() {
    return notional;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the interest rate accrual calculation.
   * <p>
   * Different kinds of swap leg are determined by the subclass used here.
   * See {@link FixedRateCalculation}, {@link IborRateCalculation} and {@link OvernightRateCalculation}.
   * @return the value of the property, not null
   */
  public RateCalculation getCalculation() {
    return calculation;
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
      RateSwapLeg other = (RateSwapLeg) obj;
      return JodaBeanUtils.equal(getPayReceive(), other.getPayReceive()) &&
          JodaBeanUtils.equal(getAccrualPeriods(), other.getAccrualPeriods()) &&
          JodaBeanUtils.equal(getPaymentPeriods(), other.getPaymentPeriods()) &&
          JodaBeanUtils.equal(getNotional(), other.getNotional()) &&
          JodaBeanUtils.equal(getCalculation(), other.getCalculation());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getPayReceive());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccrualPeriods());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentPeriods());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNotional());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCalculation());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("RateSwapLeg{");
    buf.append("payReceive").append('=').append(getPayReceive()).append(',').append(' ');
    buf.append("accrualPeriods").append('=').append(getAccrualPeriods()).append(',').append(' ');
    buf.append("paymentPeriods").append('=').append(getPaymentPeriods()).append(',').append(' ');
    buf.append("notional").append('=').append(getNotional()).append(',').append(' ');
    buf.append("calculation").append('=').append(JodaBeanUtils.toString(getCalculation()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RateSwapLeg}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code payReceive} property.
     */
    private final MetaProperty<PayReceive> payReceive = DirectMetaProperty.ofImmutable(
        this, "payReceive", RateSwapLeg.class, PayReceive.class);
    /**
     * The meta-property for the {@code accrualPeriods} property.
     */
    private final MetaProperty<PeriodicSchedule> accrualPeriods = DirectMetaProperty.ofImmutable(
        this, "accrualPeriods", RateSwapLeg.class, PeriodicSchedule.class);
    /**
     * The meta-property for the {@code paymentPeriods} property.
     */
    private final MetaProperty<PaymentSchedule> paymentPeriods = DirectMetaProperty.ofImmutable(
        this, "paymentPeriods", RateSwapLeg.class, PaymentSchedule.class);
    /**
     * The meta-property for the {@code notional} property.
     */
    private final MetaProperty<NotionalAmount> notional = DirectMetaProperty.ofImmutable(
        this, "notional", RateSwapLeg.class, NotionalAmount.class);
    /**
     * The meta-property for the {@code calculation} property.
     */
    private final MetaProperty<RateCalculation> calculation = DirectMetaProperty.ofImmutable(
        this, "calculation", RateSwapLeg.class, RateCalculation.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "payReceive",
        "accrualPeriods",
        "paymentPeriods",
        "notional",
        "calculation");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -885469925:  // payReceive
          return payReceive;
        case -92208605:  // accrualPeriods
          return accrualPeriods;
        case -1674414612:  // paymentPeriods
          return paymentPeriods;
        case 1585636160:  // notional
          return notional;
        case -934682935:  // calculation
          return calculation;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public RateSwapLeg.Builder builder() {
      return new RateSwapLeg.Builder();
    }

    @Override
    public Class<? extends RateSwapLeg> beanType() {
      return RateSwapLeg.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code payReceive} property.
     * @return the meta-property, not null
     */
    public MetaProperty<PayReceive> payReceive() {
      return payReceive;
    }

    /**
     * The meta-property for the {@code accrualPeriods} property.
     * @return the meta-property, not null
     */
    public MetaProperty<PeriodicSchedule> accrualPeriods() {
      return accrualPeriods;
    }

    /**
     * The meta-property for the {@code paymentPeriods} property.
     * @return the meta-property, not null
     */
    public MetaProperty<PaymentSchedule> paymentPeriods() {
      return paymentPeriods;
    }

    /**
     * The meta-property for the {@code notional} property.
     * @return the meta-property, not null
     */
    public MetaProperty<NotionalAmount> notional() {
      return notional;
    }

    /**
     * The meta-property for the {@code calculation} property.
     * @return the meta-property, not null
     */
    public MetaProperty<RateCalculation> calculation() {
      return calculation;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -885469925:  // payReceive
          return ((RateSwapLeg) bean).getPayReceive();
        case -92208605:  // accrualPeriods
          return ((RateSwapLeg) bean).getAccrualPeriods();
        case -1674414612:  // paymentPeriods
          return ((RateSwapLeg) bean).getPaymentPeriods();
        case 1585636160:  // notional
          return ((RateSwapLeg) bean).getNotional();
        case -934682935:  // calculation
          return ((RateSwapLeg) bean).getCalculation();
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
   * The bean-builder for {@code RateSwapLeg}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<RateSwapLeg> {

    private PayReceive payReceive;
    private PeriodicSchedule accrualPeriods;
    private PaymentSchedule paymentPeriods;
    private NotionalAmount notional;
    private RateCalculation calculation;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(RateSwapLeg beanToCopy) {
      this.payReceive = beanToCopy.getPayReceive();
      this.accrualPeriods = beanToCopy.getAccrualPeriods();
      this.paymentPeriods = beanToCopy.getPaymentPeriods();
      this.notional = beanToCopy.getNotional();
      this.calculation = beanToCopy.getCalculation();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -885469925:  // payReceive
          return payReceive;
        case -92208605:  // accrualPeriods
          return accrualPeriods;
        case -1674414612:  // paymentPeriods
          return paymentPeriods;
        case 1585636160:  // notional
          return notional;
        case -934682935:  // calculation
          return calculation;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -885469925:  // payReceive
          this.payReceive = (PayReceive) newValue;
          break;
        case -92208605:  // accrualPeriods
          this.accrualPeriods = (PeriodicSchedule) newValue;
          break;
        case -1674414612:  // paymentPeriods
          this.paymentPeriods = (PaymentSchedule) newValue;
          break;
        case 1585636160:  // notional
          this.notional = (NotionalAmount) newValue;
          break;
        case -934682935:  // calculation
          this.calculation = (RateCalculation) newValue;
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
    public RateSwapLeg build() {
      return new RateSwapLeg(
          payReceive,
          accrualPeriods,
          paymentPeriods,
          notional,
          calculation);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code payReceive} property in the builder.
     * @param payReceive  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder payReceive(PayReceive payReceive) {
      JodaBeanUtils.notNull(payReceive, "payReceive");
      this.payReceive = payReceive;
      return this;
    }

    /**
     * Sets the {@code accrualPeriods} property in the builder.
     * @param accrualPeriods  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder accrualPeriods(PeriodicSchedule accrualPeriods) {
      JodaBeanUtils.notNull(accrualPeriods, "accrualPeriods");
      this.accrualPeriods = accrualPeriods;
      return this;
    }

    /**
     * Sets the {@code paymentPeriods} property in the builder.
     * @param paymentPeriods  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentPeriods(PaymentSchedule paymentPeriods) {
      JodaBeanUtils.notNull(paymentPeriods, "paymentPeriods");
      this.paymentPeriods = paymentPeriods;
      return this;
    }

    /**
     * Sets the {@code notional} property in the builder.
     * @param notional  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder notional(NotionalAmount notional) {
      JodaBeanUtils.notNull(notional, "notional");
      this.notional = notional;
      return this;
    }

    /**
     * Sets the {@code calculation} property in the builder.
     * @param calculation  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder calculation(RateCalculation calculation) {
      JodaBeanUtils.notNull(calculation, "calculation");
      this.calculation = calculation;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(192);
      buf.append("RateSwapLeg.Builder{");
      buf.append("payReceive").append('=').append(JodaBeanUtils.toString(payReceive)).append(',').append(' ');
      buf.append("accrualPeriods").append('=').append(JodaBeanUtils.toString(accrualPeriods)).append(',').append(' ');
      buf.append("paymentPeriods").append('=').append(JodaBeanUtils.toString(paymentPeriods)).append(',').append(' ');
      buf.append("notional").append('=').append(JodaBeanUtils.toString(notional)).append(',').append(' ');
      buf.append("calculation").append('=').append(JodaBeanUtils.toString(calculation));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}