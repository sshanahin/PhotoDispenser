package home.sshanahin.photo.dispenser.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class JFilePicker extends JPanel {

    private JTextField textField;
    private JFileChooser fileChooser;

    JFilePicker(String textFieldLabel, String buttonLabel) {

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Folders";
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        // creates the GUI
        JLabel label = new JLabel(textFieldLabel);

        textField = new JTextField(50);
        JButton button = new JButton(buttonLabel);

        button.addActionListener(this::buttonActionPerformed);

        add(label);
        add(textField);
        add(button);

    }

    private void buttonActionPerformed(ActionEvent evt) {
        if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    public String getSelectedFilePath() {
        return textField.getText();
    }

    public JTextField getTextField() {
        return textField;
    }

    public JFileChooser getFileChooser() {
        return this.fileChooser;
    }
}