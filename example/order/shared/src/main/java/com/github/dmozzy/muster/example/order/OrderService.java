package com.github.dmozzy.muster.example.order;

import com.github.dmozzy.muster.api.MusterService;
import com.github.dmozzy.muster.api.MusterServiceConfiguration;
import com.github.dmozzy.muster.example.order.domain.Order;

public interface OrderService {
	@MusterServiceConfiguration(service="OrderService", name="CreateOrder", idempotency=true)
	public static interface CreateOrderFunction extends MusterService<Order, Order> {
		@Override
		Order execute(Order input);
	}
}
