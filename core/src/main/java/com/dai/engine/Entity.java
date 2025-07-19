package com.dai.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dai.engine.IComponent.EComponentId;
import com.dai.math.Transform2D;

/**
 * An Entity is present in the world.
 *
 *  */
public abstract class Entity extends Transform2D {
    protected Transform2D transform;
    protected List<IComponent> components;

    protected Entity() {
        super();

        // We use ArrayList for faster access to the memory
        this.components = new ArrayList<IComponent>();
        this.transform = this;
    }

    public Transform2D getTransform() { return this.transform; }

    public void AddComponent(IComponent component) {
        this.components.add(component);
    }

    public List<IComponent> getComponents() { return this.components; }

    /*
     * This method is somewhat ugly, since Java does not keep runtime
     * information about generic types. We're going to use ID instead,
     * and we will cast assuming method is used in a correct way.
     *
     * The proper way to safely use this method is:
     * Optional<RenderComponent> renderer = entity.getComponent(RenderComponent.id);
     */
    public <T extends IComponent> Optional<T> getComponent(EComponentId id) {
        // Find first works in this case,
        // as we will ignore duplicate components
        return components
            .stream()
            .filter(c -> c.getComponentId() == id)
            .map(c -> (T)c)
            .findFirst();
    }
}
