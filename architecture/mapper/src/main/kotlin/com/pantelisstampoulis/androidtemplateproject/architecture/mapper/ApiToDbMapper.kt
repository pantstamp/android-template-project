package com.pantelisstampoulis.androidtemplateproject.architecture.mapper

interface ApiToDbMapper<ApiModel, DbModel> {

    fun fromApiToDb(apiModel: ApiModel) : DbModel
}