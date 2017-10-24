package com.yhml.generate.doc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.yhml.generate.bean.DocTemplate;
import com.yhml.generate.util.DocType;

import static com.yhml.generate.util.DocType.*;


@State(name = "DocGenSetting", storages = {@Storage(id = "app-default", file = "$APP_CONFIG$/DocGen-settings.xml")})
public class DocGenSetting implements PersistentStateComponent<DocGenSetting> {

    private static final Logger LOGGER = Logger.getInstance(DocGenSetting.class);

    private Map<String, DocTemplate> docTemplateMap;

    public DocGenSetting() {
    }

    private void loadDefaultSettings() {
        try {
            this.docTemplateMap = new HashMap<>();
            for (DocType type : DocType.values()) {
                docTemplateMap.put(type.getName(), createTemplate(type.getName()));
            }
        } catch (Exception e) {
            LOGGER.error("loadDefaultSettings failed", e);
        }
    }

    @NotNull
    private DocTemplate createTemplate(String templateName)
            throws IOException {
        String velocityTemplate = FileUtil.loadTextAndClose(getClass().getResourceAsStream("/template/" + templateName + ".vm"));
        return DocTemplate.build(templateName, velocityTemplate);
    }

    public Map<String, DocTemplate> getDocTemplateMap() {
        if (docTemplateMap == null) {
            loadDefaultSettings();
        }
        return docTemplateMap;
    }

    @Nullable
    @Override
    public DocGenSetting getState() {
        if (this.docTemplateMap == null) {
            loadDefaultSettings();
        }

        return this;
    }

    @Override
    public void loadState(DocGenSetting setting) {
        XmlSerializerUtil.copyBean(setting, this);
    }

    public DocTemplate getTemplate(String template) {
        return docTemplateMap.get(template);
    }

    public void removeCodeTemplate(String template) {
        docTemplateMap.remove(template);
    }

}
