package Game.GettingUnderYourNerve;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class GameCam {

    private OrthographicCamera camera;
    private Vector2 position;
    public GameCam(){
        camera = new OrthographicCamera();
        position = new Vector2();
    }

    public OrthographicCamera GetCam(){
        return camera;
    }

    public void Update(float ww, float wh, float hvw, float hvh, float px, float py){
        position.x = MathUtils.clamp(px, hvw, ww -  hvw);
        position.y = MathUtils.clamp(py,  hvh, wh - hvh);

        camera.position.set(position.x, position.y, 0);
        camera.update();
    }

    public float getCamX(){
        return camera.position.x;
    }

    public float getCamY(){
        return camera.position.y;
    }
}
