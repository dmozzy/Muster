package com.github.dmozzy.muster.apilambda;
import java.math.BigDecimal;

import com.github.dmozzy.muster.api.BaseMusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;

import domain.Book;

public class GetBookDetailsFunctionImpl extends BaseMusterService<String, Book> implements GetBookDetailsFunction{



	@Override
	public Book doService(String argument, MusterOrchestrationManager orchestrationManager) {
		Book book = new Book();
		book.setName("Lambda the ultimate");
		book.setPrice(new BigDecimal("12.00"));
		book.setAuthor("Bob Jones");
		return book;
	}




}
