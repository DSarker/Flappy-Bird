package com.industries.sarker.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
    // Gdx
    SpriteBatch batch;
    Texture background;
    Texture[] birds;
    Texture topTube, bottomTube;
    Texture gameOver, title;
    ShapeRenderer shapeRenderer;

    // Game State
    int gameState;

    // Bird
    int flapState = 0;
    int counter = 0;
    int flapRate = 6;
    float birdY;
    float birdVelocity;
    float gravity = 1.8f;
    int birdStrength = 25;
    Circle birdCircle;

    // Pipes
    float gap = 400;
    Random randomGenerator;
    float tubeVelocity = 7;
    int numOfTubes = 4;
    float[] tubeX = new float[numOfTubes];
    float[] tubeOffset = new float[numOfTubes];
    float distanceBetweenTubes;
    Rectangle[] topTubeRectangles;
    Rectangle[] bottomTubeRectangles;
    int scoringTube = 0;

    // Scoring
    int gameScore;
    int highScore;
    BitmapFont font;
    float fontWidth;
    float fontSize = 1.8f;
    GlyphLayout glyphLayout;
    Preferences preferences;
    AssetManager assetsManager;

    @Override
    public void create() {
        // Saved preferences to store high score
        preferences = Gdx.app.getPreferences("High Score");
        highScore = preferences.getInteger("highscore", 0);

        // Gdx textures
        batch = new SpriteBatch();
        background = new Texture("bg.png");
        birds = new Texture[3];
        birds[0] = new Texture("bird.png");
        birds[1] = new Texture("bird2.png");
        birds[2] = new Texture("birddown.png");
        topTube = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");
        title = new Texture("title.png");
        gameOver = new Texture("game_over.png");

        // Font properties
        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetsManager = new AssetManager();
        assetsManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        assetsManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter blackParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        blackParams.fontFileName = "flappybird.ttf";
        blackParams.fontParameters.size = (int) Math.ceil(fontSize * Gdx.graphics.getWidth() / 9);
        blackParams.fontParameters.minFilter = Texture.TextureFilter.Linear;
        blackParams.fontParameters.magFilter = Texture.TextureFilter.Linear;
        assetsManager.load("flappybird.ttf", BitmapFont.class, blackParams);

        assetsManager.update();

        // Positioning of text
        glyphLayout = new GlyphLayout();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // Collision detection shapes
        shapeRenderer = new ShapeRenderer();
        birdCircle = new Circle();
        topTubeRectangles = new Rectangle[numOfTubes];
        bottomTubeRectangles = new Rectangle[numOfTubes];

        // Initialize bird position
        birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;

        // Randomize gap position for pipes;
        randomGenerator = new Random();
        distanceBetweenTubes = Gdx.graphics.getWidth() * 0.8f;
        initializeTubePosition();

        // Initial game state, title screen
        gameState = 0;
    }

    @Override
    public void render() {

        // Draws the background
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Title screen, game didn't start
        if (gameState == 0) {
            birdFlap();

            batch.draw(title, Gdx.graphics.getWidth() / 2 - title.getWidth() / 2, Gdx.graphics.getHeight() / 2 + Gdx.graphics.getHeight() / 8);

            // Starts the game by changing the game state
            if (Gdx.input.justTouched()) {
                gameState = 1;
                gameScore = 0;
            }

            // Game started
        } else if (gameState == 1) {

            birdFlap();

            // If the current tube has passed mid screen, points
            if (tubeX[scoringTube] <= Gdx.graphics.getWidth() / 2) {
                gameScore++;
                scoringTube = scoringTube < numOfTubes - 1 ? scoringTube + 1 : 0;
            }

            // Handles on touch to make bird go upward
            if (Gdx.input.justTouched()) {
                birdVelocity = -birdStrength;

                // If bird is off screen (top), reset bird position
                if (birdY - birdVelocity >= Gdx.graphics.getHeight() - birds[0].getHeight()) {
                    birdY = Gdx.graphics.getHeight() - birds[1].getHeight();

                } else {
                    birdY -= birdVelocity;
                }
            }

            // Draws the pipes
            for (int i = 0; i < numOfTubes; i++) {

                // If the tube has made a pass off screen, reposition it
                if (tubeX[i] < -topTube.getWidth()) {
                    tubeX[i] += numOfTubes * distanceBetweenTubes;
                    tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - Gdx.graphics.getHeight() / 4);

                } else {
                    tubeX[i] -= tubeVelocity;
                }

                batch.draw(topTube, tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i]);
                batch.draw(bottomTube, tubeX[i], Gdx.graphics.getHeight() / 2 - bottomTube.getHeight() - gap / 2 + tubeOffset[i]);

                topTubeRectangles[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i], topTube.getWidth(), topTube.getHeight());
                bottomTubeRectangles[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 - bottomTube.getHeight() - gap / 2 + tubeOffset[i], bottomTube.getWidth(), bottomTube.getHeight());

            }

            // Draw the current score
            if (assetsManager.update()) {
                font = assetsManager.get("flappybird.ttf", BitmapFont.class);
                glyphLayout.setText(font, String.valueOf(gameScore));
                fontWidth = glyphLayout.width;
                font.draw(batch, String.valueOf(gameScore), Gdx.graphics.getWidth() / 2 - fontWidth / 2, Gdx.graphics.getHeight() / 2 + Gdx.graphics.getHeight() / 3);
            }

            // Handles gravity
            birdVelocity += gravity;
            birdY -= birdVelocity;

            // Check if bird went down
            if (birdY <= 0) {
                gameOver();
            }

            // Game over screen
        } else if (gameState == 2) {
            batch.draw(gameOver, Gdx.graphics.getWidth() / 2 - gameOver.getWidth() / 2, Gdx.graphics.getHeight() / 2 + Gdx.graphics.getHeight() / 8);

            if (Gdx.input.justTouched()) {
                gameState = 1;
                gameScore = 0;
            }
        }

        //Draws the high score;
        if (assetsManager.update()) {
            font = assetsManager.get("flappybird.ttf", BitmapFont.class);
            glyphLayout.setText(font, "High Score:" + highScore);
            fontWidth = glyphLayout.width;
            font.draw(batch, "High Score:" + highScore, Gdx.graphics.getWidth() / 2 - fontWidth / 2, 200);
        }

        // Draws the bird
        batch.draw(birds[flapState], Gdx.graphics.getWidth() / 2 - birds[flapState].getWidth() / 2, birdY);
        batch.end();

        birdCircle.set(Gdx.graphics.getWidth() / 2, birdY + birds[flapState].getHeight() / 2, birds[flapState].getWidth() / 2);

        // Checks collision
        for (int i = 0; i < numOfTubes; i++) {
            if (Intersector.overlaps(birdCircle, topTubeRectangles[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangles[i])) {
                gameOver();
                return;
            }
        }
    }

    // Handles game over
    public void gameOver() {

        // Save new high score
        if (gameScore > highScore) {
            preferences.putInteger("highscore", gameScore);
            preferences.flush();
        }

        gameState = 2;
        flapState = 2;
        birdVelocity = 0;
        birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;

        initializeTubePosition();

    }

    public void initializeTubePosition() {

        for (int i = 0; i < numOfTubes; i++) {
            tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - Gdx.graphics.getHeight() / 4);

            tubeX[i] = Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2 + Gdx.graphics.getWidth() + i * distanceBetweenTubes;

            topTubeRectangles[i] = new Rectangle();
            bottomTubeRectangles[i] = new Rectangle();

            scoringTube = 0;
        }
    }

    public void birdFlap() {

        // Handles the flapping of the bird
        if (counter == 0 || counter == flapRate * 2) {
            flapState = 0;
            counter = 0;

        } else if (counter == flapRate) {
            flapState = 1;
        }

        counter += 1;
    }
}
