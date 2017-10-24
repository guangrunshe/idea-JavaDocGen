package com.yhml.generate.util;

import java.util.Arrays;

/**
 * @author: Jianfeng.Hu
 * @date: 2017/10/24
 */
public enum DocType {

    _mehod(DocConstant.method, "Method doc"),
    _override(DocConstant.override, "Overide Method Doc"),
    _clazz(DocConstant.clazz, "Class Doc");
    // _file(DocConstant.file, "File Doc 开发中...");

    /**
     * vm tamplate name
     */
    private String name;

    /**
     * action text
     */
    private String text;

    DocType(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public static DocType get(String name) {
        return Arrays.stream(DocType.values()).filter(type -> type.name.equals(name)).findFirst().get();
    }

}
