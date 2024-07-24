package com.sregs.template.network

import com.sregs.template.network.config.Paths
import com.sregs.template.network.model.AccountApiModel
import com.sregs.template.network.model.AccountDetailsApiModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments

internal class KtorNetworkDataSource(
    private val httpClient: HttpClient,
) : NetworkDataSource {

    override suspend fun getAccounts(): List<AccountApiModel> =
        httpClient.get {
            url { appendPathSegments(Paths.Accounts) }
        }.body()

    override suspend fun getAccountDetails(accountId: String): AccountDetailsApiModel {
        require(value = accountId.isNotEmpty()) {
            "AccountId must not be empty"
        }

        return httpClient.get {
            url { appendPathSegments(Paths.AccountDetails, accountId) }
        }.body()
    }
}
