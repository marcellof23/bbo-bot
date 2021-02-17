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
        for(int x =-1; x<=1; x++){
            for(int y=-1; y<=1; y++){
                if((x==0 && y==0) || (x==this.x && y==this.y) || (x==-1*this.x && y==-1*this.y)){
                    continue;
                }
                res.add(new Position(x,y));
            }
        }
        return res;
    }
}