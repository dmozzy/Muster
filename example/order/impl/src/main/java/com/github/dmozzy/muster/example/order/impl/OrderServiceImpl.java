package com.github.dmozzy.muster.example.order.impl;

import java.math.BigDecimal;
import java.util.concurrent.Future;

import com.github.dmozzy.muster.api.BaseMusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;
import com.github.dmozzy.muster.example.accounts.AccountsService;
import com.github.dmozzy.muster.example.accounts.domain.Account;
import com.github.dmozzy.muster.example.invoice.InvoiceService;
import com.github.dmozzy.muster.example.invoice.domain.Invoice;
import com.github.dmozzy.muster.example.order.OrderService;
import com.github.dmozzy.muster.example.order.domain.Order;
import com.github.dmozzy.muster.example.products.ProductsService;
import com.github.dmozzy.muster.example.products.domain.Product;

public class OrderServiceImpl {
	
	public static class CreateOrderFunctionImpl extends BaseMusterService<Order, Order> implements OrderService.CreateOrderFunction {


		@Override
		public Order doService(Order order,  MusterOrchestrationManager orchestrationManager) throws Exception {
			//Call remote ProductsService lambda with type safety
			Future<Product> productFuture = orchestrationManager.call(ProductsService.GetProductFunction.class, order.getProductNumber());
			//Call remote AccountsService lambda with type safety
			Future<Account> accountFuture = orchestrationManager.call(AccountsService.GetAccountFunction.class, order.getAccountNumber());
					
			Invoice invoice = new Invoice();
			invoice.setQuantity(order.getQuantity());
			
			//Wait for remote lambda calls to return.
			Product product = productFuture.get();
			Account account = accountFuture.get();

			BigDecimal price = product.getPrice();

			if(product.isDiscountable()) {
				price = price.multiply(new BigDecimal(100-account.getPercentageDiscount())).divide(new BigDecimal(100));
			}
			
			invoice.setPrice(price);
			invoice.setTotal(price.multiply(new BigDecimal(invoice.getQuantity())));
			invoice.setAccountNumber(account.getAccountNumber());
		
			orchestrationManager.call(InvoiceService.SaveInvoiceFunction.class, invoice);
			
			//TODO generate a proper reference number
			order.setReferenceNumber("O" + System.currentTimeMillis());
			return order;
		}

	}
}
