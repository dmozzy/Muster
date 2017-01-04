package com.github.dmozzy.muster.example.order.domain;

import lombok.Data;

@Data
public class Order {
	private String referenceNumber;
	private String accountNumber;
	private int quantity;
	private String productNumber;
}
