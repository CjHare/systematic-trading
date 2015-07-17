/**
 * Copyright (c) 2015, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.backtest.cash.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.exception.InsufficientFundsException;
import com.systematic.trading.data.DataPoint;

/**
 * Flat interest rates calculated daily, paid monthly.
 * 
 * @author CJ Hare
 */
public class FlatInterestRateCashAccount implements CashAccount {

    private final InterestRate rate;

    private final LocalDate lastInterestCalculation;

    private BigDecimal funds;

    /** Interest awarded, yet to be realised (paid out).*/
    private BigDecimal escrow;

    public FlatInterestRateCashAccount(final InterestRate rate, final BigDecimal openingFunds,
            final LocalDate openingDate) {
        this.rate = rate;
        this.funds = openingFunds;
        this.lastInterestCalculation = openingDate;
    }

    @Override
    public void update(final DataPoint data) {

        LocalDate last = lastInterestCalculation;

        while (last.getMonth() != data.getDate().getMonth()) {
            last = applyFullMonthInterest(last);
        }

        // Remaining days of interest to escrow
        final boolean isLeapYear = data.getDate().isLeapYear();
        final int daysInterest = data.getDate().getDayOfMonth();
        escrow = escrow.add(rate.interest(funds, daysInterest, isLeapYear));

        // Update the interest date marker
        data.getDate().adjustInto(lastInterestCalculation);
    }

    private LocalDate applyFullMonthInterest(final LocalDate last) {

        // Number of days interest this month
        final int daysInterest = last.getMonth().length(last.isLeapYear()) - last.getDayOfMonth();

        // Calculate and pay the interest
        final boolean isLeapYear = last.isLeapYear();
        funds = funds.add(rate.interest(funds, daysInterest, isLeapYear));
        funds = funds.add(escrow);
        escrow = BigDecimal.ZERO;

        // Next month begins on the first day
        return LocalDate.of(last.getYear(), last.getMonthValue(), 1);
    }

    @Override
    public void debit(final BigDecimal amount) throws InsufficientFundsException {

        if (funds.compareTo(amount) < 0) {
            throw new InsufficientFundsException(String.format("Attempting to debit %s from only %s", amount, funds));
        }

        funds = funds.subtract(amount);
    }

    @Override
    public void credit(final BigDecimal amount) {
        funds = funds.add(amount);
    }
}
