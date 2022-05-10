package org.kendar.ham;

class HamReplayerBuilderImpl implements HamReplayerBuilder {

    private HamInternalBuilder hamBuilder;

    public HamReplayerBuilderImpl(HamInternalBuilder hamBuilder){
        this.hamBuilder = hamBuilder;
    }

    @Override
    public HamReplayerBuilderImpl init() {
        return null;
    }
}
