package com.pantelisstampoulis.androidtemplateproject.architecture.mapper

interface EntityToDbMapper<Entity, DbModel> {

    fun fromEntityToDb(entity: Entity): DbModel
}
