package home.sshanahin.photo.dispenser;

import home.sshanahin.photo.dispenser.ui.SetupWindowManager;

public class Launcher {

    public static void main(String[] args) {
        System.out.println("Photo Dispenser starter!");

        SetupWindowManager manager = new SetupWindowManager();
        manager.initWindow();
    }
}
