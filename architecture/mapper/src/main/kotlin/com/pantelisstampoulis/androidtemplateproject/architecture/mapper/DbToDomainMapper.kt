package com.sregs.architecture.mapper

interface DbToDomainMapper<DbModel, DomainModel> {

    fun fromDbToDomain(dbModel: DbModel) : DomainModel
}