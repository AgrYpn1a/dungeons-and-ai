package com.dai.pools;

import com.dai.engine.EntityPool;
import com.dai.entities.IndicatorEntity;
import com.dai.entities.IndicatorEntity.EIndicator;

public final class IndicatorsPool extends EntityPool<IndicatorEntity> {

    private EIndicator indicatorType;

    public IndicatorsPool(EIndicator indicatorType) {
        this.indicatorType = indicatorType;
        init(25);
    }

	@Override
	protected IndicatorEntity create() {
        IndicatorEntity entity = new IndicatorEntity(indicatorType);
        entity.setShouldRender(false);
        return entity;
	}

}
