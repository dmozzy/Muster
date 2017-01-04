package com.github.dmozzy.muster.example.accounts;

import java.util.List;

import com.github.dmozzy.muster.api.MusterService;
import com.github.dmozzy.muster.api.MusterServiceConfiguration;
import com.github.dmozzy.muster.example.accounts.domain.Account;

public interface AccountsService {
	@MusterServiceConfiguration(service="AccountsService", name="SaveAccount", idempotency=false)
	public interface GetAccountFunction extends MusterService<String, Account> {
		@Override
		Account execute(String input);
	}
	
	@MusterServiceConfiguration(service="AccountsService", name="ListAccounts", idempotency=false)
	public interface ListAccountsFunction extends MusterService<Void, List<Account>> {
		@Override
		List<Account> execute(Void input);
	}	
}
