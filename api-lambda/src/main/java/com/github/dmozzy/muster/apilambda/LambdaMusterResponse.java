package com.github.dmozzy.muster.apilambda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LambdaMusterResponse<T> {
	private ResponseStatus responseStatus;
	private T data;
	private String errorMessage;

	public static enum ResponseStatus {
		IGNORED, IN_PROGRESS, COMPLETE, ERROR, COMPLETE_DUPLICATE
	}
	
}
