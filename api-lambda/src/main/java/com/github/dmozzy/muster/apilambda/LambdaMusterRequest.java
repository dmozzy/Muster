package com.github.dmozzy.muster.apilambda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LambdaMusterRequest<T> {
	private String parentReference;
	private String requestReference;
	private String method;
	private T data;
}
