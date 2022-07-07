package ai.infrrd.utility.mongoutil.converter;

import org.bson.types.Decimal128;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.math.BigDecimal;

@WritingConverter
public class BigDecimalDecimal128Converter implements Converter<BigDecimal, Decimal128> {
    @Override
    public Decimal128 convert(BigDecimal source) {
        return new Decimal128(source);
    }
}
