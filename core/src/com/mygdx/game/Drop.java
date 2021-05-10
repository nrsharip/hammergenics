package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class Drop extends Game {

	public SpriteBatch batch;
	public BitmapFont font;

	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont(); // use libGDX's default Arial font
		this.setScreen(new MainMenuScreen(this));
	}

	public void render() {
		super.render(); // important!
	}

	public void dispose() {
		batch.dispose();
		font.dispose();

		// Note that the dispose() method of the GameScreen class is not called automatically, see the Screen API.
		// It is your responsibility to take care of that.
		// You can call this method from the dispose() method of the Game class,
		// if the GameScreen class passes a reference to itself to the Game class.
		getScreen().dispose();
	}

}