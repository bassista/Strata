/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.fx;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.PropertyDefinition;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.data.MarketDataName;
import com.opengamma.strata.market.param.CurrencyParameterSensitivities;
import com.opengamma.strata.market.param.ParameterMetadata;
import com.opengamma.strata.market.param.ParameterPerturbation;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Fx forward based on an underlying fx forward and a forward date. 
 * The new fx forwards acts as the implied forward fx rates from the underlying.
 * <p>
 * Only the methods used for direct valuation are implemented. The methods related to sensitivities are not implemented.
 */
@BeanDefinition(builderScope = "private")
public class FxForwardRatesDecoratedForward
    implements FxForwardRates, ImmutableBean, Serializable {

  /** Underlying provider. */
  @PropertyDefinition(validate = "notNull")
  private final FxForwardRates underlying;
  /** The forward date. */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final LocalDate valuationDate;

  /**
   * Creates a new {@link FxForwardRates} from an existing one and a forward date. 
   * 
   * @param underlying  the underlying fx forward
   * @param valuationDate  the valuation date for which the curve is valid
   * @return the fx forwards
   */
  public static FxForwardRatesDecoratedForward of(FxForwardRates underlying, LocalDate valuationDate) {
    return new FxForwardRatesDecoratedForward(underlying, valuationDate);
  }

  @ImmutableConstructor
  private FxForwardRatesDecoratedForward(FxForwardRates underlying, LocalDate valuationDate) {
    JodaBeanUtils.notNull(underlying, "underlying");
    JodaBeanUtils.notNull(valuationDate, "valuationDate");
    this.underlying = underlying;
    this.valuationDate = valuationDate;
  }

  @Override
  public double rate(Currency baseCurrency, LocalDate referenceDate) {
    return underlying.rate(baseCurrency, referenceDate);
  }

  @Override
  public int getParameterCount() {
    return underlying.getParameterCount();
  }

  @Override
  public double getParameter(int parameterIndex) {
    return underlying.getParameter(parameterIndex);
  }

  @Override
  public ParameterMetadata getParameterMetadata(int parameterIndex) {
    return underlying.getParameterMetadata(parameterIndex);
  }

  @Override
  public CurrencyPair getCurrencyPair() {
    return underlying.getCurrencyPair();
  }

  @Override
  public FxForwardRates withParameter(int parameterIndex, double newValue) {
    return FxForwardRatesDecoratedForward.of(underlying.withParameter(parameterIndex, newValue), valuationDate);
  }

  @Override
  public FxForwardRates withPerturbation(ParameterPerturbation perturbation) {
    return FxForwardRatesDecoratedForward.of(underlying.withPerturbation(perturbation), valuationDate);
  }

  @Override
  public <T> Optional<T> findData(MarketDataName<T> name) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public PointSensitivityBuilder ratePointSensitivity(Currency baseCurrency, LocalDate referenceDate) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public double rateFxSpotSensitivity(Currency baseCurrency, LocalDate referenceDate) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public CurrencyParameterSensitivities parameterSensitivity(FxForwardSensitivity pointSensitivity) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public MultiCurrencyAmount currencyExposure(FxForwardSensitivity pointSensitivity) {
    throw new UnsupportedOperationException("Not implemented");
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FxForwardRatesDecoratedForward}.
   * @return the meta-bean, not null
   */
  public static FxForwardRatesDecoratedForward.Meta meta() {
    return FxForwardRatesDecoratedForward.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FxForwardRatesDecoratedForward.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  @Override
  public FxForwardRatesDecoratedForward.Meta metaBean() {
    return FxForwardRatesDecoratedForward.Meta.INSTANCE;
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
   * Gets underlying provider.
   * @return the value of the property, not null
   */
  public FxForwardRates getUnderlying() {
    return underlying;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the forward date.
   * @return the value of the property, not null
   */
  @Override
  public LocalDate getValuationDate() {
    return valuationDate;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FxForwardRatesDecoratedForward other = (FxForwardRatesDecoratedForward) obj;
      return JodaBeanUtils.equal(underlying, other.underlying) &&
          JodaBeanUtils.equal(valuationDate, other.valuationDate);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(underlying);
    hash = hash * 31 + JodaBeanUtils.hashCode(valuationDate);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("FxForwardRatesDecoratedForward{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("underlying").append('=').append(JodaBeanUtils.toString(underlying)).append(',').append(' ');
    buf.append("valuationDate").append('=').append(JodaBeanUtils.toString(valuationDate)).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FxForwardRatesDecoratedForward}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code underlying} property.
     */
    private final MetaProperty<FxForwardRates> underlying = DirectMetaProperty.ofImmutable(
        this, "underlying", FxForwardRatesDecoratedForward.class, FxForwardRates.class);
    /**
     * The meta-property for the {@code valuationDate} property.
     */
    private final MetaProperty<LocalDate> valuationDate = DirectMetaProperty.ofImmutable(
        this, "valuationDate", FxForwardRatesDecoratedForward.class, LocalDate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "underlying",
        "valuationDate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1770633379:  // underlying
          return underlying;
        case 113107279:  // valuationDate
          return valuationDate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FxForwardRatesDecoratedForward> builder() {
      return new FxForwardRatesDecoratedForward.Builder();
    }

    @Override
    public Class<? extends FxForwardRatesDecoratedForward> beanType() {
      return FxForwardRatesDecoratedForward.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code underlying} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FxForwardRates> underlying() {
      return underlying;
    }

    /**
     * The meta-property for the {@code valuationDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> valuationDate() {
      return valuationDate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1770633379:  // underlying
          return ((FxForwardRatesDecoratedForward) bean).getUnderlying();
        case 113107279:  // valuationDate
          return ((FxForwardRatesDecoratedForward) bean).getValuationDate();
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
   * The bean-builder for {@code FxForwardRatesDecoratedForward}.
   */
  private static class Builder extends DirectFieldsBeanBuilder<FxForwardRatesDecoratedForward> {

    private FxForwardRates underlying;
    private LocalDate valuationDate;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1770633379:  // underlying
          return underlying;
        case 113107279:  // valuationDate
          return valuationDate;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1770633379:  // underlying
          this.underlying = (FxForwardRates) newValue;
          break;
        case 113107279:  // valuationDate
          this.valuationDate = (LocalDate) newValue;
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
    public FxForwardRatesDecoratedForward build() {
      return new FxForwardRatesDecoratedForward(
          underlying,
          valuationDate);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("FxForwardRatesDecoratedForward.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("underlying").append('=').append(JodaBeanUtils.toString(underlying)).append(',').append(' ');
      buf.append("valuationDate").append('=').append(JodaBeanUtils.toString(valuationDate)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
