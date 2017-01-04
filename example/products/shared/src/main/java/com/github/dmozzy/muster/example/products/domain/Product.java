package com.github.dmozzy.muster.example.products.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	private String productCode;
	private String briefDescription;
	private String details;
	private BigDecimal price;
	private boolean isDiscountable;
}
