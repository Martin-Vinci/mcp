package com.greybox.mediums.models;

import freemarker.template.Template;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
@AllArgsConstructor
public class EmailData {
    private String from;
    private String to;
    private String name;
    private String subject;
    private String content;
    private Template template;
    private Map<String, String> model;
}
