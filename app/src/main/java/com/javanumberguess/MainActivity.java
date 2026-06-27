package com.javanumberguess;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private NumberGuessGame game;

    private TextInputLayout textInputLayout;
    private TextInputEditText editTextGuess;
    private MaterialButton buttonGuess;
    private MaterialButton buttonNewGame;
    private MaterialCardView cardResult;
    private TextView textViewEmoji;
    private TextView textViewResult;
    private TextView textViewHint;
    private TextView textViewAttempts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        game = new NumberGuessGame();

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        textInputLayout  = findViewById(R.id.textInputLayout);
        editTextGuess    = findViewById(R.id.editTextGuess);
        buttonGuess      = findViewById(R.id.buttonGuess);
        buttonNewGame    = findViewById(R.id.buttonNewGame);
        cardResult       = findViewById(R.id.cardResult);
        textViewEmoji    = findViewById(R.id.textViewEmoji);
        textViewResult   = findViewById(R.id.textViewResult);
        textViewHint     = findViewById(R.id.textViewHint);
        textViewAttempts = findViewById(R.id.textViewAttempts);
    }

    private void setupListeners() {
        buttonGuess.setOnClickListener(v -> handleGuess());

        // キーボードの「完了」ボタンでも予想できるように
        editTextGuess.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleGuess();
                return true;
            }
            return false;
        });

        buttonNewGame.setOnClickListener(v -> resetGame());
    }

    private void handleGuess() {
        String input = editTextGuess.getText() != null
                ? editTextGuess.getText().toString()
                : "";

        NumberGuessGame.GuessResult guessResult = game.guess(input);

        switch (guessResult.getResult()) {
            case INVALID:
                textInputLayout.setError("0〜100 の整数を入力してください");
                return;

            case CORRECT:
                textInputLayout.setError(null);
                showResult(
                    "🎉",
                    "正解！",
                    guessResult.getAttempts() + " 回で正解しました",
                    0xFF4CAF50  // green
                );
                buttonGuess.setEnabled(false);
                editTextGuess.setEnabled(false);
                break;

            case TOO_HIGH:
                textInputLayout.setError(null);
                showResult(
                    "📈",
                    "もっと低い！",
                    "入力した数字より小さいです",
                    0xFFF44336  // red
                );
                break;

            case TOO_LOW:
                textInputLayout.setError(null);
                showResult(
                    "📉",
                    "もっと高い！",
                    "入力した数字より大きいです",
                    0xFF2196F3  // blue
                );
                break;
        }

        editTextGuess.setText("");
        hideKeyboard();
        updateAttempts(guessResult.getAttempts());
    }

    private void showResult(String emoji, String main, String hint, long colorArgb) {
        cardResult.setVisibility(View.VISIBLE);
        textViewEmoji.setText(emoji);
        textViewResult.setText(main);
        textViewHint.setText(hint);
        cardResult.setCardBackgroundColor((int) colorArgb | 0x1A000000); // 薄い色
        textViewResult.setTextColor((int) colorArgb);
    }

    private void updateAttempts(int count) {
        textViewAttempts.setVisibility(View.VISIBLE);
        textViewAttempts.setText("試行回数：" + count + " 回");
    }

    private void resetGame() {
        game.reset();
        editTextGuess.setText("");
        editTextGuess.setEnabled(true);
        buttonGuess.setEnabled(true);
        cardResult.setVisibility(View.GONE);
        textViewAttempts.setVisibility(View.GONE);
        textInputLayout.setError(null);
        editTextGuess.requestFocus();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}