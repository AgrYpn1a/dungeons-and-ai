package com.dai.engine;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RenderComponent implements IComponent {
    public static EComponentId id;
    private TextureRegion texture;

    // Our renderer will work with TextureRegion only
    // for simplicity.
    public RenderComponent(TextureRegion texture) {
        this.texture = texture;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }

    public TextureRegion getTexture() { return this.texture; }

	@Override
	public EComponentId getComponentId() {
        return id;
	}
}
