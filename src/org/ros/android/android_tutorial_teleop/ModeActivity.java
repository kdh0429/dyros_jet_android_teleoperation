package org.ros.android.android_tutorial_teleop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

/**
 * Created by kim on 17. 7. 27.
 */

public class ModeActivity extends Activity{

    private int mode;
    private ImageView imageviewmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modeactivity);
        Intent intent =getIntent();
        mode= intent.getIntExtra("mode",0);

        imageviewmode = (ImageView) this.findViewById(R.id.imageViewMode);

        switch (mode) {
            case 0:
                imageviewmode.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.mode0));
                break;
            case 1:
                imageviewmode.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.mode1));
                break;
            case 2:
                imageviewmode.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.mode2));
                break;
            default:
                break;
        }
    }
}
