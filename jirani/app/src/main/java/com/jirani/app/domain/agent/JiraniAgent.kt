package com.jirani.app.domain.agent

interface JiraniAgent<Input, Output> {
    val name: String

    fun process(input: Input): Output
}
