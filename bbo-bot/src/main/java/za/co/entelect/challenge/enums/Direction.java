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
    public List <Position> getProjectedDirection() {
        List <Position> Res = new ArrayList<Position>();
        String name = this.name();
        Res.add(new Position(this.x,this.y));
        if(name.length()==1)
        {
            if(name.charAt(0) == 'W' || name.charAt(0) == 'E')
            {
                Direction D1 = Direction.valueOf("N"+name.charAt(0));
                Direction D2 = Direction.valueOf("S"+name.charAt(0));
                Res.add(new Position(D1.x,D1.y));
                Res.add(new Position(D2.x,D2.y));
            }
            else
            {
                Direction D1 = Direction.valueOf(name.charAt(0)+"W");
                Direction D2 = Direction.valueOf(name.charAt(0)+"E");
                Res.add(new Position(D1.x,D1.y));
                Res.add(new Position(D2.x,D2.y));
            }
        }
        else {
            for (int i = 0; i < name.length(); i++) {
                Direction D = Direction.valueOf(name.charAt(i) + "");
                Res.add(new Position(D.x, D.y));
            }

        }
        return Res;
    }
}
