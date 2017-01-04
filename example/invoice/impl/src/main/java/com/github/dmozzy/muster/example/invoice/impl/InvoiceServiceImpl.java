package com.github.dmozzy.muster.example.invoice.impl;

import java.util.Arrays;
import java.util.List;

import com.github.dmozzy.muster.api.BaseMusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;
import com.github.dmozzy.muster.example.invoice.InvoiceService;
import com.github.dmozzy.muster.example.invoice.domain.Invoice;

public class InvoiceServiceImpl {
	public static class ListInvoicesFunctionImpl extends BaseMusterService<Void, List<Invoice>> implements InvoiceService.ListInvoicesFunction {
		@Override
		public List<Invoice> doService(Void argument, MusterOrchestrationManager orchestrationManager) {
			return Arrays.asList(new Invoice[]{});
		}

	}
	
	public static class SaveInvoiceFunctionImpl extends BaseMusterService<Invoice, Invoice> implements InvoiceService.SaveInvoiceFunction {

		@Override
		public Invoice doService(Invoice argument, MusterOrchestrationManager orchestrationManager) {
			return null;
		}

	}
	
}
