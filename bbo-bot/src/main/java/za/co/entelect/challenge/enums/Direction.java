package za.co.entelect.challenge.enums;
import java.util.*;
import za.co.entelect.challenge.entities.Position;
public enum Direction {

    N(0, -1),
    NE(1, -1),
    E(1, 0),
    SE(1, 1),
    S(0, 1),
    SW(-1, 1),
    W(-1, 0),
    NW(-1, -1);

    public final int x;
    public final int y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public List<Position> getSafeZonePosition(){
        List<Position> res = new ArrayList<Position>();
        if(this.x==0){
            res.add(new Position(1,0));
            res.add(new Position(-1,0));
        }else if(this.y==0){
            res.add(new Position(0,1));
            res.add(new Position(0,-1));
        }else{
            res.add(new Position(-1*this.x,0));
            res.add(new Position(0,-1*this.y));
        }
        return res;
    }
}
