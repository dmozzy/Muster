package domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Book {
	String name;
	BigDecimal price;
	String author;
}
