package com.github.dmozzy.muster.example.products.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.dmozzy.muster.api.BaseMusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;
import com.github.dmozzy.muster.example.products.ProductsService;
import com.github.dmozzy.muster.example.products.domain.Product;

/**
 * Mimics a cached representation of a products service
 * @author daniel
 *
 */
public class ProductsServiceImpl {
	
	private static Map<String, Product> productsMap = new HashMap<>();
	static {
		productsMap.put("D1", new Product("D1","ACME Dog Food", "Premium grade dog food by ACME industries, when only the best kibble will do!",new BigDecimal(20.0), true));
		productsMap.put("D2", new Product("D2","ACME Dog Collar: Large", "A great dog collar for the largest breed of dogs.",new BigDecimal(15.0), false));
		productsMap.put("D3", new Product("D3","ACME Dog Collar: Small", "A great dog collar for the small breed of dogs.",new BigDecimal(13.0), false));
		productsMap.put("C1", new Product("C1","ACME Cat Food", "Premium grade cat food by ACME industries, when only the best tuna will do!",new BigDecimal(10.0), true));
		productsMap.put("C2", new Product("C2","ACME Cat Scrating post", "Scratching post that can help save your furniture and keep your spouse happy!",new BigDecimal(25.0), true));
		
	}
	public static class GetProductImpl extends BaseMusterService<String, Product> implements ProductsService.GetProductFunction{
		@Override
		public Product doService(String productCode, MusterOrchestrationManager orchestrationManager) {
			return productsMap.get(productCode);
		}
	}
	
	public static class ListProductsFunctionImpl extends BaseMusterService<Void, List<Product>> implements ProductsService.ListProductsFunction {
		@Override
		public List<Product> doService(Void argument, MusterOrchestrationManager orchestrationManager) {
			return new ArrayList<>(productsMap.values());
		}
	}
	
}
