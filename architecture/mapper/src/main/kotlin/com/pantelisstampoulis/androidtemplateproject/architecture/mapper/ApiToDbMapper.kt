package com.sregs.architecture.mapper

interface ApiToDbMapper<ApiModel, DbModel> {

    fun fromApiToDb(apiModel: ApiModel) : DbModel
}