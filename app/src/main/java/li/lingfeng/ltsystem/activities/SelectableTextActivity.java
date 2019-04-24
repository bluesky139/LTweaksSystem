package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ShareUtils;

/**
 * Created by smallville on 2017/11/9.
 */

public class SelectableTextActivity extends Activity implements View.OnClickListener {

    private TextView mTextView;
    private EditText mEditView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_selectable);
        mTextView = findViewById(R.id.text);
        mEditView = findViewById(R.id.text_edit);

        String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (text == null) {
            text = "";
        }
        mTextView.setText(text);
        mTextView.setTextIsSelectable(true);
        mEditView.setText(text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.copy:
                ClipboardManager clipManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipManager.setPrimaryClip(ClipData.newPlainText(null, mEditView.getText()));
                break;
            case R.id.share:
                ShareUtils.shareText(this, mEditView.getText().toString());
                break;
            case R.id.edit:
                mEditView.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);
                break;
            case R.id.select_all:
                try {
                    TextView textView = mEditView.getVisibility() == View.VISIBLE ? mEditView : mTextView;
                    Selection.selectAll((Spannable) textView.getText());
                    Object editor = ReflectUtils.getObjectField(textView, "mEditor");
                    ReflectUtils.callMethod(editor, "startSelectionActionModeAsync",
                            new Object[] { false }, new Class[] { boolean.class } );
                } catch (Throwable e) {
                    Logger.e("Select all exception.", e);
                }
                break;
        }
    }
}
