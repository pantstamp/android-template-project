package com.pantelisstampoulis.androidtemplateproject.architecture.mapper

interface DomainToUiMapper<DomainModel, UiModel> {

    fun fromDomainToUi(domainModel: DomainModel): UiModel
}
