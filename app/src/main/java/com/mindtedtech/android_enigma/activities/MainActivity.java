package com.mindtedtech.android_enigma.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.mindtedtech.android_enigma.R;
import com.mindtedtech.android_enigma.memory.MemoryBank;
import com.mindtedtech.android_enigma.memory.MessageInfo;
import com.mindtedtech.android_enigma.model.ListIDs;
import com.mindtedtech.enigmamachine.interfaces.MachineModel;
import com.mindtedtech.enigmamachine.utilities.WiringData;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static com.mindtedtech.android_enigma.lib.Utils.showInfoDialog;

public class MainActivity extends AppCompatActivity
{
    private static WiringData.enimgaVersionsEnum enigmaVersion;
    private MemoryBank memoryBank;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupSpinners(WiringData.enimgaVersionsEnum.ENIGMA_1);
        memoryBank = new MemoryBank(MainActivity.this);

        setupFAB();
    }

    private void setupFAB() {
        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> FABClickAction());
    }

    // testing the input and output
    private void FABClickAction(){
        /*
        EditText inputText = findViewById(R.id.input_text);
        String saveText = inputText.getText().toString();
        String scrambled = scrambleWord(saveText);
        TextView outputText = (TextView) findViewById(R.id.output_text);
        outputText.setText(scrambled);
         */
        getEncryptedMessage();
    }

    // Simple method as part of just quickly scrambling the input text and displaying the output
    private String scrambleWord(String word){
        ArrayList<Character> chars = new ArrayList<Character>(word.length());
        for (char c : word.toCharArray()){
            chars.add(c);
        }
        Collections.shuffle(chars);
        char[] scrambled = new char[chars.size()];
        for (int i = 0; i < scrambled.length; i++){
            scrambled[i] = chars.get(i);
        }
        return new String(scrambled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.action_previous_options: {
                showPreviousOptions();
                return true;
            }
            case R.id.enigma_I: {
                toggleMenuItem(item);
                setEnigmaVersion("Enigma1");
                setupSpinners(WiringData.enimgaVersionsEnum.ENIGMA_1);
                return true;
            }
            case R.id.enigma_m3: {
                toggleMenuItem(item);
                setEnigmaVersion("EnigmaM3");
                setupSpinners(WiringData.enimgaVersionsEnum.ENIGMA_M3);
                return true;
            }
            case R.id.action_settings: {
                showSettings();
                return true;
            }
            case R.id.action_about: {
                showAbout();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPreviousOptions(){

    }
    private void setEnigmaVersion(String version){

    }
    private void showSettings(){

    }
    private void showAbout(){
        String msg = "The Enigma Machine is used for encryption.\n First developed in the the early " +
                "to mid 20th century, it was used by the Germans to send secure communication and " +
                "gave them a huge advantage during WWII.\n\n This app simulates the machine used by the " +
                "Germans";
        showInfoDialog(MainActivity.this, "About the Enigma Machine",
                msg);
    }

    private void toggleMenuItem(MenuItem item) {
        item.setChecked(!item.isChecked());
    }



    public void setupSpinners(WiringData.enimgaVersionsEnum version)
    {
        this.enigmaVersion = version;

        // create reflector
        setupCustomSpinner(ListIDs.reflectorID, WiringData.getReflectorChoices(version));

        // create rotors
        for (int id : ListIDs.rotorIDs) {
            setupCustomSpinner(id, WiringData.getRotorChoices(version));
        }

        // create rotor initial positions
        for (int id : ListIDs.rotorInitialPositionIDs) {
            setupCustomSpinner(id, ListIDs.ALPHABET);
        }

        for (int id : ListIDs.rotorRingSettingIDs) {
            setupCustomSpinner(id, ListIDs.ALPHABET);
        }
    }

    public void setupCustomSpinner(int spinnerID, String[] contents)
    {
        // find the spinner from the xml
        Spinner spinner = findViewById(spinnerID);
        // create an adapter to describe how the items are displayed
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_item, contents);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        adapter.notifyDataSetChanged();
        // set the spinners adapter to the previously created one
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    // ---------------------------------------------------------------------------------------------
    public void getEncryptedMessage()
    {
        AttemptToBuildMachine attempt = new AttemptToBuildMachine(this);

        if (!attempt.isValidConfiguration()) // invalid configuration for machine
            showInfoDialog(MainActivity.this, "Error", attempt.getErrorMessage());
        else {
            // if successful
            MachineModel model = attempt.buildMachine();

            String inputText = ((EditText) findViewById(R.id.input_text)).getText().toString();
            String cipherText = typeText(model, inputText);

            TextView outputText = (TextView) findViewById(R.id.output_text);
            outputText.setText(cipherText);

            // save message info
            MessageInfo mi = attempt.getMachineMessageSettings();
            mi.plaintext = inputText;
            mi.ciphertext = cipherText;
            // save message to memory banks
            memoryBank.addMessage(mi);
        }
    }

    public String typeText(MachineModel model, String plaintext)
    {
        StringBuilder sb = new StringBuilder();

        plaintext = plaintext.toUpperCase();
        for (char letter : plaintext.toCharArray())
            if (Character.isLetter(letter))
                sb.append(model.type(letter));

        return sb.toString();
    }

    public static WiringData.enimgaVersionsEnum getEnigmaVersion()
    {
        return enigmaVersion;
    }
}