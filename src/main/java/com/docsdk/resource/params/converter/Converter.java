package com.docsdk.resource.params.converter;

public interface Converter<S, T> {

    T convert(final S source);
}
