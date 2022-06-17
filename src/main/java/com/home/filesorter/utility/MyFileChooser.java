package com.home.filesorter.utility;
import javax.swing.*;
import java.awt.*;

public class MyFileChooser extends JFileChooser {
    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        ImageIcon imageIcon = new ImageIcon("src/main/resources/images/folder_icon.png");
        dialog.setIconImage(imageIcon.getImage());
        return dialog;
    }
}
