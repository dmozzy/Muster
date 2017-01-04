package com.github.dmozzy.muster.apilambda;
import com.github.dmozzy.muster.api.MusterService;

import domain.Book;

public interface GetBookDetailsFunction extends MusterService<String, Book>{
		@Override
		Book execute(String bookName);
}