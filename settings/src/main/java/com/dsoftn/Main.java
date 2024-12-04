package com.dsoftn;

import java.io.File;

import com.dsoftn.Settings.gui.GuiMain;


public class Main {
    public static void main(String[] args) {
        System.setProperty("user.dir", new File(".").getAbsolutePath());

        GuiMain.launch(GuiMain.class, args);
    }

}