package com.github.dmozzy.muster.example.invoice.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Invoice {
	private String productCode;
	private String accountNumber;
	private int quantity;
	private BigDecimal price;	
	private BigDecimal total;
	
}
