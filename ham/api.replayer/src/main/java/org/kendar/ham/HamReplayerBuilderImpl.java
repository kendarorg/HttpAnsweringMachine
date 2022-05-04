package org.kendar.ham;

class HamReplayerBuilderImpl implements HamReplayerBuilder {

    static {
        HamBuilder.register("replayer.server", (b)-> new HamReplayerBuilderImpl(b));
    }
    private HamInternalBuilder hamBuilder;

    private HamReplayerBuilderImpl(HamInternalBuilder hamBuilder){
        this.hamBuilder = hamBuilder;
    }
}
