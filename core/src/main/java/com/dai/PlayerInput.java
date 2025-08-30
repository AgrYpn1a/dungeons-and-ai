package com.dai;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;

/**
 * We will assume all actions are registered properly.
 * */
@SuppressWarnings("unchecked")
public final class PlayerInput implements InputProcessor {

    public static enum EInputAction {
        Main,
        EndTurn
    }

    private Map<EInputAction, Consumer<?>> inputMap = new HashMap<>();

    public PlayerInput() { }

    public <T> void registerInput(EInputAction action, Consumer<T> callback) {
        inputMap.put(action, callback);
    }

	@Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return true;
    }

	@Override
	public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.Q) {
            Consumer<Integer> c = (Consumer<Integer>) inputMap.get(EInputAction.EndTurn);
            if(c != null) {
                c.accept(keycode);
            }
        }

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
	public boolean touchUp(int x, int y, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            Consumer<Vector3> c = (Consumer<Vector3>) inputMap.get(EInputAction.Main);
            if(c != null) {
                c.accept(new Vector3(x, y, 0));
            }
        }

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
