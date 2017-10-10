package com.systematic.trading.signals.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

public class BuySignalTest {

	@Test(expected = IllegalArgumentException.class)
	public void constructorNullDate() {
		new BuySignal(null);
	}

	@Test
	public void getDate() {
		final LocalDate date = LocalDate.now();
		final BuySignal signal = new BuySignal(date);

		assertEquals(date, signal.getDate());
	}

	@Test
	public void equalsSelf() {
		final BuySignal signal = new BuySignal(LocalDate.now());

		assertEquals(signal, signal);
	}

	@Test
	public void equalsOnDate() {
		final LocalDate date = LocalDate.now();
		final BuySignal signalA = new BuySignal(date);
		final BuySignal signalB = new BuySignal(date);

		assertEquals(signalA, signalB);
	}

	@Test
	public void equalsMismatchedDate() {
		final LocalDate date = LocalDate.now();
		final BuySignal signalA = new BuySignal(date);
		final BuySignal signalB = new BuySignal(date.minus(1, ChronoUnit.DAYS));

		assertNotEquals(signalA, signalB);
	}

	@Test
	public void equalsMismatchedNull() {
		final BuySignal signalA = new BuySignal(LocalDate.now());
		final BuySignal signalB = null;

		assertNotEquals(signalA, signalB);
	}

	@Test
	public void equalsMismatchedObject() {
		final BuySignal signalA = new BuySignal(LocalDate.now());

		assertNotEquals(signalA, BigDecimal.ZERO);
	}

	@Test
	public void hashCodeOnlyDate() {
		final LocalDate date = LocalDate.now();
		final BuySignal signal = new BuySignal(date);

		final int expectedHashCode = 31 + date.hashCode();
		assertEquals(expectedHashCode, signal.hashCode());
	}
}