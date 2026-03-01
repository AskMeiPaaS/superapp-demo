package com.superapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Document(collection = "ui_components")
public record UIComponent(
    @Id String id,
    String tag,
    String type,
    Map<String, Object> props
) {}