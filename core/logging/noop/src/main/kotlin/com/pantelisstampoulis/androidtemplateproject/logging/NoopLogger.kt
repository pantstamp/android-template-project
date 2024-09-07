package com.pantelisstampoulis.androidtemplateproject.logging

internal class NoopLogger(
    override val tag: String,
) : Logger {

    override fun d(throwable: Throwable?, tag: String, message: () -> String) {
        /* empty implementation */
    }

    override fun i(throwable: Throwable?, tag: String, message: () -> String) {
        /* empty implementation */
    }

    override fun w(throwable: Throwable?, tag: String, message: () -> String) {
        /* empty implementation */
    }

    override fun e(throwable: Throwable?, tag: String, message: () -> String) {
        /* empty implementation */
    }

    override fun a(throwable: Throwable?, tag: String, message: () -> String) {
        /* empty implementation */
    }

    override fun v(throwable: Throwable?, tag: String, message: () -> String) {
        /* empty implementation */
    }
}
