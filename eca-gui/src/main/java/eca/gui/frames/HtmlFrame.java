package eca.gui.frames;

import eca.gui.ButtonUtils;
import eca.gui.choosers.HtmlChooser;
import eca.gui.logging.LoggerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Frame showing text information.
 *
 * @author Roman Batygin
 */
@Slf4j
public class HtmlFrame extends JFrame {

    private static final String FILE_MENU_TEXT = "Файл";
    private static final String SAVE_MENU_TEXT = "Сохранить";
    private static final Dimension OPTIONS_PANE_PREFERRED_SIZE = new Dimension(475, 200);
    private static final String CONTENT_TYPE = "text/html";
    private static final String UTF_8 = "UTF-8";
    private JTextPane inputOptionsPane = new JTextPane();

    public HtmlFrame(String title, String text, JFrame parent) {
        this.setLayout(new GridBagLayout());
        this.setTitle(title);
        try {
            this.setIconImage(parent.getIconImage());
        } catch (Exception e) {
            LoggerUtils.error(log, e);
        }
        inputOptionsPane.setContentType(CONTENT_TYPE);
        inputOptionsPane.setEditable(false);
        inputOptionsPane.setPreferredSize(OPTIONS_PANE_PREFERRED_SIZE);
        JScrollPane scrollPanel = new JScrollPane(inputOptionsPane);
        JButton okButton = ButtonUtils.createOkButton();

        okButton.addActionListener(e -> setVisible(false));
        //----------------------------------------
        this.add(scrollPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(okButton, new GridBagConstraints(0, 1, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(4, 0, 4, 0), 0, 0));
        //---------------------------------------
        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu(FILE_MENU_TEXT);
        JMenuItem saveMenu = new JMenuItem(SAVE_MENU_TEXT);
        fileMenu.add(saveMenu);
        menu.add(fileMenu);
        //--------------------------------------------
        saveMenu.addActionListener(new ActionListener() {

            HtmlChooser fileChooser;

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (fileChooser == null) {
                        fileChooser = new HtmlChooser();
                    }
                    File file = fileChooser.getSelectedFile(HtmlFrame.this);
                    if (file != null) {
                        FileUtils.write(file, inputOptionsPane.getText(), UTF_8);
                    }
                } catch (Exception e) {
                    LoggerUtils.error(log, e);
                    JOptionPane.showMessageDialog(HtmlFrame.this, e.getMessage(),
                            null, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        this.setJMenuBar(menu);
        inputOptionsPane.setText(text);
        inputOptionsPane.setCaretPosition(0);
        this.getRootPane().setDefaultButton(okButton);
        this.pack();
        this.setLocationRelativeTo(parent);
    }

}