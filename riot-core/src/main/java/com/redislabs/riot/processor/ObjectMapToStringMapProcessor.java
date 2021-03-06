package com.redislabs.riot.processor;

import com.redislabs.riot.convert.ConversionServiceConverter;
import lombok.Builder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.Map;

public class ObjectMapToStringMapProcessor<K, V> implements ItemProcessor<Map<K, Object>, Map<K, V>> {

    private final Converter<Object, V> objectToStringConverter;

    public ObjectMapToStringMapProcessor(Converter<Object, V> objectToStringConverter) {
        this.objectToStringConverter = objectToStringConverter;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public Map<K, V> process(Map<K, Object> item) {
        item.replaceAll((k, v) -> objectToStringConverter.convert(item.get(k)));
        return (Map) item;
    }

    @Builder
    private static ObjectMapToStringMapProcessor<String, String> build() {
        return new ObjectMapToStringMapProcessor<>(new ConversionServiceConverter<>(new DefaultConversionService(), String.class));
    }
}
