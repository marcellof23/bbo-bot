package za.co.entelect.challenge;

import com.google.gson.Gson;
import za.co.entelect.challenge.command.Command;
import za.co.entelect.challenge.entities.GameState;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String ROUNDS_DIRECTORY = "rounds";
    private static final String STATE_FILE_NAME = "state.json";
    private static final boolean DEBUG = false;

    /**
     * Read the current state, feed it to the bot, get the output and print it to stdout
     *
     * @param args the args
     **/
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Gson gson = new Gson();
        Random random = new Random(System.nanoTime());

        while (true) {
            try {
                int roundNumber = sc.nextInt();

                String statePath = String.format("./%s/%d/%s", ROUNDS_DIRECTORY, roundNumber, STATE_FILE_NAME);
                String state = new String(Files.readAllBytes(Paths.get(statePath)));

                GameState gameState = gson.fromJson(state, GameState.class);
                System.out.println("WOIUIIIIIIIIIIIIIIIIIIIIIIIIII");
                if(DEBUG){
                    String target1 = gson.toJson(gameState.myPlayer);
                    String target2 = gson.toJson(gameState.opponents);
                    System.out.println("=================================");
                    System.out.println("==========INI GAME STATE=========");
                    System.out.println("=================================");
                    System.out.println(target1);
                    System.out.println("=================================");
                    System.out.println(target2);
                    TimeUnit.SECONDS.sleep(3);
                }
                Command command = new Bot(random, gameState).run(DEBUG);

                System.out.println(String.format("C;%d;%s", roundNumber, command.render()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
