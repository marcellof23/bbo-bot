package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.AttackType;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;

public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    private final int[] deltaX = {0, 1, 1, 1, 0, -1, -1, -1};
    private final int[] deltaY = {-1, -1, 0, 1, 1, 1, 0, -1};
    private final Position CENTRE = new Position(16,16);
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

    // TODO: should use snowball?
    private boolean shouldSnowball(Worm enemy) {
        return true;
    }

    // TODO: should use snowball?
    private boolean shouldSnowball(int x, int y) {
        return true;
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

    // Main for bot
    // @param boolean DEBUG
    // @return Command

    public Command run(boolean DEBUG) {
        if (DEBUG) {
            printCurrentWormInformation();
        }
        if(gameState.currentRound<=100)
        {
            System.out.println("==================asdfasdfasdfasdf===============");
            List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
            for(int i=0;i<surroundingBlocks.size();i++)
            {
                Cell block = surroundingBlocks.get(i);
                if(block.type == CellType.DIRT)
                {
                    return new DigCommand(block.x, block.y);
                }
            }
            for(int i=0;i<surroundingBlocks.size();i++)
            {
                Cell blocks = surroundingBlocks.get(i);
                if(blocks.type == CellType.AIR)
                {
                    return new MoveCommand(blocks.x, blocks.y);
                }
            }
        }
        String profession = currentWorm.profession;

        Worm enemyWorm;
        System.out.println("==================asdfasdfasdfasdf===============");
        // TODO: change to shouldBananaBombs and shouldSnowball
        if (profession.equals("Agent") && currentWorm.bananaBombs.count > 0) {
            enemyWorm = getAttackableWormInRange(AttackType.BANANA_BOMB);
            if (enemyWorm != null) return new BananaBombCommand(enemyWorm.position.x, enemyWorm.position.y);
        } else if (profession.equals("Technologist") && currentWorm.snowballs.count > 0) {
            enemyWorm = getAttackableWormInRange(AttackType.SNOWBALL);
            if (enemyWorm != null) return new SnowballCommand(enemyWorm.position.x, enemyWorm.position.y);
        }

        // check shooting
        enemyWorm = getAttackableWormInRange(AttackType.SHOOTING);
        if (enemyWorm != null) {
            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new ShootCommand(direction);
        }
        else {
            Direction direction = resolveDirection( currentWorm.position, CENTRE);
            int dX = currentWorm.position.x + direction.x;
            int dY = currentWorm.position.y + direction.y;
            if(isValidCoordinate(dX,dY))
            {
                Cell C = gameState.map[dX][dY];
                if(C.type == CellType.DIRT)
                {
                    return new DigCommand(dX, dY);
                }
                else if(C.type == CellType.AIR)
                {
                    return new MoveCommand(dX, dY);
                }
            }
            return new DoNothingCommand();

//           if(direction )
//            return new MoveCommand(direction.x , direction.y);
        }
//        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
//        int cellIdx = random.nextInt(surroundingBlocks.size());
//
//        Cell block = surroundingBlocks.get(cellIdx);
//        if (block.type == CellType.AIR) {
//            return new MoveCommand(block.x, block.y);
//        } else if (block.type == CellType.DIRT) {
//            return new DigCommand(block.x, block.y);
//        }


    }

    // Get the attackable worm in range based on priority
    // Technologist -> Agent -> Commando
    // @param AttackType type
    // @return Worm
    private Worm getAttackableWormInRange(AttackType type) {
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
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        PriorityQueue<Worm> attackableWorms = new PriorityQueue<Worm>();

        // search for all attackable worms
        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                // enemy is attackable, add to priority queue, priority is sorted by profession in compareTo functions
                attackableWorms.add(enemyWorm);
            }
        }

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

    // Get all attackable cells that in attack range of current worm (maybe empty cells, there is no worm there)
    // @param int range
    // @param AttackType type, attack type (SHOOTING, BANANA_BOMB, SNOWBALL)
    // @return List<List<Cell>>, all attackable cells
    private List<List<Cell>> getAllAttackableCellsInRange(int range, AttackType type) {
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
                if (type == AttackType.SHOOTING && isAttackBlocked(cell, false, false)) {
                    break;
                }

                // player wants to banana bomb or snowball but blocked by deep space cells
                if (type != AttackType.SHOOTING && isAttackBlocked(cell, true, false)) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
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

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
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
        StringBuilder builder = new StringBuilder();

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

        return Direction.valueOf(builder.toString());
    }
}
