package Game.GettingUnderYourNerve;

import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useImmersiveMode = true;

        initialize(new Main(), config);

        // Tell LibGDX to catch the back key
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void onBackPressed() {
        // Force the swipe-back gesture to post an Input.Keys.BACK event into LibGDX
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (Gdx.input.getInputProcessor() != null) {
                    Gdx.input.getInputProcessor().keyDown(Input.Keys.BACK);
                    Gdx.input.getInputProcessor().keyUp(Input.Keys.BACK);
                }
            }
        });
    }
}
