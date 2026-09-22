package com.pantelisstampoulis.androidtemplateproject.architecture.mapper

/**
 * Maps a storage-specific `Entity` back to the database-agnostic `DbModel` that the
 * database abstraction exposes.
 *
 * This direction moves away from storage, toward the model shared with `:core:data`.
 */
interface EntityToDbModelMapper<Entity, DbModel> {

    fun fromEntityToDbModel(entity: Entity): DbModel
}
