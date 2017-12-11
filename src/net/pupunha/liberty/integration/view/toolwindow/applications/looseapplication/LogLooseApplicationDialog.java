package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class LogLooseApplicationDialog extends DialogWrapper {

    private Project project;
    private LooseApplicationParameter parameter;
    private JTextPane textPane = new JTextPane();

    public LogLooseApplicationDialog(@Nullable Project project, LooseApplicationParameter parameter) {
        super(project, true, IdeModalityType.PROJECT);
        this.project = project;
        this.parameter = parameter;
        this.textPane.setEditable(false);
        this.textPane.setFont(UIManager.getFont("Label.font"));

        this.init();

        SwingWorker worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    LooseApplicationGenerate generate = new LooseApplicationGenerate(project, textPane);
                    generate.createLooseApplication(parameter.getProjectEAR(), parameter.getModulesInPackage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
        worker.execute();

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new MigLayout("", "[]", "[][][]"));
        panel.setPreferredSize(new Dimension(700, 400));
        JScrollPane logScrollPane = new JBScrollPane(textPane);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(new JLabel("View Log:"), "grow, wrap");
        panel.add(logScrollPane, "grow, push, wrap");

        return panel;
    }

    public void insertText(String text) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            doc.insertString(doc.getLength(), text.concat("\n"), null);
        } catch (BadLocationException e) {
            Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
        }
    }

}
