package ru.tipame.watchdog.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * Created by tipame on 28.10.2017.
 */
public class UnixTimeDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        return new Date(Long.parseLong(parser.getText()) * 1000);
    }
}
