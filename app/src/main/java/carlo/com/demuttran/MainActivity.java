package carlo.com.demuttran;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        TextToSpeech.OnInitListener {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private EditText editText_speaktotext;
    private TextView textView_add;
    private Button button_translate;
    private ImageButton button_microphone, button_speak;
    private RecyclerView recyclerView;
    private LinearLayout linearLayout_quickaccessword;
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Integer> mImageUrls = new ArrayList<>();
    ProgressDialog dialog_loader;
    TextToSpeech textToSpeech;
    DBAdapter helper;
    final Context context = this;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        editText_speaktotext = findViewById(R.id.editText_speaktotext);
        textView_add = findViewById(R.id.textView_add);
        button_microphone = findViewById(R.id.button_microphone);
        button_speak = findViewById(R.id.button_speak);
        button_translate = findViewById(R.id.button_translate);
        dialog_loader = new ProgressDialog(MainActivity.this);
        textToSpeech = new TextToSpeech(this, this);
        linearLayout_quickaccessword = findViewById(R.id.linearLayout_quickaccessword);

        button_microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        button_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editText_speaktotext_trim = editText_speaktotext.getText().toString().trim();
                if(!editText_speaktotext_trim.matches("")) {
                    speakOut();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter text.", Toast.LENGTH_SHORT).show();
                    editText_speaktotext.setText("");
                }
            }
        });

        button_translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editText_speaktotext_trim = editText_speaktotext.getText().toString().trim();
                if(!editText_speaktotext_trim.matches("")) {
                    try {
                        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                        // Leave blank
                    }

                    mImageUrls.clear();
                    mNames.clear();

                    dialog_loader.setCanceledOnTouchOutside(false);
                    dialog_loader.setCancelable(false);
                    dialog_loader.setMessage("Loading, please wait...");
                    dialog_loader.show();

                    for (int i = 0; i < editText_speaktotext.length(); i++){
                        char c = editText_speaktotext.getText().toString().charAt(i);

                        String result = String.valueOf(c).toUpperCase();
                        if(result.equals(" ") || result.equals(".") || result.equals(",") || result.equals("?") || result.equals("!") || result.equals(":") ||
                                result.equals("-") || result.equals("&") || result.equals("*") || result.equals("@") || result.equals("\\") ||
                                result.equals("/") || result.equals("[") || result.equals("]") || result.equals("(") || result.equals(")")){
                            mImageUrls.add(R.drawable.ic_separator);
                            mNames.add("");
                        } else {
                            String[] array = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
                            int numberOfItems = array.length;
                            for (int i_array =0; i_array<numberOfItems; i_array++)
                            {
                                String name = array[i_array];
                                if(result.equals(name)){
                                    mImageUrls.add(getResources().getIdentifier("ic_" + name.toLowerCase(), "drawable", getPackageName()));
                                    mNames.add(name);
                                }
                            }
                        }
                    }

                    initRecyclerView();

                    Runnable run = new Runnable() {
                        public void run() {
                            dialog_loader.dismiss();
                        }
                    };
                    Handler myHandler = new Handler(Looper.myLooper());
                    myHandler.postDelayed(run, 2000);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter text.", Toast.LENGTH_SHORT).show();
                    editText_speaktotext.setText("");
                }
            }
        });

        helper = new DBAdapter(getApplicationContext());
        AddData();
        RetreiveData();
    }

    public void AddData() {
        textView_add.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Add Quick Access Word.");

                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean isInserted = helper.insertData(input.getText().toString());
                            if(isInserted == true) {
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                TextView textView = new TextView(getApplicationContext());
                                layoutParams.setMargins(15,0,15,10);
                                textView.setLayoutParams(layoutParams);
                                final int sdk = android.os.Build.VERSION.SDK_INT;
                                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.activity_rounded));
                                } else {
                                    textView.setBackground(getResources().getDrawable(R.drawable.activity_rounded));
                                }
                                textView.setText(input.getText().toString());
                                final String get_name = input.getText().toString();
                                textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                textView.setPadding(30, 2, 30, 2);
                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        editText_speaktotext.setText(get_name);
                                        editText_speaktotext.setSelection(editText_speaktotext.getText().length());
                                    }
                                });
                                linearLayout_quickaccessword.addView(textView);
                            } else {
                                Toast.makeText(getApplicationContext(),"Word not inserted.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void RetreiveData() {
        final Cursor res = helper.getAllData();
        if(res.getCount() == 0) {
            helper.insertData("Hi");
            helper.insertData("Hello");
            helper.insertData("Good morning");
            helper.insertData("Good afternoon");
            helper.insertData("Good evening");
            helper.insertData("Good night");

            RetreiveData();
        } else {
            while (res.moveToNext()) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView textView = new TextView(getApplicationContext());
                layoutParams.setMargins(15,0,15,10);
                textView.setLayoutParams(layoutParams);
                final int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.activity_rounded));
                } else {
                    textView.setBackground(getResources().getDrawable(R.drawable.activity_rounded));
                }
                textView.setText(res.getString(1));
                final String get_name = res.getString(1);
                textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                textView.setPadding(30, 2, 30, 2);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText_speaktotext.setText(get_name);
                        editText_speaktotext.setSelection(editText_speaktotext.getText().length());
                    }
                });
                linearLayout_quickaccessword.addView(textView);
            }
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Go ahead, I'm listening...");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            // Leave blank
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getApplicationContext(), "Language is not supported on this device.", Toast.LENGTH_SHORT).show();
            } else {
                speakOut();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Language is not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void speakOut() {
        textToSpeech.speak(editText_speaktotext.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    editText_speaktotext.setText(result.get(0));
                }
                break;
            }
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, mImageUrls);
        recyclerView.setAdapter(adapter);
    }
}