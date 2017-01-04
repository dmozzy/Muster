package com.github.dmozzy.muster.example.accounts.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Account {
	private String accountNumber;
	private String address;
	private BigDecimal balance;
	private int percentageDiscount;
}
