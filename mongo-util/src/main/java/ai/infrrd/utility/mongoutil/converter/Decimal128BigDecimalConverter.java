package ai.infrrd.utility.mongoutil.converter;

import org.bson.types.Decimal128;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

@ReadingConverter
public class Decimal128BigDecimalConverter implements Converter<Decimal128, BigDecimal> {
    @Override
    public BigDecimal convert(@NonNull Decimal128 source) {
        return source.bigDecimalValue();
    }
}