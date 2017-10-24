package com.yhml.generate.doc.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.yhml.generate.util.VelocityUtil;

import static com.yhml.generate.util.DocGenerateUtil.*;

/**
 * @author: Jianfeng.Hu
 * @date: 2017/10/24
 */
public class MethodOverrideGenAction extends AnAction implements DumbAware {

    private static final Logger LOGGER = Logger.getInstance(MethodOverrideGenAction.class);

    private String overrideTamplate;

    public MethodOverrideGenAction() {
        super();
        try {
            this.overrideTamplate = FileUtil.loadTextAndClose(getClass().getResourceAsStream("/template/method-override.vm"));
        } catch (IOException e) {
            LOGGER.error("load See.vm failed", e);
        }

    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        DumbService dumbService = DumbService.getInstance(project);

        if (dumbService.isDumb()) {
            dumbService.showDumbModeNotification("JavaDocGen plugin is not " + "available during indexing");
            return;
        }

        PsiFile javaFile = e.getData(CommonDataKeys.PSI_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (javaFile == null || editor == null) {
            return;
        }

        List<PsiClass> classes = getClasses(javaFile);

        PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);

        for (PsiClass psiClass : classes) {
            for (PsiMethod psiMethod : PsiTreeUtil.getChildrenOfTypeAsList(psiClass, PsiMethod.class)) {
                createDocForMethod(psiMethod, psiElementFactory);
            }
        }
    }

    private void createDocForMethod(PsiMethod psiMethod, PsiElementFactory psiElementFactory) {
        try {
            checkFilesAccess(psiMethod);

            PsiDocComment psiDocComment = psiMethod.getDocComment();

            //return if the method has comment
            if (psiDocComment != null) {
                return;
            }

            String interfaceName = findClassNameOfSuperMethod(psiMethod);
            if (interfaceName == null) {
                return;
            }

            String methodName = psiMethod.getName();

            Map<String, Object> map = Maps.newHashMap();
            map.put("interface", interfaceName);
            map.put("method", methodName);
            map.put("paramsType", generateMethodParamsType(psiMethod));

            String seeDoc = VelocityUtil.evaluate(overrideTamplate, map);

            PsiDocComment seeDocComment = psiElementFactory.createDocCommentFromText(seeDoc);

            WriteCommandAction writeCommandAction = new DocWriteAction(psiMethod.getProject(), seeDocComment, psiMethod,
                    psiMethod.getContainingFile());

            RunResult result = writeCommandAction.execute();

            if (result.hasException()) {
                LOGGER.error(result.getThrowable());
                Messages.showErrorDialog("plugin is not available, cause: " + result.getThrowable().getMessage(), "Plugin");
            }
        } catch (Exception e) {
            LOGGER.error("Create @see Doc failed", e);
        }
    }


    private String generateMethodParamsType(PsiMethod psiMethod) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(psiMethod.getParameterList().getParameters()).forEach(parameter -> sb.append(parameter.getType()
                .getCanonicalText()).append(", "));

        if (sb.toString().endsWith(", ")) {
            sb.delete(sb.length() - 2, sb.length() - 1);
        }
        return sb.toString();
    }

}
