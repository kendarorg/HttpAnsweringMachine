package org.kendar.ham;

class HamReplayerBuilderImpl implements HamReplayerBuilder {

    static {
        HamBuilder.register("replayer.server", HamReplayerBuilderImpl::new);
    }
    private HamInternalBuilder hamBuilder;

    private HamReplayerBuilderImpl(HamInternalBuilder hamBuilder){
        this.hamBuilder = hamBuilder;
    }
}
