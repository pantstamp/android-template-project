package com.pantelisstampoulis.androidtemplateproject.architecture.mapper

interface DbToEntityMapper<DbModel, Entity> {

    fun fromDbToEntity(dbModel: DbModel): Entity
}
