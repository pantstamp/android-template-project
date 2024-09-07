package com.pantelisstampoulis.androidtemplateproject.architecture.mapper

interface ApiToDomainMapper<ApiModel, DomainModel> {

    fun fromApiToDomain(apiModel: ApiModel): DomainModel
}
