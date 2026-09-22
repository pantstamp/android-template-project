package com.pantelisstampoulis.androidtemplateproject.database.mapper

/**
 * Contracts for converting between the database abstraction's `DbModel` types and the Room
 * entities that back them.
 *
 * These deliberately live here rather than in `:architecture:mapper`. An `Entity` is Room's
 * own concept and exists only inside this module — `:core:database:api`, `:core:database:noop`
 * and `:core:data` never see one. Putting the contract in the shared architecture module would
 * make the stable part of the design name a type belonging to a swappable implementation, so
 * that a change of persistence library would reach back into it. The interfaces in
 * `:architecture:mapper` all cross boundaries that survive such a change; these two do not, so
 * they stay with the implementation they describe.
 */
internal interface DbModelToEntityMapper<DbModel, Entity> {

    fun fromDbModelToEntity(dbModel: DbModel): Entity
}

/**
 * The reverse of [DbModelToEntityMapper]: converts a Room entity back to the database-agnostic
 * model that `:core:data` consumes.
 */
internal interface EntityToDbModelMapper<Entity, DbModel> {

    fun fromEntityToDbModel(entity: Entity): DbModel
}
