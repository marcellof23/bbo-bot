package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.AttackType;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;
import za.co.entelect.challenge.enums.PowerUpType;

import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.lang.Math;
import java.util.concurrent.TimeUnit;

public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    private final Position CENTRE1 = new Position(17,16);
    private final Position CENTRE2 = new Position(19,16);
    private final Position CENTRE3 = new Position(15,16);

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    // should use snowball?
    // @return Position, position is returned if the player should use snowball to the choosen position
    private Position shouldSnowball() {
        if (currentWorm.profession.equals("Technologist") && currentWorm.snowballs.count > 0) {
            List<Cell> attackableCells = getAllAttackableCellsInRange(currentWorm.snowballs.range, AttackType.SNOWBALL);

            Position optimalPos = null;
            int minDistance = 99;
            int maxEnemyHits = 0;
            int minSelfHits = 3;

            for(Cell cell : attackableCells) {
                int selfHits = 0;
                for(Worm myWorm : gameState.myPlayer.worms) {
                    if (myWorm != currentWorm &&
                        euclideanDistance(cell.x, cell.y, myWorm.position.x, myWorm.position.y) <= currentWorm.snowballs.freezeRadius) {
                        selfHits += 1;
                    }
                }

                int enemyHits = 0;
                for(Worm enemy : opponent.worms) {
                    if(enemy.roundsUntilUnfrozen == 0 &&
                       euclideanDistance(cell.x, cell.y, enemy.position.x, enemy.position.y) <= currentWorm.snowballs.freezeRadius) {
                        enemyHits += 1;
                    }
                }

                int distance = euclideanDistance(cell.x, cell.y, currentWorm.position.x, currentWorm.position.y);

                if ((enemyHits > maxEnemyHits && selfHits < minSelfHits) ||
                    (enemyHits == maxEnemyHits && selfHits == minSelfHits && distance > currentWorm.snowballs.freezeRadius * 2 && distance < minDistance)) {
                    maxEnemyHits = enemyHits;
                    minSelfHits = selfHits;
                    minDistance = distance;
                    optimalPos = new Position(cell.x, cell.y);
                }
            }

            if ((minSelfHits == 0  || minSelfHits/getMyLivingWormCount() <= 0.5)) {
                return optimalPos;
            }

            // save for later, not worth the sacrifice
            return null;
        }

        // not technologist or no snowballs remaining
        return null;
    }

    private Worm shouldBanana(){
        PriorityQueue<Worm> attackableWorms;

        if (currentWorm.profession.equals("Agent") && currentWorm.bananaBombs.count > 0) {
            attackableWorms = getAllAttackableWormInRange(AttackType.BANANA_BOMB);

            while(!attackableWorms.isEmpty()) {
                Worm w = attackableWorms.poll();
                return w;
            }
            // all frozen or there is no attackable worms in range
            return null;
        }
        // not technologist or no snowballs remaining
        return null;
    }

    private Position shouldFlee(){
        PriorityQueue<Worm> attackableWorms;
        String profession = currentWorm.profession;
        if(profession.equals("Agent")){
            attackableWorms = getAllAttackableWormInRange(AttackType.BANANA_BOMB);
        }else if(profession.equals("Technologist")){
            attackableWorms = getAllAttackableWormInRange(AttackType.SNOWBALL);
        }else{
            attackableWorms = getAllAttackableWormInRange(AttackType.SHOOTING);
        }
        int currentEnemyWormId = opponent.currentWormId;
        Worm threat = null;
        for(Worm w : attackableWorms){
            if(w.id == currentEnemyWormId){
                threat = w;
                break;
            }
        }
        if(threat!=null && threat.roundsUntilUnfrozen==0){
            System.out.println("ANDA HARUS KABUR! KARENA ADA WORM YANG BISA MENYERANG ANDA SAAT INI");
            Position myPos = this.currentWorm.position;
            Direction d = resolveDirection(myPos, threat.position);
            System.out.println(String.format("INI POSISI KITA : %d,%d",myPos.x,myPos.y));
            System.out.println(String.format("INI POSISI MUSUH : %d,%d",threat.position.x,threat.position.y));
            System.out.println(String.format("INI DIRECTION KITA KE MUSUH : %d,%d",d.x,d.y));
            int x = myPos.x;
            int y = myPos.y;
            int myWormCount = getMyLivingWormCount();
            if(myWormCount==1 && isAttackableByBasic(myPos, threat.position)){
                //kalo udah sendiri, menghindar sampe bego
                System.out.println("UDAH SENDIRIAN BANG! SAATNYA KABUR SAMPE BEGO!");
                List<Position> possibleEscapeDirection = d.getSafeZonePosition();
                System.out.println(String.format("Ada %d direction yang aman",possibleEscapeDirection.size()));
                List<Position> targetEscapePosition = new ArrayList<Position>();
                //filter all possible escape direction
                for(Position pos : possibleEscapeDirection){
                    Position target = new Position(x+pos.x, y+pos.y);
                    if(isOccupied(target)){
                        System.out.println(String.format("THE TARGET CELL %d,%d IS OCCUPIED!",target.x,target.y));
                    }else{
                        System.out.println(String.format("THE TARGET CELL %d,%d IS NOT OCCUPIED!",target.x,target.y));
                    }
                    if(gameState.map[y+pos.y][x+pos.x].type==CellType.AIR && !isOccupied(target)){
                        targetEscapePosition.add(new Position(x+pos.x, y+pos.y));
                    }
                }
                System.out.println(String.format("Ada %d target cell yang aman",targetEscapePosition.size()));
                return getMinimumPathToCenter(targetEscapePosition);
            }
        }
        return null;
    }

    private Position getMinimumPathToCenter(List<Position> possibilities){
        if(possibilities.size()>0){
            int minDist = 1000;
            Position posRes = possibilities.get(0);
            for(Position p : possibilities) {
                int dist = euclideanDistance(16, 16, p.x, p.y);
                if(dist<minDist) {
                    minDist=dist;
                    posRes = p;
                }
            }
            return posRes;
        }
        return null;
    }

    private boolean isOccupied(Position curPosition){
        boolean res = false;
        for(Worm w : opponent.worms){
            if(curPosition.x==w.position.x && curPosition.y==w.position.y){
                res = true;
                break;
            }
        }
        if(!res){
            for(Worm w : gameState.myPlayer.worms){
                if(curPosition.x==w.position.x && curPosition.y==w.position.y){
                    res = true;
                    break;
                }
            }
        }
        
        return res;
    }

    private boolean isAttackableByBasic(Position a, Position b){
        return (a.x==b.x && Math.abs(a.y-b.y)<= 4) || (a.y == b.y && Math.abs(a.x-b.x)<=4) || (Math.abs(a.y-b.y)==Math.abs(a.x-b.x) && Math.abs(a.y-b.y)<=3);
    }

    private int getMyLivingWormCount(){
        int count = 0;
        Worm[] myWorms = gameState.myPlayer.worms;
        for(Worm w : myWorms){
            count += w.health>0? 1 : 0;
        }
        return count;
    }

    // Print current worm information for debugging
    private void printCurrentWormInformation() {
        System.out.println(String.format("Current worm id: %d", this.currentWorm.id));
        System.out.println(String.format("Position:\n\t- X: %d\n\t- Y: %d", this.currentWorm.position.x, this.currentWorm.position.y));
        System.out.println(String.format("Role : %s", this.currentWorm.profession));
        System.out.println(String.format("Basic weapon:\n\t- DMG: %d\n\t- RANGE: %d", this.currentWorm.weapon.damage, this.currentWorm.weapon.range));

        if (this.currentWorm.profession.equals("Agent")) {
            System.out.println(String.format("Banana Bomb:\n\t- DMG: %d\n\t- DMG RADIUS: %d\n\t- RANGE: %d\n\t- REMAINING COUNT: %d",
                    this.currentWorm.bananaBombs.damage,
                    this.currentWorm.bananaBombs.damageRadius,
                    this.currentWorm.bananaBombs.range,
                    this.currentWorm.bananaBombs.count
            ));
        }

        if (this.currentWorm.profession.equals("Technologist")) {
            System.out.println(String.format("Snowball:\n\t- FREEZE DURATION: %ds\n\t- FREEZE RADIUS: %d\n\t- RANGE: %d\n\t- REMAINING COUNT: %d",
                    this.currentWorm.snowballs.freezeDuration,
                    this.currentWorm.snowballs.freezeRadius,
                    this.currentWorm.snowballs.range,
                    this.currentWorm.snowballs.count
            ));
        }
    }

    private Command TriggerAttack()
    {
        String profession = currentWorm.profession;
        Worm enemyWorm;
        enemyWorm = getAttackableWormInRange(AttackType.SHOOTING);

        if (profession.equals("Agent") && currentWorm.bananaBombs.count > 0) {
            enemyWorm = shouldBanana();
            if (enemyWorm != null) return new BananaBombCommand(enemyWorm.position.x, enemyWorm.position.y);
        } else if (profession.equals("Technologist") && currentWorm.snowballs.count > 0) {
            Position snowballPosition = shouldSnowball();
            if (snowballPosition != null) return new SnowballCommand(snowballPosition.x, snowballPosition.y);
        } else {
            Position opp_tech = opponent.worms[2].position;
            return AttackFirst(opp_tech);
        }
        Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
        return new ShootCommand(direction);
    }

    private Command findDirt()
    {
        Worm enemyWorm;
        enemyWorm = getAttackableWormInRange(AttackType.SHOOTING);
        if(enemyWorm != null)
        {
            return TriggerAttack();
        }
        Vector<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y, 1);
        for(Cell surround : surroundingBlocks)
        {
            if(surround.type == CellType.DIRT) {
                return new DigCommand(surround.x, surround.y);
            }
        }
        int i = 2;
        while(i<gameState.mapSize){
            Vector<Cell> findDirts = getSurroundingCells(currentWorm.position.x, currentWorm.position.y, i);
            for(Cell surround : findDirts)
            {
                if(surround.type == CellType.DIRT) {
                    Position surroundPosition = new Position(surround.x,surround.y);
                    Direction direction = resolveDirection(currentWorm.position, surroundPosition);
                    Position P = new Position(currentWorm.position.x + direction.x , currentWorm.position.y + direction.y);
                    return MoveToPoint(P);
                }
            }
            i++;
        }
        return new DoNothingCommand();
    }

    private Command first120Round(Position CENTRE) {
        Worm enemyWorm;
        enemyWorm = getAttackableWormInRange(AttackType.SHOOTING);
        if (enemyWorm != null) {
            return TriggerAttack();
        }

        Vector<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y, 1);

        for(Cell surround : surroundingBlocks)
        {
            if(surround.type == CellType.DIRT) {
                return new DigCommand(surround.x, surround.y);
            }
        }
        Direction direction = resolveDirection( currentWorm.position, CENTRE);
        if(direction == null)
            // Masukkin algoritma flee & attack(ganti DoNothingCommand)
            return AttackFirst(CENTRE);

        int dX = currentWorm.position.x + direction.x;
        int dY = currentWorm.position.y + direction.y;
        Cell C = gameState.map[dY][dX];
        if(C.type == CellType.DIRT)
        {
            return new DigCommand(C.x, C.y);
        }
        else {
            return new MoveCommand(C.x, C.y);
        }
    }

    private Command AttackFirst(Position Point)
    {
        Worm enemyWorm;
        enemyWorm = getAttackableWormInRange(AttackType.SHOOTING);
        if (enemyWorm != null) {
            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new ShootCommand(direction);
        } else {
            return MoveToPoint(Point);
        }
    }

    // Move to point
    // @param Position point
    // @return Command
    private Command MoveToPoint(Position Point) {
        Direction direction = resolveDirection(currentWorm.position, Point);
        if(direction == null)
        {
            PriorityQueue<Worm> aliveEnemyWorms = getAllAliveEnemyWorm();
            if (aliveEnemyWorms.size() > 0) {
                return AttackFirst(aliveEnemyWorms.poll().position);
            }
        }
        int dX = currentWorm.position.x + direction.x;
        int dY = currentWorm.position.y + direction.y;
        if(isValidCoordinate(dX,dY))
        {
            Cell C = gameState.map[dY][dX];
            if(C.type == CellType.DIRT)
            {
                return new DigCommand(C.x, C.y);
            }
            else
            {
                return new MoveCommand(C.x, C.y);
            }
        }
        return new DoNothingCommand();
    }

    // Main for bot
    // @param boolean DEBUG
    // @return Command
    public Command run(boolean DEBUG) {
        Position CENTRE = new Position(0,0);
        if(gameState.currentWormId==1) {
            CENTRE = CENTRE2;
        }
        else if(gameState.currentWormId==2) {
            CENTRE = CENTRE1;
        }
        else {
            CENTRE = CENTRE3;
        }
        if (DEBUG) {
            printCurrentWormInformation();
        }
        

        if(gameState.currentRound<=120) {
         return first120Round(CENTRE);
        }
      
        String profession = currentWorm.profession;
        Worm enemyWorm;
        Position fleePosition = shouldFlee();
        if(fleePosition!=null && currentWorm.health>0){
            try{
                return new MoveCommand(fleePosition.x, fleePosition.y);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if (profession.equals("Agent") && currentWorm.bananaBombs.count > 0) {
            enemyWorm = shouldBanana();
            if (enemyWorm != null) return new BananaBombCommand(enemyWorm.position.x, enemyWorm.position.y);
        } else if (profession.equals("Technologist") && currentWorm.snowballs.count > 0) {
            Position snowballPosition = shouldSnowball();
            if (snowballPosition != null) return new SnowballCommand(snowballPosition.x, snowballPosition.y);
        }

        // check shooting
        PriorityQueue<Worm> enemyWormTarget = getAllAliveEnemyWorm();
        return AttackFirst(enemyWormTarget.poll().position);
    }

    // Get all alive enemy worm
    // @return Worm PriorityQueue
    private PriorityQueue<Worm> getAllAliveEnemyWorm() {
        PriorityQueue<Worm> aliveWorms = new PriorityQueue<Worm>();
        for(Worm enemy : opponent.worms) {
            if (enemy.health > 0) {
                aliveWorms.add(enemy);
            }
        }
        return aliveWorms;
    }

    // Get all attackable worms in range based on priority
    // Technologist -> Agent -> Commando
    // @param AttackType type
    // @return PriorityQueue<Worm>
    private PriorityQueue<Worm> getAllAttackableWormInRange(AttackType type) {
        int range = 0;

        if (type == AttackType.SHOOTING) {
            range = currentWorm.weapon.range;
        } else if (type == AttackType.BANANA_BOMB) {
            range = currentWorm.bananaBombs.range;
        } else if (type == AttackType.SNOWBALL) {
            range = currentWorm.snowballs.range;
        }

        Set<String> cells = getAllAttackableCellsInRange(range, type)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        PriorityQueue<Worm> attackableWorms = new PriorityQueue<Worm>();

        // search for all attackable worms
        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition) && enemyWorm.health > 0) {
                // enemy is attackable, add to priority queue, priority is sorted by profession in compareTo functions
                attackableWorms.add(enemyWorm);
            }
        }

        return attackableWorms;
    }

    // Get the first attackable worm in range based on priority
    // Technologist -> Agent -> Commando
    // @param PriorityQueue<Worm> attackableWorms
    // @return Worm
    private Worm getAttackableWormInRange(PriorityQueue<Worm> attackableWorms) {
        // no worm can be attacked!
        if(attackableWorms.size() == 0) {
            return null;
        }

        // get the head in priority queue
        Worm result = attackableWorms.poll();
        // Check if the enemy still has health remaining
        while(result.health <= 0 && !attackableWorms.isEmpty()) {
            result = attackableWorms.poll();
        }

        // no worm can be attacked!
        if (result.health <= 0) return null;

        return result;
    }

    // Get the first attackable worm in range based on priority
    // Technologist -> Agent -> Commando
    // @param AttackType type
    // @return Worm
    private Worm getAttackableWormInRange(AttackType type) {
        PriorityQueue<Worm> attackableWorms = getAllAttackableWormInRange(type);
        return getAttackableWormInRange(attackableWorms);
    }


    // Get all attackable cells that in attack range of current worm (maybe empty cells, there is no worm there)
    // @param int range
    // @param AttackType type, attack type (SHOOTING, BANANA_BOMB, SNOWBALL)
    // @return <List<Cell>, all attackable cells
    private List<Cell> getAllAttackableCellsInRange(int range, AttackType type) {
        // if shooting use direction lines
        if (type == AttackType.SHOOTING) {
            List<List<Cell>> directionLines = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                List<Cell> directionLine = new ArrayList<>();
                for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                    int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                    int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                    if (!isValidCoordinate(coordinateX, coordinateY)) {
                        break;
                    }

                    if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                        break;
                    }

                    Cell cell = gameState.map[coordinateY][coordinateX];

                    // if not AIR cells, cannot be attacked (there will be no worms there)
                    if (cell.type != CellType.AIR) {
                        break;
                    }

                    // player wants to shoot but blocked by deep space or dirt cells
                    if (isAttackBlocked(cell, false, false)) {
                        break;
                    }

                    directionLine.add(cell);
                }
                directionLines.add(directionLine);
            }

            // convert from 2d list to list
            return directionLines.stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        // banana bomb or snowball, use surrounding cells
        Vector<Cell> allAttackableCells = getSurroundingCells(currentWorm.position.x, currentWorm.position.y, range);
        allAttackableCells.removeIf(cell -> !isValidCoordinate(cell.x, cell.y) ||
                cell.type != CellType.AIR ||
                euclideanDistance(currentWorm.position.x, currentWorm.position.y, cell.x, cell.y) > range);
        return allAttackableCells;
    }

    // is attacking will be blocked by dirt cells or deep space?
    // @param Cell, target cell
    // @param boolean, is the attack passed dirt cells?
    // @param boolean, is the attack passed deep space cells?
    // @return boolean
    private boolean isAttackBlocked(Cell c, boolean dirtPassed, boolean deepSpacePassed) {
        int player_x = currentWorm.position.x;
        int player_y = currentWorm.position.y;

        Position playerPosition = new Position(player_x, player_y);
        Position currentCell = new Position(c.x, c.y);

        Direction d = resolveDirection(playerPosition, currentCell);

        while(!playerPosition.equals(currentCell)) {
            currentCell = currentCell.minus(d);
            if ((!dirtPassed && gameState.map[currentCell.y][currentCell.x].type == CellType.DIRT) ||
                (!deepSpacePassed && gameState.map[currentCell.y][currentCell.x].type == CellType.DEEP_SPACE)
            ) {
                // shooting will be blocked
                return true;
            }
        }

        // shooting will not be blocked
        return false;
    }

    // get surrounding cells
    // @param int x
    // @param int y
    // @param int spread
    // @return List<Cell>
    private Vector <Cell> getSurroundingCells(int x, int y, int spread) {
        Vector<Cell> cells = new Vector<>();
        for (int i = x - spread; i <= x + spread; i++) {
            for (int j = y - spread; j <= y + spread; j++) {
                // Don't include the current position
                if(i==x && j==y)
                {
                    continue;
                }
                if (isValidCoordinate(i, j))
                {
                    cells.add(gameState.map[j][i]);
                }
            }
        }
        return cells;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    // Direction of position b to position a
    // @param Position a
    // @param Position b
    // @return Direction
    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder("");

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }
        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }
        if(builder.toString().equals(""))
        {
            return null;
        }
        return Direction.valueOf(builder.toString());
    }
}
