package com.sregs.architecture.mapper

interface ApiToDomainMapper<ApiModel, DomainModel> {

    fun fromApiToDomain(apiModel: ApiModel) : DomainModel
}