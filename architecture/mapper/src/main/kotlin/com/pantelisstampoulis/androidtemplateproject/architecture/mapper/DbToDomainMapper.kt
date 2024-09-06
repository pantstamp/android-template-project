package com.pantelisstampoulis.androidtemplateproject.architecture.mapper

interface DbToDomainMapper<DbModel, DomainModel> {

    fun fromDbToDomain(dbModel: DbModel): DomainModel
}
