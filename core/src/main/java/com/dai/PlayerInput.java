package com.dai;

import java.util.function.Consumer;
import java.util.function.Function;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

public final class PlayerInput implements InputProcessor {

    private Consumer<Vector2> onMouseDown;

    public PlayerInput(Consumer<Vector2> onMouseDown) {
        this.onMouseDown = onMouseDown;
    }

	@Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        onMouseDown.accept(new Vector2(x, y));

        if (button == Input.Buttons.LEFT) {
            if(onMouseDown != null) {
                onMouseDown.accept(new Vector2(x, y));
            }
            return true;
        }

        return false;
    }

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'keyDown'");
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'keyUp'");
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'touchUp'");
		return true;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'touchCancelled'");
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'touchDragged'");
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'mouseMoved'");
		return true;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'scrolled'");
		return true;
	}

}
