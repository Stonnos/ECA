package eca.gui.dialogs;

import eca.config.EcaServiceProperties;
import eca.gui.ButtonUtils;
import eca.gui.tables.EcaServicePropertiesTable;
import eca.gui.tables.models.EcaServiceOptionsTableModel;
import eca.util.Entry;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

/**
 * @author Roman Batygin
 */
public class EcaServiceOptionsDialog extends JDialog {

    private static final EcaServiceProperties PROPERTIES = EcaServiceProperties.getInstance();

    private static final String TITLE_TEXT = "Настройки сервиса ECA";
    private static final String EMPTY_PROPERTY_ERROR_FORMAT = "Укажите значение свойства '%s'";
    private static final String INVALID_PROPERTY_ERROR_FORMAT = "Недопустимое значение свойства '%s'";
    private static final Dimension SCROLL_PANE_PREFERRED_SIZE = new Dimension(500, 150);

    private final EcaServiceOptionsTableModel model = new EcaServiceOptionsTableModel();

    public EcaServiceOptionsDialog(Window parent) {
        super(parent, TITLE_TEXT);
        this.setLayout(new GridBagLayout());
        this.setResizable(false);

        JScrollPane scrollPanel = new JScrollPane(new EcaServicePropertiesTable(model));
        scrollPanel.setPreferredSize(SCROLL_PANE_PREFERRED_SIZE);

        JButton okButton = ButtonUtils.createOkButton();

        JButton cancelButton = ButtonUtils.createCancelButton();

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    for (Iterator<Entry> iterator = model.getOptions(); iterator.hasNext(); ) {
                        Entry entry = iterator.next();
                        if (StringUtils.isEmpty(entry.getValue())) {
                            throw new Exception(String.format(EMPTY_PROPERTY_ERROR_FORMAT, entry.getKey()));
                        }

                        if (entry.getKey().equals(EcaServiceProperties.ECA_SERVICE_ENABLED)) {
                            if (!entry.getValue().equalsIgnoreCase("false") &&
                                    !entry.getValue().equalsIgnoreCase("true")) {
                                throw new Exception(String.format(INVALID_PROPERTY_ERROR_FORMAT, entry.getKey()));
                            }
                        }

                        PROPERTIES.put(entry.getKey(), entry.getValue());
                    }
                    PROPERTIES.save();
                    setVisible(false);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(EcaServiceOptionsDialog.this,
                            e.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        });

        this.add(scrollPanel, new GridBagConstraints(0, 0, 2, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(5, 5, 10, 5), 0, 0));
        this.add(okButton, new GridBagConstraints(0, 1, 1, 1, 1, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(4, 0, 4, 3), 0, 0));
        this.add(cancelButton, new GridBagConstraints(1, 1, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(4, 3, 4, 0), 0, 0));

        this.getRootPane().setDefaultButton(okButton);
        this.pack();
        this.setLocationRelativeTo(parent);
    }

}
