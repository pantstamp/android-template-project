package com.sregs.template.network

import com.sregs.template.network.model.AccountApiModel
import com.sregs.template.network.model.AccountDetailsApiModel

@Suppress("konsist.Every class implementing 'NetworkDataSource' has test")
internal class NoopNetworkDataSource : NetworkDataSource {

    override suspend fun getAccounts(): List<AccountApiModel> = emptyList()

    override suspend fun getAccountDetails(accountId: String): AccountDetailsApiModel =
        AccountDetailsApiModel(
            productName = "",
            openedDate = "",
            branch = "",
            beneficiaries = emptyList(),
        )
}
