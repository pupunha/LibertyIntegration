package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public class LogLooseApplicationPanel extends JPanel {

    private Project project;
    private Parameter parameter;
    private JTextPane textPane = new JTextPane();

    public LogLooseApplicationPanel(Project project, Parameter parameter) {
        super(new MigLayout("", "[]", "[][][]"));
        this.project = project;
        this.parameter = parameter;
        this.textPane = new JTextPane();
        textPane.setEditable(false);

        JScrollPane logScrollPane = new JBScrollPane(textPane);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(new JLabel("View Log:"), "grow, wrap");
        add(logScrollPane, "grow, push, wrap");
    }

    public void insertText(String text) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            doc.insertString(doc.getLength(), text.concat("\n"), null);
        } catch (BadLocationException e) {
            Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
        }
    }

    public static void main(String[] a) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new LogLooseApplicationPanel(null, null));
        frame.setSize(700,300);
        frame.setVisible(true);
    }
}
