package net.pupunha.liberty.integration.util;

import javax.swing.*;

public class TableUtil {

    public static void hideTableColumn(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
    }

}
