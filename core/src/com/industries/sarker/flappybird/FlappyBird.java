package com.industries.sarker.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
    // Gdx
    SpriteBatch batch;
    Texture background;
    Texture[] birds;
    Texture topTube, bottomTube;

    // Game State
    int gameState = 0;

    // Bird
    int flapState = 0;
    int counter = 0;
    int flapRate = 6;
    float birdY;
    float birdVelocity;
    float gravity = 1.8f;

    // Pipes
    float gap = 400;
    float maxTubeOffset;
    Random randomGenerator;
    float tubeVelocity = 4;
    int numOfTubes = 4;
    float[] tubeX = new float[numOfTubes];
    float[] tubeOffset = new float[numOfTubes];
    float distanceBetweenTubes;


    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("bg.png");
        birds = new Texture[2];
        birds[0] = new Texture("bird.png");
        birds[1] = new Texture("bird2.png");
        topTube = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");

        birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;

        maxTubeOffset = Gdx.graphics.getHeight() / 2 - gap / 2 - 100;

        randomGenerator = new Random();

        distanceBetweenTubes = Gdx.graphics.getWidth() * 3 / 4;

        for (int i = 0; i < numOfTubes; i++) {
            tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - Gdx.graphics.getHeight() / 4);

            tubeX[i] = Gdx.graphics.getWidth() / 2 - bottomTube.getWidth() / 2 + i * distanceBetweenTubes;
        }
    }

    @Override
    public void render() {

        // Draws the background
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // If game has started
        if (gameState != 0) {

            // Draws the pipes
            for (int i = 0; i < numOfTubes; i++) {
                if (tubeX[i] < - topTube.getWidth()) {
                    tubeX[i] += numOfTubes * distanceBetweenTubes;
                }

                batch.draw(topTube, tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i]);
                batch.draw(bottomTube, tubeX[i], Gdx.graphics.getHeight() / 2 - bottomTube.getHeight() - gap / 2 + tubeOffset[i]);

                tubeX[i] -= tubeVelocity;
            }

            // Handles gravity
            birdVelocity += gravity;
            birdY -= birdVelocity;

//            // Handles bird death
//            if (birdY <= 0) {
//                gameState = 0;
//                birdVelocity = 0;
//                birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;
//            }

            // Handles on touch to make bird go upward
            if (Gdx.input.justTouched()) {
                birdVelocity = -30;

                birdY -= birdVelocity;


            }

            // Game has not started
        } else {
            // Starts the game by changing the game state
            if (Gdx.input.justTouched()) {
                gameState = 1;
            }
        }

        // Handles the flapRate of the bird
        if (counter == 0 || counter == flapRate * 2) {
            flapState = 0;
            counter = 0;
        } else if (counter == flapRate) {
            flapState = 1;
        }

        counter += 1;

        // Draws the bird
        batch.draw(birds[flapState], Gdx.graphics.getWidth() / 2 - birds[flapState].getWidth() / 2, birdY);
        batch.end();
    }
}
