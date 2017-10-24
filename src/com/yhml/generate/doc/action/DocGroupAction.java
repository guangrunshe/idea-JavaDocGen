package com.yhml.generate.doc.action;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.yhml.generate.doc.DocGenSetting;

/**
 * @author: Jianfeng.Hu
 * @date: 2017/10/24
 */
public class DocGroupAction extends ActionGroup implements DumbAware {

    private DocGenSetting settings;

    public DocGroupAction() {
        super();
        settings = ServiceManager.getService(DocGenSetting.class);
        // settings = new DocGenSetting();
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent event) {
        if (event == null) {
            return AnAction.EMPTY_ARRAY;
        }

        Project project = PlatformDataKeys.PROJECT.getData(event.getDataContext());
        if (project == null) {
            return AnAction.EMPTY_ARRAY;
        }

        final List<AnAction> children = new ArrayList<>();
        settings.getDocTemplateMap().forEach((key, value) -> children.add(getOrCreateAction(key)));

        return children.toArray(new AnAction[]{});
    }

    private AnAction getOrCreateAction(String templateName) {
        final String actionId = "JavaDocGen.Menu.Action." + templateName;
        AnAction action = ActionManager.getInstance().getAction(actionId);
        if (action == null) {
            action = new DocGenAction(templateName);
            ActionManager.getInstance().registerAction(actionId, action);
        }
        return action;
    }
}
