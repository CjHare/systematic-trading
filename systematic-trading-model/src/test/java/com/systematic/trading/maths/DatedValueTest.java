package com.systematic.trading.maths;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

public class DatedValueTest {

	private static final LocalDate DATE = LocalDate.now();
	private static final BigDecimal VALUE = BigDecimal.ZERO;

	/** MOdel object being tested. */
	private DatedValue datedValue;

	@Before
	public void setUp() {
		datedValue = new DatedValue(DATE, VALUE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorDateNull() {
		new DatedValue(null, VALUE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorValueNull() {
		new DatedValue(DATE, null);
	}

	@Test
	public void getDate() {
		assertEquals(DATE, datedValue.getDate());
	}

	@Test
	public void getValue() {
		assertEquals(VALUE, datedValue.getValue());
	}

	@Test
	public void getClosingPrice() {
		assertEquals(VALUE, datedValue.getClosingPrice().getPrice());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getLowestPrice() {
		datedValue.getLowestPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getHighestPrice() {
		datedValue.getHighestPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getOpeningPrice() {
		datedValue.getOpeningPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getTickerSymbol() {
		datedValue.getTickerSymbol();
	}
}