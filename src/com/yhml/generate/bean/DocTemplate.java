package com.yhml.generate.bean;

import com.yhml.generate.util.DocType;

public class DocTemplate extends BaseTemplate {

    public static DocTemplate build(String templateName, String vmTamplate) {
        DocTemplate template = new DocTemplate();
        template.setTemplateName(templateName);
        template.setVmTemplate(vmTamplate);
        template.setText(DocType.get(templateName).getText());

        return template;
    }
}
