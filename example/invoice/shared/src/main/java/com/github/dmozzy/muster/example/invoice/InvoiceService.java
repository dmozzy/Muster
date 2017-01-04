package com.github.dmozzy.muster.example.invoice;

import java.util.List;

import com.github.dmozzy.muster.api.MusterService;
import com.github.dmozzy.muster.api.MusterServiceConfiguration;
import com.github.dmozzy.muster.example.invoice.domain.Invoice;

public interface InvoiceService {
	@MusterServiceConfiguration(service="InvoiceService", name="ListInvoices", idempotency=false)
	public interface ListInvoicesFunction extends MusterService<Void, List<Invoice>> {
		@Override
		List<Invoice> execute(Void input);
	}

	@MusterServiceConfiguration(service="InvoiceService", name="SaveInvoice", idempotency=true)
	public interface SaveInvoiceFunction extends MusterService<Invoice, Invoice> {
		@Override
		Invoice execute(Invoice input);
	}	
}
