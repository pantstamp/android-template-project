package com.pantelisstampoulis.androidtemplateproject.logging

interface Logger {

    val tag: String

    fun d(throwable: Throwable? = null, tag: String = this.tag, message: () -> String)

    fun i(throwable: Throwable? = null, tag: String = this.tag, message: () -> String)

    fun w(throwable: Throwable? = null, tag: String = this.tag, message: () -> String)

    fun e(throwable: Throwable? = null, tag: String = this.tag, message: () -> String)

    fun a(throwable: Throwable? = null, tag: String = this.tag, message: () -> String)

    fun v(throwable: Throwable? = null, tag: String = this.tag, message: () -> String)
}
