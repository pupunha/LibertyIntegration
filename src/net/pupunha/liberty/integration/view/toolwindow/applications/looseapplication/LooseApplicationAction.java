package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LooseApplicationAction extends AnAction {

    public LooseApplicationAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        try {
            LooseApplicationDialog dialog = new LooseApplicationDialog(project);
            dialog.setTitle("Create Loose Application EAR");
            dialog.show();


//            DialogBuilder builder = new DialogBuilder(project);
//            LooseApplicationPanel panel = new LooseApplicationPanel(project);
//
//            builder.setTitle("Create Loose Application EAR");
//            builder.setCenterPanel(panel);
//            builder.removeAllActions();

//            DialogWrapperDialog

//            builder.dispose();addCancelAction()dDisposable();
//            panel.getButtonCancel().addActionListener(e -> {
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>");
//                builder.addCancelAction();
//            });
//            panel.getButtonOK().addActionListener(e -> {
//
//            });

//            builder.show();


//            builder.get
//            builder.addCancelAction();
//            builder.addOkAction();
//            builder.showNotModal();
//            boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
//            if (isOk) {
//
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
        }
    }

}
