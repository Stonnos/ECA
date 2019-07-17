package eca.gui.tables;

import eca.gui.renderers.CheckboxRenderer;
import eca.gui.editors.CustomCellEditor;
import eca.gui.renderers.CustomCellRenderer;
import eca.gui.renderers.TextFieldRenderer;
import eca.gui.tables.models.EcaServiceOptionsTableModel;
import eca.gui.text.LengthDocument;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Eca - service properties table.
 *
 * @author Roman Batygin
 */
public class EcaServicePropertiesTable extends JDataTableBase {

    private static final int FIELD_LENGTH = 15;
    private static final int MAX_FIELD_LENGTH = 255;
    private static final int ENABLED_ROW = 0;

    public EcaServicePropertiesTable(EcaServiceOptionsTableModel model) {
        super(model);
        this.initialize();
        this.setAutoResizeOff(false);
    }

    private void initialize() {
        TableColumn column = this.getColumnModel().getColumn(1);
        //Sets editors
        JTextField text = new JTextField(FIELD_LENGTH);
        text.setDocument(new LengthDocument(MAX_FIELD_LENGTH));
        CustomCellEditor customCellEditor = new CustomCellEditor(this, new DefaultCellEditor(text));
        customCellEditor.setEditorAt(ENABLED_ROW, new DefaultCellEditor(new JCheckBox()));
        column.setCellEditor(customCellEditor);
        //Sets renderer
        CustomCellRenderer customCellRenderer = new CustomCellRenderer(new TextFieldRenderer());
        customCellRenderer.setRendererAt(ENABLED_ROW, new CheckboxRenderer());
        column.setCellRenderer(customCellRenderer);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent evt) {
                Point p = new Point(evt.getX(), evt.getY());
                int i = rowAtPoint(p);
                int j = columnAtPoint(p);
                changeSelection(i, j, false, false);
            }
        });
    }
}
