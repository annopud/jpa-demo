package dev.annopud.jpa_demo.converter;

import dev.annopud.jpa_demo.entity.EmailStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class EmailStatusConverter implements AttributeConverter<EmailStatus, String> {

    @Override
    public String convertToDatabaseColumn(EmailStatus status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public EmailStatus convertToEntityAttribute(String code) {
        return code != null ? EmailStatus.fromCode(code) : null;
    }

//    @Override
//    public EmailStatus convertToEntityAttribute(String code) {
//        if (code == null) {
//            return null;
//        }
//
//        return Stream.of(EmailStatus.values())
//            .filter(c -> c.getCode().equals(code))
//            .findFirst()
//            .orElseThrow(IllegalArgumentException::new);
//    }
}