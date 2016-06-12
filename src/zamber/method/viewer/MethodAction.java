package zamber.method.viewer;

import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.AbstractPopup;
import com.intellij.util.ArrayUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MethodAction extends AnAction {

    protected EditorImpl editor;
    protected AbstractPopup popup;

    public void actionPerformed(AnActionEvent e) {
        if (e == null) {
            showErrorPopUp();
            return;
        }

        EditorImpl preEditor = (EditorImpl) e.getData(PlatformDataKeys.EDITOR);
        if (preEditor == null){
            return;
        }

        editor = preEditor;

        final DataContext parentContext= DataManager.getInstance().getDataContext(editor.getComponent());

        PsiClass psiClass = getPsiClassFromEvent(e);

        if (psiClass == null) {
            showNoResultPopUp(parentContext);
            return;
        }

        PsiMethod[] allFields = psiClass.getMethods();
        PsiMethod[] fields = new PsiMethod[allFields.length];

        int i = 0;
        List<String> methodList = new ArrayList<String>();

        for (PsiMethod field : allFields) {
            if (!field.hasModifierProperty(PsiModifier.STATIC)) {
                fields[i++] = field;
                methodList.add(field.getName());
            }
        }

        showMethodsPopUp(methodList, fields, parentContext);

    }

    private void showNoResultPopUp(DataContext dc) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder("No Methods in Selected Zone",
                        null,
                        new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                        .setFadeoutTime(5000).createBalloon()
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }

    private void showErrorPopUp() {
        Notifications.Bus.notify(
                new Notification("warning", "MethodViewer Warning!", "NO More Method in There", NotificationType.WARNING)
        );

    }

    private void showMethodsPopUp(final List<String> methodsName,final PsiMethod[] continuation,DataContext dc){
        final JList list=new JBList();
        list.setListData(ArrayUtil.toObjectArray(methodsName));

        if (popup != null) {
            popup.dispose();
        }
        popup = (AbstractPopup)JBPopupFactory.getInstance().createListPopupBuilder(list).setTitle("methods").setResizable(true).
                setItemChoosenCallback(new Runnable(){
                    public void run(){
                        if (list.getSelectedIndices().length > 0) {
                            PsiMethod selectmethod = continuation[list.getSelectedIndex()];

                            editor.getCaretModel().moveToOffset(selectmethod.getTextRange().getStartOffset());
                            editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                        }
                    }
                }
        ).createPopup();

        popup.showInBestPositionFor(dc);
    }

    private PsiClass getPsiClassFromEvent(AnActionEvent event) {
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        if (psiFile == null || editor == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);

        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

}
