package com.mindtedtech.android_enigma.memory;

import com.mindtedtech.enigmamachine.utilities.WiringData;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageInfo implements Serializable
{
    public WiringData.enimgaVersionsEnum enigmaVersion;

    public ArrayList<String> plugboardConnections = new ArrayList<>();

    public String reflector;

    public ArrayList<String> rotors = new ArrayList<>();
    public ArrayList<String> rotorInitialPositions = new ArrayList<>();
    public ArrayList<String> rotorRingSettings = new ArrayList<>();


    public String plaintext;
    public String ciphertext;
}
