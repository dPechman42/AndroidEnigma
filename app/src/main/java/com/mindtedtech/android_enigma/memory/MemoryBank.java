package com.mindtedtech.android_enigma.memory;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class MemoryBank
{
    private Context context;
    private String fileDirectory;
    private String fileName = "SavedMessages.ser";

    public LinkedList<MessageInfo> sentMessages = new LinkedList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    public MemoryBank(Context c)
    {
        context = c;
        fileDirectory = context.getFilesDir().toString();

        acquireFile();
    }

    public void addMessage(MessageInfo message) // add latest messages to beginning
    {
        sentMessages.add(0, message);
        writeFile();
    }

    /* ----------------------------------------------------------------------------
    File Operations
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void acquireFile()
    {
        Path totalPath = Paths.get(fileDirectory + fileName);

        if (Files.exists(totalPath))
            readFile();
        else
            writeFile();
    }

    private void readFile()
    {
        FileInputStream fis = null;
        ObjectInputStream in = null;

        try {
            fis = new FileInputStream(fileDirectory + fileName);
            in = new ObjectInputStream(fis);
            sentMessages = (LinkedList<MessageInfo>) in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading from file.");
            e.printStackTrace();
        }
    }

    private void writeFile()
    {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;

        try {
            fos = new FileOutputStream(fileDirectory + fileName, false); // overwrite the file
            out = new ObjectOutputStream(fos);
            out.writeObject(sentMessages);
            out.close();
        } catch (IOException e) {
            System.out.println("Error writing to file.");
            e.printStackTrace();
        }
    }
}
