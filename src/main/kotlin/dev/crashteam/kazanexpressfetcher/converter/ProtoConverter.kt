package dev.crashteam.kazanexpressfetcher.converter

import org.springframework.core.convert.converter.Converter

interface ProtoConverter<S, T> : Converter<S, T>
