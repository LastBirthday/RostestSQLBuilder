package com.home;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.*;

/**
 * Created by Dds on 27.05.2016.
 */
public class TableUtils {

    public static void installCopyPasteHandler(TableView<?> table) {
        table.setOnKeyPressed(new TableKeyEventHandler());
    }

    public static class TableKeyEventHandler implements EventHandler<KeyEvent> {

        KeyCodeCombination copyKeyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);

        @Override
        public void handle(KeyEvent event) {
            if (copyKeyCodeCombination.match(event)) {
                if (event.getSource() instanceof TableView) {
                    copySelectionToClipboard((TableView<?>) event.getSource());
                    System.out.println("Selection copied to clipboard");
                    event.consume();
                }
            }
        }
    }

    public static void copySelectionToClipboard (TableView<?> table) {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();
        int prevRow = -1;

        for (TablePosition position: positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = (Object) table.getColumns().get(col).getCellData(row);

            if (cell == null) {
                cell = "";
            }

            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');
            }

            String text = cell.toString();
            clipboardString.append(text);
            prevRow = row;
        }

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

}
