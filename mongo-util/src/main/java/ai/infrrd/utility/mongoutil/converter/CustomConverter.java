package ai.infrrd.utility.mongoutil.converter;

import org.springframework.core.convert.converter.Converter;

import java.util.List;

public interface CustomConverter {
    /***
     * Returns a list of custom converters
     * Must be implemented by the application using the utils
     * @return
     */
    List<Converter<?, ?>> getCustomConverterList();
}
