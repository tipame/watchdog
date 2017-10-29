package ru.tipame.watchdog.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Created by tipame on 28.10.2017.
 */
public class PlatformDeserializer extends JsonDeserializer<Platform> {
    @Override
    public Platform deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        return Platform.valueOf(Integer.valueOf(parser.getText()));
    }
}
