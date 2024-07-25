package com.sregs.architecture.mapper

interface DomainToUiMapper<DomainModel, UiModel> {

    fun fromDomainToUi(domainModel: DomainModel) : UiModel
}