package me.unariginal.moresparkles.placeholders;

import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class ParseContext {
    private final ServerPlayerEntity player;
    private final Boost boost;
    private final BoostArea boostArea;
    private final BoostType boostType;

    private ParseContext(Builder builder) {
        this.player = builder.player;
        this.boost = builder.boost;
        this.boostArea = builder.boostArea;
        this.boostType = builder.boostType;
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

    @Nullable
    public BoostType getBoostType() {
        return boostType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ServerPlayerEntity player;
        private Boost boost;
        private BoostArea boostArea;
        private BoostType boostType;

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

        public Builder boostType(BoostType boostType) {
            this.boostType = boostType;
            return this;
        }

        public ParseContext build() {
            return new ParseContext(this);
        }
    }
}
