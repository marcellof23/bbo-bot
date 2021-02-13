package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;
import za.co.entelect.challenge.enums.Direction;

public class Position {
    @SerializedName("x")
    public int x;

    @SerializedName("y")
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Position p) {
        return this.x == p.x && this.y == p.y;
    }

    public Position add(Direction d) {
        return new Position(
                this.x + d.x,
                this.y + d.y
        );
    }

    public Position minus(Direction d) {
        return new Position(
                this.x - d.x,
                this.y - d.y
        );
    }
}
