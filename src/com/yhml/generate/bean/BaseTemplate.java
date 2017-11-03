package com.yhml.generate.bean;

import com.yhml.generate.util.DocConstant;

/**
 * @author: Jianfeng.Hu
 * @date: 2017/10/24
 */
public class BaseTemplate {
    /**
     * 模板名称
     */
    private String templateName;

    /**
     * vm 模板
     */
    private String vmTemplate;

    /**
     * action text
     */
    private String text;

    private String fileEncoding = DocConstant.UTF_8;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getVmTemplate() {
        return vmTemplate;
    }

    public void setVmTemplate(String vmTemplate) {
        this.vmTemplate = vmTemplate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }


}
