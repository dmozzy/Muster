package com.github.dmozzy.muster.example.products;

import java.util.List;

import com.github.dmozzy.muster.api.MusterService;
import com.github.dmozzy.muster.api.MusterServiceConfiguration;
import com.github.dmozzy.muster.example.products.domain.Product;

public interface ProductsService {
	@MusterServiceConfiguration(service="ProductsService", name="ListProducts", idempotency=false)
	public static interface ListProductsFunction extends MusterService<Void, List<Product>> {
		@Override
		List<Product> execute(Void input);
	}
	
	@MusterServiceConfiguration(service="ProductsService", name="GetProduct", idempotency=false)
	public static interface GetProductFunction extends MusterService<String, Product> {
		@Override	
		Product execute(String productId);
	}
}
