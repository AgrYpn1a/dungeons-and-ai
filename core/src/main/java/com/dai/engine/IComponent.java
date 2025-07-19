package com.dai.engine;

public interface IComponent {
    public enum EComponentId {
        Render;
    }

    public EComponentId getComponentId();
}
