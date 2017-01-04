package com.github.dmozzy.muster.apilambda;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.github.dmozzy.muster.api.BaseMusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;
import com.github.dmozzy.muster.api.MusterServiceConfiguration;

import domain.Book;
import lombok.extern.slf4j.Slf4j;

@MusterServiceConfiguration(service="Testing", name="TestOrchestration1")
@Slf4j
public class TestMusterFunction1 extends BaseMusterService <String, String> {
	
	@Override
	public String doService(String argument, MusterOrchestrationManager orchestrationManager) {
		
		Future<Book> call = orchestrationManager.call(GetBookDetailsFunction.class, "BookName");		
		
		try {
			log.info("Am going to return" + call.get().getAuthor());

			return call.get().getAuthor();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
}
