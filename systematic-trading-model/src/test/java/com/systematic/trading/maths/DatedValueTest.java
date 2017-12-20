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
	public void date() {

		assertEquals(DATE, datedValue.date());
	}

	@Test
	public void value() {

		assertEquals(VALUE, datedValue.value());
	}

	@Test
	public void closingPrice() {

		assertEquals(VALUE, datedValue.closingPrice().price());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void lowestPrice() {

		datedValue.lowestPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void highestPrice() {

		datedValue.highestPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void openingPrice() {

		datedValue.openingPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void tickerSymbol() {

		datedValue.tickerSymbol();
	}
}