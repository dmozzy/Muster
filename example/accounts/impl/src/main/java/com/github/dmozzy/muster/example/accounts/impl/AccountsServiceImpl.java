package com.github.dmozzy.muster.example.accounts.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.github.dmozzy.muster.api.BaseMusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;
import com.github.dmozzy.muster.example.accounts.AccountsService;
import com.github.dmozzy.muster.example.accounts.domain.Account;

public class AccountsServiceImpl {
	public static class GetAccountFunctionImpl extends BaseMusterService<String, Account>
			implements AccountsService.GetAccountFunction {

		@Override
		public Account doService(String accountNumber, MusterOrchestrationManager orchestrationManager) {
			Account account = new Account();
			account.setAccountNumber("1234");
			account.setAddress("10 Here Street, Hereville");
			account.setBalance(new BigDecimal(100));
			return account;
		}
	}

	public static class ListAccountsFunctionImpl extends BaseMusterService<Void, List<Account>>
			implements AccountsService.ListAccountsFunction {

		@Override
		public List<Account> doService(Void argument, MusterOrchestrationManager orchestrationManager) {
			Account account = new Account();
			account.setAccountNumber("1234");
			account.setAddress("10 Here Street, Hereville");
			account.setBalance(new BigDecimal(100));

			return Arrays.asList(new Account[] { account });
		}

	}

}
