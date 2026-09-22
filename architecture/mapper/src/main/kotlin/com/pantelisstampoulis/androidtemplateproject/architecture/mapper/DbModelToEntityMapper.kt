package com.pantelisstampoulis.androidtemplateproject.architecture.mapper

/**
 * Maps a database-agnostic `DbModel` to the storage-specific `Entity` a database
 * implementation persists.
 *
 * `DbModel` names the type, not a destination: it is the model the database abstraction
 * exposes, and the `Entity` is the one closer to storage.
 */
interface DbModelToEntityMapper<DbModel, Entity> {

    fun fromDbModelToEntity(dbModel: DbModel): Entity
}
