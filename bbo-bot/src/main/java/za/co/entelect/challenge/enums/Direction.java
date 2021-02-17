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
                if((x!=this.x && y!=this.y)||(x!=-1*this.x && y!=-1*this.y)){
                    res.add(new Position(x,y));
                }
            }
        }
        // res.add(new Position(0,-1));
        // res.add(new Position(1,-1));
        // res.add(new Position(1,0));
        // res.add(new Position(1,1));
        // res.add(new Position(0,1));
        // res.add(new Position(-1,1));
        // res.add(new Position(-1,0));
        // res.add(new Position(-1,-1));
        // res.remove(new Position(this.x, this.y));
        // res.remove(new Position(-1*this.x, -1*this.y));
        return res;
    }
}
