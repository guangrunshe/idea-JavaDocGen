package com.yhml.generate.doc.action;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.DateFormatUtil;
import com.yhml.generate.bean.DocTemplate;
import com.yhml.generate.doc.DocGenSetting;
import com.yhml.generate.util.DocConstant;
import com.yhml.generate.util.VelocityUtil;

import static com.yhml.generate.util.DocConstant.plugin_name;
import static com.yhml.generate.util.DocGenerateUtil.*;

/**
 *
 */
public class DocGenAction extends AnAction implements DumbAware {

    private static final Logger logger = Logger.getInstance(DocGenAction.class);

    private DocGenSetting settings;

    private String templateName;

    private DocTemplate template;

    DocGenAction(String templateName) {
        this.settings = ServiceManager.getService(DocGenSetting.class);
        this.templateName = templateName;
        this.template = settings.getTemplate(templateName);
        getTemplatePresentation().setDescription("");
        getTemplatePresentation().setText(template.getText(), false);
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        DumbService dumbService = DumbService.getInstance(project);

        if (dumbService.isDumb()) {
            dumbService.showDumbModeNotification("JavaDocGen plugin is not " + "available during indexing");
            return;
        }


        PsiFile javaFile = event.getData(CommonDataKeys.PSI_FILE);
        Editor editor = event.getData(CommonDataKeys.EDITOR);

        if (javaFile == null || editor == null) {
            return;
        }

        PsiClass classes = getSingleClass(javaFile);

        PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);

        PsiTreeUtil.getChildrenOfTypeAsList(classes, PsiMethod.class).forEach(method -> create(classes, method,
                psiElementFactory));

        // classes.forEach(psiClass -> PsiTreeUtil.getChildrenOfTypeAsList(psiClass, PsiMethod.class).forEach(method -> create
        //         (method, psiElementFactory)));

        // for (PsiClass psiClass : classes) {
        //     for (PsiMethod psiMethod : PsiTreeUtil.getChildrenOfTypeAsList(psiClass, PsiMethod.class)) {
        //         createDoc(psiMethod, psiElementFactory);
        //     }
        // }

    }

    private void create(PsiClass psiClass, PsiMethod psiMethod, PsiElementFactory psiElementFactory) {
        String methodName = psiMethod.getName();

        // 过滤 main 方法
        if (StringUtils.equals(methodName, "main")) {
            return;
        }

        switch (templateName) {
            case DocConstant.method:
                createDocForMethod(psiMethod, psiElementFactory);
                break;
            case DocConstant.override:
                createDocForOverride(psiMethod, psiElementFactory);
                break;
            case DocConstant.clazz:
                createDocForClass(psiClass, psiElementFactory);
                break;
        }
    }

    private void createDocForMethod(PsiMethod psiMethod, PsiElementFactory psiElementFactory) {
        try {
            checkFilesAccess(psiMethod);

            PsiDocComment psiDocComment = psiMethod.getDocComment();

            // return if the method has comment
            if (psiDocComment != null) {
                return;
            }

            Map<String, Object> map = Maps.newHashMap();

            map.put("params", getMethodParams(psiMethod));
            map.put("throws", getThrowsList(psiMethod));
            map.put("return", psiMethod.getReturnTypeElement().getText());
            map.put("date", DateFormatUtil.formatDate(new Date()));
            // map.put("returnType", psiMethod.getReturnType().getCanonicalText());

            createDoc(psiMethod, psiElementFactory, map);

        } catch (Exception e) {
            logger.error("Create @see Doc failed", e);
        }
    }

    private void createDocForOverride(PsiMethod psiMethod, PsiElementFactory psiElementFactory) {
        try {
            checkFilesAccess(psiMethod);

            PsiDocComment psiDocComment = psiMethod.getDocComment();

            // return if the method has comment
            if (psiDocComment != null) {
                return;
            }

            String interfaceName = findClassNameOfSuperMethod(psiMethod);
            if (interfaceName == null) {
                return;
            }

            Map<String, Object> map = Maps.newHashMap();
            map.put("interface", interfaceName);
            map.put("method", psiMethod.getName());
            map.put("paramsType", getMethodParamsType(psiMethod));

            createDoc(psiMethod, psiElementFactory, map);

        } catch (Exception e) {
            logger.error("Create @see Doc failed", e);
        }
    }

    private void createDoc(PsiElement psiMethod, PsiElementFactory psiElementFactory, Map<String, Object> map) {
        String docString = VelocityUtil.evaluate(template.getVmTemplate(), map);
        PsiDocComment docComment = psiElementFactory.createDocCommentFromText(docString);

        WriteCommandAction writeCommandAction = new DocWriteAction(psiMethod.getProject(), docComment, psiMethod,
                psiMethod.getContainingFile());

        RunResult result = writeCommandAction.execute();

        if (result.hasException()) {
            logger.error(result.getThrowable());
            Messages.showErrorDialog("plugin is not available, cause: " + result.getThrowable().getMessage(), plugin_name);
        }
    }

    private void createDocForClass(PsiClass psiClass, PsiElementFactory psiElementFactory) {
        try {
            checkFilesAccess(psiClass);

            PsiDocComment psiDocComment = psiClass.getDocComment();

            // return if the method has comment
            if (psiDocComment != null) {
                return;
            }

            Map<String, Object> map = Maps.newHashMap();
            map.put("date", DateFormatUtil.formatDate(new Date()));

            createDoc(psiClass, psiElementFactory, map);

        } catch (Exception e) {
            logger.error("Create @see Doc failed", e);
        }
    }

}
