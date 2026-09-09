package me.unariginal.moresparkles.placeholders;

import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class ParseContext {
    private final ServerPlayerEntity player;
    private final Boost boost;
    private final BoostArea boostArea;

    private ParseContext(Builder builder) {
        this.player = builder.player;
        this.boost = builder.boost;
        this.boostArea = builder.boostArea;
    }

    @Nullable
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Nullable
    public Boost getBoost() {
        return boost;
    }

    @Nullable
    public BoostArea getBoostArea() {
        return boostArea;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ServerPlayerEntity player;
        private Boost boost;
        private BoostArea boostArea;

        public Builder player(ServerPlayerEntity player) {
            this.player = player;
            return this;
        }

        public Builder boost(Boost boost) {
            this.boost = boost;
            return this;
        }

        public Builder boostArea(BoostArea boostArea) {
            this.boostArea = boostArea;
            return this;
        }

        public ParseContext build() {
            return new ParseContext(this);
        }
    }
}
