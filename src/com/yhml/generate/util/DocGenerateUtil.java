package com.yhml.generate.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;

/**
 * @author: Jianfeng.Hu
 * @date: 2017/10/24
 */
public class DocGenerateUtil {
    private static final Logger LOGGER = Logger.getInstance(DocGenerateUtil.class);

    public static List<PsiClass> getClasses(PsiElement element) {
        List<PsiClass> elements = Lists.newArrayList();
        List<PsiClass> classElements = PsiTreeUtil.getChildrenOfTypeAsList(element, PsiClass.class);
        elements.addAll(classElements);

        for (PsiClass classElement : classElements) {
            elements.addAll(getClasses(classElement));
        }

        return elements;
    }

    public static PsiClass getSingleClass(PsiElement element) {
        List<PsiClass> classElements = PsiTreeUtil.getChildrenOfTypeAsList(element, PsiClass.class);
        return classElements.get(0);
    }

    public static String getMethodParamsType(PsiMethod psiMethod) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(psiMethod.getParameterList().getParameters()).forEach(parameter -> sb.append(parameter.getType()
                .getCanonicalText()).append(", "));

        if (sb.toString().endsWith(", ")) {
            sb.delete(sb.length() - 2, sb.length() - 1);
        }
        return sb.toString();
    }

    public static List<String> getMethodParams(PsiMethod psiMethod) {
        List<String> list = Lists.newArrayList();
        Arrays.stream(psiMethod.getParameterList().getParameters()).forEach(parameter -> list.add(parameter.getName()));
        return list;
    }

    public static List<String> getThrowsList(PsiMethod psiMethod) {
        List<String> list = Lists.newArrayList();
        PsiClassType[] types = psiMethod.getThrowsList().getReferencedTypes();
        Arrays.stream(types).forEach(type -> list.add(type.getClassName()));
        return list;
    }

    public static String findClassNameOfSuperMethod(PsiMethod psiMethod) {
        PsiMethod[] superMethods = psiMethod.findDeepestSuperMethods();
        if (superMethods.length == 0 || superMethods[0].getContainingClass() == null) {
            return null;
        }
        return superMethods[0].getContainingClass().getQualifiedName();
    }

    public static boolean checkFilesAccess(@NotNull PsiElement beforeElement) {
        PsiFile containingFile = beforeElement.getContainingFile();
        if (containingFile == null || !containingFile.isValid()) {
            throw new IllegalStateException("File cannot be used to generate javadocs");
        }

        ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(beforeElement.getProject()).
                ensureFilesWritable(Collections.singletonList(containingFile.getVirtualFile()));
        if (status.hasReadonlyFiles()) {
            LOGGER.info(status.getReadonlyFilesMessage());
            throw new IllegalStateException(status.getReadonlyFilesMessage());
        }

        return true;
    }
}
